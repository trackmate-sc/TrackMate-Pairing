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
