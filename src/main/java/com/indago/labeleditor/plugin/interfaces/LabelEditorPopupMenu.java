package com.indago.labeleditor.plugin.interfaces;

import com.indago.labeleditor.core.controller.LabelEditorController;
import com.indago.labeleditor.core.model.LabelEditorModel;
import com.indago.labeleditor.plugin.behaviours.LabelingModificationBehaviours;
import com.indago.labeleditor.plugin.behaviours.TagModificationBehaviours;
import com.indago.labeleditor.plugin.behaviours.ViewBehaviours;
import org.scijava.Context;
import org.scijava.plugin.Parameter;

import javax.swing.*;
import java.awt.event.ActionListener;

public class LabelEditorPopupMenu<L> extends JPopupMenu {

	@Parameter
	Context context;

	private final LabelEditorModel<L> model;
	private final LabelEditorController<L> control;

	public LabelEditorPopupMenu(LabelEditorModel<L> model, LabelEditorController<L> control) {

		this.model = model;
		this.control = control;

		makeSelectMenu();
		makeEditMenu();
		makeViewMenu();
		makeExportMenu();
	}

	private void makeExportMenu() {
	}

	private void makeViewMenu() {
		ViewBehaviours viewBehaviours = new ViewBehaviours();
		viewBehaviours.init(model, control);
		if(context != null) context.inject(viewBehaviours);
		JMenu menu = new JMenu("view");
		menu.add(getMenuItem(
				actionEvent -> new Thread( () -> viewBehaviours.getViewBehaviour().viewSelected()).start(),
				"View in new window"));
		add(menu);

	}

	private void makeEditMenu() {
		LabelingModificationBehaviours modificationBehaviours = new LabelingModificationBehaviours();
		modificationBehaviours.init(model, control);
		if(context != null) context.inject(modificationBehaviours);
		TagModificationBehaviours tagBehaviours = new TagModificationBehaviours();
		tagBehaviours.init(model, control);
		if(context != null) context.inject(modificationBehaviours);
		JMenu menu = new JMenu("edit");
		menu.add(getMenuItem(
				actionEvent -> new Thread( () -> modificationBehaviours.getDeleteBehaviour().deleteSelected()).start(),
				"Delete selected"));
		menu.add(getMenuItem(
				actionEvent -> new Thread( () -> modificationBehaviours.getSplitBehaviour().splitSelected()).start(),
				"Split selected"));
		menu.add(getMenuItem(
				actionEvent -> new Thread( () -> modificationBehaviours.getMergeBehaviour().assignSelectedToFirst()).start(),
				"Merge selected"));
		menu.add(getMenuItem(
				actionEvent -> new Thread( () -> tagBehaviours.getTagByPropertyBehaviour().circularity()).start(),
				"Tag with random numbers"));
		add(menu);
	}

	private void makeSelectMenu() {
		//TODO
	}

	private JMenuItem getMenuItem(ActionListener actionListener, String label) {
		JMenuItem item = new JMenuItem(label);
		item.addActionListener(actionListener);
		return item;
	}
}