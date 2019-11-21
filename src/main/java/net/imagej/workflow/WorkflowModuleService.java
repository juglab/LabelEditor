package net.imagej.workflow;

import org.scijava.module.Module;
import org.scijava.service.SciJavaService;

public interface WorkflowModuleService extends SciJavaService {

	/**
	 * Runs preprocessing for module, but skips all {@link org.scijava.widget.InputHarvester} preprocessors.
	 */
	<M extends Module> void preprocessWithoutHarvesting(M module);
}
