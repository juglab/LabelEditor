package sc.fiji.labeleditor.plugin.renderers;

import net.imglib2.RandomAccessibleInterval;
import org.scijava.plugin.Plugin;
import sc.fiji.labeleditor.core.model.LabelEditorModel;
import sc.fiji.labeleditor.core.view.LabelEditorRenderer;

@Plugin(type = LabelEditorRenderer.class, name = "data", priority = 0)
public class DataLabelEditorRenderer<L> implements LabelEditorRenderer<L> {

	private RandomAccessibleInterval output;

	@Override
	public void init(LabelEditorModel<L> model) {
		if(model.getData() != null)
		this.output = model.getData();
	}

	@Override
	public synchronized void updateOnTagChange(LabelEditorModel<L> model) {
	}

	@Override
	public void updateOnLabelingChange() {}

	@Override
	public RandomAccessibleInterval getOutput() {
		return output;
	}

	@Override
	public <M extends LabelEditorModel> boolean canDisplay(M model) {
		return model.getData() != null;
	}

}
