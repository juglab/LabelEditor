package sc.fiji.labeleditor.plugin.renderers;

import sc.fiji.labeleditor.core.model.LabelEditorModel;
import sc.fiji.labeleditor.core.view.LabelEditorRenderer;
import sc.fiji.labeleditor.core.view.LabelEditorTargetComponent;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converter;
import net.imglib2.converter.Converters;
import net.imglib2.roi.boundary.IntTypeBoundary;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.IntType;
import org.scijava.plugin.Plugin;

@Plugin(type = LabelEditorRenderer.class, name = "borders", priority = 2)
public class BorderLabelEditorRenderer<L> extends DefaultLabelEditorRenderer<L> {

	private RandomAccessibleInterval output;

	@Override
	public void init(LabelEditorModel<L> model) {
		super.init(model);
		this.output = new IntTypeBoundary(model.labeling().getIndexImg(), -1);
	}

	@Override
	public synchronized void updateOnTagChange(LabelEditorModel<L> model) {
		updateLUT(model.labeling().getMapping(), model.colors(), LabelEditorTargetComponent.BORDER);
	}

	@Override
	public RandomAccessibleInterval<ARGBType> getOutput() {
		Converter<IntType, ARGBType> converter = (i, o) -> o.set(getLUT()[i.get()]);
		return Converters.convert(output, converter, new ARGBType() );
	}

}
