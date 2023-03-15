package dev.emi.emi;

import java.util.List;
import java.util.function.Predicate;

import com.google.common.collect.Lists;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.FluidEmiStack;
import dev.emi.emi.data.EmiData;
import dev.emi.emi.data.IndexStackData;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public class EmiStackList {
	private static final TagKey<Item> ITEM_HIDDEN = TagKey.of(EmiPort.getItemRegistry().getKey(), new Identifier("c", "hidden_from_recipe_viewers"));
	private static final TagKey<Block> BLOCK_HIDDEN = TagKey.of(EmiPort.getBlockRegistry().getKey(), new Identifier("c", "hidden_from_recipe_viewers"));
	private static final TagKey<Fluid> FLUID_HIDDEN = TagKey.of(EmiPort.getFluidRegistry().getKey(), new Identifier("c", "hidden_from_recipe_viewers"));
	public static List<Predicate<EmiStack>> invalidators = Lists.newArrayList();
	public static List<EmiStack> stacks = List.of();
	public static Object2IntMap<EmiStack> indices = new Object2IntOpenHashMap<>();

	public static void clear() {
		invalidators.clear();
		stacks = List.of();
		indices.clear();
	}

	public static void reload() {
		List<EmiStack> stacks = Lists.newLinkedList();
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
				} catch (Throwable e) {
				}
			}
		}
		
		EmiStackList.stacks = stacks;
	}

	@SuppressWarnings("deprecation")
	public static void bake() {
		stacks.removeIf(s -> {
			for (Predicate<EmiStack> invalidator : invalidators) {
				if (invalidator.test(s)) {
					return true;
				}
			}
			if (s.getKey() instanceof Item i) {
				if (i instanceof BlockItem bi && bi.getBlock().getDefaultState().isIn(BLOCK_HIDDEN)) {
					return true;
				} else if (s.getItemStack().isIn(ITEM_HIDDEN)) {
					return true;
				}
			} else if (s.getKey() instanceof Fluid f) {
				if (f.isIn(FLUID_HIDDEN)) {
					return true;
				}
			}
			return false;
		});
		for (IndexStackData ssd : EmiData.stackData) {
			if (!ssd.removed().isEmpty()) {
				stacks.removeIf(s -> {
					for (EmiIngredient invalidator : ssd.removed()) {
						for (EmiStack stack : invalidator.getEmiStacks()) {
							if (stack.equals(s)) {
								return true;
							}
						}
					}
					return false;
				});
			}
			for (IndexStackData.Added added : ssd.added()) {
				if (added.added().isEmpty()) {
					continue;
				}
				if (added.after().isEmpty()) {
					stacks.add(added.added().getEmiStacks().get(0));
				} else {
					int i = stacks.indexOf(added.after());
					if (i == -1) {
						i = stacks.size() - 1;
					}
					stacks.add(i + 1, added.added().getEmiStacks().get(0));
				}
			}
		}
		stacks = stacks.stream().toList();
		for (int i = 0; i < stacks.size(); i++) {
			indices.put(stacks.get(i), i);
		}
	}
}
