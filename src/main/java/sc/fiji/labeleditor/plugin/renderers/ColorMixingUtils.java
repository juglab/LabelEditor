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

import java.util.Collections;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ColorMixingUtils {

	//https://en.wikipedia.org/wiki/Alpha_compositing
	//https://wikimedia.org/api/rest_v1/media/math/render/svg/12ea004023a1756851fc7caa0351416d2ba03bae

	public static Integer mixColorsOverlay(Stream<Integer> colors) {
		return colors.collect(new Collector<Integer, int[], Integer>() {
			@Override
			public Supplier<int[]> supplier() {
				return () -> new int[4];
			}

			@Override
			public BiConsumer<int[], Integer> accumulator() {
				return (a, t) -> {
					if(t == 0) return;
					float newalpha = ((float)ARGBType.alpha(t))/255.f;
					float alpha = (float)(a[3])/255.f;
					if(a[3] < 0.0001 && newalpha < 0.0001) return;
					float new_alpha_inverted = newalpha * (1 - alpha);
					a[0] = (int)((a[0]*alpha+ (float) ARGBType.red(t) * new_alpha_inverted)/(alpha + new_alpha_inverted));
					a[1] = (int)((a[1]*alpha+ (float) ARGBType.green(t) * new_alpha_inverted)/(alpha + new_alpha_inverted));
					a[2] = (int)((a[2]*alpha+ (float) ARGBType.blue(t) * new_alpha_inverted)/(alpha + new_alpha_inverted));
					a[3] = (int)(255 * (alpha + new_alpha_inverted));
				};
			}

			@Override
			public BinaryOperator<int[]> combiner() {
				return (a, b) -> {
					if((b[0] == 0 && b[1] == 0 && b[2] == 0) || b[3] == 0) return a;
					float newalpha = ((float)b[3])/255.f;
					float alpha = (float)(a[3])/255.f;
					if(alpha < 0.0001 && newalpha < 0.0001) return a;
					float new_alpha_inverted = newalpha * (1 - alpha);
					a[0] = (int)((a[0]*alpha+ (float) b[0] * new_alpha_inverted)/(alpha + new_alpha_inverted));
					a[1] = (int)((a[1]*alpha+ (float) b[1] * new_alpha_inverted)/(alpha + new_alpha_inverted));
					a[2] = (int)((a[2]*alpha+ (float) b[2] * new_alpha_inverted)/(alpha + new_alpha_inverted));
					a[3] = (int)(255* alpha + new_alpha_inverted);
					return a;
				};
			}

			@Override
			public Function<int[], Integer> finisher() {
				return (a) -> {
					return ARGBType.rgba(a[0], a[1], a[2], a[3]);
				};
			}

			@Override
			public Set<Characteristics> characteristics() {
				return Collections.emptySet();
			}
		});

//		return ARGBType.rgba((int)red, (int)green, (int)blue, (int)(alpha*255));
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

	public static int mixColorsAdditive(Stream<Integer> colors) {
		float red = 0;
		float green = 0;
		float blue = 0;
		float alpha = 0;
		for (int color : colors.collect(Collectors.toList())) {
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
