package dev.emi.emi.api.recipe;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.apache.commons.compress.utils.Lists;

import dev.emi.emi.EmiConfig;
import dev.emi.emi.EmiFavorite;
import dev.emi.emi.EmiMain;
import dev.emi.emi.EmiRecipeFiller;
import dev.emi.emi.EmiRecipes;
import dev.emi.emi.EmiStackList;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.EmiRecipeHandler;
import dev.emi.emi.api.stack.Comparison;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.mixin.accessor.ScreenHandlerAccessor;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;

public class EmiPlayerInventory {
	public Map<EmiStack, EmiStack> inventory;
	
	public EmiPlayerInventory(PlayerEntity entity) {
		inventory = Maps.newHashMap();
		Comparison none = Comparison.builder().amount(false).nbt(false).build();
		Comparison nbt = Comparison.builder().amount(false).nbt(true).build();
		PlayerInventory pInv = entity.getInventory();
		for (int i = 0; i < pInv.main.size(); i++) {
			ItemStack is = pInv.main.get(i);
			EmiStack stack = EmiStack.of(is).comparison(c -> none);
			if (stack.isEmpty()) {
				continue;
			}
			if (inventory.containsKey(stack)) {
				for (EmiStack other : inventory.keySet()) {
					if (other.isEqual(stack, nbt)) {
						other.setAmount(other.getAmount() + stack.getAmount());
						break;
					}
				}
			} else {
				inventory.put(stack, stack);
			}
		}
	}

	public Predicate<EmiRecipe> getPredicate() {
		if (!EmiConfig.localCraftable) {
			return r -> this.canCraft(r);
		}
		HandledScreen<?> screen = EmiApi.getHandledScreen();
		ScreenHandler screenHandler = screen.getScreenHandler();
		ScreenHandlerType<?> type = ((ScreenHandlerAccessor) screenHandler).emi$getType();
		if ((type == null && screenHandler instanceof PlayerScreenHandler) || (type != null && EmiMain.handlers.containsKey(type))) {
			EmiRecipeHandler<?> handler;
			if (type == null) {
				handler = (EmiRecipeHandler<?>) EmiMain.INVENTORY;
			} else {
				handler = (EmiRecipeHandler<?>) EmiMain.handlers.get(type);
			}
			Set<EmiRecipeHandler<?>> empty = Set.of();
			return r -> {
				return EmiRecipeFiller.RECIPE_HANDLERS.getOrDefault(r.getCategory(), empty).contains(handler)
					&& r.canCraft(this, screen);
			};
		}
		return null;
	}

	public List<EmiIngredient> getCraftables() {
		Predicate<EmiRecipe> predicate = getPredicate();
		if (predicate == null) {
			return List.of();
		}
		Set<EmiRecipe> set = Sets.newHashSet();
		for (EmiStack stack : inventory.keySet()) {
			set.addAll(EmiRecipes.byInput.getOrDefault(stack.getKey(), Map.of()).values().stream().flatMap(l -> l.stream()).toList());
		}
		return set.stream().filter(r -> predicate.test(r) && r.getOutputs().size() > 0)
			.map(r -> new EmiFavorite(r.getOutputs().get(0), r))
			.sorted((a, b) -> Integer.compare(
				EmiStackList.indices.getOrDefault(a.getStack(), Integer.MAX_VALUE),
				EmiStackList.indices.getOrDefault(b.getStack(), Integer.MAX_VALUE)
			)).collect(Collectors.toList());
	}

	public List<Boolean> getCraftAvailability(EmiRecipe recipe) {
		Object2IntMap<EmiStack> used = new Object2IntOpenHashMap<>();
		List<Boolean> states = Lists.newArrayList();
		outer:
		for (EmiIngredient ingredient : recipe.getInputs()) {
			for (EmiStack stack : ingredient.getEmiStacks()) {
				int desired = stack.getAmount();
				if (inventory.containsKey(stack)) {
					EmiStack identity = inventory.get(stack);
					int alreadyUsed = used.getOrDefault(identity, 0);
					int available = identity.getAmount() - alreadyUsed;
					if (available >= desired) {
						used.put(identity, desired + alreadyUsed);
						states.add(true);
						continue outer;
					}
				}
			}
			states.add(false);
		}
		return states;
	}

	public boolean canCraft(EmiRecipe recipe) {
		Object2IntMap<EmiStack> used = new Object2IntOpenHashMap<>();
		outer:
		for (EmiIngredient ingredient : recipe.getInputs()) {
			for (EmiStack stack : ingredient.getEmiStacks()) {
				int desired = stack.getAmount();
				if (inventory.containsKey(stack)) {
					EmiStack identity = inventory.get(stack);
					int alreadyUsed = used.getOrDefault(identity, 0);
					int available = identity.getAmount() - alreadyUsed;
					if (available >= desired) {
						used.put(identity, desired + alreadyUsed);
						continue outer;
					}
				}
			}
			return false;
		}
		return true;
	}

	public boolean isEqual(EmiPlayerInventory other) {
		if (other == null) {
			return false;
		}
		Comparison comparison = Comparison.builder().nbt(true).amount(true).build();
		if (other.inventory.size() != inventory.size()) {
			return false;
		} else {
			for (EmiStack stack : inventory.keySet()) {
				if (!other.inventory.containsKey(stack) || !other.inventory.get(stack).isEqual(stack, comparison)) {
					return false;
				}
			}
		}
		return true;
	}
}
