package net.imagej.workflow;

public class DefaultImageWorkflowStep implements ImageWorkflowStep {
	private String title;
	private String description;

	public DefaultImageWorkflowStep(String title, String description) {
		this.title = title;
		this.description = description;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public String getDescription() {
		return description;
	}

}
