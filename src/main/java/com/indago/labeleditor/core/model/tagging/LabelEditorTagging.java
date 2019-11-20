package com.indago.labeleditor.core.model.tagging;

import org.scijava.listeners.Listeners;

import java.util.Map;
import java.util.Set;

//TODO is this interface needed?
public interface LabelEditorTagging<L> {

	Map<L, Set<Object>> get();

	void addTag(Object tag, L label);

	void removeTag(Object tag, L label);

	Set<Object> getTags(L label);

	void removeTag(Object tag);

	Set<L> getLabels(Object tag);

	Listeners< TagChangeListener > listeners();

	void pauseListeners();

	void resumeListeners();

	Set<Object> getAllTags();

	Set<L> filterLabelsWithTag(Set<L> labels, Object tag);

	Set filterLabelsWithAnyTag(Set<L> labels, Set<Object> tags);
}
