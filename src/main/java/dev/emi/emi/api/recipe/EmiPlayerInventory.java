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
import dev.emi.emi.EmiRecipeFiller;
import dev.emi.emi.EmiRecipes;
import dev.emi.emi.EmiStackList;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.EmiRecipeHandler;
import dev.emi.emi.api.stack.Comparison;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

@SuppressWarnings({"unchecked", "rawtypes"})
public class EmiPlayerInventory {
	private final Comparison none = Comparison.builder().amount(false).nbt(false).build();
	private final Comparison nbt = Comparison.builder().amount(false).nbt(true).build();
	public Map<EmiStack, EmiStack> inventory = Maps.newHashMap();
	
	public EmiPlayerInventory(PlayerEntity entity) {
		HandledScreen<?> screen = EmiApi.getHandledScreen();
		if (screen != null) {
			List<EmiRecipeHandler<?>> handlers = (List) EmiRecipeFiller.getAllHandlers(screen);
			if (!handlers.isEmpty()) {
				// TODO this only supports slots from the first one
				List<Slot> slots = ((EmiRecipeHandler) handlers.get(0)).getInputSources(screen.getScreenHandler());
				for (Slot slot : slots) {
					if (slot.canTakeItems(entity)) {
						addStack(slot.getStack());
					}
				}
				return;
			}
		}

		PlayerInventory pInv = entity.getInventory();
		for (int i = 0; i < pInv.main.size(); i++) {
			addStack(pInv.main.get(i));
		}
	}

	private void addStack(ItemStack is) {
		EmiStack stack = EmiStack.of(is).comparison(c -> none);
		if (!stack.isEmpty()) {
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
		HandledScreen screen = EmiApi.getHandledScreen();
		List<EmiRecipeHandler> handlers = EmiRecipeFiller.getAllHandlers(screen);
		if (!handlers.isEmpty()) {
			return r -> {
				for (int i = 0; i < handlers.size(); i++) {
					EmiRecipeHandler handler = handlers.get(i);
					if (handler.supportsRecipe(r)) {
						return handler.canCraft(r, EmiPlayerInventory.this, screen);
					}
				}
				return false;
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
		return set.stream().filter(r -> !r.hideCraftable() && predicate.test(r) && r.getOutputs().size() > 0)
			.map(r -> new EmiFavorite(r.getOutputs().get(0), r))
			.sorted((a, b) -> Integer.compare(
				EmiStackList.indices.getOrDefault(a.getStack(), Integer.MAX_VALUE),
				EmiStackList.indices.getOrDefault(b.getStack(), Integer.MAX_VALUE)
			)).collect(Collectors.toList());
	}

	public List<Boolean> getCraftAvailability(EmiRecipe recipe) {
		Object2LongMap<EmiStack> used = new Object2LongOpenHashMap<>();
		List<Boolean> states = Lists.newArrayList();
		outer:
		for (EmiIngredient ingredient : recipe.getInputs()) {
			for (EmiStack stack : ingredient.getEmiStacks()) {
				long desired = stack.getAmount();
				if (inventory.containsKey(stack)) {
					EmiStack identity = inventory.get(stack);
					long alreadyUsed = used.getOrDefault(identity, 0);
					long available = identity.getAmount() - alreadyUsed;
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
		return canCraft(recipe, 1);
	}

	public boolean canCraft(EmiRecipe recipe, long amount) {
		Object2LongMap<EmiStack> used = new Object2LongOpenHashMap<>();
		outer:
		for (EmiIngredient ingredient : recipe.getInputs()) {
			if (ingredient.isEmpty()) {
				continue;
			}
			for (EmiStack stack : ingredient.getEmiStacks()) {
				long desired = stack.getAmount() * amount;
				if (inventory.containsKey(stack)) {
					EmiStack identity = inventory.get(stack);
					long alreadyUsed = used.getOrDefault(identity, 0);
					long available = identity.getAmount() - alreadyUsed;
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
