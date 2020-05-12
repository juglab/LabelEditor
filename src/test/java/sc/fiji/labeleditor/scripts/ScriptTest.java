package sc.fiji.labeleditor.scripts;

import net.imagej.ImageJ;
import org.junit.After;
import org.scijava.log.LogMessage;

import static org.junit.Assert.assertNotEquals;

abstract class ScriptTest {

	protected ImageJ ij;

	ScriptTest() {
		ij = new ImageJ();
		ij.log().addLogListener(this::logListener);
	}

	@After
	public void dispose() {
		ij.dispose();
	}

	void logListener(LogMessage logMessage) {
		assertNotEquals("Error during script execution: \n" + logMessage.toString(), 1, logMessage.level());
	}

	void runWithUI() throws Exception {
		ij.launch();
		run();
	}

	abstract void run() throws Exception;
}
