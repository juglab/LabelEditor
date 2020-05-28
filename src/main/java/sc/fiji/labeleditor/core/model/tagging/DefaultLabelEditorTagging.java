/*-
 * #%L
 * UI component for image segmentation label comparison and selection
 * %%
 * Copyright (C) 2019 - 2020 DAIS developers
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package sc.fiji.labeleditor.core.model.tagging;

import org.scijava.listeners.Listeners;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.table.DefaultGenericTable;
import org.scijava.table.GenericTable;
import sc.fiji.labeleditor.core.model.LabelEditorModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class DefaultLabelEditorTagging<L> implements LabelEditorTagging<L> {

	@Parameter
	LogService log;

	private final LabelEditorModel model;
	private final GenericTable table = new DefaultGenericTable();
	private final HashMap<Object, Integer> tagToColumn = new HashMap<>();
	private final HashMap<Integer, Object> columnToTag = new HashMap<>();
	private final HashMap<L, Integer> labelToRow = new HashMap<>();
	private final HashMap<Integer, L> rowToLabel = new HashMap<>();

	//	private final HashMap<L, Set<Object>> tags = new HashMap<>();
	private final Listeners.List<TagChangeListener> listeners = new Listeners.SynchronizedList<>();
	private boolean listenersPaused = false;
	private List<TagChangedEvent> keptEvents = new ArrayList<>();

	public DefaultLabelEditorTagging(LabelEditorModel model) {
		this.model = model;
	}

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
//			System.out.println(keptEvents);
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
//			System.out.println(e);
			listeners.list.forEach(listener -> listener.tagChanged(Collections.singletonList(e)));
		}
	}

	private synchronized void notifyListeners(Object tag, L label, LabelEditorModel model, TagChangedEvent.Action action) {
		TagChangedEvent e = new TagChangedEvent();
		e.action = action;
		e.tag = tag;
		e.model = model;
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
		notifyListeners(tag, label, model, TagChangedEvent.Action.ADDED);
	}

	@Override
	public void addValueToLabel(Object tag, Object value, L label) {
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
		table.set(col, row, value);
		notifyListeners(tag, label, model, TagChangedEvent.Action.ADDED);
	}

	@Override
	public Object getValue(Object tag, L label) {
		Integer row = labelToRow.get(label);
		if(row == null) return null;
		Integer col = tagToColumn.get(tag);
		if(col == null) return null;
		return table.get(col, row);
	}

	@Override
	public void removeTagFromLabel(Object tag, L label) {
		Integer row = labelToRow.get(label);
		if(row == null) return;
		Integer col = tagToColumn.get(tag);
		if(col == null) return;
		if(table.get(col, row) == null) return;
		table.set(col, row, null);
		notifyListeners(tag, label, model, TagChangedEvent.Action.REMOVED);
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
			if(existed) notifyListeners(tag, rowToLabel.get(i),model,  TagChangedEvent.Action.REMOVED);
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
