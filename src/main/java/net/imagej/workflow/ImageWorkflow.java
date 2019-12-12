package net.imagej.workflow;

import org.scijava.module.Module;
import org.scijava.module.ModuleException;

import java.util.List;

public interface ImageWorkflow {

	/**
	 * Initializes a new workflow.
	 * @param title the title of the workflow
	 * @param workflowSteps the steps of the workflow
	 */
	void initWorkflow(String title, List<ImageWorkflowStep> workflowSteps);

	/**
	 * Signalize that we are now running the workflow in preview mode
	 */
	void startPreviewRun();

	/**
	 * Signalize that we are done running the workflow in preview mode
	 */
	void donePreviewRun();

	/**
	 * Wait for the user to approve the current workflow step
	 */
	boolean waitForOK() throws InterruptedException;

	/**
	 * Signalize that we are now running the workflow on the full data
	 */
	void startMainRun();

	/**
	 * Signalize that we are done running the workflow on the full data
	 */
	void doneMainRun();

	/**
	 * Add a module to a specific workflow step
	 */
	void addCommand(ImageWorkflowStep step, Module module) throws ModuleException, InterruptedException;

	/**
	 * Signalize that we are now at a specific step in preview mode
	 */
	void setPreviewStep(ImageWorkflowStep step);

	/**
	 * Signalize that we are now at a specific step in main mode
	 */
	void setMainStep(ImageWorkflowStep step);
}