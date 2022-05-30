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
