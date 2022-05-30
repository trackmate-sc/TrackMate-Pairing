package fiji.plugin.trackmate.pairing.method;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.Spot;
import fiji.plugin.trackmate.TrackModel;
import fiji.plugin.trackmate.pairing.AbstractPairing;
import fiji.plugin.trackmate.pairing.Pairing;
import fiji.plugin.trackmate.pairing.Pairing.Builder;
import fiji.plugin.trackmate.pairing.Pairing.SpotPair;

/**
 * Performs pairing by searching for each track in the first model, what track
 * in the second model has the most spots 'in common'. By 'in common' we mean,
 * spots that are in the same frame, and are closer than the max pairing
 * distance specified in the constructor.
 * 
 * @author Jean-Yves Tinevez
 *
 */
public class SpotConcensusPairing implements PairingMethod
{

	@Override
	public Builder pair( final Model model1, final Model model2, final double maxPairingDistance )
	{
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
				final Collection< SpotPair > commons = AbstractPairing.commonSpots( track1, track2, maxPairingDistance );
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

		return builder;
	}
}
