
package sc.fiji.labeleditor.scripts;

import net.imagej.ImageJ;
import org.junit.Ignore;
import org.junit.Test;
import sc.fiji.labeleditor.core.InteractiveLabeling;

import javax.script.ScriptException;
import java.io.File;
import java.io.FileNotFoundException;

public class RunScript_E02_2DLabelingAndSource {

	public static void main(String... args) throws FileNotFoundException,
			ScriptException
	{
		new RunScript_E02_2DLabelingAndSource().run();
	}

	@Test
	@Ignore //FIXME
	public void run() throws FileNotFoundException, ScriptException {
		ImageJ ij = new ImageJ();
		ij.ui().showUI();

		ij.script().run(new File(InteractiveLabeling.class.getResource(
			"/script-templates/ImageJ2/LabelEditor/E02_2DLabelingAndSource.groovy").getPath()), false, new Object[] { "ops", ij
				.op(), "ui", ij.ui(), "io", ij.io() });
		ij.context().dispose();
	}
}
