/*-
 * #%L
 * UI component for image segmentation label comparison and selection
 * %%
 * Copyright (C) 2019 - 2020 DAIS developers
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package sc.fiji.labeleditor.plugin.renderers;

import net.imglib2.type.numeric.ARGBType;

public class ColorMixingUtils {

	//https://en.wikipedia.org/wiki/Alpha_compositing
	//https://wikimedia.org/api/rest_v1/media/math/render/svg/12ea004023a1756851fc7caa0351416d2ba03bae
	public static int mixColorsOverlay(int[] colors) {
		float red = 0;
		float green = 0;
		float blue = 0;
		float alpha = 0;
		for (int color : colors) {
			if(color == 0) continue;
			float newred = ARGBType.red(color);
			float newgreen = ARGBType.green(color);
			float newblue = ARGBType.blue(color);
			float newalpha = ((float)ARGBType.alpha(color))/255.f;
			if(alpha < 0.0001 && newalpha < 0.0001) continue;
			red = (red*alpha+newred*newalpha*(1-alpha))/(alpha + newalpha*(1-alpha));
			green = (green*alpha+newgreen*newalpha*(1-alpha))/(alpha + newalpha*(1-alpha));
			blue = (blue*alpha+newblue*newalpha*(1-alpha))/(alpha + newalpha*(1-alpha));
			alpha = alpha + newalpha*(1-alpha);
		}
		return ARGBType.rgba((int)red, (int)green, (int)blue, (int)(alpha*255));
	}

	public static int mixColorsAdditive(int[] colors) {
		float red = 0;
		float green = 0;
		float blue = 0;
		float alpha = 0;
		for (int color : colors) {
			if(color == 0) continue;
			float newred = ARGBType.red(color);
			float newgreen = ARGBType.green(color);
			float newblue = ARGBType.blue(color);
			float newalpha = ((float)ARGBType.alpha(color))/255.f;
			if(alpha < 0.0001 && newalpha < 0.0001) continue;
			red = Math.min(255, red + newred*newalpha);
			green = Math.min(255, green + newgreen*newalpha);
			blue = Math.min(255, blue + newblue*newalpha);
			alpha = alpha + newalpha*(1-alpha);
		}
		return ARGBType.rgba((int)red, (int)green, (int)blue, (int)(alpha*255));
	}
}
