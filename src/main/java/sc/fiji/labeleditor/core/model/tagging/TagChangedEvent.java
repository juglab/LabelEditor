package sc.fiji.labeleditor.core.model.tagging;

public class TagChangedEvent<L> {
	public Object tag;
	public L label;
	public Action action;

	public enum Action {
		REMOVED, ADDED;
	}

	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		switch(action) {
			case REMOVED:
				stringBuilder.append("Removed"); break;
			case ADDED:
				stringBuilder.append("Added"); break;
		}
		stringBuilder.append(" tag ");
		stringBuilder.append(tag);
		switch(action) {
			case REMOVED:
				stringBuilder.append(" from label "); break;
			case ADDED:
				stringBuilder.append(" to label "); break;
		}
		stringBuilder.append(label);
		return stringBuilder.toString();
	}
}
