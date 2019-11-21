package net.imagej.workflow;

import org.scijava.module.Module;
import org.scijava.module.ModuleException;

import java.util.List;

public interface ImageWorkflow {

	void initWorkflow(String title, List<ImageWorkflowStep> workflowSteps);

	void startPreviewRun();

	void donePreviewRun();

	boolean waitForOK() throws InterruptedException;

	void startMainRun();

	void doneMainRun();

	void addCommand(ImageWorkflowStep step, Module module) throws ModuleException, InterruptedException;

	void setTestStep(ImageWorkflowStep step);
	void setMainStep(ImageWorkflowStep step);
}