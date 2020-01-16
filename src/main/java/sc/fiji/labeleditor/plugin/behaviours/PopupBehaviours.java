package sc.fiji.labeleditor.plugin.behaviours;

import org.scijava.Context;
import org.scijava.plugin.Parameter;
import org.scijava.ui.behaviour.ClickBehaviour;
import org.scijava.ui.behaviour.util.Behaviours;
import sc.fiji.labeleditor.core.controller.InteractiveLabeling;
import sc.fiji.labeleditor.core.controller.LabelEditorBehaviours;
import sc.fiji.labeleditor.plugin.interfaces.LabelEditorPopupMenu;

import java.awt.*;

public class PopupBehaviours<L> implements LabelEditorBehaviours<L> {

	@Parameter
	private Context context;

	private InteractiveLabeling<L> labeling;
	private static final String OPEN_POPUP_TRIGGERS = "button3";
	private static final String OPEN_POPUP_NAME = "LABELEDITOR_OPENPOPUP";

	@Override
	public void init(InteractiveLabeling<L> labeling) {
		this.labeling = labeling;
	}

	@Override
	public void install(Behaviours behaviours, Component panel) {
		behaviours.behaviour(getOpenPopupBehaviour(), OPEN_POPUP_NAME, OPEN_POPUP_TRIGGERS);
	}

	public ClickBehaviour getOpenPopupBehaviour() {
		return this::openPopupAt;
	}

	private void openPopupAt(int x, int y) {
		LabelEditorPopupMenu<L> menu = new LabelEditorPopupMenu<>(labeling);
		if(context != null) context.inject(menu);
		menu.populate();
		menu.show(labeling.interfaceInstance().getComponent(), x, y);
	}

}
