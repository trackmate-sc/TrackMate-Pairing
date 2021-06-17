package fiji.plugin.trackmate.pairing;

import java.awt.Color;
import java.io.File;
import java.util.Collection;
import java.util.Map;

import fiji.plugin.trackmate.Spot;
import fiji.plugin.trackmate.io.TmXmlReader;
import fiji.plugin.trackmate.pairing.Pairing.SpotPair;
import fiji.plugin.trackmate.pairing.Pairing.TrackPair;
import fiji.plugin.trackmate.util.TMUtils;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.Line;
import ij.gui.OvalRoi;
import ij.gui.Overlay;
import ij.gui.TextRoi;
import ij.plugin.ZProjector;

public class PairingPreviewCreator
{

	private static final Color COLOR1 = Color.CYAN;

	private static final Color COLOR2 = Color.MAGENTA;

	private static final Color COLOR3 = Color.WHITE;

	private static final int FONT_SIZE = 6;

	public static ImagePlus openImage( final String xml )
	{
		final TmXmlReader reader = new TmXmlReader( new File( xml ) );
		if ( !reader.isReadingOk() )
		{
			IJ.error( reader.getErrorMessage() );
			return null;
		}
		return reader.readImage();
	}

	public static ImagePlus preview2D( final Pairing pairing, final ImagePlus imp )
	{
		/*
		 * Check if image is 3D. If yes make a MIP.
		 */
		final ImagePlus output;
		final boolean is3D = imp.getNSlices() > 1;
		if ( is3D )
			output = ZProjector.run( imp, "max all" );
		else
			output = imp;

		/*
		 * Create overlay.
		 */

		Overlay overlay = output.getOverlay();
		if ( null == overlay )
		{
			overlay = new Overlay();
			output.setOverlay( overlay );
		}
		final double[] calibration = TMUtils.getSpatialCalibration( imp );

		/*
		 * Add unpaired tracks to overlay.
		 */

		addUnmatchedTracks( pairing.unmatchedTracks1, overlay, calibration, COLOR1, 0 );
		addUnmatchedTracks( pairing.unmatchedTracks2, overlay, calibration, COLOR2, 1 );

		/*
		 * Add paired tracks to overlay.
		 */

		addMatchedTracks( pairing.pairs, overlay, calibration, COLOR3, 2 );

		return output;
	}

	private static void addMatchedTracks(
			final Collection< TrackPair > pairs,
			final Overlay overlay,
			final double[] calibration,
			final Color color,
			final int group )
	{
		for ( final TrackPair trackPair : pairs )
		{
			for ( final SpotPair spotPair : trackPair.paired )
			{
				final OvalRoi roi1 = spotToRoi( spotPair.s1, calibration, color, group );
				final OvalRoi roi2 = spotToRoi( spotPair.s2, calibration, color, group );
				overlay.add( roi1 );
				overlay.add( roi2 );
				overlay.add( lineBetween( roi1, roi2, color, group ) );
				overlay.add( annotate( trackPair.id1 + "&" + trackPair.id2, roi1, roi2, color, group ) );
			}
		}
	}

	private static final void addUnmatchedTracks(
			final Map< Integer, Collection< Spot > > unmatchedTracks,
			final Overlay overlay,
			final double[] calibration,
			final Color color,
			final int group )
	{
		for ( final Integer id : unmatchedTracks.keySet() )
		{
			final Collection< Spot > track = unmatchedTracks.get( id );
			for ( final Spot spot : track )
			{
				final OvalRoi roi = spotToRoi( spot, calibration, color, group );
				overlay.add( roi );
				overlay.add( annotate( "" + id, roi, roi, color, group ) );
			}
		}
	}

	private static TextRoi annotate( final String text, final OvalRoi roi1, final OvalRoi roi2, final Color color, final int group )
	{
		final double r1 = roi1.getFloatWidth() / 2.;
		final double r2 = roi2.getFloatWidth() / 2.;
		final double xc1 = roi1.getXBase() + r1;
		final double yc1 = roi1.getYBase() + r1;
		final double xc2 = roi2.getXBase() + r2;
		final double yc2 = roi2.getYBase() + r2;

		final double mx1 = xc1 + r1 + 1.5;
		final double mx2 = xc2 + r2 + 1.5;
		final double mx;
		final double my;
		if ( mx1 > mx2 )
		{
			mx = mx1;
			my = yc1;
		}
		else
		{
			mx = mx2;
			my = yc2;
		}
		final TextRoi roi = new TextRoi( mx, my, text );
		roi.setFontSize( FONT_SIZE );
		final float height = roi.getCurrentFont().getSize2D();
		roi.setLocation( mx, my - height / 2. );
		roi.setGroup( group );
		roi.setStrokeColor( color );
		roi.setName( roi1.getName() + '-' + roi2.getName() );
		roi.setPosition( 0, 0, roi1.getTPosition() );
		return roi;
	}

	private static Line lineBetween( final OvalRoi roi1, final OvalRoi roi2, final Color color, final int group )
	{
		final double r1 = roi1.getFloatWidth() / 2.;
		final double r2 = roi2.getFloatWidth() / 2.;
		final double xc1 = roi1.getXBase() + r1;
		final double yc1 = roi1.getYBase() + r1;
		final double xc2 = roi2.getXBase() + r2;
		final double yc2 = roi2.getYBase() + r2;
		final double xs1 = xc1 - 0.5;
		final double ys1 = yc1 - 0.5;
		final double xs2 = xc2 - 0.5;
		final double ys2 = yc2 - 0.5;
		final Line roi = new Line( xs1, ys1, xs2, ys2 );
		roi.setGroup( group );
		roi.setStrokeColor( color );
		roi.setName( roi1.getName() + '-' + roi2.getName() );
		roi.setPosition( 0, 0, roi1.getTPosition() );
		return roi;
	}

	private static final OvalRoi spotToRoi(
			final Spot spot,
			final double[] calibration,
			final Color color,
			final int group )
	{
		final int frame = spot.getFeature( Spot.FRAME ).intValue();
		final double x = spot.getDoublePosition( 0 ) / calibration[ 0 ];
		final double y = spot.getDoublePosition( 1 ) / calibration[ 1 ];
		final double radius = spot.getFeature( Spot.RADIUS ).doubleValue() / calibration[ 0 ];
		final OvalRoi roi = new OvalRoi( x - radius, y - radius, 2 * radius, 2 * radius );
		roi.setGroup( group );
		roi.setStrokeColor( color );
		roi.setName( spot.getName() );
		roi.setPosition( 0, 0, frame + 1 );
		return roi;
	}
}
