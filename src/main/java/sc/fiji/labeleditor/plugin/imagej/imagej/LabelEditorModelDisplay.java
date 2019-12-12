package sc.fiji.labeleditor.plugin.imagej.imagej;

import sc.fiji.labeleditor.core.model.LabelEditorModel;
import org.scijava.display.AbstractDisplay;
import org.scijava.display.Display;
import org.scijava.plugin.Plugin;

@Plugin(type = Display.class)
public class LabelEditorModelDisplay extends AbstractDisplay<LabelEditorModel> {

	public LabelEditorModelDisplay() {
		super(LabelEditorModel.class);
	}

}