/*-
 * #%L
 * Fiji distribution of ImageJ for the life sciences.
 * %%
 * Copyright (C) 2021 - 2022 The Institut Pasteur.
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
package fiji.plugin.trackmate.pairing;

import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.pairing.method.SpotConcensusPairing;
import net.imglib2.algorithm.OutputAlgorithm;

/**
 * Pair two models based on the max distance.
 * <p>
 * This algorithm loads two TrackMate files and pair the tracks they contain
 * based on the how many spots in common frames are within a specified maximum
 * pairing distance. A track pair is the pair for which this number of common
 * spots is the highest.
 * 
 * @author Jean-Yves Tinevez
 *
 */
public class PairingTrackMate extends AbstractPairing implements OutputAlgorithm< Pairing >
{

	private final double maxPairingDistance;

	private Pairing output;

	public PairingTrackMate( final String xml1, final String xml2, final double maxPairingDistance )
	{
		super( xml1, xml2 );
		this.maxPairingDistance = maxPairingDistance;
	}

	@Override
	public boolean checkInput()
	{
		if ( maxPairingDistance <= 0. )
		{
			errorMessage = "Max pairing distance is negative or zero: " + maxPairingDistance;
			return false;
		}
		return super.checkInput();
	}

	@Override
	public boolean process()
	{
		/*
		 * Read models.
		 */

		final Model model1 = readModel( xml1 );
		if ( model1 == null )
			return false;

		final Model model2 = readModel( xml2 );
		if ( model2 == null )
			return false;

		/*
		 * Build pair data structure.
		 */

		final String sourceImagePath = readImagePath( xml1 );
		final SpotConcensusPairing method = new SpotConcensusPairing();
		output = method.pair( model1, model2, maxPairingDistance, sourceImagePath );
		return true;
	}

	@Override
	public Pairing getResult()
	{
		return output;
	}
}
