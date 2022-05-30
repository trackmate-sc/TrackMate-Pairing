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
package fiji.plugin.trackmate.pairing.plugin;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.data.xy.DefaultXYDataset;

import com.opencsv.CSVWriterBuilder;
import com.opencsv.ICSVWriter;

import fiji.plugin.trackmate.gui.Icons;
import fiji.plugin.trackmate.pairing.PairingCumulativeHistogram;
import fiji.plugin.trackmate.pairing.PairingPreviewCreator;
import fiji.plugin.trackmate.pairing.PairingTrackMate;
import fiji.plugin.trackmate.pairing.method.PairingMethod;
import fiji.plugin.trackmate.pairing.method.PairingMethods;
import fiji.plugin.trackmate.util.EverythingDisablerAndReenabler;
import fiji.plugin.trackmate.util.ExportableChartPanel;
import fiji.plugin.trackmate.util.FileChooser;
import fiji.plugin.trackmate.util.FileChooser.DialogType;
import ij.IJ;
import ij.ImagePlus;

public class PairingTrackMateController
{

	private PairingTrackMatePanel gui;

	public void showGUI()
	{
		if ( gui == null )
		{
			gui = new PairingTrackMatePanel();
			gui.btnPair.addActionListener( e -> new Thread( () -> {

				final EverythingDisablerAndReenabler reenabler = new EverythingDisablerAndReenabler( gui, new Class[] { JLabel.class } );
				try
				{
					reenabler.disable();
					pair(
							gui.tf1.getText(),
							gui.tf2.getText(),
							( ( PairingMethods ) gui.cmbboxPairingMethod.getSelectedItem() ).method(),
							( ( Number ) gui.ftfMaxDist.getValue() ).doubleValue() );
				}
				finally
				{
					reenabler.reenable();
				}
			} ).start() );
			gui.btnPreview.addActionListener( e -> new Thread( () -> {

				final EverythingDisablerAndReenabler reenabler = new EverythingDisablerAndReenabler( gui, new Class[] { JLabel.class } );
				try
				{
					reenabler.disable();
					preview(
							gui.tf1.getText(),
							gui.tf2.getText() );
				}
				finally
				{
					reenabler.reenable();
				}
			} ).start() );
			final JFrame frame = new JFrame( "Pairing TrackMate" );
			frame.setIconImage( Icons.TRACKMATE_ICON.getImage() );
			frame.getContentPane().add( gui );
			frame.pack();
			frame.setLocationRelativeTo( null );
			frame.addWindowListener( new WindowAdapter()
			{
				@Override
				public void windowClosing( final java.awt.event.WindowEvent e )
				{
					gui = null;
				};
			} );
			frame.setVisible( true );
		}
	}

	private void preview( final String path1, final String path2 )
	{
		/*
		 * Compute distance histogram.
		 */

		IJ.log( "Creating distance cumulative histogram for " + path1 + " and " + path2 );
		final PairingCumulativeHistogram histo = new PairingCumulativeHistogram( path1, path2 );
		if ( !histo.checkInput() || !histo.process() )
		{
			IJ.error( "Pairing histogram", "Problem with the files:\n" + histo.getErrorMessage() );
			return;
		}
		IJ.log( "Histogram measured." );
		IJ.log( histo.getResult().toString() );
		final DefaultXYDataset dataset = histo.getResult();

		/*
		 * Create histogram plot.
		 */

		final String units = histo.getUnits();
		final String xlabel = "Pair distance (" + units + ")";
		final String ylabel = "cdf";
		final String title = "Pair distance cumulative histogram";
		final JFreeChart chart = ChartFactory.createXYLineChart(
				title, xlabel, ylabel, dataset, PlotOrientation.VERTICAL, false, false, false );

		final XYPlot plot = chart.getXYPlot();
		final XYLineAndShapeRenderer renderer = ( XYLineAndShapeRenderer ) plot.getRenderer();
		renderer.setSeriesOutlinePaint( 0, new Color( 0.2f, 0.2f, 0.2f ) );
		renderer.setSeriesPaint( 0, new Color( 0.3f, 0.3f, 0.3f, 0.5f ) );
		renderer.setSeriesStroke( 0, new BasicStroke( 2f ) );

		plot.setBackgroundPaint( new Color( 1, 1, 1, 0 ) );
		plot.setOutlineVisible( false );
		plot.setDomainCrosshairVisible( false );
		plot.setDomainGridlinesVisible( false );
		plot.setRangeCrosshairVisible( false );
		plot.setRangeGridlinesVisible( false );

		plot.getRangeAxis().setTickLabelInsets( new RectangleInsets( 20, 10, 20, 10 ) );
		plot.getDomainAxis().setTickLabelInsets( new RectangleInsets( 10, 20, 10, 20 ) );

		chart.setBorderVisible( false );
		chart.setBackgroundPaint( new Color( 0.8f, 0.8f, 0.9f ) );

		final ExportableChartPanel chartPanel = new ExportableChartPanel( chart )
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected JPopupMenu createPopupMenu( final boolean properties, final boolean copy, final boolean save, final boolean print, final boolean zoom )
			{
				final JPopupMenu menu = super.createPopupMenu( properties, copy, false, print, zoom );
				menu.remove( 11 );
				return menu;
			}
		};
		chartPanel.setPreferredSize( new java.awt.Dimension( 500, 520 ) );
		chartPanel.setOpaque( false );

		final JFrame frame = new JFrame( title );
		frame.getContentPane().add( chartPanel );
		frame.pack();
		frame.setLocationRelativeTo( gui );
		frame.setVisible( true );
	}

	public void pair( final String path1, final String path2, final PairingMethod method, final double maxPairDistance )
	{
		/*
		 * Pairing.
		 */

		IJ.log( "Pairing " + path1 + " and " + path2 );
		final PairingTrackMate pairing = new PairingTrackMate( path1, path2, method, maxPairDistance );
		if ( !pairing.checkInput() || !pairing.process() )
		{
			IJ.error( "Pairing TrackMate", "Problem pairing the files:\n" + pairing.getErrorMessage() );
			return;
		}
		IJ.log( "Pairing finished!" );
		IJ.log( pairing.getResult().toString() );

		/*
		 * Preview image.
		 */

		IJ.log( "Generating preview image." );
		final ImagePlus imp = PairingPreviewCreator.openImage( path1 );
		if ( imp == null )
		{
			IJ.log( "Could not open image referenced in the the first TrackMate file.\n"
					+ "Preview image won't be generated.\n"
					+ "Did you resave the image as ImageJ tif before running TrackMate?" );
		}
		else
		{
			final ImagePlus output = PairingPreviewCreator.preview2D( pairing.getResult(), imp );
			output.show();
			IJ.log( "Preview finished!" );
		}

		/*
		 * Build CSV path.
		 */

		final Path parent = Paths.get( path1 ).getParent();
		final String filename1 = Paths.get( path1 ).getFileName().toString();
		final String filename2 = Paths.get( path2 ).getFileName().toString();
		final String prefix = longestCommonPrefix( filename1, filename2 );
		final File csvFile = Paths.get( parent.toString(), prefix + ".csv" ).toFile();

		/*
		 * Ask for where to save.
		 */

		final File choosenFile = FileChooser.chooseFile(
				gui,
				csvFile.getAbsolutePath(),
				new FileNameExtensionFilter( "CSV files", "csv", "CSV" ),
				"Save pairing results to CSV file",
				DialogType.SAVE );
		if ( choosenFile == null )
		{
			IJ.log( "Saving aborted." );
			return;
		}

		/*
		 * Write to CSV.
		 */

		IJ.log( "Writing to CSV file: " + choosenFile );
		try (final ICSVWriter writer = new CSVWriterBuilder(
				new FileWriter( choosenFile ) ).withSeparator( ',' ).build())
		{
			writer.writeAll( pairing.getResult().toCsv() );
		}
		catch ( final IOException e )
		{
			IJ.error( e.getMessage() );
			e.printStackTrace();
		}
		IJ.log( "Done." );
	}

	public void setPath1( final String path1 )
	{
		if ( gui == null )
			return;
		gui.tf1.setText( path1 );
	}

	public void setPath2( final String path2 )
	{
		if ( gui == null )
			return;
		gui.tf2.setText( path2 );
	}

	public void setMaxPairingDistance( final double maxPairingDistance )
	{
		if ( gui == null )
			return;
		gui.ftfMaxDist.setValue( Double.valueOf( maxPairingDistance ) );
	}

	public static String longestCommonPrefix( final String a, final String b )
	{
		final int end = Math.min( a.length(), b.length() );
		int i = 0;
		while ( i < end && a.charAt( i ) == b.charAt( i ) )
			i++;

		final String pre = a.substring( 0, i );
		return pre;
	}
}
