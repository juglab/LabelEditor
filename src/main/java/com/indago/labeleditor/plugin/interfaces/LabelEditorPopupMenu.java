package com.indago.labeleditor.plugin.interfaces;

import com.indago.labeleditor.core.controller.LabelEditorController;
import com.indago.labeleditor.core.model.LabelEditorModel;
import com.indago.labeleditor.core.view.LabelEditorRenderer;
import com.indago.labeleditor.core.view.LabelEditorView;
import com.indago.labeleditor.plugin.behaviours.export.ExportBehaviours;
import com.indago.labeleditor.plugin.behaviours.modification.LabelingModificationBehaviours;
import com.indago.labeleditor.plugin.behaviours.select.SelectionBehaviours;
import com.indago.labeleditor.plugin.behaviours.modification.TagModificationBehaviours;
import com.indago.labeleditor.plugin.behaviours.view.ViewBehaviours;
import org.scijava.Context;
import org.scijava.plugin.Parameter;

import javax.swing.*;
import java.awt.event.ActionListener;

// TODO - can I do that with net.imagej.plugins.tools.ContextHandler?
public class LabelEditorPopupMenu<L> extends JPopupMenu {

	@Parameter
	Context context;

	private final LabelEditorModel<L> model;
	private final LabelEditorController<L> control;
	private final LabelEditorView view;

	private static final String MENU_EXPORT = "Export";
	private static final String MENU_VIEW = "View";
	private static final String MENU_EDIT = "Edit";
	private static final String MENU_SELECT = "Select";
	private static final String MENU_EXPORT_INDEXIMG = "Export index image";
	private static final String MENU_EXPORT_SOURCE = "Export source image";
	private static final String MENU_EXPORT_RENDERERS = "Renderers";
	private static final String MENU_VIEW_SELECTED = "View in new window";
	private static final String MENU_SELECT_ALL = "Select all";
	private static final String MENU_SELECT_NONE = "Deselect all";
	private static final String MENU_SELECT_INVERT = "Invert selection";
	private static final String MENU_SELECT_BYTAG = "By tag..";


	public LabelEditorPopupMenu(LabelEditorModel<L> model, LabelEditorController<L> control, LabelEditorView view) {
		this.model = model;
		this.control = control;
		this.view = view;
	}

	public void populate() {
		makeSelectMenu();
		makeEditMenu();
		makeViewMenu();
		makeExportMenu();
	}

	private void makeExportMenu() {
		if(context != null) {
			ExportBehaviours exportBehaviours = new ExportBehaviours();
			exportBehaviours.init(model, control);
			context.inject(exportBehaviours);
			JMenu menu = new JMenu(MENU_EXPORT);
			menu.add(getMenuItem(e -> new Thread(exportBehaviours::showIndexImg).start(), MENU_EXPORT_INDEXIMG));
			menu.add(getMenuItem(e -> new Thread(exportBehaviours::showData).start(), MENU_EXPORT_SOURCE));
			if (view.renderers().size() > 0) {
				JMenu renderers = new JMenu(MENU_EXPORT_RENDERERS);
				for (LabelEditorRenderer renderer : view.renderers()) {
					renderers.add(getMenuItem(e -> new Thread(() ->
							exportBehaviours.showRenderer(renderer)).start(), "Export " + renderer.getName()));
				}
				menu.add(renderers);
			}
			add(menu);
		}
	}

	private void makeViewMenu() {
		ViewBehaviours viewBehaviours = new ViewBehaviours();
		viewBehaviours.init(model, control);
		if(context != null) context.inject(viewBehaviours);
		JMenu menu = new JMenu(MENU_VIEW);
		menu.add(getMenuItem(e -> new Thread( () -> viewBehaviours.getViewBehaviour().viewSelected()).start(), MENU_VIEW_SELECTED));
		add(menu);
	}

	private void makeEditMenu() {
		JMenu menu = new JMenu(MENU_EDIT);
		LabelingModificationBehaviours modificationBehaviours = new LabelingModificationBehaviours();
		modificationBehaviours.init(model, control);
		TagModificationBehaviours tagBehaviours = new TagModificationBehaviours();
		tagBehaviours.init(model, control);
		String MENU_EDIT_DELETE = "Delete selected";
		String MENU_EDIT_MERGE = "Merge selected";
		menu.add(getMenuItem(e -> new Thread( () -> modificationBehaviours.getDeleteBehaviour().deleteSelected()).start(), MENU_EDIT_DELETE));
		menu.add(getMenuItem(e -> new Thread( () -> modificationBehaviours.getMergeBehaviour().assignSelectedToFirst()).start(), MENU_EDIT_MERGE));
		if(context != null) {
			context.inject(modificationBehaviours);
			context.inject(tagBehaviours);
			//TODO make this work properly first
//			menu.add(getMenuItem(e -> new Thread( () -> modificationBehaviours.getSplitBehaviour().splitSelected()).start(), "Split selected"));
			//TODO find all geom ops, add as tagpropertybehaviour
//			menu.add(getMenuItem(
//					actionEvent -> new Thread( () -> tagBehaviours.getTagByPropertyBehaviour().circularity()).start(),
//					"Tag with random numbers"));
		}
		add(menu);
	}

	private void makeSelectMenu() {
		JMenu menu = new JMenu(MENU_SELECT);
		SelectionBehaviours selectionBehaviours = new SelectionBehaviours();
		selectionBehaviours.init(model, control);
		menu.add(getMenuItem(e -> new Thread(() -> selectionBehaviours.selectAll()).start(), MENU_SELECT_ALL));
		menu.add(getMenuItem(e -> new Thread( () -> selectionBehaviours.deselectAll()).start(), MENU_SELECT_NONE));
		menu.add(getMenuItem(e -> new Thread( () -> selectionBehaviours.invertSelection()).start(), MENU_SELECT_INVERT));
		if(context != null) {
			context.inject(selectionBehaviours);
			menu.add(getMenuItem(e -> new Thread( () -> selectionBehaviours.selectByTag()).start(), MENU_SELECT_BYTAG));
		}
		add(menu);
	}

	private JMenuItem getMenuItem(ActionListener actionListener, String label) {
		JMenuItem item = new JMenuItem(label);
		item.addActionListener(actionListener);
		return item;
	}
}