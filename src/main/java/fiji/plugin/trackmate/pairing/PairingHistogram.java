package fiji.plugin.trackmate.pairing;

import java.util.Collection;
import java.util.Set;

import org.jfree.data.statistics.HistogramDataset;
import org.scijava.util.DoubleArray;

import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.Spot;
import fiji.plugin.trackmate.TrackModel;
import fiji.plugin.trackmate.pairing.Pairing.SpotPair;
import fiji.plugin.trackmate.util.TMUtils;
import net.imglib2.algorithm.OutputAlgorithm;

public class PairingHistogram extends AbstractPairing implements OutputAlgorithm< HistogramDataset >
{

	private HistogramDataset output;

	private String units;

	public PairingHistogram( final String xml1, final String xml2 )
	{
		super( xml1, xml2 );
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

		this.units = model1.getSpaceUnits();

		final Model model2 = readModel( xml2 );
		if ( model2 == null )
			return false;

		/*
		 * Build pair data structure.
		 */

		final TrackModel tm1 = model1.getTrackModel();
		final TrackModel tm2 = model2.getTrackModel();

		final DoubleArray arr = new DoubleArray();
		for ( final Integer id1 : tm1.unsortedTrackIDs( true ) )
		{
			final Set< Spot > track1 = tm1.trackSpots( id1 );
			for ( final Integer id2 : tm2.unsortedTrackIDs( true ) )
			{
				final Set< Spot > track2 = tm2.trackSpots( id2 );
				final Collection< SpotPair > commons = commonSpots( track1, track2, Double.POSITIVE_INFINITY );

				for ( final SpotPair pair : commons )
				{
					final double d = pair.distance();
					arr.addValue( d );
				}
			}
		}

		output = new HistogramDataset();
		final double[] distances = arr.copyArray();
		final int nBins = TMUtils.getNBins( distances, 8, 100 );
		output.addSeries( "Unpaired distances", distances, nBins );
		return true;
	}

	public String getUnits()
	{
		return units;
	}

	@Override
	public String getErrorMessage()
	{
		return errorMessage;
	}

	@Override
	public HistogramDataset getResult()
	{
		return output;
	}
}
