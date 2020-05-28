package sc.fiji.labeleditor.plugin.interfaces;

import org.scijava.Context;
import org.scijava.plugin.Parameter;
import sc.fiji.labeleditor.core.controller.InteractiveLabeling;
import sc.fiji.labeleditor.core.controller.LabelEditorInterface;
import sc.fiji.labeleditor.core.view.LabelEditorRenderer;
import sc.fiji.labeleditor.plugin.behaviours.AllModelsBehaviours;
import sc.fiji.labeleditor.plugin.behaviours.OptionsBehaviours;
import sc.fiji.labeleditor.plugin.behaviours.export.ExportBehaviours;
import sc.fiji.labeleditor.plugin.behaviours.modification.LabelingModificationBehaviours;
import sc.fiji.labeleditor.plugin.behaviours.modification.TagModificationBehaviours;
import sc.fiji.labeleditor.plugin.behaviours.select.SelectionBehaviours;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

// TODO - can I do that with net.imagej.plugins.tools.ContextHandler?
public class LabelEditorPopupMenu extends JPopupMenu {

	@Parameter
	private Context context;

	private final LabelEditorInterface labelEditorInterface;

	private static final String MENU_EDIT = "Edit";
	private static final String MENU_EDIT_DELETE = "Delete selected";
	private static final String MENU_EDIT_MERGE = "Merge selected";

	private static final String MENU_EXPORT = "Export";
	private static final String MENU_EXPORT_SELECTED = "Export selected labels";
	private static final String MENU_EXPORT_LABELMAP = "Export label map";
	private static final String MENU_EXPORT_INDEXIMG = "Export index image";
	private static final String MENU_EXPORT_SOURCE = "Export source image";
	private static final String MENU_EXPORT_RENDERERS = "Renderers";
	private static final String MENU_EXPORT_TABLE = "Export as table";

	private static final String MENU_SELECT = "Select";
	private static final String MENU_SELECT_ALL = "Select all";
	private static final String MENU_SELECT_NONE = "Deselect all";
	private static final String MENU_SELECT_INVERT = "Invert selection";
	private static final String MENU_SELECT_BYTAG = "By tag..";

	private static final String MENU_OPTIONS = "Options";

	public LabelEditorPopupMenu(LabelEditorInterface labelEditorInterface) {
		this.labelEditorInterface = labelEditorInterface;
	}

	public void build(Set<InteractiveLabeling<?>> labelings) {
		removeAll();
		for (InteractiveLabeling<?> labeling : labelings) {
			populate(labeling);
		}
		addModelsOptionsEntry(labelings);
	}

	public void populate(InteractiveLabeling<?> labeling) {
		JMenu labelingMenu = new JMenu(labeling.model().getName());
		makeSelectMenu(labeling, labelingMenu);
		makeEditMenu(labeling, labelingMenu);
		makeExportMenu(labeling, labelingMenu);
		makeOptionsEntry(labeling, labelingMenu);
		add(labelingMenu);
	}

	private void addModelsOptionsEntry(Set<InteractiveLabeling<?>> labelings) {
		if(context != null) {
			AllModelsBehaviours behaviours = new AllModelsBehaviours();
			context.inject(behaviours);
			behaviours.init(labelings, labelEditorInterface);
			add(getMenuItem(e -> runInNewThread(behaviours::showOptions), MENU_OPTIONS));
		}
	}

	private <L> void makeOptionsEntry(InteractiveLabeling<L> labeling, JMenu labelingMenu) {
		if(context != null) {
			OptionsBehaviours<L> optionsBehaviours = new OptionsBehaviours<>();
			context.inject(optionsBehaviours);
			optionsBehaviours.init(labeling);
			labelingMenu.add(getMenuItem(e -> runInNewThread(optionsBehaviours::showOptions), MENU_OPTIONS));
		}
	}

	private <L> void makeExportMenu(InteractiveLabeling<L> labeling, JMenu labelingMenu) {
		if(context != null) {
			ExportBehaviours exportBehaviours = new ExportBehaviours();
			exportBehaviours.init(labeling);
			context.inject(exportBehaviours);
			JMenu menu = new JMenu(MENU_EXPORT);
			menu.add(getMenuItem(e -> runInNewThread(exportBehaviours.getExportSelectedLabels()::exportSelected), MENU_EXPORT_SELECTED));
			menu.add(getMenuItem(e -> runInNewThread(exportBehaviours::showLabelMap), MENU_EXPORT_LABELMAP));
			menu.add(getMenuItem(e -> runInNewThread(exportBehaviours::showIndexImg), MENU_EXPORT_INDEXIMG));
			menu.add(getMenuItem(e -> runInNewThread(exportBehaviours::showData), MENU_EXPORT_SOURCE));
			menu.add(getMenuItem(e -> runInNewThread(exportBehaviours::showTables), MENU_EXPORT_TABLE));
			if (labeling.view().renderers().size() > 0) {
				JMenu renderers = new JMenu(MENU_EXPORT_RENDERERS);
				for (LabelEditorRenderer<L> renderer : labeling.view().renderers()) {
					renderers.add(getMenuItem(e -> new Thread(() ->
							exportBehaviours.showRenderer(renderer)).start(), "Export " + renderer.getName()));
				}
				menu.add(renderers);
			}
			labelingMenu.add(menu);
		}
	}

	private <L> void makeEditMenu(InteractiveLabeling<L> labeling, JMenu labelingMenu) {
		JMenu menu = new JMenu(MENU_EDIT);
		LabelingModificationBehaviours<L> modificationBehaviours = new LabelingModificationBehaviours<>();
		modificationBehaviours.init(labeling);
		TagModificationBehaviours<L> tagBehaviours = new TagModificationBehaviours<>();
		tagBehaviours.init(labeling);
		menu.add(getMenuItem(e -> runWhilePausingListeners(labeling, modificationBehaviours.getDeleteBehaviour()::deleteSelected), MENU_EDIT_DELETE));
		menu.add(getMenuItem(e -> runWhilePausingListeners(labeling, modificationBehaviours.getMergeBehaviour()::assignSelectedToFirst), MENU_EDIT_MERGE));
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
		labelingMenu.add(menu);
	}

	private <L> void makeSelectMenu(InteractiveLabeling<L> labeling, JMenu labelingMenu) {
		JMenu menu = new JMenu(MENU_SELECT);
		SelectionBehaviours<L> selectionBehaviours = new SelectionBehaviours<>();
		selectionBehaviours.init(labeling);
		menu.add(getMenuItem(e -> runWhilePausingListeners(labeling, selectionBehaviours::selectAll), MENU_SELECT_ALL));
		menu.add(getMenuItem(e -> runWhilePausingListeners(labeling, selectionBehaviours::deselectAll), MENU_SELECT_NONE));
		menu.add(getMenuItem(e -> runWhilePausingListeners(labeling, selectionBehaviours::invertSelection), MENU_SELECT_INVERT));
		if(context != null) {
			context.inject(selectionBehaviours);
			menu.add(getMenuItem(e -> runInNewThread(selectionBehaviours::selectByTag), MENU_SELECT_BYTAG));
		}
		labelingMenu.add(menu);
	}

	private void runInNewThread(Runnable method) {
		new Thread(method).start();
	}

	private void runWhilePausingListeners(InteractiveLabeling<?> labeling, Runnable method) {
		new Thread(() -> {
			labeling.model().tagging().pauseListeners();
			method.run();
			labeling.model().tagging().resumeListeners();
		}).start();
	}

	private JMenuItem getMenuItem(ActionListener actionListener, String label) {
		JMenuItem item = new JMenuItem(label);
		item.addActionListener(actionListener);
		return item;
	}
}
