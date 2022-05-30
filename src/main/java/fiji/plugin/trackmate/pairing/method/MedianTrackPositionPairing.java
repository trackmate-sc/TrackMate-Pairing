package fiji.plugin.trackmate.pairing.method;

import java.util.Collection;

import fiji.plugin.trackmate.Spot;
import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;
import net.imglib2.util.Util;

/**
 * Identical to {@link AverageTrackPositionPairing} except that we take the
 * median of the spot positions.
 * 
 * @author Jean-Yves Tinevez
 *
 */
public class MedianTrackPositionPairing extends AverageTrackPositionPairing
{

	@Override
	protected RealLocalizable trackPosition( final Collection< Spot > track )
	{
		if ( track.isEmpty() )
			return new RealPoint( Double.NaN, Double.NaN, Double.NaN );

		final double[] xs = new double[ track.size() ];
		final double[] ys = new double[ track.size() ];
		final double[] zs = new double[ track.size() ];
		int i = 0;
		for ( final Spot spot : track )
		{
			xs[ i ] = spot.getDoublePosition( 0 );
			ys[ i ] = spot.getDoublePosition( 1 );
			zs[ i ] = spot.getDoublePosition( 2 );
			i++;
		}

		final double x = Util.median( xs );
		final double y = Util.median( ys );
		final double z = Util.median( zs );
		return new RealPoint( x, y, z );
	}

}
