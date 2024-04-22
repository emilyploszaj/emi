package dev.emi.emi.api.stack;

import java.util.List;
import java.util.function.Function;

import net.minecraft.component.ComponentChanges;
import net.minecraft.component.ComponentHolder;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.ComponentMapImpl;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;

import dev.emi.emi.registry.EmiComparisonDefaults;
import dev.emi.emi.screen.tooltip.RemainderTooltipComponent;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * An abstract representation of a resource in EMI.
 * Can be an item, a fluid, or something else.
 */
public abstract class EmiStack implements EmiIngredient, ComponentHolder {
	public static final EmiStack EMPTY = new EmptyEmiStack();
	private EmiStack remainder = EMPTY;
	protected Comparison comparison = Comparison.DEFAULT_COMPARISON;
	protected long amount = 1;
	protected float chance = 1;

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

	public EmiStack comparison(Comparison comparison) {
		this.comparison = comparison;
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

	public float getChance() {
		return chance;
	}
	
	public EmiStack setChance(float chance) {
		this.chance = chance;
		return this;
	}

	public abstract ComponentMap getComponents();

	public abstract ComponentChanges getComponentChanges();

	public abstract Object getKey();

	@SuppressWarnings("unchecked")
	public <T> @Nullable T getKeyOfType(Class<T> clazz) {
		Object o = getKey();
		if (clazz.isAssignableFrom(o.getClass())) {
			return (T) o;
		}
		return null;
	}

	public abstract Identifier getId();

	public ItemStack getItemStack() {
		return ItemStack.EMPTY;
	}

	public boolean isEqual(EmiStack stack) {
		if (!getKey().equals(stack.getKey())) {
			return false;
		}
		Comparison a = comparison == Comparison.DEFAULT_COMPARISON ? EmiComparisonDefaults.get(getKey()) : comparison;
		Comparison b = stack.comparison == Comparison.DEFAULT_COMPARISON ? EmiComparisonDefaults.get(stack.getKey()) : stack.comparison;
		if (a == b) {
			return a.compare(this, stack);
		} else {
			return a.compare(this, stack) && b.compare(this, stack);
		}
	}

	public boolean isEqual(EmiStack stack, Comparison comparison) {
		return getKey().equals(stack.getKey()) && comparison.compare(this, stack);
	}

	public abstract List<Text> getTooltipText();

	public List<TooltipComponent> getTooltip() {
		List<TooltipComponent> list = Lists.newArrayList();
		if (!getRemainder().isEmpty()) {
			list.add(new RemainderTooltipComponent(this));
		}
		return list;
	}

	public abstract Text getName();

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof EmiStack stack) {
			return this.isEqual(stack);
		} else if (obj instanceof EmiIngredient stack) {
			return EmiIngredient.areEqual(this, stack);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return getKey().hashCode();
	}

	@Override
	public String toString() {
		String s = "" + getKey();
		ComponentMap componentMap = getComponents();
		if (componentMap != ComponentMap.EMPTY) {
			s += componentMap;
		}
		return s + " x" + getAmount();
	}

	public static EmiStack of(ItemStack stack) {
		if (stack.isEmpty()) {
			return EmiStack.EMPTY;
		}
		return new ItemEmiStack(stack);
	}

	public static EmiStack of(ItemStack stack, long amount) {
		if (stack.isEmpty()) {
			return EmiStack.EMPTY;
		}
		return new ItemEmiStack(stack, amount);
	}

	public static EmiStack of(ItemConvertible item) {
		return of(item.asItem().getDefaultStack(), 1);
	}

	public static EmiStack of(ItemConvertible item, long amount) {
		return of(item.asItem().getDefaultStack(), amount);
	}

	public static EmiStack of(ItemConvertible item, ComponentChanges componentChanges) {
		return of(item, componentChanges, 1);
	}

	public static EmiStack of(ItemConvertible item, ComponentChanges componentChanges, long amount) {
		return new ItemEmiStack(item.asItem(), componentChanges, amount);
	}

	public static EmiStack of(Fluid fluid) {
		return of(fluid, ComponentChanges.EMPTY);
	}

	public static EmiStack of(Fluid fluid, long amount) {
		return of(fluid, ComponentChanges.EMPTY, amount);
	}

	public static EmiStack of(Fluid fluid, ComponentChanges componentChanges) {
		return of(fluid, componentChanges, 0);
	}

	public static EmiStack of(Fluid fluid, ComponentChanges componentChanges, long amount) {
		if (fluid instanceof FlowableFluid ff && ff.getStill() != Fluids.EMPTY) {
			fluid = ff.getStill();
		}
		if (fluid == Fluids.EMPTY) {
			return EmiStack.EMPTY;
		}
		return new FluidEmiStack(fluid, componentChanges, amount);
	}

	static abstract class Entry<T> {
	}
}
