package dev.emi.emi.api.stack;

import java.util.List;
import java.util.function.Function;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;

import dev.emi.emi.EmiPort;
import dev.emi.emi.registry.EmiComparisonDefaults;
import dev.emi.emi.screen.tooltip.RemainderTooltipComponent;

/**
 * An abstract representation of a resource in EMI.
 * Can be an item, a fluid, or something else.
 */
public abstract class EmiStack implements EmiIngredient {
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

	public abstract DataComponentPatch getComponentChanges();

	public <T> @Nullable T get(DataComponentType<? extends T> type) {
		return getComponentChanges().get(DataComponentMap.EMPTY, type);
	}

	public <T> T getOrDefault(DataComponentType<? extends T> type, T fallback) {
		var componentValue = this.get(type);
		return componentValue != null ? componentValue : fallback;
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

	public abstract List<Component> getTooltipText();

	public List<ClientTooltipComponent> getTooltip() {
		List<ClientTooltipComponent> list = Lists.newArrayList();
		if (!getRemainder().isEmpty()) {
			list.add(new RemainderTooltipComponent(this));
		}
		return list;
	}

	public abstract Component getName();

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
		DataComponentPatch changes = getComponentChanges();
		if (changes != DataComponentPatch.EMPTY) {
			s += changes;
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

	public static EmiStack of(ItemLike item) {
		return of(item.asItem().getDefaultInstance(), 1);
	}

	public static EmiStack of(ItemLike item, long amount) {
		return of(item.asItem().getDefaultInstance(), amount);
	}

	public static EmiStack of(ItemLike item, DataComponentPatch componentChanges) {
		return of(item, componentChanges, 1);
	}

	public static EmiStack of(ItemLike item, DataComponentPatch componentChanges, long amount) {
		return new ItemEmiStack(item.asItem(), componentChanges, amount);
	}

	public static EmiStack of(Fluid fluid) {
		return of(fluid, EmiPort.emptyExtraData());
	}

	public static EmiStack of(Fluid fluid, long amount) {
		return of(fluid, EmiPort.emptyExtraData(), amount);
	}

	public static EmiStack of(Fluid fluid, DataComponentPatch componentChanges) {
		return of(fluid, componentChanges, 0);
	}

	public static EmiStack of(Fluid fluid, DataComponentPatch componentChanges, long amount) {
		if (fluid instanceof FlowingFluid ff && ff.getSource() != Fluids.EMPTY) {
			fluid = ff.getSource();
		}
		if (fluid == Fluids.EMPTY) {
			return EmiStack.EMPTY;
		}
		return new FluidEmiStack(fluid, componentChanges, amount);
	}

	static abstract class Entry<T> {
	}
}
