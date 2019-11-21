package sc.fiji.labeleditor.plugin.behaviours;

import sc.fiji.labeleditor.core.view.LabelEditorView;
import org.scijava.command.Command;
import org.scijava.command.InteractiveCommand;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin(type = Command.class, name = "LabelEditor options")
public class LabelEditorOptionsCommand extends InteractiveCommand {

	@Parameter
	LabelEditorView view;

	private static final String TOOLTIPS_FULL = "labels and tags";
	private static final String TOOLTIPS_LABELS = "labels";
	private static final String TOOLTIPS_TAGS = "tags";
	private static final String TOOLTIPS_NONE = "nothing";
	@Parameter(description = "What should be displayed in tooltip?", choices = {TOOLTIPS_FULL, TOOLTIPS_LABELS, TOOLTIPS_TAGS, TOOLTIPS_NONE})
	private String toolTipMode = TOOLTIPS_FULL;

	@Override
	public void run() {
		view.setShowToolTip(toolTipMode.equals(TOOLTIPS_NONE));
		if(toolTipMode.equals(TOOLTIPS_FULL)) {
			view.setShowLabelsInToolTip(true);
			view.setShowTagsInToolTip(true);
		} else {
			view.setShowLabelsInToolTip(toolTipMode.equals(TOOLTIPS_LABELS));
			view.setShowTagsInToolTip(toolTipMode.equals(TOOLTIPS_TAGS));
		}
	}

}
