import os

from java.io import File

from  org.scijava.util import VersionUtils

from ij import IJ
from ij.plugin.frame import RoiManager

from fiji.plugin.trackmate.io import TmXmlWriter
from fiji.plugin.trackmate import Settings
from fiji.plugin.trackmate import TrackMate
from fiji.plugin.trackmate import Logger
from fiji.plugin.trackmate.util import TMUtils
from fiji.plugin.trackmate.features import FeatureFilter
from fiji.plugin.trackmate.detection import HessianDetectorFactory
from fiji.plugin.trackmate.tracking.jaqaman import SimpleSparseLAPTrackerFactory
from fiji.plugin.trackmate.action.closegaps import CloseGapsByDetection
from fiji.plugin.trackmate.gui.displaysettings import DisplaySettingsIO
from fiji.plugin.trackmate.gui.wizard.descriptors import ConfigureViewsDescriptor


#---------------------------------------------------------------------------------------

LOG_MESSAGE = "Batch tracking with TrackMate v" + VersionUtils.getVersion( TrackMate )

def performTracking(imp, settings):
	trackmate = TrackMate(settings)
	if (not trackmate.checkInput()) or (not trackmate.process()):
		print('Problem with tracking:')
		print(trackmate.getErrorMessage())
		print('Skipping')
		return None
	return trackmate

def performGapClosing(gapCloser, trackmate):
	gapCloser.execute(trackmate, Logger.VOID_LOGGER)

def saveTrackMate(trackmate, save_file):
	writer = TmXmlWriter( File( save_file ), Logger.VOID_LOGGER )
	writer.appendLog( LOG_MESSAGE + "\n" + TMUtils.getCurrentTimeString() )
	writer.appendModel( trackmate.getModel() )
	writer.appendSettings( trackmate.getSettings() )
	writer.appendGUIState( ConfigureViewsDescriptor.KEY )
	writer.appendDisplaySettings( DisplaySettingsIO.readUserDefault() )
	writer.writeToFile()
	

#---------------------------------------------------------------------------------------

# Where the images and the ROI files are.
source_folder = '/Users/tinevez/Desktop/ForJYT_testBatch'
# In what subfolder are the ROI files saved.
roi_sub_folder = 'ROI'
# Where to save the TrackMate files.
trackmate_sub_folder = 'Tracking'

# Prepare the settings for tracking.
# Detection.
detector_settings_ch1 = {	
	'TARGET_CHANNEL' : 1,
	'RADIUS' : 0.5, #  m
	'RADIUS_Z' : 0.8, #  m
	'THRESHOLD' : 0.7,
	'DO_SUBPIXEL_LOCALIZATION' : True,
	'NORMALIZE' : True,
}
detector_settings_ch2 = {	
	'TARGET_CHANNEL' : 2,
	'RADIUS' : 0.3, #  m
	'RADIUS_Z' : 0.5, #  m
	'THRESHOLD' : 0.8,
	'DO_SUBPIXEL_LOCALIZATION' : True,
	'NORMALIZE' : True,
}

# Tracking.
tracker_settings_ch1 = SimpleSparseLAPTrackerFactory().getDefaultSettings()
tracker_settings_ch1['LINKING_MAX_DISTANCE'] = 0.9 # µm
tracker_settings_ch1['GAP_CLOSING_MAX_DISTANCE'] = 1.4 # µm
tracker_settings_ch1['MAX_FRAME_GAP'] = 12
tracker_settings_ch2 = SimpleSparseLAPTrackerFactory().getDefaultSettings()
tracker_settings_ch2['LINKING_MAX_DISTANCE'] = 0.9 # µm
tracker_settings_ch2['GAP_CLOSING_MAX_DISTANCE'] = 1.4 # µm
tracker_settings_ch2['MAX_FRAME_GAP'] = 12

# Filtering.
min_n_spots_per_track_ch1 = 60
min_n_spots_per_track_ch2 = 20

# Gap-closing.
radiusFactor = 2. # In units of the spot radius.
gapCloser = CloseGapsByDetection()
gapCloser.getParameters().get(0).value = radiusFactor

# Loop over each TIF files.
for im_file in os.listdir(source_folder):
	if not im_file.endswith('.tif'):
		continue
	
	im_path = os.path.join(source_folder, im_file)
	print('\nProcessing %s' % im_path)
	
	print(' - Loading image.')
	imp = IJ.openImage(im_path)
	
	# Look for the corresponding ROIs, stored as a ZIP file.
	roiManager = RoiManager.getRoiManager()
	if roiManager:
		roiManager.close()
	
	# First ROI syntax: the image name + '.zip'
	roi_file = os.path.splitext(im_file)[0]+'.zip' 
	roi_path = os.path.join(source_folder, roi_sub_folder, roi_file)
	if not os.path.exists(roi_path):
		# Second ROI syntax: the image name + '_RoiSet.zip'
		roi_file = os.path.splitext(im_file)[0]+'_RoiSet.zip' 
		roi_path = os.path.join(source_folder, roi_sub_folder, roi_file)
		if not os.path.exists(roi_path):	
			print(' - Could not find matching ROI file "%s". Will process the whole image.' % roi_path)
		else:
			print(' - Opened ROI file %s' % roi_path)
			IJ.open(roi_path)
	else:
		print(' - Opened ROI file %s' % roi_path)
		IJ.open(roi_path)
	
	# Prepare settings objects.
	settings_ch1 = Settings(imp)
	settings_ch1.detectorFactory = HessianDetectorFactory()
	settings_ch1.detectorSettings = detector_settings_ch1
	settings_ch1.trackerFactory = SimpleSparseLAPTrackerFactory()
	settings_ch1.trackerSettings = tracker_settings_ch1
	settings_ch1.addTrackFilter(FeatureFilter('NUMBER_SPOTS', min_n_spots_per_track_ch1, True))
	
	settings_ch2 = Settings(imp)
	settings_ch2.detectorFactory = HessianDetectorFactory()
	settings_ch2.detectorSettings = detector_settings_ch2
	settings_ch2.trackerFactory = SimpleSparseLAPTrackerFactory()
	settings_ch2.trackerSettings = tracker_settings_ch2
	settings_ch2.addTrackFilter(FeatureFilter('NUMBER_SPOTS', min_n_spots_per_track_ch2, True))
	
	# Perform tracking and save image for CH1.
	save_file_ch1 = os.path.join(source_folder, trackmate_sub_folder, os.path.splitext(im_file)[0] + '-ch1.xml')
	print(' - Executing tracking for channel 1.')	
	trackmate_ch1 = performTracking(imp, settings_ch1)
	if not trackmate_ch1:
		continue
	performGapClosing(gapCloser, trackmate_ch1)
	saveTrackMate(trackmate_ch1, save_file_ch1)

	# Perform tracking and save image for CH2.
	save_file_ch2 = os.path.join(source_folder, trackmate_sub_folder, os.path.splitext(im_file)[0] + '-ch2.xml')
	print(' - Executing tracking for channel 2.')	
	trackmate_ch2 = performTracking(imp, settings_ch2)
	if not trackmate_ch2:
		continue
	performGapClosing(gapCloser, trackmate_ch2)
	saveTrackMate(trackmate_ch2, save_file_ch2)

	print(' - Done.')
	
	# Force closing the image.
	imp.flush()
	imp.changes = False
	imp.close()

print('\nFinished batch tracking in folder %s' % source_folder)
