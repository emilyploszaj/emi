package dev.emi.emi.api.widget;

import dev.emi.emi.runtime.EmiDrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class AnimatedTextureWidget extends TextureWidget {
	protected final int time;
	protected final boolean horizontal, endToStart, fullToEmpty;

	public AnimatedTextureWidget(Identifier texture, int x, int y, int width, int height, int u, int v,
			int regionWidth, int regionHeight, int textureWidth, int textureHeight, int time,
			boolean horizontal, boolean endToStart, boolean fullToEmpty) {
		super(texture, x, y, width, height, u, v, regionWidth, regionHeight, textureWidth, textureHeight);
		this.time = time;
		this.horizontal = horizontal;
		this.endToStart = endToStart;
		this.fullToEmpty = fullToEmpty;
	}
	public AnimatedTextureWidget(Identifier texture, int x, int y, int width, int height, int u, int v, int time,
			boolean horizontal, boolean endToStart, boolean fullToEmpty) {
		this(texture, x, y, width, height, u, v, width, height, 256, 256, time, horizontal, endToStart, fullToEmpty);
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		EmiDrawContext context = EmiDrawContext.wrap(matrices);
		int subTime = (int) (System.currentTimeMillis() % time);
		if (endToStart ^ fullToEmpty) {
			subTime = time - subTime;
		}
		int mx = x, my = y;
		int mw = width, mh = height;
		int mu = u, mv = v;
		int mrw = regionWidth, mrh = regionHeight;
		if (horizontal) {
			if (endToStart) {
				mx = x + width * subTime / time;
				mu = u + regionWidth * subTime / time;
				mw = width - (mx - x);
				mrw = regionWidth - (mu - u);
			} else {
				mw = width * subTime / time;
				mrw = regionWidth * subTime / time;
			}
		} else {
			if (endToStart) {
				my = y + height * subTime / time;
				mv = v + regionHeight * subTime / time;
				mh = height - (my - y);
				mrh = regionHeight - (mv - v);
			} else {
				mh = height * subTime / time;
				mrh = regionHeight * subTime / time;
			}
		}
		context.drawTexture(this.texture, mx, my, mw, mh, mu, mv, mrw, mrh, textureWidth, textureHeight);
	}	
}
