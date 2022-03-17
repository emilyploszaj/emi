package dev.emi.emi.api.stack;

import java.util.List;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import dev.emi.emi.api.stack.comparison.ReferenceComparison;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

/**
 * An abstract representation of an object in EMI.
 * Can be an item, a fluid, or something else.
 */
public abstract class EmiStack implements EmiIngredient {
	public static final EmiStack EMPTY = new EmptyEmiStack();
	private EmiStack remainder = EMPTY;
	protected Comparison comparison = ReferenceComparison.INSTANCE;
	protected int amount = 1;

	@Override
	public List<EmiStack> getEmiStacks() {
		return List.of(this);
	}

	public EmiStack getRemainder() {
		return remainder;
	}

	public EmiIngredient setRemainder(EmiStack stack) {
		remainder = stack;
		return this;
	}

	public EmiStack comparison(Function<Comparison, Comparison> comparison) {
		this.comparison = comparison.apply(this.comparison);
		return this;
	}

	public abstract EmiStack copy();

	public abstract boolean isEmpty();

	public int getAmount() {
		return amount;
	}

	public abstract Object getKey();

	public abstract Entry<?> getEntry();

	@SuppressWarnings("unchecked")
	public <T> @Nullable Entry<T> getEntryOfType(Class<T> clazz) {
		Entry<?> entry = getEntry();
		if (entry.getType() == clazz) {
			return (Entry<T>) entry;
		}
		return null;
	}

	public ItemStack getItemStack() {
		return ItemStack.EMPTY;
	}

	public boolean isEqual(EmiStack stack) {
		return comparison.areEqual(this, stack) && stack.comparison.areEqual(stack, this);
	}

	@Override
	public void render(MatrixStack matrices, int x, int y, float delta) {
		renderIcon(matrices, x, y, delta);
	}

	public abstract void renderIcon(MatrixStack matrices, int x, int y, float delta);

	public abstract void renderOverlay(MatrixStack matrices, int x, int y, float delta);

	public abstract List<Text> getTooltipText();

	public abstract List<TooltipComponent> getTooltip();

	public abstract Text getName();

	@Override
	public boolean equals(Object obj) {
		return obj instanceof EmiStack stack && this.isEqual(stack);
	}

	@Override
	public int hashCode() {
		return getKey().hashCode();
	}

	public static EmiStack of(ItemStack stack) {
		return new ItemEmiStack(stack);
	}

	public static EmiStack of(Item item) {
		return of(new ItemStack(item));
	}

	public static abstract class Entry<T> {
		private final T value;

		public Entry(T value) {
			this.value = value;
		}

		public T getValue() {
			return value;
		}

		abstract Class<T> getType();
	}

	public static interface Comparison {

		boolean areEqual(EmiStack a, EmiStack b);

		Comparison copy();

		Comparison nbt(boolean compare);

		Comparison amount(boolean compare);
	}
}
