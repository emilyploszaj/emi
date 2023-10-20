package dev.emi.emi.api.recipe.handler;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.systems.RenderSystem;

import dev.emi.emi.api.recipe.EmiPlayerInventory;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.Widget;
import dev.emi.emi.platform.EmiClient;
import dev.emi.emi.registry.EmiRecipeFiller;
import dev.emi.emi.runtime.EmiDrawContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

public interface StandardRecipeHandler<T extends ScreenHandler> extends EmiRecipeHandler<T> {
	
	/**
	 * @return The slots for the recipe handler to source ingredients from.
	 * Typically this should include the player's inventory, and crafting slots.
	 */
	List<Slot> getInputSources(T handler);

	/**
	 * @return The slots where inputs should be placed to perform crafting.
	 */
	List<Slot> getCraftingSlots(T handler);

	/**
	 * @return The slots where inputs should be placed to perform crafting for a particular context.
	 */
	default List<Slot> getCraftingSlots(EmiRecipe recipe, T handler) {
		return getCraftingSlots(handler);
	}

	/**
	 * @return The output slot for recipe handlers that support instant interaction, like crafting tables.
	 * For handlers that have processing time, or where this concept is otherwise inapplicable, null.
	 */
	default @Nullable Slot getOutputSlot(T handler) {
		return null;
	}

	@Override
	default EmiPlayerInventory getInventory(HandledScreen<T> screen) {
		return new EmiPlayerInventory(getInputSources(screen.getScreenHandler()).stream().map(Slot::getStack).map(EmiStack::of).toList());
	}

	@Override
	default boolean canCraft(EmiRecipe recipe, EmiCraftContext<T> context) {
		return context.getInventory().canCraft(recipe);
	}

	@Override
	default boolean craft(EmiRecipe recipe, EmiCraftContext<T> context) {
		List<ItemStack> stacks = EmiRecipeFiller.getStacks(this, recipe, context.getScreen(), context.getAmount());
		if (stacks != null) {
			if (stacks != null) {
				MinecraftClient.getInstance().setScreen(context.getScreen());
				if (!EmiClient.onServer) {
					return EmiRecipeFiller.clientFill(this, recipe, context.getScreen(), stacks, context.getDestination());
				} else {
					EmiClient.sendFillRecipe(this, context.getScreen(), context.getScreenHandler().syncId, switch(context.getDestination()) {
						case NONE -> 0;
						case CURSOR -> 1;
						case INVENTORY -> 2;
					}, stacks, recipe);
				}
				return true;
			}
		}
		return false;
	}

	@Override
	default void render(EmiRecipe recipe, EmiCraftContext<T> context, List<Widget> widgets, DrawContext draw) {
		renderMissing(recipe, context.getInventory(), widgets, draw);
	}

	@ApiStatus.Internal
	public static void renderMissing(EmiRecipe recipe, EmiPlayerInventory inv, List<Widget> widgets, DrawContext draw) {
		EmiDrawContext context = EmiDrawContext.wrap(draw);
		RenderSystem.enableDepthTest();
		Map<EmiIngredient, Boolean> availableForCrafting = getAvailable(recipe, inv);
		for (Widget w : widgets) {
			if (w instanceof SlotWidget sw) {
				EmiIngredient stack = sw.getStack();
				Bounds bounds = sw.getBounds();
				if (sw.getRecipe() == null && availableForCrafting.containsKey(stack) && !stack.isEmpty()) {
					if (availableForCrafting.get(stack)) {
						//context.fill(bounds.x(), bounds.y(), bounds.width(), bounds.height(), 0x4400FF00);
					} else {
						context.fill(bounds.x(), bounds.y(), bounds.width(), bounds.height(), 0x44FF0000);
					}
				}
			}
		}
	}
	
	private static Map<EmiIngredient, Boolean> getAvailable(EmiRecipe recipe, EmiPlayerInventory inventory) {
		Map<EmiIngredient, Boolean> availableForCrafting = new IdentityHashMap<>();
		List<Boolean> list = inventory.getCraftAvailability(recipe);
		var inputs = recipe.getInputs();
		if (list.size() != inputs.size()) {
			return Map.of();
		}
		for (int i = 0; i < list.size(); i++) {
			availableForCrafting.put(inputs.get(i), list.get(i));
		}
		return availableForCrafting;
	}
}
