package fiji.plugin.trackmate.pairing.plugin;

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

	final JTextField tf1;

	final JTextField tf2;

	final JButton btnPair;

	final JFormattedTextField ftfMaxDist;

	public PairingTrackMatePanel()
	{
		final PrefService prefService = TMUtils.getContext().getService( PrefService.class );

		final GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 0, 0 };
		gridBagLayout.rowHeights = new int[] { 0, 0, 45, 0, 45, 0, 45, 0, 0, 0 };
		gridBagLayout.columnWeights = new double[] { 1.0, 1.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE };
		setLayout( gridBagLayout );

		final JLabel lblTitle = new JLabel( "Pairing TrackMate v" + VersionUtils.getVersion( PairingTrackMatePanel.class ) );
		lblTitle.setFont( Fonts.BIG_FONT );
		lblTitle.setHorizontalAlignment( SwingConstants.CENTER );
		final GridBagConstraints gbcLblTitle = new GridBagConstraints();
		gbcLblTitle.gridwidth = 2;
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
		gbcBtnBrowse1.anchor = GridBagConstraints.SOUTHEAST;
		gbcBtnBrowse1.insets = new Insets( 5, 5, 5, 5 );
		gbcBtnBrowse1.gridx = 1;
		gbcBtnBrowse1.gridy = 2;
		add( btnBrowse1, gbcBtnBrowse1 );

		tf1 = new JTextField();
		tf1.setText( prefService.get( PairingTrackMate.class, "Path1", System.getProperty( "user.home" ) ) );
		final GridBagConstraints gbcTf1 = new GridBagConstraints();
		gbcTf1.gridwidth = 2;
		gbcTf1.insets = new Insets( 5, 5, 5, 5 );
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
		gbcBtnBrowse2.anchor = GridBagConstraints.SOUTHEAST;
		gbcBtnBrowse2.insets = new Insets( 5, 5, 5, 5 );
		gbcBtnBrowse2.gridx = 1;
		gbcBtnBrowse2.gridy = 4;
		add( btnBrowse2, gbcBtnBrowse2 );

		tf2 = new JTextField();
		tf2.setText( prefService.get( PairingTrackMate.class, "Path2", System.getProperty( "user.home" ) ) );
		final GridBagConstraints gbcTf2 = new GridBagConstraints();
		gbcTf2.insets = new Insets( 5, 5, 5, 5 );
		gbcTf2.gridwidth = 2;
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
		gbcFtfMaxDist.insets = new Insets( 5, 5, 5, 5 );
		gbcFtfMaxDist.fill = GridBagConstraints.HORIZONTAL;
		gbcFtfMaxDist.gridx = 1;
		gbcFtfMaxDist.gridy = 6;
		add( ftfMaxDist, gbcFtfMaxDist );

		btnPair = new JButton( "Pair" );
		final GridBagConstraints gbc_btnPair = new GridBagConstraints();
		gbc_btnPair.gridwidth = 2;
		gbc_btnPair.anchor = GridBagConstraints.EAST;
		gbc_btnPair.insets = new Insets( 5, 5, 5, 5 );
		gbc_btnPair.gridx = 0;
		gbc_btnPair.gridy = 8;
		add( btnPair, gbc_btnPair );

		/*
		 * Listeners.
		 */

		// Browse.
		btnBrowse1.addActionListener( e -> browse( tf1, "Path1", prefService ) );
		btnBrowse2.addActionListener( e -> browse( tf2, "Path2", prefService ) );

		// Persistence.
		tf1.addActionListener( e -> prefService.put( PairingTrackMate.class, "Path1", tf1.getText() ) );
		tf2.addActionListener( e -> prefService.put( PairingTrackMate.class, "Path2", tf2.getText() ) );
		ftfMaxDist.addActionListener( e -> prefService.put( PairingTrackMate.class, "MaxPairingDistance", 
				( ( Number ) ftfMaxDist.getValue() ).doubleValue() ) );
		ftfMaxDist.addFocusListener( new FocusListener()
		{

			@Override
			public void focusLost( final FocusEvent e )
			{
				prefService.put( PairingTrackMate.class, "MaxPairingDistance", 
						( ( Number ) ftfMaxDist.getValue() ).doubleValue() );
			}

			@Override
			public void focusGained( final FocusEvent e )
			{}
		} );
	}

	private void browse( final JTextField tf, final String persistanceKey, final PrefService prefService )
	{
		final File file = new File( tf.getText() );
		final File tmpFile = IOUtils.askForFileForLoading( file, "Specify a TrackMate XML file",
				( Frame ) SwingUtilities.getWindowAncestor( this ), Logger.IJ_LOGGER );
		if ( null == tmpFile )
			return;

		tf.setText( tmpFile.getAbsolutePath() );
		prefService.put( PairingTrackMate.class, persistanceKey, tf.getText() );
	}
}
