
package sc.fiji.labeleditor.scripts;

import org.junit.Test;
import sc.fiji.labeleditor.core.controller.InteractiveLabeling;

import java.io.File;

public class RunScript_E01_2DLabeling extends ScriptTest {

	@Test
	public void run() throws Exception {
		ij.script().run(new File(InteractiveLabeling.class.getResource(
			"/script-templates/ImageJ2/LabelEditor/E01_2DLabeling.groovy").getPath()), true).get();
	}

	public static void main(String... args) throws Exception
	{
		new RunScript_E01_2DLabeling().runWithUI();
	}
}
