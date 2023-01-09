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
package fiji.plugin.trackmate.pairing;

import java.util.Map;

import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.Settings;
import fiji.plugin.trackmate.detection.DetectorKeys;
import fiji.plugin.trackmate.pairing.Pairing.Builder;
import fiji.plugin.trackmate.pairing.method.PairingMethod;
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

	private final PairingMethod method;

	public PairingTrackMate( final String xml1, final String xml2, final PairingMethod method, final double maxPairingDistance )
	{
		super( xml1, xml2 );
		this.method = method;
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

		final Builder builder = method.pair( model1, model2, maxPairingDistance );

		// Add path to source image.
		final String sourceImagePath = readImagePath( xml1 );
		builder.sourceImagePath( sourceImagePath );

		// Add detection channel for first model.
		builder.targetChannel1( determineDetectionChannel( readSettings( xml1 ) ) );
		builder.targetChannel2( determineDetectionChannel( readSettings( xml2 ) ) );
		
		output = builder.get();
		return true;
	}

	@Override
	public Pairing getResult()
	{
		return output;
	}

	/**
	 * Tries to determine from the settings in what channel the detection
	 * happened.
	 * 
	 * @param settings
	 *            the settings to investigate.
	 * @return the channel in which the detection happened, or 0 if nothing can
	 *         be found.
	 */
	protected static final int determineDetectionChannel( final Settings settings )
	{
		if ( settings == null )
			return DetectorKeys.DEFAULT_TARGET_CHANNEL;

		final Map< String, Object > ds = settings.detectorSettings;
		if ( ds == null )
			return DetectorKeys.DEFAULT_TARGET_CHANNEL;

		final Object obj = ds.get( DetectorKeys.KEY_TARGET_CHANNEL );
		if ( obj == null )
			return DetectorKeys.DEFAULT_TARGET_CHANNEL;

		return ( ( Number ) obj ).intValue();
	}
}
