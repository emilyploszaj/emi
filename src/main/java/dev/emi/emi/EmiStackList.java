package dev.emi.emi;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;

import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.FluidEmiStack;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.registry.Registry;

public class EmiStackList {
	public static List<Predicate<EmiStack>> invalidators = Lists.newArrayList();
	public static List<EmiStack> stacks = Lists.newArrayList();

	public static void reload() {
		invalidators.clear();
		List<EmiStack> stacks = Lists.newArrayList();
		for (int i = 0; i < Registry.ITEM.size(); i++) {
			Item item = Registry.ITEM.get(i);
			if (item == Items.AIR) {
				continue;
			}
			DefaultedList<ItemStack> itemStacks = DefaultedList.of();
			item.appendStacks(ItemGroup.SEARCH, itemStacks);
			stacks.addAll(itemStacks.stream().map(EmiStack::of).collect(Collectors.toList()));
			if (itemStacks.isEmpty()) {
				stacks.add(EmiStack.of(item));
			}
		}
		for (int i = 0; i < Registry.FLUID.size(); i++) {
			Fluid fluid = Registry.FLUID.get(i);
			if (fluid.isStill(fluid.getDefaultState())) {
				stacks.add(new FluidEmiStack(FluidVariant.of(fluid)));
			}
		}
		
		EmiStackList.stacks = stacks;
	}

	public static void bake() {
		stacks = stacks.stream().filter(s -> {
			for (Predicate<EmiStack> invalidator : invalidators) {
				if (invalidator.test(s)) {
					return false;
				}
			}
			return true;
		}).toList();;
	}
}
