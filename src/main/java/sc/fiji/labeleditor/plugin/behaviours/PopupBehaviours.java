package sc.fiji.labeleditor.plugin.behaviours;

import org.scijava.Context;
import org.scijava.plugin.Parameter;
import org.scijava.ui.behaviour.ClickBehaviour;
import org.scijava.ui.behaviour.util.Behaviours;
import sc.fiji.labeleditor.core.controller.LabelEditorBehaviours;
import sc.fiji.labeleditor.core.controller.LabelEditorController;
import sc.fiji.labeleditor.core.model.LabelEditorModel;
import sc.fiji.labeleditor.core.view.LabelEditorView;
import sc.fiji.labeleditor.plugin.interfaces.LabelEditorPopupMenu;

import java.awt.*;

public class PopupBehaviours implements LabelEditorBehaviours {

	@Parameter
	Context context;

	private LabelEditorView view;
	private LabelEditorModel model;
	private LabelEditorController control;

	private static final String OPEN_POPUP_TRIGGERS = "button3";
	private static final String OPEN_POPUP_NAME = "LABELEDITOR_OPENPOPUP";

	@Override
	public void init(LabelEditorModel model, LabelEditorController controller, LabelEditorView view) {
		this.model = model;
		this.control = controller;
		this.view = view;
	}

	@Override
	public void install(Behaviours behaviours, Component panel) {
		behaviours.behaviour(getOpenPopupBehaviour(), OPEN_POPUP_NAME, OPEN_POPUP_TRIGGERS);
	}

	public ClickBehaviour getOpenPopupBehaviour() {
		return (arg0, arg1) -> openPopupAt(arg0, arg1);
	}

	private void openPopupAt(int x, int y) {
		LabelEditorPopupMenu menu = new LabelEditorPopupMenu(model, control, view);
		if(context != null) context.inject(menu);
		menu.populate();
		menu.show(control.interfaceInstance().getComponent(), x, y);
	}

}
