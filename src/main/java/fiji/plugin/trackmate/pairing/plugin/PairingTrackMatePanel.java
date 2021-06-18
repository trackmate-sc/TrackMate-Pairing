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
package fiji.plugin.trackmate.pairing.plugin;

import static fiji.plugin.trackmate.io.TmXmlKeys.MODEL_ELEMENT_KEY;
import static fiji.plugin.trackmate.io.TmXmlKeys.SPATIAL_UNITS_ATTRIBUTE_NAME;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.text.DecimalFormat;

import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.scijava.prefs.PrefService;
import org.scijava.util.VersionUtils;

import fiji.plugin.trackmate.Logger;
import fiji.plugin.trackmate.gui.Fonts;
import fiji.plugin.trackmate.gui.GuiUtils;
import fiji.plugin.trackmate.io.IOUtils;
import fiji.plugin.trackmate.pairing.PairingTrackMate;
import fiji.plugin.trackmate.util.TMUtils;

public class PairingTrackMatePanel extends JPanel
{

	private static final long serialVersionUID = 1L;

	private final PrefService prefService;

	final JTextField tf1;

	final JTextField tf2;

	final JButton btnPair;

	final JFormattedTextField ftfMaxDist;

	final JLabel lblUnits;


	public PairingTrackMatePanel()
	{
		this.prefService = TMUtils.getContext().getService( PrefService.class );

		final GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 0, 0, 0 };
		gridBagLayout.rowHeights = new int[] { 0, 0, 45, 0, 45, 0, 45, 0, 0, 0 };
		gridBagLayout.columnWeights = new double[] { 1.0, 1.0, 0.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE };
		setLayout( gridBagLayout );

		final JLabel lblTitle = new JLabel( "Pairing TrackMate v" + VersionUtils.getVersion( PairingTrackMatePanel.class ) );
		lblTitle.setFont( Fonts.BIG_FONT );
		lblTitle.setHorizontalAlignment( SwingConstants.CENTER );
		final GridBagConstraints gbcLblTitle = new GridBagConstraints();
		gbcLblTitle.gridwidth = 3;
		gbcLblTitle.insets = new Insets( 5, 5, 5, 5 );
		gbcLblTitle.fill = GridBagConstraints.HORIZONTAL;
		gbcLblTitle.gridx = 0;
		gbcLblTitle.gridy = 0;
		add( lblTitle, gbcLblTitle );

		final JLabel lblPath1 = new JLabel( "Path to first file:" );
		final GridBagConstraints gbcLblPath1 = new GridBagConstraints();
		gbcLblPath1.anchor = GridBagConstraints.SOUTH;
		gbcLblPath1.fill = GridBagConstraints.HORIZONTAL;
		gbcLblPath1.insets = new Insets( 5, 5, 5, 5 );
		gbcLblPath1.gridx = 0;
		gbcLblPath1.gridy = 2;
		add( lblPath1, gbcLblPath1 );

		final JButton btnBrowse1 = new JButton( "Browse" );
		final GridBagConstraints gbcBtnBrowse1 = new GridBagConstraints();
		gbcBtnBrowse1.gridwidth = 2;
		gbcBtnBrowse1.anchor = GridBagConstraints.SOUTHEAST;
		gbcBtnBrowse1.insets = new Insets( 5, 5, 5, 5 );
		gbcBtnBrowse1.gridx = 1;
		gbcBtnBrowse1.gridy = 2;
		add( btnBrowse1, gbcBtnBrowse1 );

		tf1 = new JTextField();
		tf1.setText( prefService.get( PairingTrackMate.class, "Path1", System.getProperty( "user.home" ) ) );
		final GridBagConstraints gbcTf1 = new GridBagConstraints();
		gbcTf1.gridwidth = 3;
		gbcTf1.insets = new Insets( 0, 5, 5, 5 );
		gbcTf1.fill = GridBagConstraints.HORIZONTAL;
		gbcTf1.gridx = 0;
		gbcTf1.gridy = 3;
		add( tf1, gbcTf1 );
		tf1.setColumns( 10 );

		final JLabel lblPath2 = new JLabel( "Path to second file:" );
		final GridBagConstraints gbcLblNewLabel = new GridBagConstraints();
		gbcLblNewLabel.anchor = GridBagConstraints.SOUTH;
		gbcLblNewLabel.fill = GridBagConstraints.HORIZONTAL;
		gbcLblNewLabel.insets = new Insets( 5, 5, 5, 5 );
		gbcLblNewLabel.gridx = 0;
		gbcLblNewLabel.gridy = 4;
		add( lblPath2, gbcLblNewLabel );

		final JButton btnBrowse2 = new JButton( "Browse" );
		final GridBagConstraints gbcBtnBrowse2 = new GridBagConstraints();
		gbcBtnBrowse2.gridwidth = 2;
		gbcBtnBrowse2.anchor = GridBagConstraints.SOUTHEAST;
		gbcBtnBrowse2.insets = new Insets( 5, 5, 5, 5 );
		gbcBtnBrowse2.gridx = 1;
		gbcBtnBrowse2.gridy = 4;
		add( btnBrowse2, gbcBtnBrowse2 );

		tf2 = new JTextField();
		tf2.setText( prefService.get( PairingTrackMate.class, "Path2", System.getProperty( "user.home" ) ) );
		final GridBagConstraints gbcTf2 = new GridBagConstraints();
		gbcTf2.insets = new Insets( 5, 5, 0, 5 );
		gbcTf2.gridwidth = 3;
		gbcTf2.fill = GridBagConstraints.HORIZONTAL;
		gbcTf2.gridx = 0;
		gbcTf2.gridy = 5;
		add( tf2, gbcTf2 );
		tf2.setColumns( 10 );

		final JLabel lblMaxPairingDistance = new JLabel( "MaxPairing Distance:" );
		final GridBagConstraints gbcLblMaxPairingDistance = new GridBagConstraints();
		gbcLblMaxPairingDistance.anchor = GridBagConstraints.SOUTHEAST;
		gbcLblMaxPairingDistance.insets = new Insets( 5, 5, 5, 5 );
		gbcLblMaxPairingDistance.gridx = 0;
		gbcLblMaxPairingDistance.gridy = 6;
		add( lblMaxPairingDistance, gbcLblMaxPairingDistance );

		ftfMaxDist = new JFormattedTextField( new DecimalFormat( ".###" ) );
		ftfMaxDist.setValue( prefService.getDouble( PairingTrackMate.class, "MaxPairingDistance", 1. ) );
		ftfMaxDist.setHorizontalAlignment( SwingConstants.CENTER );
		GuiUtils.selectAllOnFocus( ftfMaxDist );
		final GridBagConstraints gbcFtfMaxDist = new GridBagConstraints();
		gbcFtfMaxDist.anchor = GridBagConstraints.SOUTH;
		gbcFtfMaxDist.insets = new Insets( 5, 5, 0, 0 );
		gbcFtfMaxDist.fill = GridBagConstraints.HORIZONTAL;
		gbcFtfMaxDist.gridx = 1;
		gbcFtfMaxDist.gridy = 6;
		add( ftfMaxDist, gbcFtfMaxDist );

		lblUnits = new JLabel( "pixels" );
		final GridBagConstraints gbc_lblUnits = new GridBagConstraints();
		gbc_lblUnits.anchor = GridBagConstraints.SOUTH;
		gbc_lblUnits.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblUnits.insets = new Insets( 5, 0, 5, 5 );
		gbc_lblUnits.gridx = 2;
		gbc_lblUnits.gridy = 6;
		add( lblUnits, gbc_lblUnits );

		btnPair = new JButton( "Pair" );
		final GridBagConstraints gbc_btnPair = new GridBagConstraints();
		gbc_btnPair.gridwidth = 3;
		gbc_btnPair.anchor = GridBagConstraints.EAST;
		gbc_btnPair.insets = new Insets( 5, 5, 5, 5 );
		gbc_btnPair.gridx = 0;
		gbc_btnPair.gridy = 8;
		add( btnPair, gbc_btnPair );

		/*
		 * Listeners.
		 */

		updateMaxDistanceField();

		// Browse.
		btnBrowse1.addActionListener( e -> browse( tf1, "Path1" ) );
		btnBrowse2.addActionListener( e -> browse( tf2, "Path2" ) );

		// Persistence & physical units.
		tf1.addActionListener( e -> {
			prefService.put( PairingTrackMate.class, "Path1", tf1.getText() );
			updateMaxDistanceField();
		} );
		tf2.addActionListener( e -> {
			prefService.put( PairingTrackMate.class, "Path2", tf2.getText() );
			updateMaxDistanceField();
		} );
		ftfMaxDist.addActionListener( e -> updateMaxDistanceField() );
		ftfMaxDist.addFocusListener( new FocusListener()
		{

			@Override
			public void focusLost( final FocusEvent e )
			{
				updateMaxDistanceField();
			}

			@Override
			public void focusGained( final FocusEvent e )
			{}
		} );
	}

	private void updateMaxDistanceField()
	{
		prefService.put( PairingTrackMate.class, "MaxPairingDistance",
				( ( Number ) ftfMaxDist.getValue() ).doubleValue() );

		/*
		 * Try to guess the spatial units.
		 */
		new Thread( () -> {

			final String path1 = tf1.getText();
			final String path2 = tf2.getText();
			if ( path1 != null && !path1.isEmpty() )
			{
				final String units = readSpatialUnits( path1 );
				if ( units != null )
				{
					lblUnits.setText( units );
					return;
				}
			}
			if ( path2 != null && !path2.isEmpty() )
			{
				final String units = readSpatialUnits( path2 );
				if ( units != null )
					lblUnits.setText( units );
			}
		} ).start();
	}

	private String readSpatialUnits( final String path )
	{
		final SAXBuilder sb = new SAXBuilder();
		try
		{
			final Document document = sb.build( new File( path ) );
			final Element root = document.getRootElement();
			final Element modelElement = root.getChild( MODEL_ELEMENT_KEY );
			if ( null == modelElement )
				return null;
			return modelElement.getAttributeValue( SPATIAL_UNITS_ATTRIBUTE_NAME );
		}
		catch ( final Exception e )
		{}
		return null;
	}

	private void browse( final JTextField tf, final String persistanceKey )
	{
		final File file = new File( tf.getText() );
		final File tmpFile = IOUtils.askForFileForLoading( file, "Specify a TrackMate XML file",
				( Frame ) SwingUtilities.getWindowAncestor( this ), Logger.IJ_LOGGER );
		if ( null == tmpFile )
			return;

		tf.setText( tmpFile.getAbsolutePath() );
		prefService.put( PairingTrackMate.class, persistanceKey, tf.getText() );
		updateMaxDistanceField();
	}
}
