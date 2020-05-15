package sc.fiji.labeleditor.plugin.behaviours;

import org.scijava.Context;
import org.scijava.plugin.Parameter;
import org.scijava.ui.behaviour.ClickBehaviour;
import org.scijava.ui.behaviour.util.Behaviours;
import sc.fiji.labeleditor.core.controller.InteractiveLabeling;
import sc.fiji.labeleditor.plugin.interfaces.LabelEditorPopupMenu;

import java.awt.*;

public class PopupBehaviours {

	@Parameter
	private Context context;

	LabelEditorPopupMenu menu = new LabelEditorPopupMenu();

	private static final String OPEN_POPUP_TRIGGERS = "button3";
	private static final String OPEN_POPUP_NAME = "LABELEDITOR_OPENPOPUP";
	private Component component;

	public void add(InteractiveLabeling<?> labeling) {
		menu.populate(labeling);
	}

	public void install(Behaviours behaviours, Component panel) {
		this.component = panel;
		behaviours.behaviour(getOpenPopupBehaviour(), OPEN_POPUP_NAME, OPEN_POPUP_TRIGGERS);
		if(context != null) context.inject(menu);
	}

	public ClickBehaviour getOpenPopupBehaviour() {
		return this::openPopupAt;
	}

	private void openPopupAt(int x, int y) {
		menu.show(component, x, y);
	}

}
