package fiji.plugin.trackmate.pairing;

import java.io.File;

import org.scijava.Context;
import org.scijava.ui.swing.script.TextEditor;

import fiji.plugin.trackmate.util.TMUtils;
import sc.fiji.Main;

public class TestPairingScripting
{
	public static void main( final String[] args ) throws Exception
	{
		Main.main( args );
		final Context context = TMUtils.getContext();
		final TextEditor te = new TextEditor( context );
		te.open( new File( "scripts/BatchTrackMatePairing.py" ) );
		te.setVisible( true );
	}
}
