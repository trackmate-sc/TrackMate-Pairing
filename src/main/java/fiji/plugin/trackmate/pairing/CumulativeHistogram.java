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

import java.util.Arrays;

import org.jfree.data.xy.DefaultXYDataset;
import org.scijava.util.DoubleArray;

public class CumulativeHistogram
{

	public static DefaultXYDataset toCumulativeHistogram( final DoubleArray arr )
	{
		final double[] values = arr.copyArray();
		final int n = values.length;
		Arrays.sort( values );

		final double[] x = new double[ 2 * n + 1 ];
		final double[] y = new double[ 2 * n + 1 ];

		x[ 0 ] = 0.;
		y[ 0 ] = 0.;
		for ( int i = 0; i < n; i++ )
		{
			final double v = values[ i ];
			x[ 1 + 2 * i ] = v;
			x[ 1 + 2 * i + 1 ] = v;
			y[ 1 + 2 * i ] = y[ 1 + 2 * i - 1 ];
			y[ 1 + 2 * i + 1 ] = y[ 1 + 2 * i ] + 1. / n;
		}

		final DefaultXYDataset dataset = new DefaultXYDataset();
		final double[][] data = new double[][] { x, y };
		dataset.addSeries( "Cumulative histogram", data );
		return dataset;
	}
}
