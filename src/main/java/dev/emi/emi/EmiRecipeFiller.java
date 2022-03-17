package dev.emi.emi;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;

import org.apache.commons.compress.utils.Lists;
import org.jetbrains.annotations.Nullable;

import dev.emi.emi.api.EmiRecipeHandler;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.mixin.accessor.ScreenHandlerAccessor;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;

public class EmiRecipeFiller {
	private static final Set<EmiRecipeHandler<?>> EMPTY_SET = Set.of();
	public static final Map<EmiRecipeCategory, Set<EmiRecipeHandler<?>>> RECIPE_HANDLERS = Maps.newHashMap();
	
	@SuppressWarnings("unchecked")
	public static <T extends ScreenHandler> @Nullable List<ItemStack> fillRecipe(EmiRecipe recipe, HandledScreen<T> screen, boolean all) {
		try {
			T screenHandler = screen.getScreenHandler();
			ScreenHandlerType<?> type = ((ScreenHandlerAccessor) screenHandler).emi$getType();
			if ((type == null && screenHandler instanceof PlayerScreenHandler) || (type != null && EmiMain.handlers.containsKey(type))) {
				EmiRecipeHandler<T> handler;
				if (type == null) {
					handler = (EmiRecipeHandler<T>) EmiMain.INVENTORY;
				} else {
					handler = (EmiRecipeHandler<T>) EmiMain.handlers.get(type);
				}
				if (!RECIPE_HANDLERS.getOrDefault(recipe.getCategory(), EMPTY_SET).contains(handler)) {
					return null;
				}
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
						DiscoveredItem di = new DiscoveredItem(item, 0, item.getCount());
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
					unique.add(new DiscoveredItem(di.stack, di.amount, di.consumed));
				}
				int maxAmount = unique.get(0).amount / unique.get(0).consumed;
				for (DiscoveredItem ui : unique) {
					maxAmount = Math.min(maxAmount, ui.amount / ui.consumed);
					maxAmount = Math.min(maxAmount, ui.stack.getMaxCount());
				}
				if (!all) {
					maxAmount = Math.min(maxAmount, 1);
				}

				if (maxAmount == 0) {
					return null;
				}
				
				List<ItemStack> desired = Lists.newArrayList();
				for (int i = 0; i < discovered.size(); i++) {
					DiscoveredItem di = discovered.get(i);
					if (di != null) {
						ItemStack is = di.stack.copy();
						int amount = di.consumed * maxAmount;
						is.setCount(amount);
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
		public ItemStack stack;
		public int consumed;
		public int amount;

		public DiscoveredItem(ItemStack stack, int amount, int consumed) {
			this.stack = stack.copy();
			this.amount = amount;
			this.consumed = consumed;
		}
	}
}
