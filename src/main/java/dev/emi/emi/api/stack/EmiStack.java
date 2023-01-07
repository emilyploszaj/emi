package dev.emi.emi.api.stack;

import java.util.List;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import dev.emi.emi.EmiComparisonDefaults;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * An abstract representation of an object in EMI.
 * Can be an item, a fluid, or something else.
 */
public abstract class EmiStack implements EmiIngredient {
	public static final EmiStack EMPTY = new EmptyEmiStack();
	private EmiStack remainder = EMPTY;
	protected Comparison comparison = Comparison.DEFAULT_COMPARISON;
	protected long amount = 1;

	@Override
	public List<EmiStack> getEmiStacks() {
		return List.of(this);
	}

	public EmiStack getRemainder() {
		return remainder;
	}

	public EmiStack setRemainder(EmiStack stack) {
		if (stack == this) {
			stack = stack.copy();
		}
		remainder = stack;
		return this;
	}

	public EmiStack comparison(Function<Comparison, Comparison> comparison) {
		this.comparison = comparison.apply(this.comparison);
		return this;
	}

	public abstract EmiStack copy();

	public abstract boolean isEmpty();

	public long getAmount() {
		return amount;
	}
	
	public EmiStack setAmount(long amount) {
		this.amount = amount;
		return this;
	}

	public abstract NbtCompound getNbt();

	public boolean hasNbt() {
		return getNbt() != null;
	}

	public abstract Object getKey();

	@SuppressWarnings("unchecked")
	public <T> @Nullable T getKeyOfType(Class<T> clazz) {
		Object o = getKey();
		if (clazz.isAssignableFrom(o.getClass())) {
			return (T) o;
		}
		return null;
	}

	/**
	 * @deprecated Use getKey
	 */
	@Deprecated
	public Entry<?> getEntry() {
		return EmptyEmiStack.ENTRY;
	}

	public abstract Identifier getId();

	/**
	 * @deprecated Use getKeyOfType
	 */
	@Deprecated
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
		boolean amount = comparison.amount.orElseGet(() -> EmiComparisonDefaults.get(getKey()).amount.orElse(false))
			|| stack.comparison.amount.orElseGet(() -> EmiComparisonDefaults.get(stack.getKey()).amount.orElse(false));
		boolean nbt = comparison.nbt.orElseGet(() -> EmiComparisonDefaults.get(getKey()).nbt.orElse(false))
			|| stack.comparison.nbt.orElseGet(() -> EmiComparisonDefaults.get(stack.getKey()).nbt.orElse(false));
		if (!getEntry().equals(stack.getEntry())) {
			return false;
		}
		if (nbt && (hasNbt() != stack.hasNbt() || (hasNbt() && !getNbt().equals(stack.getNbt())))) {
			return false;
		}
		if (amount && getAmount() != stack.getAmount()) {
			return false;
		}
		return true;
	}

	public boolean isEqual(EmiStack stack, Comparison comparison) {
		boolean amount = comparison.amount.orElse(false);
		boolean nbt = comparison.nbt.orElse(false);
		if (!getEntry().equals(stack.getEntry())) {
			return false;
		}
		if (nbt && (hasNbt() != stack.hasNbt() || (hasNbt() && !getNbt().equals(stack.getNbt())))) {
			return false;
		}
		if (amount && getAmount() != stack.getAmount()) {
			return false;
		}
		return true;
	}

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

	@Override
	public String toString() {
		String s = "" + getKey();
		NbtCompound nbt = getNbt();
		if (nbt != null) {
			s += nbt;
		}
		return s + " x" + getAmount();
	}

	public static EmiStack of(ItemStack stack) {
		return new ItemEmiStack(stack);
	}

	public static EmiStack of(ItemStack stack, long amount) {
		return new ItemEmiStack(stack, amount);
	}

	public static EmiStack of(ItemConvertible item) {
		return of(ItemVariant.of(item), 1);
	}

	public static EmiStack of(ItemConvertible item, long amount) {
		return of(ItemVariant.of(item), amount);
	}

	@Deprecated
	public static EmiStack of(ItemVariant item) {
		return new ItemEmiStack(item, 1);
	}

	@Deprecated
	public static EmiStack of(ItemVariant item, long amount) {
		return new ItemEmiStack(item, amount);
	}

	@Deprecated
	public static EmiStack of(FluidVariant fluid) {
		return new FluidEmiStack(fluid);
	}

	@Deprecated
	public static EmiStack of(FluidVariant fluid, long amount) {
		return new FluidEmiStack(fluid, amount);
	}

	public static EmiStack of(Fluid fluid) {
		return new FluidEmiStack(fluid, null);
	}

	public static EmiStack of(Fluid fluid, long amount) {
		return new FluidEmiStack(fluid, null, amount);
	}

	public static EmiStack of(Fluid fluid, NbtCompound nbt) {
		return new FluidEmiStack(fluid, nbt);
	}

	public static EmiStack of(Fluid fluid, NbtCompound nbt, long amount) {
		return new FluidEmiStack(fluid, nbt, amount);
	}

	@Deprecated
	public static abstract class Entry<T> {
		private final T value;

		public Entry(T value) {
			this.value = value;
		}

		public T getValue() {
			return value;
		}

		public abstract Class<? extends T> getType();
	}
}
