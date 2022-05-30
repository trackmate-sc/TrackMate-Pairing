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
package fiji.plugin.trackmate.pairing.method;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.Spot;
import fiji.plugin.trackmate.TrackModel;
import fiji.plugin.trackmate.pairing.AbstractPairing;
import fiji.plugin.trackmate.pairing.Pairing;
import fiji.plugin.trackmate.pairing.Pairing.Builder;
import fiji.plugin.trackmate.pairing.Pairing.SpotPair;
import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;
import net.imglib2.util.Util;

/**
 * Pair tracks based on the average positions of its spots, ignoring frame. This
 * can improve pairing when track pairs have few common time-points.
 * 
 * @author Jean-Yves Tinevez.
 */
public class AverageTrackPositionPairing implements PairingMethod
{

	@Override
	public Builder pair( final Model model1, final Model model2, final double maxPairingDistance )
	{
		final Builder builder = Pairing.build().units( model1.getSpaceUnits() );

		final TrackModel tm1 = model1.getTrackModel();
		final TrackModel tm2 = model2.getTrackModel();

		final Deque< Integer > ids1 = new ArrayDeque<>( tm1.unsortedTrackIDs( true ) );
		final Map< Integer, RealLocalizable > pos1 = new HashMap<>( ids1.size() );
		for ( final Integer id : ids1 )
		{
			final Set< Spot > track = tm1.trackSpots( id );
			final RealLocalizable pos = trackPosition( track );
			pos1.put( id, pos );
		}

		final List< Integer > ids2 = new ArrayList<>( tm2.unsortedTrackIDs( true ) );
		final Map< Integer, RealLocalizable > pos2 = new HashMap<>( ids2.size() );
		for ( final Integer id : ids2 )
		{
			final Set< Spot > track = tm2.trackSpots( id );
			final RealLocalizable pos = trackPosition( track );
			pos2.put( id, pos );
		}

		/*
		 * Greedy optimization.
		 */

		while ( !ids1.isEmpty() )
		{
			final Integer id1 = ids1.pop();
			final RealLocalizable l1 = pos1.get( id1 );

			Integer bestMatch = null;
			double smallesDist = Double.POSITIVE_INFINITY;
			for ( final Integer id2 : ids2 )
			{
				final RealLocalizable l2 = pos2.get( id2 );
				final double d = Util.distance( l1, l2 );
				if ( d > maxPairingDistance )
					continue;

				if ( d < smallesDist )
				{
					smallesDist = d;
					bestMatch = id2;
				}
			}

			final Set< Spot > track1 = tm1.trackSpots( id1 );
			if ( bestMatch != null )
			{
				ids2.remove( bestMatch );
				final Set< Spot > track2 = tm2.trackSpots( bestMatch );

				final Collection< SpotPair > bestCommons = AbstractPairing.commonSpots( track1, track2, maxPairingDistance );
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

		return builder;
	}

	protected RealLocalizable trackPosition( final Collection< Spot > track )
	{
		if ( track.isEmpty() )
			return new RealPoint( Double.NaN, Double.NaN, Double.NaN );

		final Iterator< Spot > it = track.iterator();
		final Spot first = it.next();
		double sx = first.getDoublePosition( 0 );
		double sy = first.getDoublePosition( 1 );
		double sz = first.getDoublePosition( 2 );
		int n = 1;

		while ( it.hasNext() )
		{
			final Spot spot = it.next();
			sx += spot.getDoublePosition( 0 );
			sy += spot.getDoublePosition( 1 );
			sz += spot.getDoublePosition( 2 );
			n++;
		}

		return new RealPoint( sx / n, sy / n, sz / n );
	}
}
