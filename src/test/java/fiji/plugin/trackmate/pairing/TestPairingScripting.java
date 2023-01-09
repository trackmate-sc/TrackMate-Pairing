/*-
 * #%L
 * Fiji distribution of ImageJ for the life sciences.
 * %%
 * Copyright (C) 2021 - 2023 The Institut Pasteur.
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

import java.io.File;

import org.scijava.Context;
import org.scijava.ui.swing.script.TextEditor;

import fiji.plugin.trackmate.util.TMUtils;
import sc.fiji.Main;

public class TestPairingScripting
{
	public static void main( final String[] args ) throws Exception
	{
		Main.main( args );
		final Context context = TMUtils.getContext();
		final TextEditor te = new TextEditor( context );
		te.open( new File( "scripts/BatchTrackMatePairing.py" ) );
		te.setVisible( true );
	}
}
