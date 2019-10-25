package com.indago.labeleditor.display;

import com.indago.labeleditor.LabelEditorPanel;
import net.imglib2.type.numeric.integer.IntType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class LUTBuilderTest {

	@Test
	public void overrideLUTBuilder() {
		LabelEditorPanel panel = new LabelEditorPanel<IntType, String>(null) {
			@Override
			protected LUTBuilder<String> initLUTBuilder() {
				return (model) -> new int[]{0,1};
			}
		};
		LUTBuilder builder = panel.getLUTBuilder();
		assertNotNull(builder);
		int[] lut = builder.build(null);
		assertNotNull(lut);
		assertEquals(2, lut.length);
		assertEquals(0, lut[0]);
		assertEquals(1, lut[1]);
	}

}
