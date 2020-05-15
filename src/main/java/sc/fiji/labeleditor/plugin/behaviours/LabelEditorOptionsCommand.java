package sc.fiji.labeleditor.plugin.behaviours;

import sc.fiji.labeleditor.core.view.LabelEditorView;
import org.scijava.command.Command;
import org.scijava.command.InteractiveCommand;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin(type = Command.class, name = "LabelEditor options")
public class LabelEditorOptionsCommand extends InteractiveCommand {

	@Parameter
	private LabelEditorView view;


	@Override
	public void run() {
	}

}
