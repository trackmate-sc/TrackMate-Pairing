package fiji.plugin.trackmate.pairing.plugin;

import ij.plugin.PlugIn;

public class PairingTrackMatePlugin implements PlugIn
{

	@Override
	public void run( final String arg )
	{
		new PairingTrackMateController().showGUI();
	}
}
