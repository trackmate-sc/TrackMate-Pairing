import os

from ij import IJ
from ij.plugin.frame import RoiManager

from fiji.plugin.trackmate.pairing.scripting import PairTrackMate
from fiji.plugin.trackmate.pairing.method import AverageTrackPositionPairing
from fiji.plugin.trackmate import Settings
from fiji.plugin.trackmate.detection import HessianDetectorFactory
from fiji.plugin.trackmate.tracking.sparselap import SimpleSparseLAPTrackerFactory
from fiji.plugin.trackmate.tracking import LAPUtils


# Where the images and the ROI files are.
source_folder = '/Users/tinevez/Projects/TSabate/Data/221207_ForJYT'
# In what subfolder are the ROI files saved.
roi_sub_folder = 'ROI'
# Where to save the TrackMate files.
trackmate_sub_folder = 'Tracking'

# Prepare the settings for tracking.
# Detection.
detector_settings_ch1 = {	
	'TARGET_CHANNEL' : 1,
	'RADIUS' : 0.2, #  m
	'RADIUS_Z' : 0.6, #  m
	'THRESHOLD' : 0.95,
	'DO_SUBPIXEL_LOCALIZATION' : True,
	'NORMALIZE' : True,
}
detector_settings_ch2 = {	
	'TARGET_CHANNEL' : 2,
	'RADIUS' : 0.2, #  m
	'RADIUS_Z' : 0.6, #  m
	'THRESHOLD' : 0.95,
	'DO_SUBPIXEL_LOCALIZATION' : True,
	'NORMALIZE' : True,
}

# Tracking.
tracker_settings_ch1 = LAPUtils.getDefaultLAPSettingsMap()
tracker_settings_ch1['LINKING_MAX_DISTANCE'] = 4. #  m
tracker_settings_ch1['GAP_CLOSING_MAX_DISTANCE'] = 4. #  m
tracker_settings_ch1['MAX_FRAME_GAP'] = 2
tracker_settings_ch2 = LAPUtils.getDefaultLAPSettingsMap()
tracker_settings_ch2['LINKING_MAX_DISTANCE'] = 4. #  m
tracker_settings_ch2['GAP_CLOSING_MAX_DISTANCE'] = 4. #  m
tracker_settings_ch2['MAX_FRAME_GAP'] = 2

# Pairing method
method = AverageTrackPositionPairing()
max_pair_distance = 1. #  m

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
	
	settings_ch2 = Settings(imp)
	settings_ch2.detectorFactory = HessianDetectorFactory()
	settings_ch2.detectorSettings = detector_settings_ch2
	settings_ch2.trackerFactory = SimpleSparseLAPTrackerFactory()
	settings_ch2.trackerSettings = tracker_settings_ch2
	
	# Perform tracking and pairing.
	print(' - Executing tracking and pairing.')
	PairTrackMate.process(imp, settings_ch1, settings_ch2, method, max_pair_distance, trackmate_sub_folder)
	print(' - Done.')
	
	# Force closing the image.
	imp.flush()
	imp.changes = false
	imp.close()

print('\nFinished batch pairing in folder %s' % source_folder)
