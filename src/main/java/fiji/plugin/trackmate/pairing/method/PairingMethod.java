package fiji.plugin.trackmate.pairing.method;

import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.pairing.Pairing;

/**
 * Interface for methods that can pair tracks together.
 * 
 * @author Jean-Yves Tinevez
 *
 */
public interface PairingMethod
{

	/**
	 * Performs the pairing between the tracks of the two specified models.
	 * 
	 * @param model1
	 *            the first model.
	 * @param model2
	 *            the second model.
	 * @param sourceImagePath
	 *            the path to the source image, to be added to the pairing
	 *            structure.
	 * @return a new pairing.
	 */
	public Pairing pair( Model model1, Model model2, double maxPairingDistance, String sourceImagePath );

}
