package com.indago.labeleditor.core.view;

import org.junit.Test;
import org.scijava.Context;
import org.scijava.plugin.PluginInfo;

import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class LabelEditorRenderersTest {

	@Test
	public void discoverRenderers() {
		List<PluginInfo<?>> renderer = new Context().getPluginIndex().get(LabelEditorRenderer.class);
		System.out.println(renderer);
		assertNotNull(renderer);
		assertTrue(renderer.size() > 1);
	}

}
