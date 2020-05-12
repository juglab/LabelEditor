
package sc.fiji.labeleditor.scripts;

import net.imglib2.img.Img;
import org.junit.Test;
import sc.fiji.labeleditor.core.controller.InteractiveLabeling;

import java.io.File;

public class RunScript_E04_LabelMap extends ScriptTest {

	@Test
	public void run() throws Exception {
		Img input = (Img) ij.io().open(getClass().getResource("/labelmap.png").getPath());
		ij.script().run(new File(InteractiveLabeling.class.getResource(
			"/script-templates/ImageJ2/LabelEditor/E04_LabelMap.groovy").getPath()),
				true, new Object[] { "input", input }).get();
	}

	public static void main(String... args) throws Exception
	{
		new RunScript_E04_LabelMap().runWithUI();
	}
}
