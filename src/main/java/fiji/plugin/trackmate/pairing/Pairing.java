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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fiji.plugin.trackmate.Spot;

/**
 * Results of the pairing of two TrackMate models.
 * 
 * @author Jean-Yves Tinevez
 */
public class Pairing
{

	public static class TrackPair
	{

		public final Integer id1;

		public final Integer id2;

		public final Collection< SpotPair > paired;

		private TrackPair( final Integer id1, final Integer id2, final Collection< SpotPair > paired )
		{
			this.id1 = id1;
			this.id2 = id2;
			// Sort by time.
			final List< SpotPair > list = new ArrayList<>( paired );
			list.sort( Comparator.comparingInt( p -> p.s1.getFeature( Spot.FRAME ).intValue() ) );
			this.paired = Collections.unmodifiableCollection( list );
		}

		public double meanDistance()
		{
			return paired.stream()
					.mapToDouble( SpotPair::distance )
					.summaryStatistics()
					.getAverage();
		}

		public String getName()
		{
			return id1 + "&" + id2;
		}
	}

	public static class SpotPair
	{
		public final Spot s1;

		public final Spot s2;

		public SpotPair( final Spot s1, final Spot s2 )
		{
			this.s1 = s1;
			this.s2 = s2;
		}

		@Override
		public String toString()
		{
			return s1.ID() + "-" + s2.ID();
		}

		public double distance()
		{
			return Math.sqrt( s1.squareDistanceTo( s2 ) );
		}
	}

	public final Collection< TrackPair > pairs;

	public final Map< Integer, Collection< Spot > > unmatchedTracks1;

	public final Map< Integer, Collection< Spot > > unmatchedTracks2;

	public final String units;

	private final String sourceImagePath;

	private Pairing(
			final Collection< TrackPair > pairs,
			final Map< Integer, Collection< Spot > > unmatchedTracks1,
			final Map< Integer, Collection< Spot > > unmatchedTracks2,
			final String units,
			final String sourceImagePath )
	{
		this.pairs = pairs;
		this.unmatchedTracks1 = unmatchedTracks1;
		this.unmatchedTracks2 = unmatchedTracks2;
		this.units = units;
		this.sourceImagePath = sourceImagePath;
	}

	public List< String[] > toCsv()
	{
		final List< String[] > strs = new ArrayList<>();
		// Header.
		final String[] header = new String[] {
				"Track_pair",
				"Track_1_id",
				"Track_1_id",
				"Frame",
				"Spot_1_X",
				"Spot_1_Y",
				"Spot_1_Z",
				"Spot_2_X",
				"Spot_2_Y",
				"Spot_2_Z",
				"Distance",
				"Source_Image"
		};
		strs.add( header );
		for ( final TrackPair trackPair : pairs )
		{
			for ( final SpotPair pair : trackPair.paired )
			{
				final String[] str = new String[] {
						trackPair.getName(),
						trackPair.id1.toString(),
						trackPair.id2.toString(),
						"" + pair.s1.getFeature( Spot.FRAME ).intValue(),
						Double.toString( pair.s1.getDoublePosition( 0 ) ),
						Double.toString( pair.s1.getDoublePosition( 1 ) ),
						Double.toString( pair.s1.getDoublePosition( 2 ) ),
						Double.toString( pair.s2.getDoublePosition( 0 ) ),
						Double.toString( pair.s2.getDoublePosition( 1 ) ),
						Double.toString( pair.s2.getDoublePosition( 2 ) ),
						Double.toString( pair.distance() ),
						sourceImagePath
				};
				strs.add( str );
			}
		}
		return strs;
	}

	@Override
	public String toString()
	{
		final StringBuilder str = new StringBuilder( super.toString() );
		if ( pairs.isEmpty() )
		{
			str.append( "\nNo pairs." );
		}
		else
		{
			str.append( "\nPairs: " );
			for ( final TrackPair pair : pairs )
				str.append( String.format( "\n - %d & %d, dist = %.2f %s", 
						pair.id1,
						pair.id2,
						pair.meanDistance(),
						units ) );
		}
		if ( unmatchedTracks1.isEmpty() )
		{
			str.append( "\nNo unmatched tracks 1." );
		}
		else
		{
			str.append( "\nUnmatched tracks 1:" );
			for ( final Integer id1 : unmatchedTracks1.keySet() )
				str.append( "\n - " + id1 );
		}
		if ( unmatchedTracks2.isEmpty() )
		{
			str.append( "\nNo unmatched tracks 2." );
		}
		else
		{
			str.append( "\nUnmatched tracks 2:" );
			for ( final Integer id2 : unmatchedTracks2.keySet() )
				str.append( "\n - " + id2 );
		}
		return str.toString();
	}

	public static Builder build()
	{
		return new Builder();
	}

	/*
	 * Builder.
	 */

	public static final class Builder
	{
		private final Collection< TrackPair > pairs = new ArrayList<>();

		private final Map< Integer, Collection< Spot > > unmatchedTracks1 = new HashMap<>();

		private final Map< Integer, Collection< Spot > > unmatchedTracks2 = new HashMap<>();

		private String units = "";

		private String sourceImagePath;

		public Builder units( final String units )
		{
			this.units = units;
			return this;
		}

		public Builder pair( final Integer id1, final Integer id2, final Collection< SpotPair > paired )
		{
			if ( id1 == null )
				throw new IllegalArgumentException( "Id1 is null." );
			if ( id2 == null )
				throw new IllegalArgumentException( "Id2 is null." );
			if ( paired == null || paired.isEmpty() )
				throw new IllegalArgumentException( "The paired spots are null or empty." );
			pairs.add( new TrackPair( id1, id2, paired ) );
			return this;
		}

		public Builder unmatchedTrack1( final Integer id1, final Collection< Spot > track1 )
		{
			if ( id1 == null )
				throw new IllegalArgumentException( "Id1 is null." );
			unmatchedTracks1.put( id1, track1 );
			return this;
		}

		public Builder unmatchedTrack2( final Integer id2, final Collection< Spot > track2 )
		{
			if ( id2 == null )
				throw new IllegalArgumentException( "Id2 is null." );
			unmatchedTracks2.put( id2, track2 );
			return this;
		}

		public Builder sourceImagePath( final String sourceImagePath )
		{
			this.sourceImagePath = sourceImagePath;
			return this;
		}

		public Pairing get()
		{
			return new Pairing(
					Collections.unmodifiableCollection( pairs ),
					Collections.unmodifiableMap( unmatchedTracks1 ),
					Collections.unmodifiableMap( unmatchedTracks2 ),
					units,
					sourceImagePath );
		}
	}
}
