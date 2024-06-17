package dev.emi.emi.registry;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import dev.emi.emi.EmiPort;
import dev.emi.emi.api.stack.Comparison;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiRegistryAdapter;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.config.IndexSource;
import dev.emi.emi.data.EmiData;
import dev.emi.emi.data.IndexStackData;
import dev.emi.emi.runtime.EmiHidden;
import dev.emi.emi.runtime.EmiLog;
import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.tag.TagKey;

public class EmiStackList {
	private static final TagKey<Item> ITEM_HIDDEN = TagKey.of(EmiPort.getItemRegistry().getKey(), EmiTags.HIDDEN_FROM_RECIPE_VIEWERS);
	private static final TagKey<Block> BLOCK_HIDDEN = TagKey.of(EmiPort.getBlockRegistry().getKey(), EmiTags.HIDDEN_FROM_RECIPE_VIEWERS);
	private static final TagKey<Fluid> FLUID_HIDDEN = TagKey.of(EmiPort.getFluidRegistry().getKey(), EmiTags.HIDDEN_FROM_RECIPE_VIEWERS);
	public static List<Predicate<EmiStack>> invalidators = Lists.newArrayList();
	public static List<EmiStack> stacks = List.of();
	public static List<EmiStack> filteredStacks = List.of();
	private static Object2IntMap<EmiStack> strictIndices = new Object2IntOpenCustomHashMap<>(new StrictHashStrategy());
	private static Object2IntMap<Object> keyIndices = new Object2IntOpenHashMap<>();

	public static void clear() {
		invalidators.clear();
		stacks = List.of();
		strictIndices.clear();
		keyIndices.clear();
	}

	public static void reload() {
		try {
			MinecraftClient client = MinecraftClient.getInstance();
			ItemGroups.updateDisplayContext(client.player.networkHandler.getEnabledFeatures(), false, client.world.getRegistryManager());
		} catch (Throwable t) {
			EmiLog.error("Failed to update creative tabs. Using fallback index.");
			EmiLog.error(t);
		}
		List<IndexGroup> groups = Lists.newArrayList();
		Map<String, IndexGroup> namespaceGroups = new LinkedHashMap<>();
		for (Item item : EmiPort.getItemRegistry()) {
			String itemName = "null";
			try {
				itemName = item.toString();
				EmiStack stack = EmiStack.of(item);
				namespaceGroups.computeIfAbsent(stack.getId().getNamespace(), (k) -> new IndexGroup()).stacks.add(stack);
			} catch (Exception e) {
				EmiLog.error("Item " + itemName + " threw while EMI was attempting to construct the index, items may be missing.");
				EmiLog.error(e);
			}
		}
		if (EmiConfig.indexSource != IndexSource.REGISTERED) {
			for (ItemGroup group : ItemGroups.getGroups()) {
				String groupName = "null";
				try {
					groupName = group.getDisplayName().getString();
					Object2IntMap<String> usedNamespaces = new Object2IntOpenHashMap<>();
					IndexGroup ig = new IndexGroup();
					Collection<ItemStack> searchStacks = group.getSearchTabStacks();
					for (ItemStack stack : searchStacks) {
						EmiStack es = EmiStack.of(stack);
						String namespace = es.getId().getNamespace();
						usedNamespaces.put(namespace, usedNamespaces.getOrDefault(namespace, 0) + 1);
						ig.stacks.add(es);
					}
					if (EmiConfig.indexSource == IndexSource.CREATIVE) {
						for (String namespace : usedNamespaces.keySet()) {
							if (namespaceGroups.containsKey(namespace)) {
								IndexGroup ng = namespaceGroups.get(namespace);
								if (usedNamespaces.getInt(namespace) * 3 >= searchStacks.size() || usedNamespaces.getInt(namespace) * 3 >= ng.stacks.size()) {
									ng.suppressedBy.add(ig);
								}
							}
						}
					}
					groups.add(ig);
				} catch (Exception e) {
					EmiLog.error("Creative item group " + groupName + " threw while EMI was attempting to construct the index, items may be missing.");
					EmiLog.error(e);
				}
			}
		}
		groups.addAll(namespaceGroups.values());
		IndexGroup fluidGroup = new IndexGroup();
		for (Fluid fluid : EmiPort.getFluidRegistry()) {
			String fluidName = null;
			try {
				fluidName = fluid.toString();
				if (fluid.isStill(fluid.getDefaultState()) || (fluid instanceof FlowableFluid ff && ff.getStill() == Fluids.EMPTY)) {
					EmiStack fs = EmiStack.of(fluid);
					fluidGroup.stacks.add(fs);
				}
			} catch (Exception e) {
				EmiLog.error("Fluid  " + fluidName + " threw while EMI was attempting to construct the index, stack may be missing.");
				EmiLog.error(e);
			}
		}
		groups.add(fluidGroup);

		Set<EmiStack> added = new ObjectOpenCustomHashSet<>(new StrictHashStrategy());
		
		stacks = Lists.newLinkedList();
		for (IndexGroup group : groups) {
			if (group.shouldDisplay()) {
				for (EmiStack stack : group.stacks) {
					if (!added.contains(stack)) {
						stacks.add(stack);
						added.add(stack);
					}
				}
			}
		}
	}

	@SuppressWarnings({"deprecation", "unchecked"})
	private static <T> boolean isHiddenFromRecipeViewers(T key) {
		if (key instanceof Item i) {
			if (i instanceof BlockItem bi && bi.getBlock().getDefaultState().isIn(BLOCK_HIDDEN)) {
				return true;
			} else if (i.getRegistryEntry().isIn(ITEM_HIDDEN)) {
				return true;
			}
		} else if (key instanceof Fluid f) {
			if (f.isIn(FLUID_HIDDEN)) {
				return true;
			}
		} else {
			EmiRegistryAdapter<T> adapter = (EmiRegistryAdapter<T>) EmiTags.ADAPTERS_BY_CLASS.get(key.getClass());
			if (adapter != null) {
				return adapter.getRegistry().getEntry(key).isIn(TagKey.of(adapter.getRegistry().getKey(), EmiTags.HIDDEN_FROM_RECIPE_VIEWERS));
			}
		}
		return false;
	}

	public static void bake() {
		stacks.removeIf(s -> {
			try {
				if (s.isEmpty()) {
					return true;
				}
				for (Predicate<EmiStack> invalidator : invalidators) {
					if (invalidator.test(s)) {
						return true;
					}
				}
				if (isHiddenFromRecipeViewers(s.getKey())) {
					return true;
				}
				return false;
			} catch (Throwable t) {
				EmiLog.error("Stack threw error while baking");
				t.printStackTrace();
				return true;
			}
		});
		for (Supplier<IndexStackData> supplier : EmiData.stackData) {
			IndexStackData ssd = supplier.get();
			if (!ssd.removed().isEmpty()) {
				Set<EmiStack> removed = Sets.newHashSet();
				for (EmiIngredient invalidator : ssd.removed()) {
					for (EmiStack stack : invalidator.getEmiStacks()) {
						removed.add(stack.copy().comparison(c -> EmiPort.compareStrict()));
					}
				}
				stacks.removeAll(removed);
			}
			if (!ssd.filters().isEmpty()) {
				stacks.removeIf(s -> {
					String id = "" + s.getId();
					for (IndexStackData.Filter filter : ssd.filters()) {
						if (filter.filter().test(id)) {
							return true;
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
		stacks = stacks.stream().filter(stack -> {
			String name = "Unknown";
			String id = "unknown";
			try {
				if (stack.isEmpty()) {
					return false;
				}
				name = stack.toString();
				id = stack.getId().toString();
				if (name != null && stack.getKey() != null && stack.getName() != null) {
					return true;
				}
				EmiLog.warn("Hiding stack " + name + " with id " + id + " from index due to returning dangerous values");
				return false;
			} catch (Throwable t) {
				EmiLog.warn("Hiding stack " + name + " with id " + id + " from index due to throwing errors");
				t.printStackTrace();
				return false;
			}
		}).toList();
		for (int i = 0; i < stacks.size(); i++) {
			EmiStack stack = stacks.get(i);
			strictIndices.put(stack, i);
			keyIndices.put(stack.getKey(), i);
		}
		bakeFiltered();
	}

	public static void bakeFiltered() {
		filteredStacks = stacks.stream().filter(s -> !EmiHidden.isDisabled(s) && !EmiHidden.isHidden(s)).toList();
	}

	public static int getIndex(EmiIngredient ingredient) {
		EmiStack stack = ingredient.getEmiStacks().get(0);
		int ret = strictIndices.getOrDefault(stack, Integer.MAX_VALUE);
		if (ret == Integer.MAX_VALUE) {
			ret = keyIndices.getOrDefault(stack, ret);
		}
		return ret;
	}

	public static class IndexGroup {
		public List<EmiStack> stacks = Lists.newArrayList();
		public Set<IndexGroup> suppressedBy = Sets.newHashSet();

		public boolean shouldDisplay() {
			for (IndexGroup suppressor : suppressedBy) {
				if (suppressor.shouldDisplay()) {
					return false;
				}
			}
			return true;
		}
	}

	public static class StrictHashStrategy implements Hash.Strategy<EmiStack> {

		@Override
		public boolean equals(EmiStack a, EmiStack b) {
			if (a == b) {
				return true;
			} else if (a == null || b == null) {
				return false;
			} else if (a.isEmpty() && b.isEmpty()) {
				return true;
			}
			return a.isEqual(b, EmiPort.compareStrict());
		}

		@Override
		public int hashCode(EmiStack stack) {
			if (stack != null) {
				NbtCompound nbtCompound = stack.getNbt();
				int i = 31 + stack.getKey().hashCode();
				return 31 * i + (nbtCompound == null ? 0 : nbtCompound.hashCode());
			}
			return 0;
		}
	}

	public static class ComparisonHashStrategy implements Hash.Strategy<EmiStack> {

		@Override
		public boolean equals(EmiStack a, EmiStack b) {
			if (a == b) {
				return true;
			} else if (a == null || b == null) {
				return false;
			} else if (a.isEmpty() && b.isEmpty()) {
				return true;
			}
			return a.isEqual(b, EmiComparisonDefaults.get(a.getKey()));
		}

		@Override
		public int hashCode(EmiStack stack) {
			if (stack != null) {
				int i = 31 + stack.getKey().hashCode();
				return 31 * i + EmiComparisonDefaults.get(stack.getKey()).getHash(stack);
			}
			return 0;
		}
	}
}
