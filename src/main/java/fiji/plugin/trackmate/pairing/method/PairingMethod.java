package fiji.plugin.trackmate.pairing.method;

import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.pairing.Pairing.Builder;

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
	 * @return a builder that can generating a pairing results. The builder will
	 *         have the pairing results ready and can be decorated with
	 *         supplemental data.
	 */
	public Builder pair( Model model1, Model model2, double maxPairingDistance );

}
