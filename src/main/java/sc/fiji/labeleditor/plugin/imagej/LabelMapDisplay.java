package sc.fiji.labeleditor.plugin.imagej;

import org.scijava.Priority;
import org.scijava.display.AbstractDisplay;
import org.scijava.display.Display;
import org.scijava.plugin.Plugin;
import sc.fiji.labeleditor.application.LabelMap;

@Plugin(type = Display.class, priority = Priority.HIGH)
public class LabelMapDisplay extends AbstractDisplay<LabelMap> {

	public LabelMapDisplay() {
		super(LabelMap.class);
	}

}
