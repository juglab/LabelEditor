package net.imagej.workflow;

import org.scijava.Context;
import org.scijava.module.DefaultModuleService;
import org.scijava.module.Module;
import org.scijava.module.ModuleRunner;
import org.scijava.module.process.PreprocessorPlugin;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.PluginService;
import org.scijava.service.Service;
import org.scijava.widget.InputHarvester;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

@Plugin(type = Service.class)
public class DefaultWorkflowModuleService extends DefaultModuleService implements WorkflowModuleService {

	@Parameter
	PluginService pluginService;

	@Parameter
	Context context;

	@Override
	public <M extends Module> void preprocessWithoutHarvesting(M module) {
		ModuleRunner moduleRunner = new ModuleRunner(context, module, pre(true), Collections.emptyList());
		moduleRunner.preProcess();
	}

	private List<? extends PreprocessorPlugin> pre(final boolean process) {
		if (!process) return null;
		//remove input harvesters from preprocessing
		List<PreprocessorPlugin> preprocessors = pluginService.createInstancesOfType(PreprocessorPlugin.class);
		for (Iterator<PreprocessorPlugin> iterator = preprocessors.iterator(); iterator.hasNext(); ) {
			PreprocessorPlugin preprocessor = iterator.next();
			if (preprocessor instanceof InputHarvester) {
				iterator.remove();
			}
		}
		return preprocessors;
	}
}