package sc.fiji.labeleditor.plugin.behaviours.select;

import de.embl.cba.table.select.Listeners;
import de.embl.cba.table.select.SelectionListener;
import de.embl.cba.table.select.SelectionModel;
import net.imglib2.roi.labeling.LabelingType;
import org.scijava.command.CommandService;
import org.scijava.plugin.Parameter;
import org.scijava.ui.behaviour.Behaviour;
import org.scijava.ui.behaviour.ClickBehaviour;
import org.scijava.ui.behaviour.ScrollBehaviour;
import org.scijava.ui.behaviour.util.Behaviours;
import sc.fiji.labeleditor.core.controller.LabelEditorBehaviours;
import sc.fiji.labeleditor.core.controller.LabelEditorController;
import sc.fiji.labeleditor.core.model.LabelEditorModel;
import sc.fiji.labeleditor.core.model.tagging.LabelEditorTag;
import sc.fiji.labeleditor.core.view.LabelEditorView;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class SelectionBehaviours<L> implements SelectionModel< L >, LabelEditorBehaviours<L> {

	@Parameter
	CommandService commandService;

	protected LabelEditorModel<L> model;
	protected LabelEditorController<L> controller;

	private final Listeners.List<SelectionListener> listeners = new Listeners.SynchronizedList<>();
	private boolean listenersPaused = false;

	protected static final String TOGGLE_LABEL_SELECTION_NAME = "LABELEDITOR_TOGGLELABELSELECTION";
	protected static final String TOGGLE_LABEL_SELECTION_TRIGGERS = "shift scroll";
	protected static final String SELECT_FIRST_LABEL_NAME = "LABELEDITOR_SELECTFIRSTLABEL";
	protected static final String SELECT_FIRST_LABEL_TRIGGERS = "button1";
	protected static final String ADD_LABEL_TO_SELECTION_NAME = "LABELEDITOR_ADDLABELTOSELECTION";
	protected static final String ADD_LABEL_TO_SELECTION_TRIGGERS = "shift button1";
	protected static final String SELECT_ALL_LABELS_NAME = "LABELEDITOR_SELECTALL";
	protected static final String SELECT_ALL_LABELS_TRIGGERS = "ctrl A";

	@Override
	public void init(LabelEditorModel<L> model, LabelEditorController<L> controller, LabelEditorView<L> view) {
		this.model = model;
		this.controller = controller;
	}

	@Override
	public void install(Behaviours behaviours, Component panel) {

		behaviours.behaviour(getToggleLabelSelectionBehaviour(), TOGGLE_LABEL_SELECTION_NAME, TOGGLE_LABEL_SELECTION_TRIGGERS);
		behaviours.behaviour(getSelectFirstLabelBehaviour(), SELECT_FIRST_LABEL_NAME, SELECT_FIRST_LABEL_TRIGGERS);
		behaviours.behaviour(getAddFirstLabelToSelectionBehaviour(), ADD_LABEL_TO_SELECTION_NAME, ADD_LABEL_TO_SELECTION_TRIGGERS);
		behaviours.behaviour(getSelectAllBehaviour(), SELECT_ALL_LABELS_NAME, SELECT_ALL_LABELS_TRIGGERS);

	}

	private Behaviour getSelectAllBehaviour() {
		return (ClickBehaviour) (arg0, arg1) -> {
			model.tagging().pauseListeners();
			selectAll();
			model.tagging().resumeListeners();
		};
	}

	protected Behaviour getAddFirstLabelToSelectionBehaviour() {
		return (ClickBehaviour) (arg0, arg1) -> {
			model.tagging().pauseListeners();
			addFirstLabelToSelection(arg0, arg1);
			model.tagging().resumeListeners();
		};
	}

	protected Behaviour getSelectFirstLabelBehaviour() {
		return (ClickBehaviour) (arg0, arg1) -> {
			model.tagging().pauseListeners();
			selectFirstLabel(arg0, arg1);
			model.tagging().resumeListeners();
		};
	}

	protected Behaviour getToggleLabelSelectionBehaviour() {
		return (ScrollBehaviour) (wheelRotation, isHorizontal, x, y) -> {
			if(!isHorizontal) {
				model.tagging().pauseListeners();
				toggleLabelSelection(wheelRotation > 0, x, y);
				model.tagging().resumeListeners();
			}};
	}

	public void selectAll() {
		controller.labelSetInScope().forEach(this::select);
	}

	protected void selectFirstLabel(int x, int y) {
		LabelingType<L> labels = controller.interfaceInstance().findLabelsAtMousePosition(x, y, model);
		if (foundLabels(labels)) {
			selectFirst(labels);
		} else {
			clearSelection();
		}
	}

	private boolean foundLabels(LabelingType<L> labels) {
		return labels != null && labels.size() > 0;
	}

	protected void addFirstLabelToSelection(int x, int y) {
		LabelingType<L> labels = controller.interfaceInstance().findLabelsAtMousePosition(x, y, model);
		if (foundLabels(labels)) {
			toggleSelectionOfFirst(labels);
		}
	}

	protected void toggleLabelSelection(boolean forwardDirection, int x, int y) {
		LabelingType<L> labels = controller.interfaceInstance().findLabelsAtMousePosition(x, y, model);
		if(!foundLabels(labels)) return;
		if(!anySelected(labels)) {
			selectFirst(labels);
			return;
		}
		if (forwardDirection)
			selectNext(labels);
		else
			selectPrevious(labels);
	}

	protected void selectFirst(LabelingType<L> labels) {
		L label = getFirst(labels);
		if(model.tagging().getTags(label).contains(LabelEditorTag.SELECTED)) return;
		clearSelection();
		select(label);
	}

	protected void toggleSelectionOfFirst(LabelingType<L> labels) {
		L label = getFirst(labels);
		if(model.tagging().getTags(label).contains(LabelEditorTag.SELECTED)) {
			setSelected(label, false);
		} else {
			select(label);
		}
	}

	protected L getFirst(LabelingType<L> labels) {
		if(labels.size() == 0) return null;
		List<L> orderedLabels = new ArrayList<>(labels);
		orderedLabels.sort(model.getLabelComparator());
		return orderedLabels.get(0);
	}

	public boolean isSelected(L label) {
		return model.tagging().getTags(label).contains(LabelEditorTag.SELECTED);
	}

	@Override
	public void setSelected(L label, boolean select) {
		if(select) {
			model.tagging().addTagToLabel(LabelEditorTag.SELECTED, label);
		} else {
			model.tagging().removeTagFromLabel(LabelEditorTag.SELECTED, label);
		}
		notifyListeners();
		notifyListeners(label);
	}

	@Override
	public void toggle(L label) {
		if(isSelected(label)) {
			model.tagging().removeTagFromLabel(LabelEditorTag.SELECTED, label);
		} else {
			model.tagging().addTagToLabel(LabelEditorTag.SELECTED, label);
		}
		notifyListeners();
	}

	@Override
	public void focus(L label) {
		model.tagging().addTagToLabel(LabelEditorTag.FOCUS, label);
	}

	@Override
	public boolean isFocused(L label) {
		return model.tagging().getTags(label).contains(LabelEditorTag.FOCUS);
	}

	@Override
	public boolean setSelected(Collection<L> labels, boolean select) {
		labels.forEach(label -> {
			model.tagging().addTagToLabel(LabelEditorTag.SELECTED, label);
		});
		notifyListeners();
		return true;
	}

	private void notifyListeners() {
		listeners.list.forEach(listener -> {
			listener.selectionChanged();
		});
	}

	private void notifyListeners(L label) {
		listeners.list.forEach(listener -> {
			listener.focusChanged();
		});
	}

	@Override
	public boolean clearSelection() {
		model.tagging().removeTagFromLabel(LabelEditorTag.SELECTED);
		notifyListeners();
		return false;
	}

	@Override
	public Set<L> getSelected() {
		return model.tagging().getLabels(LabelEditorTag.SELECTED);
	}

	@Override
	public L getFocused() {
		Set<L> labels = model.tagging().getLabels(LabelEditorTag.FOCUS);
		if(labels == null) return null;
		return labels.iterator().next();
	}

	@Override
	public boolean isEmpty() {
		Set<L> selected = getSelected();
		if(selected == null) return true;
		return selected.size() == 0;
	}

	@Override
	public Listeners<SelectionListener> listeners() {
		return listeners;
	}

	@Override
	public void resumeListeners() {
		listenersPaused = false;
	}

	@Override
	public void pauseListeners() {
		listenersPaused = true;
	}

	protected boolean anySelected(LabelingType<L> labels) {
		return labels.stream().anyMatch(label -> model.tagging().getTags(label).contains(LabelEditorTag.SELECTED));
	}

	protected void select(L label) {
		setSelected(label, true);
	}

	protected void selectPrevious(LabelingType<L> labels) {
		List<L> reverseLabels = new ArrayList<>(labels);
		Collections.reverse(reverseLabels);
		selectNext(reverseLabels);
	}

	protected void selectNext(Collection<L> labels) {
		boolean foundSelected = false;
		for (Iterator<L> iterator = labels.iterator(); iterator.hasNext(); ) {
			L label = iterator.next();
			if (isSelected(label)) {
				foundSelected = true;
				if(iterator.hasNext()) {
					setSelected(label, false);
				}
			} else {
				if (foundSelected) {
					select(label);
					return;
				}
			}
		}
	}

	public void invertSelection() {
		Set<L> all = new HashSet(controller.labelSetInScope());
		Set<L> selected = model.tagging().filterLabelsWithTag(all, LabelEditorTag.SELECTED);
		all.removeAll(selected);
		setSelected(all, true);
		setSelected(selected, false);
	}

	public void selectByTag() {
		commandService.run(SelectByTagCommand.class, true,
				"model", model, "control", controller);
	}
}
