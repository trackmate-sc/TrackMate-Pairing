package fiji.plugin.trackmate.pairing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

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
			this.paired = Collections.unmodifiableCollection( paired );
		}

		public double meanDistance()
		{
			return paired.stream()
					.mapToDouble( p -> Math.sqrt( p.s1.squareDistanceTo( p.s2 ) ) )
					.summaryStatistics()
					.getAverage();
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
	}

	public final Collection< TrackPair > pairs;

	public final Collection< Integer > unmatchedTracks1;

	public final Collection< Integer > unmatchedTracks2;

	private final String units;

	private Pairing(
			final Collection< TrackPair > pairs,
			final Collection< Integer > unmatchedTracks1,
			final Collection< Integer > unmatchedTracks2,
			final String units )
	{
		this.pairs = pairs;
		this.unmatchedTracks1 = unmatchedTracks1;
		this.unmatchedTracks2 = unmatchedTracks2;
		this.units = units;
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
			for ( final Integer id1 : unmatchedTracks1 )
				str.append( "\n - " + id1 );
		}
		if ( unmatchedTracks2.isEmpty() )
		{
			str.append( "\nNo unmatched tracks 2." );
		}
		else
		{
			str.append( "\nUnmatched tracks 2:" );
			for ( final Integer id2 : unmatchedTracks2 )
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

		private final Collection< Integer > unmatchedTracks1 = new ArrayList<>();

		private final Collection< Integer > unmatchedTracks2 = new ArrayList<>();

		private String units = "";

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

		public Builder unmatchedTrack1( final Integer id1 )
		{
			if ( id1 == null )
				throw new IllegalArgumentException( "Id1 is null." );
			unmatchedTracks1.add( id1 );
			return this;
		}

		public Builder unmatchedTrack2( final Integer id2 )
		{
			if ( id2 == null )
				throw new IllegalArgumentException( "Id2 is null." );
			unmatchedTracks2.add( id2 );
			return this;
		}

		public Pairing get()
		{
			return new Pairing(
					Collections.unmodifiableCollection( pairs ),
					Collections.unmodifiableCollection( unmatchedTracks1 ),
					Collections.unmodifiableCollection( unmatchedTracks2 ),
					units );
		}
	}
}
