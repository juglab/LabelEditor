package sc.fiji.labeleditor.core.view;

import org.scijava.listeners.Listeners;

import java.util.List;

public interface LabelEditorView<L> {

	void updateRenderers();

	List< LabelEditorRenderer<L> > renderers();

	Listeners< ViewChangeListener > listeners();

	void addDefaultRenderers();

	void add(LabelEditorRenderer<L> renderer);

	void setActive(LabelEditorRenderer<?> renderer, boolean active);
}
