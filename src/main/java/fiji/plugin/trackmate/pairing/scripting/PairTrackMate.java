/*-
 * #%L
 * Fiji distribution of ImageJ for the life sciences.
 * %%
 * Copyright (C) 2021 - 2023 The Institut Pasteur.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
package fiji.plugin.trackmate.pairing.scripting;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.scijava.util.VersionUtils;

import com.opencsv.CSVWriterBuilder;
import com.opencsv.ICSVWriter;

import fiji.plugin.trackmate.Logger;
import fiji.plugin.trackmate.Settings;
import fiji.plugin.trackmate.TrackMate;
import fiji.plugin.trackmate.gui.displaysettings.DisplaySettingsIO;
import fiji.plugin.trackmate.gui.wizard.descriptors.ConfigureViewsDescriptor;
import fiji.plugin.trackmate.io.TmXmlWriter;
import fiji.plugin.trackmate.pairing.PairingTrackMate;
import fiji.plugin.trackmate.pairing.method.PairingMethod;
import fiji.plugin.trackmate.pairing.plugin.PairingTrackMateController;
import fiji.plugin.trackmate.util.TMUtils;
import ij.IJ;
import ij.ImagePlus;
import ij.Prefs;

/**
 * Utility class to facilitate writing scripts for pairing files and saving
 * results in batch.
 * 
 * @author Jean-Yves Tinevez
 *
 */
public class PairTrackMate
{

	public static Logger logger = Logger.DEFAULT_LOGGER;

	private static final String LOG_MESSAGE = "PairingTrackMate v" + VersionUtils.getVersion( PairTrackMate.class )
			+ "\n"
			+ TrackMate.PLUGIN_NAME_STR + " v" + TrackMate.PLUGIN_NAME_VERSION;

	/**
	 * Performs tracking on the image with the specified path, with the tracking
	 * parameters in the specified settings objects, respectively for the first
	 * and second channel. The results of both tracking process are saved to two
	 * TrackMate XML file, in the folder where the image is saved. Then the two
	 * tracking results are paired, and pairing results are saved in a CSV file
	 * again in the image folder.
	 * 
	 * @param imagePath
	 * @param settingsCh1
	 * @param settingsCh2
	 * @param method
	 * @param maxPairDistance
	 * @param trackmateFileSubFolder
	 */
	public static final void process(
			final String imagePath,
			final Settings settingsCh1,
			final Settings settingsCh2,
			final PairingMethod method,
			final double maxPairDistance,
			final String trackmateFileSubFolder )
	{
		logger.log( "Opening image " + imagePath + '\n' );
		final ImagePlus imp = IJ.openImage( imagePath );
		process( imp, settingsCh1, settingsCh2, method, maxPairDistance, trackmateFileSubFolder );
	}

	/**
	 * Performs tracking on the specified image, with the tracking parameters in
	 * the specified settings objects, respectively for the first and second
	 * channel. The results of both tracking process are saved to two TrackMate
	 * XML file, in the folder where the image is saved. Then the two tracking
	 * results are paired, and pairing results are saved in a CSV file again in
	 * the image folder.
	 * 
	 * @param imp
	 * @param settingsCh1
	 * @param settingsCh2
	 * @param method
	 * @param maxPairDistance
	 * @param trackmateFileSubFolder
	 *            the name of a subfolder in which the TrackMate files will be
	 *            saved.
	 */
	public static final void process(
			final ImagePlus imp,
			final Settings settingsCh1,
			final Settings settingsCh2,
			final PairingMethod method,
			final double maxPairDistance,
			final String trackmateFileSubFolder )
	{

		final String directory = imp.getOriginalFileInfo().directory;
		final String fileName = imp.getOriginalFileInfo().fileName;
		if ( directory == null || fileName == null )
		{
			logger.error( "Image file could not be found on disk. Please save it before processing.\n" );
			return;
		}
		final String savePathCh1;
		final String savePathCh2;
		final int idx = fileName.lastIndexOf( '.' );
		if ( idx < 0 )
		{
			savePathCh1 = Paths.get( directory, trackmateFileSubFolder, fileName + "-ch1.xml" ).toString();
			savePathCh2 = Paths.get( directory, trackmateFileSubFolder, fileName + "-ch2.xml" ).toString();
		}
		else
		{
			savePathCh1 = Paths.get( directory, trackmateFileSubFolder, fileName.substring( 0, idx ) + "-ch1.xml").toString();
			savePathCh2 = Paths.get( directory, trackmateFileSubFolder, fileName.substring( 0, idx ) + "-ch2.xml" ).toString();
		}

		/*
		 * Tracking first channel.
		 */
		logger.log( "Performing tracking on channel 1.\n" );
		final boolean ok1 = track( imp, settingsCh1, savePathCh1 );
		if ( !ok1 )
		{
			logger.log( "Skipping.\n" );
			return;
		}
		logger.log( "Tracking results saved to " + savePathCh1 + '\n' );

		/*
		 * Tracking second channel.
		 */
		logger.log( "Performing tracking on channel 2.\n" );
		final boolean ok2 = track( imp, settingsCh2, savePathCh2 );
		if ( !ok2 )
		{
			logger.log( "Skipping.\n" );
			return;
		}
		logger.log( "Tracking results saved to " + savePathCh2 + '\n' );

		/*
		 * Pairing and saving to CSV.
		 */

		logger.log( "Performing pairing.\n" );
		final String csvFile = pair( savePathCh1, savePathCh2, method, maxPairDistance );
		if ( csvFile == null )
		{
			logger.log( "Skipping.\n" );
			return;
		}
		logger.log( "Pairing results saved to " + csvFile + '\n' );
	}

	/**
	 * Performs tracking on the image with the specified path, with the tracking parameters in
	 * the specified settings object, and saves the results to a TrackMate XML
	 * file, in the folder where the image is saved.
	 * 
	 * @param imagePath
	 *            the path to the image to track.
	 * @param settings
	 *            the tracking parameters.
	 * @return the path to the file the results are saved to. Returns
	 *         <code>null</code> if an error happens.
	 */
	public static final String track( final String imagePath, final Settings settings )
	{
		final ImagePlus imp = IJ.openImage( imagePath );
		return track( imp, settings );
	}

	/**
	 * Performs tracking on the specified image, with the tracking parameters in
	 * the specified settings object, and saves the results to a TrackMate XML
	 * file, in the folder where the image is saved.
	 * 
	 * @param imp
	 *            the image to track.
	 * @param settings
	 *            the tracking parameters.
	 * @return the path to the file the results are saved to. Returns
	 *         <code>null</code> if an error happens.
	 */
	public static final String track( final ImagePlus imp, final Settings settings )
	{
		final String fileName = imp.getOriginalFileInfo().fileName;
		if ( fileName == null )
		{
			logger.error( "Image file could not be found on disk. Please save it before processing.\n" );
			return null;
		}
		
		final String saveName;
		final int idx = fileName.lastIndexOf( '.' );
		if ( idx < 0 )
			saveName = fileName + ".xml";
		else
			saveName = fileName.substring( 0, idx ) + ".xml";

		final String savePath = new File( saveName ).getAbsolutePath();
		track( imp, settings, savePath );
		return savePath;
	}

	public static final boolean track( final ImagePlus imp, final Settings settings, final String targetFile )
	{
		// Prepare TrackMate.
		final Settings copy = settings.copyOn( imp );
		final TrackMate trackmate = new TrackMate( copy );
		final String spaceUnits = settings.imp.getCalibration().getXUnit();
		final String timeUnits = settings.imp.getCalibration().getTimeUnit();
		trackmate.getModel().setPhysicalUnits( spaceUnits, timeUnits );
		trackmate.setNumThreads( Prefs.getThreads() );

		// Perform tracking.
		if ( !trackmate.checkInput() || !trackmate.process() )
		{
			logger.error( "Problem tracking image:\n" + trackmate.getErrorMessage() + '\n' );
			return false;
		}

		// Save results.
		final TmXmlWriter writer = new TmXmlWriter( new File( targetFile ), Logger.VOID_LOGGER );
		writer.appendLog( LOG_MESSAGE + "\n" + TMUtils.getCurrentTimeString() );
		writer.appendModel( trackmate.getModel() );
		writer.appendSettings( trackmate.getSettings() );
		writer.appendGUIState( ConfigureViewsDescriptor.KEY );
		writer.appendDisplaySettings( DisplaySettingsIO.readUserDefault() );
		try
		{
			writer.writeToFile();
		}
		catch ( final FileNotFoundException e )
		{
			logger.error( "Problem saving TrackMate results. File not found:\n" + e.getMessage() + '\n' );
			return false;
		}
		catch ( final IOException e )
		{
			logger.error( "Problem saving TrackMate results. Input/Output error:\n" + e.getMessage() + '\n' );
			return false;
		}
		return true;
	}

	public static final String pair( final String path1, final String path2, final PairingMethod method, final double maxPairDistance )
	{
		// Perform pairing.
		final PairingTrackMate pairing = new PairingTrackMate( path1, path2, method, maxPairDistance );
		if ( !pairing.checkInput() || !pairing.process() )
		{
			logger.error( "Problem pairing the files:\n" + pairing.getErrorMessage() + '\n' );
			return null;
		}

		// Build CSV path.
		final Path parent = Paths.get( path1 ).getParent();
		final String filename1 = Paths.get( path1 ).getFileName().toString();
		final String filename2 = Paths.get( path2 ).getFileName().toString();
		String prefix = PairingTrackMateController.longestCommonPrefix( filename1, filename2 );
		if (prefix.endsWith( "-ch" ))
			prefix = prefix.substring( 0, prefix.length() - 3 );
		final File csvFile = Paths.get( parent.toString(), prefix + ".csv" ).toFile();

		// Save to CSV.
		try (final ICSVWriter writer = new CSVWriterBuilder(
				new FileWriter( csvFile ) ).withSeparator( ',' ).build())
		{
			writer.writeAll( pairing.getResult().toCsv() );
		}
		catch ( final IOException e )
		{
			logger.error( "Problem writing CSV file:\n" + e.getMessage() + '\n' );
			return null;
		}
		return csvFile.getAbsolutePath();
	}
}
