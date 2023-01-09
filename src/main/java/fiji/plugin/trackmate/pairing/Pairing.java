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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fiji.plugin.trackmate.Spot;
import fiji.plugin.trackmate.features.spot.SpotContrastAndSNRAnalyzerFactory;

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

	private final int ch1;

	private final int ch2;

	private Pairing(
			final Collection< TrackPair > pairs,
			final Map< Integer, Collection< Spot > > unmatchedTracks1,
			final Map< Integer, Collection< Spot > > unmatchedTracks2,
			final String units,
			final String sourceImagePath,
			final int ch1,
			final int ch2 )
	{
		this.pairs = pairs;
		this.unmatchedTracks1 = unmatchedTracks1;
		this.unmatchedTracks2 = unmatchedTracks2;
		this.units = units;
		this.sourceImagePath = sourceImagePath;
		this.ch1 = ch1;
		this.ch2 = ch2;
	}

	public List< String[] > toCsv()
	{
		/*
		 * Define supplemental features to add. We specify them as a map of the
		 * header for that column vs the pair of feature keys, one for spot1,
		 * one for spot2.
		 */
		final SupplementalFeatures supFeatures = new SupplementalFeaturesBuilder()
				.addFeature( "Mean_intensity_Spot_1", "MEAN_INTENSITY_CH" + ch1, true )
				.addFeature( "Mean_intensity_Spot_2", "MEAN_INTENSITY_CH" + ch2, false )
				.addFeature( "Max_intensity_Spot_1", "MAX_INTENSITY_CH" + ch1, true )
				.addFeature( "Max_intensity_Spot_2", "MAX_INTENSITY_CH" + ch2, false )
				.addFeature( "Std_intensity_Spot_1", "STD_INTENSITY_CH" + ch1, true )
				.addFeature( "Std_intensity_Spot_2", "STD_INTENSITY_CH" + ch2, false )
				.addFeature( "SNR_Spot_1", SpotContrastAndSNRAnalyzerFactory.SNR + ch1, true )
				.addFeature( "SNR_Spot_2", SpotContrastAndSNRAnalyzerFactory.SNR + ch2, false )
				.addFeature( "Contrast_Spot_1", SpotContrastAndSNRAnalyzerFactory.CONTRAST + ch1, true )
				.addFeature( "Contrast_Spot_2", SpotContrastAndSNRAnalyzerFactory.CONTRAST + ch2, false )
				.get();

		/*
		 * Generate the list of string arrays to export to CSV.
		 */

		final List< String[] > strs = new ArrayList<>();

		// Header.
		final List< String > header = new ArrayList<>();
		final List< String > mainHeader = Arrays.asList(
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
				"Source_Image" );
		header.addAll( mainHeader );
		header.addAll( supFeatures.headers() );
		strs.add( header.toArray( new String[] {} ) );

		for ( final TrackPair trackPair : pairs )
		{
			for ( final SpotPair pair : trackPair.paired )
			{
				final List< String > str = new ArrayList<>();
				final List< String > mainValues = Arrays.asList(
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
						sourceImagePath );
				str.addAll( mainValues );
				str.addAll( supFeatures.toStrValues( pair.s1, pair.s2 ) );
				strs.add( str.toArray( new String[] {} ) );
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
		str.append( "\nTarget channel for the first model: " + ch1 );
		str.append( "\nTarget channel for the second model: " + ch2 );
		str.append( "\nSource image file: " + sourceImagePath );
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

		private int ch1;

		private int ch2;

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

		public Builder targetChannel1( final int ch1 )
		{
			this.ch1 = ch1;
			return this;
		}

		public Builder targetChannel2( final int ch2 )
		{
			this.ch2 = ch2;
			return this;
		}

		public Pairing get()
		{
			return new Pairing(
					Collections.unmodifiableCollection( pairs ),
					Collections.unmodifiableMap( unmatchedTracks1 ),
					Collections.unmodifiableMap( unmatchedTracks2 ),
					units,
					sourceImagePath,
					ch1,
					ch2 );
		}
	}

	/**
	 * Utility to declare a collection of features to be extracted from the
	 * first or second spot of the pair, and added to the CSV export.
	 */
	private static class SupplementalFeaturesBuilder
	{

		private final List< String > headers = new ArrayList<>();

		private final List< String > keys = new ArrayList<>();

		private final List< Boolean > firstSpots = new ArrayList<>();

		public SupplementalFeaturesBuilder addFeature( final String header, final String key, final boolean firstSpot )
		{
			headers.add( header );
			keys.add( key );
			firstSpots.add( firstSpot );
			return this;
		}
		
		public SupplementalFeatures get()
		{
			return new SupplementalFeatures( headers, keys, firstSpots );
		}
	}

	private static class SupplementalFeatures
	{

		private final List< String > headers;

		private final List< String > keys;

		private final List< Boolean > firstSpots;

		public SupplementalFeatures( final List< String > headers, final List< String > keys, final List< Boolean > firstSpot )
		{
			this.headers = headers;
			this.keys = keys;
			this.firstSpots = firstSpot;
		}

		public List< String > headers()
		{
			return Collections.unmodifiableList( headers );
		}

		public List< String > toStrValues( final Spot spot1, final Spot spot2 )
		{
			final int n = headers.size();
			final List< String > out = new ArrayList<>( n );
			for ( int i = 0; i < n; i++ )
			{
				final String key = keys.get( i );
				final Boolean b = firstSpots.get( i );
				final Double obj = b.booleanValue()
						? spot1.getFeature( key )
						: spot2.getFeature( key );

				if ( obj == null )
					out.add( Double.toString( Double.NaN ) );
				else
					out.add( Double.toString( obj.doubleValue() ) );
			}
			return out;
		}
	}
}
