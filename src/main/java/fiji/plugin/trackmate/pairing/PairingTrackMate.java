package fiji.plugin.trackmate.pairing;

import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.Spot;
import fiji.plugin.trackmate.TrackModel;
import fiji.plugin.trackmate.io.TmXmlReader;
import fiji.plugin.trackmate.pairing.Pairing.Builder;
import fiji.plugin.trackmate.pairing.Pairing.SpotPair;
import net.imglib2.algorithm.OutputAlgorithm;

public class PairingTrackMate implements OutputAlgorithm< Pairing >
{

	private static final String BASE_ERROR_MSG = "[PairingTrackMate] ";

	private final String xml1;

	private final String xml2;

	private String errorMessage;

	private final double maxPairingDistance;

	private Pairing output;

	public PairingTrackMate( final String xml1, final String xml2, final double maxPairingDistance )
	{
		this.xml1 = xml1;
		this.xml2 = xml2;
		this.maxPairingDistance = maxPairingDistance;
	}

	@Override
	public boolean checkInput()
	{
		if ( maxPairingDistance <= 0. )
		{
			errorMessage = BASE_ERROR_MSG + "Max pairing distance is negative or zero: " + maxPairingDistance;
			return false;
		}

		// Null or empty string.
		if ( xml1 == null || xml1.isEmpty() )
		{
			errorMessage = BASE_ERROR_MSG + "Path to first TrackMate file is null or empty.";
			return false;
		}
		if ( xml2 == null || xml2.isEmpty() )
		{
			errorMessage = BASE_ERROR_MSG + "Path to second TrackMate file is null or empty.";
			return false;
		}

		// File exist and can be read.
		final File file1 = new File( xml1 );
		final File file2 = new File( xml2 );
		if ( !file1.exists() || !file1.canRead() )
		{
			errorMessage = BASE_ERROR_MSG + "First TrackMate file does not exist or cannot be read: " + file1;
			return false;
		}
		if ( !file2.exists() || !file2.canRead() )
		{
			errorMessage = BASE_ERROR_MSG + "Second TrackMate file does not exist or cannot be read: " + file2;
			return false;
		}

		// Files are proper XML files.
		final TmXmlReader reader1 = new TmXmlReader( file1 );
		final TmXmlReader reader2 = new TmXmlReader( file2 );
		if ( !reader1.isReadingOk() )
		{
			errorMessage = BASE_ERROR_MSG + reader1.getErrorMessage();
			return false;
		}
		if ( !reader2.isReadingOk() )
		{
			errorMessage = BASE_ERROR_MSG + reader2.getErrorMessage();
			return false;
		}

		return true;
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
				builder.unmatchedTrack1( id1 );
			}
		}
		// Add the remaining track2 to unmatched list.
		for ( final Integer id2 : ids2 )
			builder.unmatchedTrack2( id2 );

		output = builder.get();
		return true;
	}

	private static final Collection< SpotPair > commonSpots( final Set< Spot > track1, final Set< Spot > track2, final double maxDist )
	{
		final Collection< SpotPair > commons = new ArrayList<>();
		for ( final Spot s1 : track1 )
		{
			for ( final Spot s2 : track2 )
			{
				if ( s1.getFeature( Spot.FRAME ).intValue() == s2.getFeature( Spot.FRAME ).intValue() )
				{
					final double d2 = s1.squareDistanceTo( s2 );
					if ( d2 < maxDist * maxDist )
					{
						commons.add( new SpotPair( s1, s2 ) );
						break;
					}
				}
			}
		}
		return commons;
	}

	private Model readModel( final String path )
	{
		final File file = new File( path );
		final TmXmlReader reader = new TmXmlReader( file );
		final Model model = reader.getModel();
		if ( !reader.isReadingOk() )
		{
			errorMessage = BASE_ERROR_MSG + reader.getErrorMessage();
			return null;
		}
		return model;
	}

	@Override
	public String getErrorMessage()
	{
		return errorMessage;
	}

	@Override
	public Pairing getResult()
	{
		return output;
	}
}
