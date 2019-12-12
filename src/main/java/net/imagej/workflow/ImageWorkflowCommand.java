package net.imagej.workflow;

import net.imglib2.RandomAccessibleInterval;
import org.scijava.Cancelable;
import org.scijava.command.Command;

import java.util.List;

public interface ImageWorkflowCommand extends Command, Cancelable {

	/**
	 * @return the title of the workflow command
	 */
	String getTitle();

	/**
	 * Defines on which data the workflow will be executed
	 */
	RandomAccessibleInterval getInput();


	/**
	 * @return the steps of this workflow
	 */
	List<ImageWorkflowStep> getSteps();

	/**
	 * Defines the workflow for an image.
	 */
	void run(RandomAccessibleInterval img) throws Exception;
}
