package com.indago.labeleditor;

import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.io.yaml.YamlConfigIO;
import org.scijava.ui.behaviour.util.InputActionBindings;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class InputTriggerConfig2D {
	/**
	 * @return the loaded <code>InputTriggerConfig</code>, or <code>null</code>
	 *         if none was found.
	 *
	 */
	public InputTriggerConfig load(JPanel parent) {
		try {

			System.out.println( "Try to fetch yaml from " + ClassLoader.getSystemResource( "metaseg.yaml" ) );
			URL yamlURL = ClassLoader.getSystemResource( "metaseg.yaml" );
			if ( yamlURL == null ) {
				System.out.println( "Try to fetch yaml from " + getClass().getClassLoader().getResource( "metaseg.yaml" ) );
				yamlURL = getClass().getClassLoader().getResource( "metaseg.yaml" );
			}
			if ( yamlURL != null ) {
				final BufferedReader in = new BufferedReader( new InputStreamReader( yamlURL.openStream() ) );
				final InputTriggerConfig conf = new InputTriggerConfig( YamlConfigIO.read( in ) );

				final InputActionBindings bindings = new InputActionBindings();
				SwingUtilities.replaceUIActionMap( parent, bindings.getConcatenatedActionMap() );
				SwingUtilities.replaceUIInputMap( parent, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, bindings.getConcatenatedInputMap() );

//				final AbstractActions a = new AbstractActions( bindings, "tabs", conf, new String[] { "tr2d" } );
//
//				a.runnableAction(
//						() -> tabs.setSelectedIndex( Math.min( tabs.getSelectedIndex() + 1, tabs.getTabCount() - 1 ) ),
//						"next tab",
//						"COLON" );
//				a.runnableAction(
//						() -> tabs.setSelectedIndex( Math.max( tabs.getSelectedIndex() - 1, 0 ) ),
//						"previous tab",
//						"COMMA" );

				return conf;
			} else {
				System.out.println( "Falling back to default BDV action settings." );
				final InputTriggerConfig conf = new InputTriggerConfig();
				final InputActionBindings bindings = new InputActionBindings();
				SwingUtilities.replaceUIActionMap( parent, bindings.getConcatenatedActionMap() );
				SwingUtilities.replaceUIInputMap( parent, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, bindings.getConcatenatedInputMap() );
				return conf;
			}
		} catch ( IllegalArgumentException | IOException e ) {
			e.printStackTrace();
		}

		return null;
	}

}
