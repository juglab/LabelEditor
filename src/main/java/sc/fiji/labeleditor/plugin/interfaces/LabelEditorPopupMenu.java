package sc.fiji.labeleditor.plugin.interfaces;

import sc.fiji.labeleditor.core.controller.LabelEditorController;
import sc.fiji.labeleditor.core.model.LabelEditorModel;
import sc.fiji.labeleditor.core.view.LabelEditorRenderer;
import sc.fiji.labeleditor.core.view.LabelEditorView;
import sc.fiji.labeleditor.plugin.behaviours.OptionsBehaviours;
import sc.fiji.labeleditor.plugin.behaviours.export.ExportBehaviours;
import sc.fiji.labeleditor.plugin.behaviours.modification.LabelingModificationBehaviours;
import sc.fiji.labeleditor.plugin.behaviours.select.SelectionBehaviours;
import sc.fiji.labeleditor.plugin.behaviours.modification.TagModificationBehaviours;
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

	private static final String MENU_EDIT = "Edit";
	private static final String MENU_EDIT_DELETE = "Delete selected";
	private static final String MENU_EDIT_MERGE = "Merge selected";

	private static final String MENU_EXPORT = "Export";
	private static final String MENU_EXPORT_SELECTED = "Export selected labels";
	private static final String MENU_EXPORT_LABELMAP = "Export label map";
	private static final String MENU_EXPORT_INDEXIMG = "Export index image";
	private static final String MENU_EXPORT_SOURCE = "Export source image";
	private static final String MENU_EXPORT_TABLE = "Export as table";
	private static final String MENU_EXPORT_RENDERERS = "Renderers";

	private static final String MENU_SELECT = "Select";
	private static final String MENU_SELECT_ALL = "Select all";
	private static final String MENU_SELECT_NONE = "Deselect all";
	private static final String MENU_SELECT_INVERT = "Invert selection";
	private static final String MENU_SELECT_BYTAG = "By tag..";

	private static final String MENU_OPTIONS = "Options";


	public LabelEditorPopupMenu(LabelEditorModel<L> model, LabelEditorController<L> control, LabelEditorView view) {
		this.model = model;
		this.control = control;
		this.view = view;
	}

	public void populate() {
		makeSelectMenu();
		makeEditMenu();
		makeExportMenu();
		makeOptionsEntry();
	}

	private void makeOptionsEntry() {
		if(context != null) {
			OptionsBehaviours optionsBehaviours = new OptionsBehaviours();
			context.inject(optionsBehaviours);
			optionsBehaviours.init(model, control, view);
			add(getMenuItem(e -> runInNewThread(optionsBehaviours::showOptions), MENU_OPTIONS));
		}
	}

	private void makeExportMenu() {
		if(context != null) {
			ExportBehaviours exportBehaviours = new ExportBehaviours();
			exportBehaviours.init(model, control, view);
			context.inject(exportBehaviours);
			JMenu menu = new JMenu(MENU_EXPORT);
			menu.add(getMenuItem(e -> runInNewThread(exportBehaviours.getExportSelectedLabels()::exportSelected), MENU_EXPORT_SELECTED));
			menu.add(getMenuItem(e -> runInNewThread(exportBehaviours::showLabelMap), MENU_EXPORT_LABELMAP));
			menu.add(getMenuItem(e -> runInNewThread(exportBehaviours::showIndexImg), MENU_EXPORT_INDEXIMG));
			menu.add(getMenuItem(e -> runInNewThread(exportBehaviours::showData), MENU_EXPORT_SOURCE));
			menu.add(getMenuItem(e -> runInNewThread(exportBehaviours::showTables), MENU_EXPORT_TABLE));
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

	private void makeEditMenu() {
		JMenu menu = new JMenu(MENU_EDIT);
		LabelingModificationBehaviours modificationBehaviours = new LabelingModificationBehaviours();
		modificationBehaviours.init(model, control, view);
		TagModificationBehaviours tagBehaviours = new TagModificationBehaviours();
		tagBehaviours.init(model, control, view);
		menu.add(getMenuItem(e -> runWhilePausingListeners(modificationBehaviours.getDeleteBehaviour()::deleteSelected), MENU_EDIT_DELETE));
		menu.add(getMenuItem(e -> runWhilePausingListeners(modificationBehaviours.getMergeBehaviour()::assignSelectedToFirst), MENU_EDIT_MERGE));
		if(context != null) {
			context.inject(modificationBehaviours);
			context.inject(tagBehaviours);
			//TODO make this work properly first
//			menu.add(getMenuItem(e -> runInNewThread(modificationBehaviours.getSplitBehaviour()::splitSelected), "Split selected"));
			//TODO find all geom ops, add as tagpropertybehaviour
//			menu.add(getMenuItem(
//					actionEvent -> runInNewThread(tagBehaviours.getTagByPropertyBehaviour()::circularity),
//					"Tag with random numbers"));
		}
		add(menu);
	}

	private void makeSelectMenu() {
		JMenu menu = new JMenu(MENU_SELECT);
		SelectionBehaviours selectionBehaviours = new SelectionBehaviours();
		selectionBehaviours.init(model, control, view);
		menu.add(getMenuItem(e -> runWhilePausingListeners(selectionBehaviours::selectAll), MENU_SELECT_ALL));
		menu.add(getMenuItem(e -> runWhilePausingListeners(selectionBehaviours::clearSelection), MENU_SELECT_NONE));
		menu.add(getMenuItem(e -> runWhilePausingListeners(selectionBehaviours::invertSelection), MENU_SELECT_INVERT));
		if(context != null) {
			context.inject(selectionBehaviours);
			menu.add(getMenuItem(e -> runInNewThread(selectionBehaviours::selectByTag), MENU_SELECT_BYTAG));
		}
		add(menu);
	}

	private void runInNewThread(Runnable method) {
		new Thread(() -> method.run()).start();
	}

	private void runWhilePausingListeners(Runnable method) {
		new Thread(() -> {
			model.tagging().pauseListeners();
			method.run();
			model.tagging().resumeListeners();
		}).start();
	}

	private JMenuItem getMenuItem(ActionListener actionListener, String label) {
		JMenuItem item = new JMenuItem(label);
		item.addActionListener(actionListener);
		return item;
	}
}