package fiji.plugin.trackmate.pairing;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import fiji.plugin.trackmate.pairing.plugin.PairingTrackMatePlugin;
import ij.ImageJ;

public class PairingTrackMatePluginExample
{

	public static void main( final String[] args ) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException
	{
		UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
		ImageJ.main( args );
		new PairingTrackMatePlugin().run( null );
	}
}
