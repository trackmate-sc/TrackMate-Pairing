package fiji.plugin.trackmate.pairing.plugin;

import java.awt.event.WindowAdapter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.JFrame;
import javax.swing.JLabel;

import com.opencsv.CSVWriterBuilder;
import com.opencsv.ICSVWriter;

import fiji.plugin.trackmate.gui.Icons;
import fiji.plugin.trackmate.pairing.PairingPreviewCreator;
import fiji.plugin.trackmate.pairing.PairingTrackMate;
import fiji.plugin.trackmate.util.EverythingDisablerAndReenabler;
import ij.IJ;
import ij.ImagePlus;

public class PairingTrackMateController
{

	private PairingTrackMatePanel gui;

	public void showGUI()
	{
		if ( gui == null )
		{
			gui = new PairingTrackMatePanel();
			gui.btnPair.addActionListener( e -> new Thread( () -> {

				final EverythingDisablerAndReenabler reenabler = new EverythingDisablerAndReenabler( gui, new Class[] { JLabel.class } );
				try
				{
					reenabler.disable();
					pair(
							gui.tf1.getText(),
							gui.tf2.getText(),
							( ( Number ) gui.ftfMaxDist.getValue() ).doubleValue() );
				}
				finally
				{
					reenabler.reenable();
				}
			} ).start() );
			final JFrame frame = new JFrame( "Pairing TrackMate" );
			frame.setIconImage( Icons.TRACKMATE_ICON.getImage() );
			frame.getContentPane().add( gui );
			frame.pack();
			frame.setLocationRelativeTo( null );
			frame.addWindowListener( new WindowAdapter()
			{
				@Override
				public void windowClosing( final java.awt.event.WindowEvent e )
				{
					gui = null;
				};
			} );
			frame.setVisible( true );
		}
	}

	public void pair( final String path1, final String path2, final double maxPairDistance )
	{
		/*
		 * Pairing.
		 */

		IJ.log( "Pairing " + path1 + " and " + path2 );
		final PairingTrackMate pairing = new PairingTrackMate( path1, path2, maxPairDistance );
		if ( !pairing.checkInput() || !pairing.process() )
		{
			IJ.error( "Pairing TrackMate", "Problem pairing the files:\n" + pairing.getErrorMessage() );
			return;
		}
		IJ.log( "Pairing finished!" );
		IJ.log( pairing.getResult().toString() );

		/*
		 * Preview image.
		 */

		IJ.log( "Generating preview image." );
		final ImagePlus imp = PairingPreviewCreator.openImage( path1 );
		final ImagePlus output = PairingPreviewCreator.preview2D( pairing.getResult(), imp );
		output.show();
		IJ.log( "Preview finished!" );

		/*
		 * Build CSV path.
		 */

		final Path parent = Paths.get( path1 ).getParent();
		final String filename1 = Paths.get( path1 ).getFileName().toString();
		final String filename2 = Paths.get( path2 ).getFileName().toString();
		final String prefix = longestCommonPrefix( filename1, filename2 );
		final File csvFile = Paths.get( parent.toString(), prefix + ".csv" ).toFile();

		/*
		 * Write to CSV.
		 */

		IJ.log( "Writing to CSV file: " + csvFile );
		try (final ICSVWriter writer = new CSVWriterBuilder(
				new FileWriter( csvFile ) ).withSeparator( ',' ).build())
		{
			writer.writeAll( pairing.getResult().toCsv() );
		}
		catch ( final IOException e )
		{
			IJ.error( e.getMessage() );
			e.printStackTrace();
		}
		IJ.log( "Done." );
	}

	public void setPath1( final String path1 )
	{
		if ( gui == null )
			return;
		gui.tf1.setText( path1 );
	}

	public void setPath2( final String path2 )
	{
		if ( gui == null )
			return;
		gui.tf2.setText( path2 );
	}

	public void setMaxPairingDistance( final double maxPairingDistance )
	{
		if ( gui == null )
			return;
		gui.ftfMaxDist.setValue( Double.valueOf( maxPairingDistance ) );
	}

	public static String longestCommonPrefix( final String a, final String b )
	{
		final int end = Math.min( a.length(), b.length() );
		int i = 0;
		while ( i < end && a.charAt( i ) == b.charAt( i ) )
			i++;

		final String pre = a.substring( 0, i );
		return pre;
	}
}
