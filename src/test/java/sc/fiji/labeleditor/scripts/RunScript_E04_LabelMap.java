
package sc.fiji.labeleditor.scripts;

import net.imagej.ImageJ;
import net.imglib2.img.Img;
import org.junit.Ignore;
import org.junit.Test;
import sc.fiji.labeleditor.core.InteractiveLabeling;

import javax.script.ScriptException;
import java.io.File;
import java.io.IOException;

public class RunScript_E04_LabelMap {

	public static void main(String... args) throws IOException,
			ScriptException
	{
		new RunScript_E04_LabelMap().run();
	}

	@Test
	@Ignore //FIXME
	public void run() throws IOException, ScriptException {
		ImageJ ij = new ImageJ();
		ij.ui().showUI();

		Img input = (Img) ij.io().open(getClass().getResource("/labelmap.png").getPath());

		ij.script().run(new File(InteractiveLabeling.class.getResource(
			"/script-templates/ImageJ2/LabelEditor/E04_LabelMap.groovy").getPath()), false, new Object[] { "input", input, "ui", ij.ui(), "io", ij.io() });
		ij.context().dispose();
	}
}
