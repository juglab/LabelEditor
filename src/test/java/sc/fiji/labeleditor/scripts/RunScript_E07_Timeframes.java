
package sc.fiji.labeleditor.scripts;

import net.imagej.ImageJ;
import org.junit.Test;
import sc.fiji.labeleditor.core.InteractiveLabeling;

import javax.script.ScriptException;
import java.io.File;
import java.io.FileNotFoundException;

public class RunScript_E07_Timeframes {

	public static void main(String... args) throws FileNotFoundException,
			ScriptException
	{
		new RunScript_E07_Timeframes().run();
	}

	@Test
	public void run() throws FileNotFoundException, ScriptException {
		ImageJ ij = new ImageJ();
		ij.ui().showUI();

		ij.script().run(new File(InteractiveLabeling.class.getResource(
			"/script-templates/ImageJ2/LabelEditor/E07_Timeframes.groovy").getPath()), false, new Object[] { "ui", ij.ui() });
		ij.context().dispose();
	}
}
