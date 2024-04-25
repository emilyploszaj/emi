package dev.emi.emi.registry;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiPlayerInventory;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.handler.EmiCraftContext;
import dev.emi.emi.api.recipe.handler.EmiRecipeHandler;
import dev.emi.emi.api.recipe.handler.StandardRecipeHandler;
import dev.emi.emi.api.stack.Comparison;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.handler.CoercedRecipeHandler;
import dev.emi.emi.mixin.accessor.CraftingResultSlotAccessor;
import dev.emi.emi.runtime.EmiSidebars;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.CraftingResultSlot;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

public class EmiRecipeFiller {
	public static Map<ScreenHandlerType<?>, List<EmiRecipeHandler<?>>> handlers = Maps.newHashMap();
	public static BiFunction<ScreenHandler, EmiRecipe, EmiRecipeHandler<?>> extraHandlers = (h, r) -> null;

	public static void clear() {
		handlers.clear();
		extraHandlers = (h, r) -> null;
	}

	public static boolean isSupported(EmiRecipe recipe) {
		for (List<EmiRecipeHandler<?>> list : handlers.values()) {
			for (EmiRecipeHandler<?> handler : list) {
				if (handler.supportsRecipe(recipe) && handler.alwaysDisplaySupport(recipe)) {
					return true;
				}
			}
		}
		HandledScreen<?> hs = EmiApi.getHandledScreen();
		if (hs != null) {
			for (EmiRecipeHandler<?> handler : getAllHandlers(hs)) {
				if (handler.supportsRecipe(recipe)) {
					return true;
				}
			}
			EmiRecipeHandler<?> handler = extraHandlers.apply(hs.getScreenHandler(), recipe);
			if (handler != null && handler.supportsRecipe(recipe)) {
				return true;
			}
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	public static <T extends ScreenHandler> List<EmiRecipeHandler<T>> getAllHandlers(HandledScreen<T> screen) {
		if (screen != null) {
			T screenHandler = screen.getScreenHandler();
			ScreenHandlerType<?> type;
			try {
				type = screenHandler instanceof PlayerScreenHandler ? null : screenHandler.getType();
			} catch (UnsupportedOperationException e) {
				type = null;
			}
			if ((type != null || screenHandler instanceof PlayerScreenHandler) && handlers.containsKey(type)) {
				return (List<EmiRecipeHandler<T>>) (List<?>) handlers.get(type);
			}
			for (Slot slot : screen.getScreenHandler().slots) {
				if (slot instanceof CraftingResultSlot crs) {
					RecipeInputInventory inv = ((CraftingResultSlotAccessor) crs).getInput();
					if (inv != null && inv.getWidth() > 0 && inv.getHeight() > 0) {
						return List.of(new CoercedRecipeHandler<T>(crs));
					}
				}
			}
		}
		return List.of();
	}

	@SuppressWarnings("unchecked")
	public static <T extends ScreenHandler> @Nullable EmiRecipeHandler<T> getFirstValidHandler(EmiRecipe recipe, HandledScreen<T> screen) {
		EmiRecipeHandler<T> ret = null;
		for (EmiRecipeHandler<T> handler : getAllHandlers(screen)) {
			if (handler.supportsRecipe(recipe)) {
				ret = handler;
				break;
			}
		}
		if (ret == null || (ret instanceof CoercedRecipeHandler && !(screen instanceof InventoryScreen))) {
			EmiRecipeHandler<T> extra = (EmiRecipeHandler<T>) extraHandlers.apply(screen.getScreenHandler(), recipe);
			if (extra != null) {
				ret = extra;
			}
		}
		return ret;
	}

	public static <T extends ScreenHandler> boolean performFill(EmiRecipe recipe, HandledScreen<T> screen,
			EmiCraftContext.Type type, EmiCraftContext.Destination destination, int amount) {
		EmiRecipeHandler<T> handler = getFirstValidHandler(recipe, screen);
		if (handler != null && handler.supportsRecipe(recipe)) {
			EmiPlayerInventory inv = handler.getInventory(screen);
			EmiCraftContext<T> context = new EmiCraftContext<T>(screen, inv, type, destination, amount);
			if (handler.canCraft(recipe, context)) {
				EmiSidebars.craft(recipe);
				return handler.craft(recipe, context);
			}
		}
		return false;
	}

	public static <T extends ScreenHandler> @Nullable List<ItemStack> getStacks(StandardRecipeHandler<T> handler, EmiRecipe recipe, HandledScreen<T> screen, int amount) {
		try {
			T screenHandler = screen.getScreenHandler();
			if (handler != null) {
				List<Slot> slots = handler.getInputSources(screenHandler);
				List<Slot> craftingSlots = handler.getCraftingSlots(recipe, screenHandler);
				List<EmiIngredient> ingredients = recipe.getInputs();
				List<DiscoveredItem> discovered = Lists.newArrayList();
				Object2IntMap<EmiStack> weightDivider = new Object2IntOpenHashMap<>();
				for (int i = 0; i < ingredients.size(); i++) {
					List<DiscoveredItem> d = Lists.newArrayList();
					EmiIngredient ingredient = ingredients.get(i);
					List<EmiStack> emiStacks = ingredient.getEmiStacks();
					if (ingredient.isEmpty()) {
						discovered.add(null);
						continue;
					}
					for (int e = 0; e < emiStacks.size(); e++) {
						EmiStack stack = emiStacks.get(e);
						slotLoop:
						for (Slot s : slots) {
							ItemStack ss = s.getStack();
							if (EmiStack.of(s.getStack()).isEqual(stack)) {
								for (DiscoveredItem di : d) {
									if (ItemStack.areItemsAndComponentsEqual(ss, di.stack)) {
										di.amount += ss.getCount();
										continue slotLoop;
									}
								}
								d.add(new DiscoveredItem(stack, ss, ss.getCount(), (int) ingredient.getAmount(), ss.getMaxCount()));
							}
						}
					}
					DiscoveredItem biggest = null;
					for (DiscoveredItem di : d) {
						if (biggest == null) {
							biggest = di;
						} else {
							int a = di.amount / (weightDivider.getOrDefault(di.ingredient, 0) + di.consumed);
							int ba = biggest.amount / (weightDivider.getOrDefault(biggest.ingredient, 0) + biggest.consumed);
							if (ba < a) {
								biggest = di;
							}
						}
					}
					if (biggest == null || i >= craftingSlots.size()) {
						return null;
					}
					Slot slot = craftingSlots.get(i);
					if (slot == null) {
						return null;
					}
					weightDivider.put(biggest.ingredient, weightDivider.getOrDefault(biggest.ingredient, 0) + biggest.consumed);
					biggest.max = Math.min(biggest.max, slot.getMaxItemCount());
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
						if (ItemStack.areItemsAndComponentsEqual(di.stack, ui.stack)) {
							ui.consumed += di.consumed;
							continue outer;
						}
					}
					unique.add(new DiscoveredItem(di.ingredient, di.stack, di.amount, di.consumed, di.max));
				}
				int maxAmount = Integer.MAX_VALUE;
				for (DiscoveredItem ui : unique) {
					if (!ui.catalyst()) {
						maxAmount = Math.min(maxAmount, ui.amount / ui.consumed);
						maxAmount = Math.min(maxAmount, ui.max);
					}
				}
				maxAmount = Math.min(maxAmount, amount + batchesAlreadyPresent(recipe, handler, screen));

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

	public static <T extends ScreenHandler> int batchesAlreadyPresent(EmiRecipe recipe, StandardRecipeHandler<T> handler, HandledScreen<T> screen) {
		List<EmiIngredient> inputs = recipe.getInputs();
		List<ItemStack> stacks = Lists.newArrayList();
		Slot output = handler.getOutputSlot(screen.getScreenHandler());
		if (output != null && !output.getStack().isEmpty() && recipe.getOutputs().size() > 0
				&& !ItemStack.areEqual(output.getStack(), recipe.getOutputs().get(0).getItemStack())) {
			return 0;
		}
		for (Slot slot : handler.getCraftingSlots(recipe, screen.getScreenHandler())) {
			if (slot != null) {
				stacks.add(slot.getStack());
			} else {
				stacks.add(ItemStack.EMPTY);
			}
		}
		long amount = Long.MAX_VALUE;
		outer:
		for (int i = 0; i < inputs.size(); i++) {
			EmiIngredient input = inputs.get(i);
			if (input.isEmpty()) {
				if (stacks.get(i).isEmpty()) {
					continue;
				}
				return 0;
			}
			if (i >= stacks.size()) {
				return 0;
			}
			EmiStack es = EmiStack.of(stacks.get(i));
			for (EmiStack v : input.getEmiStacks()) {
				if (v.isEmpty()) {
					continue;
				}
				if (v.isEqual(es) && es.getAmount() >= v.getAmount()) {
					amount = Math.min(amount, es.getAmount() / v.getAmount());
					continue outer;
				}
			}
			return 0;
		}
		if (amount < Long.MAX_VALUE && amount > 0) {
			return (int) amount;
		}
		return 0;
	}

	public static <T extends ScreenHandler> boolean clientFill(StandardRecipeHandler<T> handler, EmiRecipe recipe,
			HandledScreen<T> screen, List<ItemStack> stacks, EmiCraftContext.Destination destination) {
		T screenHandler = screen.getScreenHandler();
		if (handler != null && screenHandler.getCursorStack().isEmpty()) {
			MinecraftClient client = MinecraftClient.getInstance();
			ClientPlayerInteractionManager manager = client.interactionManager;
			PlayerEntity player = client.player;
			List<Slot> clear = handler.getCraftingSlots(screenHandler);
			for (Slot slot : clear) {
				if (slot != null) {
					manager.clickSlot(screenHandler.syncId, slot.id, 0, SlotActionType.QUICK_MOVE, player);
				}
			}
			List<Slot> inputs = handler.getInputSources(screenHandler);
			List<Slot> slots = handler.getCraftingSlots(recipe, screenHandler);
			outer:
			for (int i = 0; i < stacks.size(); i++) {
				ItemStack stack = stacks.get(i);
				if (stack.isEmpty()) {
					continue;
				}
				if (i >= slots.size()) {
					return false;
				}
				Slot crafting = slots.get(i);
				if (crafting == null) {
					return false;
				}
				int needed = stack.getCount();
				for (Slot input : inputs) {
					if (slots.contains(input)) {
						continue;
					}
					ItemStack is = input.getStack().copy();
					if (ItemStack.areItemsAndComponentsEqual(is, stack)) {
						manager.clickSlot(screenHandler.syncId, input.id, 0, SlotActionType.PICKUP, player);
						if (is.getCount() <= needed) {
							needed -= is.getCount();
							manager.clickSlot(screenHandler.syncId, crafting.id, 0, SlotActionType.PICKUP, player);
						} else {
							while (needed > 0) {
								manager.clickSlot(screenHandler.syncId, crafting.id, 1, SlotActionType.PICKUP, player);
								needed--;
							}
							manager.clickSlot(screenHandler.syncId, input.id, 0, SlotActionType.PICKUP, player);
						}
					}
					if (needed == 0) {
						continue outer;
					}
				}
				return false;
			}
			Slot slot = handler.getOutputSlot(screenHandler);
			if (slot != null) {
				if (destination == EmiCraftContext.Destination.CURSOR) {
					manager.clickSlot(screenHandler.syncId, slot.id, 0, SlotActionType.PICKUP, player);
				} else if (destination == EmiCraftContext.Destination.INVENTORY) {
					manager.clickSlot(screenHandler.syncId, slot.id, 0, SlotActionType.QUICK_MOVE, player);
				}
			}
			return true;
		}
		return false;
	}

	private static class DiscoveredItem {
		private static final Comparison COMPARISON = Comparison.DEFAULT_COMPARISON;
		public EmiStack ingredient;
		public ItemStack stack;
		public int consumed;
		public int amount;
		public int max;

		public DiscoveredItem(EmiStack ingredient, ItemStack stack, int amount, int consumed, int max) {
			this.ingredient = ingredient;
			this.stack = stack.copy();
			this.amount = amount;
			this.consumed = consumed;
			this.max = max;
		}

		public boolean catalyst() {
			return ingredient.getRemainder().isEqual(ingredient, COMPARISON);
		}
	}
}
