package com.indago.labeleditor.display;

import bdv.util.VirtualChannels;

public class LUTChannel implements VirtualChannels.VirtualChannel {
	private final int argb;

	public LUTChannel(int argb) {
		this.argb = argb;
	}

	@Override
	public void updateVisibility() {
		update();
	}

	@Override
	public void updateSetupParameters() {
		update();
	}

	private void update() {

	}

	public int getColor() {
		return argb;
	}
}
