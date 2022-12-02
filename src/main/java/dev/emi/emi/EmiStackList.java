package dev.emi.emi;

import java.util.List;
import java.util.function.Predicate;

import com.google.common.collect.Lists;

import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.FluidEmiStack;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemGroups;

public class EmiStackList {
	public static List<Predicate<EmiStack>> invalidators = Lists.newArrayList();
	public static List<EmiStack> stacks = Lists.newArrayList();
	public static Object2IntMap<EmiStack> indices = new Object2IntOpenHashMap<>();

	public static void clear() {
		invalidators.clear();
		stacks = Lists.newArrayList();
		indices.clear();
	}

	public static void reload() {
		List<EmiStack> stacks = Lists.newArrayList();
		MinecraftClient client = MinecraftClient.getInstance();
		ItemGroups.updateDisplayParameters(client.player.networkHandler.getEnabledFeatures(), false);
		stacks.addAll(ItemGroups.getSearchGroup().getDisplayStacks().stream().map(EmiStack::of).toList());
		for (int i = 0; i < EmiPort.getFluidRegistry().size(); i++) {
			Fluid fluid = EmiPort.getFluidRegistry().get(i);
			if (fluid.isStill(fluid.getDefaultState())) {
				EmiStack fs = new FluidEmiStack(fluid);
				try {
					fs.getName();
					fs.getTooltip();
					stacks.add(fs);
				} catch (Exception e) {
				}
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
		}).toList();
		for (int i = 0; i < stacks.size(); i++) {
			indices.put(stacks.get(i), i);
		}
	}
}
