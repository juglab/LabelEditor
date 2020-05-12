package sc.fiji.labeleditor.core.model.tagging;

import net.imglib2.type.numeric.integer.IntType;
import org.scijava.listeners.Listeners;

import java.util.Map;
import java.util.Set;

public interface LabelEditorTagging<L> {

	void addTagToLabel(Object tag, L label);

	void removeTagFromLabel(Object tag, L label);

	Set<Object> getTags(L label);

	void removeTagFromLabel(Object tag);

	Set<L> getLabels(Object tag);

	Listeners< TagChangeListener > listeners();

	void pauseListeners();

	void resumeListeners();

	Set<Object> getAllTags();

	Set<L> filterLabelsWithTag(Set<L> labels, Object tag);

	Set filterLabelsWithAnyTag(Set<L> labels, Set<Object> tags);

	void toggleTag(Object tag, L label);

	void addValueToLabel(Object tag, Object value, L label);

	Object getValue(Object tag, L label);
}
