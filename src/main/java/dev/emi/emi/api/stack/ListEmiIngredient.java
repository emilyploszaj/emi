package dev.emi.emi.api.stack;

import java.util.List;

import org.apache.commons.compress.utils.Lists;

import dev.emi.emi.EmiPort;
import dev.emi.emi.api.render.EmiRender;
import dev.emi.emi.screen.tooltip.IngredientTooltipComponent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.util.math.MatrixStack;

public class ListEmiIngredient implements EmiIngredient {
	private final List<? extends EmiIngredient> ingredients;
	private final List<EmiStack> fullList;
	private final long amount;

	public ListEmiIngredient(List<? extends EmiIngredient> ingredients, long amount) {
		this.ingredients = ingredients;
		this.fullList = ingredients.stream().flatMap(i -> i.getEmiStacks().stream()).toList();
		if (fullList.isEmpty()) {
			throw new IllegalArgumentException("ListEmiIngredient cannot be empty");
		}
		this.amount = amount;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ListEmiIngredient other) {
			return other.getEmiStacks().equals(this.getEmiStacks());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return fullList.hashCode();
	}

	@Override
	public List<EmiStack> getEmiStacks() {
		return fullList;
	}

	@Override
	public long getAmount() {
		return amount;
	}

	@Override
	public void render(MatrixStack matrices, int x, int y, float delta, int flags) {
		if ((flags & RENDER_ICON) != 0) {
			int item = (int) (System.currentTimeMillis() / 1000 % ingredients.size());
			ingredients.get(item).render(matrices, x, y, delta, -1 ^ RENDER_AMOUNT);
		}
		if ((flags & RENDER_AMOUNT) != 0) {
			String count = "";
			if (amount != 1) {
				count += amount;
			}
			MinecraftClient client = MinecraftClient.getInstance();
			client.getItemRenderer().renderGuiItemOverlay(client.textRenderer, fullList.get(0).getItemStack(), x, y, count);
		}
		if ((flags & RENDER_INGREDIENT) != 0) {
			EmiRender.renderIngredientIcon(this, matrices, x, y);
		}
	}

	@Override
	public List<TooltipComponent> getTooltip() {
		List<TooltipComponent> tooltip = Lists.newArrayList();
		tooltip.add(TooltipComponent.of(EmiPort.ordered(EmiPort.translatable("tooltip.emi.accepts"))));
		tooltip.add(new IngredientTooltipComponent(ingredients));
		int item = (int) (System.currentTimeMillis() / 1000 % ingredients.size());
		tooltip.addAll(ingredients.get(item).getTooltip());
		return tooltip;
	}
}