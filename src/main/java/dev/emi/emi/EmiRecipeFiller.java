package dev.emi.emi;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

import org.apache.commons.compress.utils.Lists;
import org.jetbrains.annotations.Nullable;

import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.EmiFillAction;
import dev.emi.emi.api.EmiRecipeHandler;
import dev.emi.emi.api.recipe.EmiPlayerInventory;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.Comparison;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.mixin.accessor.ScreenHandlerAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;

public class EmiRecipeFiller {
	public static Map<ScreenHandlerType<?>, List<EmiRecipeHandler<?>>> handlers = Maps.newHashMap();

	public static boolean isSupported(EmiRecipe recipe) {
		for (List<EmiRecipeHandler<?>> list : handlers.values()) {
			for (EmiRecipeHandler<?> handler : list) {
				if (handler.supportsRecipe(recipe) && !handler.onlyDisplayWhenApplicable(recipe)) {
					return true;
				}
			}
		}
		for (EmiRecipeHandler<?> handler : getAllHandlers(EmiApi.getHandledScreen())) {
			if (handler.supportsRecipe(recipe)) {
				return true;
			}
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	public static <T extends ScreenHandler> List<EmiRecipeHandler<T>> getAllHandlers(HandledScreen<T> screen) {
		if (screen != null) {
			T screenHandler = screen.getScreenHandler();
			ScreenHandlerType<?> type = ((ScreenHandlerAccessor) screenHandler).emi$getType();
			if ((type != null || screenHandler instanceof PlayerScreenHandler) && handlers.containsKey(type)) {
				return (List<EmiRecipeHandler<T>>) (List<?>) handlers.get(type);
			}
		}
		return List.of();
	}

	public static <T extends ScreenHandler> @Nullable EmiRecipeHandler<T> getFirstValidHandler(EmiRecipe recipe, HandledScreen<T> screen) {
		for (EmiRecipeHandler<T> handler : getAllHandlers(screen)) {
			if (handler.supportsRecipe(recipe)) {
				return handler;
			}
		}
		return null;
	}

	public static <T extends ScreenHandler> boolean performFill(EmiRecipe recipe, HandledScreen<T> screen, EmiFillAction action, int amount) {
		EmiRecipeHandler<T> handler = getFirstValidHandler(recipe, screen);
		if (handler != null && handler.supportsRecipe(recipe)) {
			MinecraftClient client = MinecraftClient.getInstance();
			EmiPlayerInventory inv = new EmiPlayerInventory(client.player);
			if (handler.canCraft(recipe, inv, screen)) {
				return handler.performFill(recipe, screen, action, amount);
			}
		}
		return false;
	}
	
	public static <T extends ScreenHandler> @Nullable List<ItemStack> getStacks(EmiRecipe recipe, HandledScreen<T> screen, int amount) {
		try {
			T screenHandler = screen.getScreenHandler();
			EmiRecipeHandler<T> handler = getFirstValidHandler(recipe, screen);
			if (handler != null) {
				List<Slot> slots = handler.getInputSources(screenHandler);
				List<EmiIngredient> ingredients = recipe.getInputs();
				List<DiscoveredItem> discovered = Lists.newArrayList();
				for (int i = 0; i < ingredients.size(); i++) {
					List<DiscoveredItem> d = Lists.newArrayList();
					EmiIngredient ingredient = ingredients.get(i);
					List<EmiStack> emiStacks = ingredient.getEmiStacks();
					for (int e = 0; e < emiStacks.size(); e++) {
						EmiStack stack = emiStacks.get(e);
						ItemStack item = stack.getItemStack();
						DiscoveredItem di = new DiscoveredItem(stack, item, 0, item.getCount());
						for (Slot s : slots) {
							if (ItemStack.canCombine(item, s.getStack())) {
								di.amount += s.getStack().getCount();
							}
						}
						if (di != null) {
							d.add(di);
						}
					}
					DiscoveredItem biggest = null;
					for (DiscoveredItem di : d) {
						if (biggest == null || biggest.amount < di.amount) {
							biggest = di;
						}
					}
					if (biggest == null && !ingredient.isEmpty()) {
						return null;
					}
					if (biggest != null) {
						Slot slot = handler.getCraftingSlots(screenHandler).get(i);
						biggest.max = Math.min(biggest.max, slot.getMaxItemCount());
					}
					discovered.add(biggest);
				}
				if (discovered.isEmpty()) {
					return null;
				}

				List<DiscoveredItem> unique = Lists.newArrayList();
				outer:
				for (DiscoveredItem di : discovered) {
					if (di == null) {
						continue;
					}
					for (DiscoveredItem ui : unique) {
						if (ItemStack.canCombine(di.stack, ui.stack)) {
							ui.consumed += di.consumed;
							continue outer;
						}
					}
					unique.add(new DiscoveredItem(di.ingredient, di.stack, di.amount, di.consumed));
				}
				int maxAmount = Integer.MAX_VALUE;
				for (DiscoveredItem ui : unique) {
					if (!ui.catalyst()) {
						maxAmount = Math.min(maxAmount, ui.amount / ui.consumed);
						maxAmount = Math.min(maxAmount, ui.max);
					}
				}
				maxAmount = Math.min(maxAmount, amount);

				if (maxAmount == 0) {
					return null;
				}
				
				List<ItemStack> desired = Lists.newArrayList();
				for (int i = 0; i < discovered.size(); i++) {
					DiscoveredItem di = discovered.get(i);
					if (di != null) {
						ItemStack is = di.stack.copy();
						int a = di.catalyst() ? di.consumed : di.consumed * maxAmount;
						is.setCount(a);
						desired.add(is);
					} else {
						desired.add(ItemStack.EMPTY);
					}
				}
				return desired;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private static class DiscoveredItem {
		private static final Comparison COMPARISON = Comparison.builder().nbt(false).amount(false).build();
		public EmiStack ingredient;
		public ItemStack stack;
		public int consumed;
		public int amount;
		public int max;

		public DiscoveredItem(EmiStack ingredient, ItemStack stack, int amount, int consumed) {
			this.ingredient = ingredient;
			this.stack = stack.copy();
			this.amount = amount;
			this.consumed = consumed;
			max = stack.getMaxCount();
		}

		public boolean catalyst() {
			return ingredient.getRemainder().isEqual(ingredient, COMPARISON);
		}
	}
}
