package dev.emi.emi.api.stack;

import java.util.List;

import org.jetbrains.annotations.ApiStatus;

import com.google.common.collect.Lists;

import dev.emi.emi.EmiPort;
import dev.emi.emi.api.render.EmiRender;
import dev.emi.emi.screen.tooltip.IngredientTooltipComponent;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;

@ApiStatus.Internal
public class ListEmiIngredient implements EmiIngredient {
	private final List<? extends EmiIngredient> ingredients;
	private final List<EmiStack> fullList;
	private long amount;
	private float chance = 1;

	@ApiStatus.Internal
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
	public EmiIngredient copy() {
		EmiIngredient stack = new ListEmiIngredient(ingredients, amount);
		stack.setChance(chance);
		return stack;
	}

	@Override
	public String toString() {
		return "Ingredient" + getEmiStacks();
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
	public EmiIngredient setAmount(long amount) {
		this.amount = amount;
		return this;
	}

	@Override
	public float getChance() {
		return chance;
	}

	@Override
	public EmiIngredient setChance(float chance) {
		this.chance = chance;
		return this;
	}

	@Override
	public void render(DrawContext draw, int x, int y, float delta, int flags) {
		int item = (int) (System.currentTimeMillis() / 1000 % ingredients.size());
		EmiIngredient current = ingredients.get(item);
		if ((flags & RENDER_ICON) != 0) {
			current.render(draw, x, y, delta, -1 ^ RENDER_AMOUNT);
		}
		if ((flags & RENDER_AMOUNT) != 0) {
			current.copy().setAmount(amount).render(draw, x, y, delta, RENDER_AMOUNT);
		}
		if ((flags & RENDER_INGREDIENT) != 0) {
			EmiRender.renderIngredientIcon(this, draw, x, y);
		}
	}

	@Override
	public List<TooltipComponent> getTooltip() {
		List<TooltipComponent> tooltip = Lists.newArrayList();
		tooltip.add(TooltipComponent.of(EmiPort.ordered(EmiPort.translatable("tooltip.emi.accepts"))));
		tooltip.add(new IngredientTooltipComponent(ingredients));
		int item = (int) (System.currentTimeMillis() / 1000 % ingredients.size());
		tooltip.addAll(ingredients.get(item).copy().setAmount(amount).getTooltip());
		return tooltip;
	}
}