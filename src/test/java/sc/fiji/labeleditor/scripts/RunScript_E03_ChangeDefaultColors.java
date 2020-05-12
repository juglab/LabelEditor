
package sc.fiji.labeleditor.scripts;

import org.junit.Test;
import sc.fiji.labeleditor.core.controller.InteractiveLabeling;

import java.io.File;

public class RunScript_E03_ChangeDefaultColors extends ScriptTest {

	@Test
	public void run() throws Exception {
		ij.script().run(new File(InteractiveLabeling.class.getResource(
				"/script-templates/ImageJ2/LabelEditor/E03_ChangeDefaultColors.groovy").getPath()), true).get();
	}

	public static void main(String... args) throws Exception
	{
		new RunScript_E03_ChangeDefaultColors().runWithUI();
	}
}
