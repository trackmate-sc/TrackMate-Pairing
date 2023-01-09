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
	 * @param maxPairingDistance
	 *            the distance beyond which to reject pairing.
	 * @return a builder that can generating a pairing results. The builder will
	 *         have the pairing results ready and can be decorated with
	 *         supplemental data.
	 */
	public Builder pair( Model model1, Model model2, double maxPairingDistance );

}
