package sc.fiji.labeleditor.core.model.colors;

import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.IntType;
import org.junit.Test;
import sc.fiji.labeleditor.core.view.LabelEditorTargetComponent;

import static org.junit.Assert.assertEquals;

public class ValueColorTest {

	@Test
	public void testValueTag() {
		LabelEditorColorset colorset = new DefaultLabelEditorColorset(null);
		LabelEditorValueColor<IntType> color = new LabelEditorValueColor<>(colorset, new IntType(0), new IntType(100));
		colorset.put(LabelEditorTargetComponent.FACE, color);
		color.setMinColor(0,0,255,255);
		color.setMaxColor(255,0,0,255);

		assertEquals(ARGBType.rgba(0, 0, 255, 255), color.getColor(new IntType(0)));
		assertEquals(ARGBType.rgba(255/2, 0, 255/2, 255), color.getColor(new IntType(50)));
		assertEquals(ARGBType.rgba(255, 0, 0, 255), color.getColor(new IntType(100)));
		assertEquals(ARGBType.rgba(255, 0, 0, 255), color.getColor(new IntType(150)));
	}
}
