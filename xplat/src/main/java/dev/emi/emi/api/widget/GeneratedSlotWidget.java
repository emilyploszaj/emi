package dev.emi.emi.api.widget;

import java.util.Random;
import java.util.function.Function;

import dev.emi.emi.api.render.EmiRender;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.client.util.math.MatrixStack;

public class GeneratedSlotWidget extends SlotWidget {
	private static final int INCREMENT = 1000;
	private final Function<Random, EmiIngredient> stackSupplier;
	private final int unique;
	private long lastGenerate = 0;
	private EmiIngredient stack = null;

	public GeneratedSlotWidget(Function<Random, EmiIngredient> stackSupplier, int unique, int x, int y) {
		super(EmiStack.EMPTY, x, y);
		this.stackSupplier = stackSupplier;
		this.unique = unique;
	}
	
	@Override
	public void drawOverlay(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		if (!getStack().isEmpty()) {
			int off = 1;
			if (output) {
				off = 5;
			}
			EmiRender.renderIngredientIcon(getStack(), matrices, x + off, y + off);
		}
		super.drawOverlay(matrices, mouseX, mouseY, delta);
	}
	
	@Override
	public EmiIngredient getStack() {
		long time = System.currentTimeMillis() / INCREMENT;
		if (stack == null || time > lastGenerate) {
			lastGenerate = time;
			stack = stackSupplier.apply(getRandom(time));
		}
		return stack;
	}

	private Random getRandom(long time) {
		return new Random(new Random(time ^ unique).nextInt());
	}
}
