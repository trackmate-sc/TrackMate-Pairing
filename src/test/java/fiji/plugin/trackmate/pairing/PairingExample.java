package fiji.plugin.trackmate.pairing;

public class PairingExample
{

	public static void main( final String[] args )
	{
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
		System.out.println( "Finished!" );
		System.out.println( pairing.getResult() );
	}

}
