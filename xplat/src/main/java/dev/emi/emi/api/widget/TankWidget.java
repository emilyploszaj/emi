package dev.emi.emi.api.widget;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.FluidEmiStack;
import dev.emi.emi.platform.EmiAgnos;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.fluid.Fluid;
import net.minecraft.util.Identifier;

public class TankWidget extends SlotWidget {
	private final long capacity;

	public TankWidget(EmiIngredient stack, int x, int y, int width, int height, long capacity) {
		super(stack, x, y);
		this.bounds = new Bounds(x, y, width, height);
		this.capacity = capacity;
	}

	@Override
	public Bounds getBounds() {
		return bounds;
	}

	/**
	 * Sets the slot to use a custom texture.
	 * The size of the texture drawn is based on the size of the tank.
	 */
	public SlotWidget backgroundTexture(Identifier id, int u, int v) {
		return super.backgroundTexture(textureId, u, v);
	}

	@Override
	public void drawStack(DrawContext draw, int mouseX, int mouseY, float delta) {
		EmiIngredient ingredient = getStack();
		for (EmiStack stack : ingredient.getEmiStacks()) {
			if (stack.getKey() instanceof Fluid fluid) {
				FluidEmiStack fes = new FluidEmiStack(fluid, stack.getComponentChanges(), ingredient.getAmount());
				boolean floaty = EmiAgnos.isFloatyFluid(fes);
				Bounds bounds = getBounds();
				int x = bounds.x() + 1;
				int y = bounds.y() + 1;
				int w = bounds.width() - 2;
				int h = bounds.height() - 2;
				int filledHeight = Math.max(1, (int) Math.min(h, (fes.getAmount() * h / capacity)));
				int sy = floaty ? y : y + h;
				for (int oy = 0; oy < filledHeight; oy += 16) {
					int rh = Math.min(16, filledHeight - oy);
					for (int ox = 0; ox < w; ox += 16) {
						int rw = Math.min(16, w - ox);
						if (floaty) {
							EmiAgnos.renderFluid(fes, draw.getMatrices(), x + ox, sy + oy, delta, 0, 0, rw, rh);
						} else {
							EmiAgnos.renderFluid(fes, draw.getMatrices(), x + ox, sy + (oy + rh) * -1, delta, 0, 16 - rh, rw, rh);
						}
					}
				}
				return;
			}
		}
	}
}
