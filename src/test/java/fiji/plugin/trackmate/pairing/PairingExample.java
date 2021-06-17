package fiji.plugin.trackmate.pairing;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.opencsv.CSVWriterBuilder;
import com.opencsv.ICSVWriter;

import ij.ImageJ;
import ij.ImagePlus;

public class PairingExample
{

	public static void main( final String[] args ) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException
	{
		UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
		ImageJ.main( args );

		final String xml1 = "samples/1.5x-timelqpe_2021-04-02_c1.xml";
		final String xml2 = "samples/1.5x-timelqpe_2021-04-02_c2.xml";

		System.out.println( "Pairing " + xml1 + " and " + xml2 );
		final PairingTrackMate pairing = new PairingTrackMate( xml1, xml2, 10. );
		if ( !pairing.checkInput() || !pairing.process() )
		{
			System.err.println( "Problem pairing the files:" );
			System.err.println( pairing.getErrorMessage() );
			return;
		}
		System.out.println( "Pairing finished!" );
		System.out.println( pairing.getResult() );

		System.out.println( "Generating preview image." );
		final ImagePlus imp = PairingPreviewCreator.openImage( xml1 );
		final ImagePlus output = PairingPreviewCreator.preview2D( pairing.getResult(), imp );
		output.show();
		System.out.println( "Preview finished!" );

		System.out.println( "Writing to CSV file." );
		try (final ICSVWriter writer = new CSVWriterBuilder(
				new FileWriter( new File( "samples/1.5x-timelqpe_2021-04-02.csv" ) ) ).withSeparator( ',' ).build())
		{
			writer.writeAll( pairing.getResult().toCsv() );
		}
		catch ( final IOException e )
		{
			e.printStackTrace();
		}
		System.out.println( "Done!" );
	}
}
