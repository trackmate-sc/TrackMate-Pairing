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

import java.util.Collection;
import java.util.Set;

import org.jfree.data.xy.DefaultXYDataset;
import org.scijava.util.DoubleArray;

import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.Spot;
import fiji.plugin.trackmate.TrackModel;
import fiji.plugin.trackmate.pairing.Pairing.SpotPair;
import net.imglib2.algorithm.OutputAlgorithm;

public class PairingCumulativeHistogram extends AbstractPairing implements OutputAlgorithm< DefaultXYDataset >
{

	private DefaultXYDataset output;

	private String units;

	public PairingCumulativeHistogram( final String xml1, final String xml2 )
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

		output = CumulativeHistogram.toCumulativeHistogram( arr );
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
	public DefaultXYDataset getResult()
	{
		return output;
	}
}
