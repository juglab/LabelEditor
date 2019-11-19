package com.indago.labeleditor.application.workflow;

import org.scijava.module.Module;
import org.scijava.service.SciJavaService;

import java.util.concurrent.Future;

public interface WorkflowModuleService extends SciJavaService {
	/**
	 * Runs the module and postprocessing.
	 */
	<M extends Module> Future<M> runWithPostprocessing(M module);

	/**
	 * Runs the module without any pre- or postprocessing.
	 */
	<M extends Module> Future<M> runSilently(M module);

	/**
	 * Runs preprocessing for module, but skips all {@link org.scijava.widget.InputHarvester} preprocessors.
	 */
	<M extends Module> void preprocessWithoutHarvesting(M module);
}
