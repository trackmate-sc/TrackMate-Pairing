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

import static fiji.plugin.trackmate.io.TmXmlKeys.IMAGE_ELEMENT_KEY;
import static fiji.plugin.trackmate.io.TmXmlKeys.IMAGE_FILENAME_ATTRIBUTE_NAME;
import static fiji.plugin.trackmate.io.TmXmlKeys.IMAGE_FOLDER_ATTRIBUTE_NAME;
import static fiji.plugin.trackmate.io.TmXmlKeys.SETTINGS_ELEMENT_KEY;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.Spot;
import fiji.plugin.trackmate.io.TmXmlReader;
import fiji.plugin.trackmate.pairing.Pairing.SpotPair;
import net.imglib2.algorithm.Algorithm;

public abstract class AbstractPairing implements Algorithm
{

	protected final String xml1;

	protected final String xml2;

	protected String errorMessage;

	public AbstractPairing( final String xml1, final String xml2 )
	{
		this.xml1 = xml1;
		this.xml2 = xml2;
	}

	@Override
	public boolean checkInput()
	{
		// Null or empty string.
		if ( xml1 == null || xml1.isEmpty() )
		{
			errorMessage = "Path to first TrackMate file is null or empty.";
			return false;
		}
		if ( xml2 == null || xml2.isEmpty() )
		{
			errorMessage = "Path to second TrackMate file is null or empty.";
			return false;
		}

		// File exist and can be read.
		final File file1 = new File( xml1 );
		final File file2 = new File( xml2 );
		if ( !file1.exists() || !file1.canRead() )
		{
			errorMessage = "First TrackMate file does not exist or cannot be read: " + file1;
			return false;
		}
		if ( !file2.exists() || !file2.canRead() )
		{
			errorMessage = "Second TrackMate file does not exist or cannot be read: " + file2;
			return false;
		}

		// Files are proper XML files.
		final TmXmlReader reader1 = new TmXmlReader( file1 );
		final TmXmlReader reader2 = new TmXmlReader( file2 );
		if ( !reader1.isReadingOk() )
		{
			errorMessage = reader1.getErrorMessage();
			return false;
		}
		if ( !reader2.isReadingOk() )
		{
			errorMessage = reader2.getErrorMessage();
			return false;
		}

		return true;
	}

	protected Model readModel( final String path )
	{
		final File file = new File( path );
		final TmXmlReader reader = new TmXmlReader( file );
		final Model model = reader.getModel();
		if ( !reader.isReadingOk() )
		{
			errorMessage = reader.getErrorMessage();
			return null;
		}
		return model;
	}

	public static final Collection< SpotPair > commonSpots( final Set< Spot > track1, final Set< Spot > track2, final double maxDist )
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

	protected String readImagePath( final String path )
	{
		final SAXBuilder sb = new SAXBuilder();
		try
		{
			final Document document = sb.build( new File( path ) );
			final Element root = document.getRootElement();
			final Element settingsElement = root.getChild( SETTINGS_ELEMENT_KEY );
			if ( null == settingsElement )
				return null;
			final Element imageInfoElement = settingsElement.getChild( IMAGE_ELEMENT_KEY );
			final String filename = imageInfoElement.getAttributeValue( IMAGE_FILENAME_ATTRIBUTE_NAME );
			final String folder = imageInfoElement.getAttributeValue( IMAGE_FOLDER_ATTRIBUTE_NAME );
			return folder + filename;
		}
		catch ( final Exception e )
		{}
		return null;
	}

	@Override
	public String getErrorMessage()
	{
		return errorMessage;
	}
}
