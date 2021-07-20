/*-
 * #%L
 * Fiji distribution of ImageJ for the life sciences.
 * %%
 * Copyright (C) 2021 The Institut Pasteur.
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

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.Spot;
import fiji.plugin.trackmate.TrackModel;
import fiji.plugin.trackmate.pairing.Pairing.Builder;
import fiji.plugin.trackmate.pairing.Pairing.SpotPair;
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

		final TrackModel tm1 = model1.getTrackModel();
		final TrackModel tm2 = model2.getTrackModel();

		final Deque< Integer > ids1 = new ArrayDeque<>( tm1.unsortedTrackIDs( true ) );
		final Set< Integer > ids2 = new HashSet<>( tm2.unsortedTrackIDs( true ) );

		final Builder builder = Pairing.build().units( model1.getSpaceUnits() );
		while ( !ids1.isEmpty() )
		{
			final Integer id1 = ids1.pop();
			final Set< Spot > track1 = tm1.trackSpots( id1 );

			/*
			 * Match by local nearest neighbor. I don't think we need global
			 * optimization in that case.
			 */
			Integer bestMatch = null;
			Collection< SpotPair > bestCommons = null;
			int largestCommonNbr = 0;
			for ( final Integer id2 : ids2 )
			{
				final Set< Spot > track2 = tm2.trackSpots( id2 );
				final Collection< SpotPair > commons = commonSpots( track1, track2, maxPairingDistance );
				if ( commons.size() > largestCommonNbr )
				{
					largestCommonNbr = commons.size();
					bestCommons = commons;
					bestMatch = id2;
				}
			}
			if ( bestMatch != null )
			{
				ids2.remove( bestMatch );
				builder.pair( id1, bestMatch, bestCommons );
			}
			else
			{
				builder.unmatchedTrack1( id1, track1 );
			}
		}
		// Add the remaining track2 to unmatched list.
		for ( final Integer id2 : ids2 )
		{
			final Set< Spot > track2 = tm2.trackSpots( id2 );
			builder.unmatchedTrack2( id2, track2 );
		}
		
		// Add the path to the source image.
		final String sourceImagePath = readImagePath( xml1 );
		builder.sourceImagePath( sourceImagePath );

		output = builder.get();
		return true;
	}

	@Override
	public Pairing getResult()
	{
		return output;
	}
}
