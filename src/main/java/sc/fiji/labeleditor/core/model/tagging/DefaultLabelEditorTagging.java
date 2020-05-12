package sc.fiji.labeleditor.core.model.tagging;

import org.scijava.listeners.Listeners;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.table.DefaultDoubleTable;
import org.scijava.table.DefaultGenericTable;
import org.scijava.table.GenericTable;
import org.scijava.table.Table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class DefaultLabelEditorTagging<L> implements LabelEditorTagging<L> {

	@Parameter
	LogService log;

	private final GenericTable table = new DefaultGenericTable();
	private final HashMap<Object, Integer> tagToColumn = new HashMap<>();
	private final HashMap<Integer, Object> columnToTag = new HashMap<>();
	private final HashMap<L, Integer> labelToRow = new HashMap<>();
	private final HashMap<Integer, L> rowToLabel = new HashMap<>();

	//	private final HashMap<L, Set<Object>> tags = new HashMap<>();
	private final Listeners.List<TagChangeListener> listeners = new Listeners.SynchronizedList<>();
	private boolean listenersPaused = false;
	private List<TagChangedEvent> keptEvents = new ArrayList<>();

	@Override
	public Listeners< TagChangeListener > listeners() {
		return listeners;
	}

	@Override
	public void pauseListeners() {
		listenersPaused = true;
	}

	@Override
	public void resumeListeners() {
		listenersPaused = false;
		if(keptEvents.size() > 0) {
			System.out.println(keptEvents);
			listeners.list.forEach(listener -> listener.tagChanged(keptEvents));
			keptEvents.clear();
		}
	}

	@Override
	public Set<Object> getAllTags() {
		return tagToColumn.keySet();
	}

	@Override
	public Set<L> filterLabelsWithTag(Set<L> labels, Object tag) {
		return labels.stream()
				.filter(label -> this.getTags(label).contains(tag))
				.collect(Collectors.toSet());
	}

	@Override
	public Set filterLabelsWithAnyTag(Set<L> labels, Set<Object> tags) {
		return labels.stream()
				.filter(label -> getTags(label).stream().anyMatch(tags::contains))
				.collect(Collectors.toSet());
	}

	@Override
	public void toggleTag(Object tag, L label) {
		if(getTags(label).contains(tag)) removeTagFromLabel(tag, label);
		else addTagToLabel(tag, label);
	}

	private void notifyListeners(TagChangedEvent e) {
		if(log!= null) log.debug(e.toString());
		if(listenersPaused) {
			keptEvents.add(e);
		} else {
			System.out.println(e);
			listeners.list.forEach(listener -> listener.tagChanged(Collections.singletonList(e)));
		}
	}

	private void notifyListeners(Object tag, L label, TagChangedEvent.Action action) {
		TagChangedEvent e = new TagChangedEvent();
		e.action = action;
		e.tag = tag;
		e.label = label;
		notifyListeners(e);
	}

	@Override
	public void addTagToLabel(Object tag, L label) {
		Integer row = labelToRow.get(label);
		if(row == null) {
			table.appendRow();
			row = table.getRowCount()-1;
			labelToRow.put(label, row);
			rowToLabel.put(row, label);
		}
		Integer col = tagToColumn.get(tag);
		if(col == null) {
			table.appendColumn();
			col = table.getColumnCount()-1;
			tagToColumn.put(tag, col);
			columnToTag.put(col, tag);
		}
		if(table.get(col, row) != null) return;
		table.set(col, row, true);
		notifyListeners(tag, label, TagChangedEvent.Action.ADDED);
	}

	@Override
	public void removeTagFromLabel(Object tag, L label) {
		Integer row = labelToRow.get(label);
		if(row == null) return;
		Integer col = tagToColumn.get(tag);
		if(col == null) return;
		if(table.get(col, row) == null) return;
		table.set(col, row, null);
		notifyListeners(tag, label, TagChangedEvent.Action.REMOVED);
	}

	@Override
	public Set<Object> getTags(L label) {
		Integer row = labelToRow.get(label);
		if(row == null) return Collections.emptySet();
		Set<Object> res = new HashSet<>();
		for (int i = 0; i < table.getColumnCount(); i++) {
			if(table.get(i, row) != null) res.add(columnToTag.get(i));
		}
		return Collections.unmodifiableSet(res);
	}

	@Override
	public synchronized void removeTagFromLabel(Object tag) {
		Integer col = tagToColumn.get(tag);
		if(col == null) return;
		for (int i = 0; i < table.getRowCount(); i++) {
			boolean existed = table.get(col, i) != null;
			table.set(col, i, null);
			if(existed) notifyListeners(tag, rowToLabel.get(i), TagChangedEvent.Action.REMOVED);
		}
	}

	@Override
	public Set<L> getLabels(Object tag) {
		Integer col = tagToColumn.get(tag);
		if(col == null) return Collections.emptySet();
		Set<L> res = new HashSet<>();
		for (int i = 0; i < table.getRowCount(); i++) {
			if(table.get(col, i) != null) res.add(rowToLabel.get(i));
		}
		return Collections.unmodifiableSet(res);
	}

}
