package sc.fiji.labeleditor.application;

import java.util.Iterator;

public class LabelGenerator implements Iterator<String> {

	private final String name;
	private Integer i = 0;

	public LabelGenerator(String name) {
		this.name = name;
	}

	@Override
	public boolean hasNext() {
		return i < Integer.MAX_VALUE;
	}

	@Override
	public String next() {
		return name + i++;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
}
