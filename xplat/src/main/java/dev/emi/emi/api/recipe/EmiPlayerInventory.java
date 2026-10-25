package dev.emi.emi.api.recipe;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import dev.emi.emi.EmiPort;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.handler.EmiCraftContext;
import dev.emi.emi.api.recipe.handler.EmiRecipeHandler;
import dev.emi.emi.api.recipe.handler.StandardRecipeHandler;
import dev.emi.emi.api.stack.Comparison;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.registry.EmiRecipeFiller;
import dev.emi.emi.registry.EmiStackList;
import dev.emi.emi.runtime.EmiFavorite;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;

@SuppressWarnings({"unchecked", "rawtypes"})
public class EmiPlayerInventory {
	private final Comparison none = Comparison.DEFAULT_COMPARISON;
	private final Comparison strict = EmiPort.compareStrict();
	public Map<EmiStack, EmiStack> inventory = Maps.newHashMap();
	
	@Deprecated
	@ApiStatus.Internal
	public EmiPlayerInventory(Player entity) {
		AbstractContainerScreen<?> screen = EmiApi.getHandledScreen();
		if (screen != null && screen.getMenu() != null) {
			if (screen.getMenu().getCarried() != null) {
				addStack(screen.getMenu().getCarried());
			}
			List<EmiRecipeHandler<?>> handlers = (List) EmiRecipeFiller.getAllHandlers(screen);
			if (!handlers.isEmpty()) {
				if (handlers.get(0) instanceof StandardRecipeHandler standard) {
					List<Slot> slots = standard.getInputSources(screen.getMenu());
					for (Slot slot : slots) {
						if (slot.mayPickup(entity)) {
							addStack(slot.getItem());
						}
					}
					return;
				}
			}
		}

		Inventory pInv = entity.getInventory();
		for (int i = 0; i < pInv.getNonEquipmentItems().size(); i++) {
			addStack(pInv.getNonEquipmentItems().get(i));
		}
	}

	public EmiPlayerInventory(List<EmiStack> stacks) {
		for (EmiStack stack : stacks) {
			addStack(stack);
		}
		AbstractContainerScreen<?> screen = EmiApi.getHandledScreen();
		if (screen != null && screen.getMenu() != null) {
			if (screen.getMenu().getCarried() != null) {
				addStack(screen.getMenu().getCarried());
			}
		}
	}

	public static EmiPlayerInventory of(Player entity) {
		AbstractContainerScreen<?> screen = EmiApi.getHandledScreen();
		if (screen != null) {
			List<EmiRecipeHandler<?>> handlers = (List) EmiRecipeFiller.getAllHandlers(screen);
			if (!handlers.isEmpty()) {
				return handlers.get(0).getInventory((AbstractContainerScreen) screen);
			}
		}
		if (entity == null) {
			return new EmiPlayerInventory(List.of());
		}
		return new EmiPlayerInventory(entity);
	}

	private void addStack(ItemStack is) {
		EmiStack stack = EmiStack.of(is).comparison(c -> none);
		addStack(stack);
	}

	private void addStack(EmiStack stack) {
		if (!stack.isEmpty()) {
			inventory.merge(stack, stack, (a, b) -> a.setAmount(a.getAmount() + b.getAmount()));
		}
	}

	public Predicate<EmiRecipe> getPredicate() {
		AbstractContainerScreen screen = EmiApi.getHandledScreen();
		List<EmiRecipeHandler> handlers = EmiRecipeFiller.getAllHandlers(screen);
		if (!handlers.isEmpty()) {
			EmiCraftContext context = new EmiCraftContext(screen, this, EmiCraftContext.Type.CRAFTABLE);
			return r -> {
				for (int i = 0; i < handlers.size(); i++) {
					EmiRecipeHandler handler = handlers.get(i);
					if (handler.supportsRecipe(r)) {
						return handler.canCraft(r, context);
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
			set.addAll(EmiApi.getRecipeManager().getRecipesByInput(stack));
		}
		return set.stream().filter(r -> !r.hideCraftable() && predicate.test(r) && r.getOutputs().size() > 0)
			.map(r -> new EmiFavorite.Craftable(r))
			.sorted((a, b) -> {
				int i = Integer.compare(
					EmiStackList.getIndex(a.getStack()),
					EmiStackList.getIndex(b.getStack()));
				if (i != 0) {
					return i;
				}
				return Long.compare(a.getAmount(), b.getAmount());
			}).collect(Collectors.toList());
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
		Comparison comparison = Comparison.of((a, b) -> {
			return strict.compare(a, b) && a.getAmount() == b.getAmount();
		});
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
