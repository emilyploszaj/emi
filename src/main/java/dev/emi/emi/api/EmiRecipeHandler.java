package dev.emi.emi.api;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import dev.emi.emi.EmiClient;
import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiRecipeFiller;
import dev.emi.emi.api.recipe.EmiPlayerInventory;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.handler.EmiCraftContext;
import dev.emi.emi.api.recipe.handler.StandardRecipeHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;

/**
 * @deprecated Migrate to {@link StandardRecipeHandler}
 */
@Deprecated
public interface EmiRecipeHandler<T extends ScreenHandler> extends StandardRecipeHandler<T> {
	public static final Text NOT_ENOUGH_INGREDIENTS = EmiPort.translatable("emi.not_enough_ingredients");
	
	/**
	 * @return The slots for the recipe handler to source ingredients from.
	 * Typically this should include the player's inventory, and crafting slots.
	 */
	List<Slot> getInputSources(T handler);

	default List<Slot> getInputSources(HandledScreen<T> screen) {
		return getInputSources(screen.getScreenHandler());
	}

	/**
	 * @return The slots where inputs should be placed to perform crafting.
	 */
	List<Slot> getCraftingSlots(T handler);

	default List<Slot> getCraftingSlots(HandledScreen<T> screen) {
		return getCraftingSlots(screen.getScreenHandler());
	}

	/**
	 * @return The slots where inputs should be placed to perform crafting for a particular context.
	 */
	default List<Slot> getCraftingSlots(EmiRecipe recipe, HandledScreen<T> screen) {
		return getCraftingSlots(screen.getScreenHandler());
	}
	
	@SuppressWarnings("unchecked")
	default List<Slot> getCraftingSlots(EmiRecipe recipe, T handler) {
		if (EmiApi.getHandledScreen() instanceof HandledScreen<?> hs && hs.getScreenHandler() == handler) {
			return getCraftingSlots(recipe, (HandledScreen<T>) hs);
		}
		return List.of();
	}

	/**
	 * @return The output slot for recipe handlers that support instant interaction, like crafting tables.
	 * For handlers that have processing time, or where this concept is otherwise inapplicable, null.
	 */
	default @Nullable Slot getOutputSlot(T handler) {
		return null;
	}

	/**
	 * @return Whether this handler is applicable for the provided recipe.
	 */
	boolean supportsRecipe(EmiRecipe recipe);

	/**
	 * @return Whether the provided recipe is only conscious of the handler when the handler is applicable.
	 * Effectively, whether to not display the plus fill button outside of applicable screens.
	 */
	default boolean onlyDisplayWhenApplicable(EmiRecipe recipe) {
		return false;
	}

	@Override
	default boolean alwaysDisplaySupport(EmiRecipe recipe) {
		return !onlyDisplayWhenApplicable(recipe);
	}

	@Override
	default boolean canCraft(EmiRecipe recipe, EmiCraftContext<T> context) {
		return canCraft(recipe, context.getInventory(), context.getScreen());
	}

	default boolean canCraft(EmiRecipe recipe, EmiPlayerInventory inventory, HandledScreen<T> screen) {
		return inventory.canCraft(recipe);
	}

	default Text getInvalidReason(EmiRecipe recipe, EmiPlayerInventory inventory, HandledScreen<T> screen) {
		return NOT_ENOUGH_INGREDIENTS;
	}

	default boolean performFill(EmiRecipe recipe, HandledScreen<T> screen, EmiFillAction action, int amount) {
		List<ItemStack> stacks = EmiRecipeFiller.getStacks(this, recipe, screen, amount);
		if (stacks != null) {
			stacks = mutateFill(recipe, screen, stacks);
			if (stacks != null) {
				MinecraftClient.getInstance().setScreen(screen);
				if (!EmiClient.onServer) {
					return EmiRecipeFiller.clientFill(this, recipe, screen, stacks, switch (action) {
						case FILL -> EmiCraftContext.Destination.NONE;
						case QUICK_MOVE -> EmiCraftContext.Destination.INVENTORY;
						case CURSOR -> EmiCraftContext.Destination.CURSOR;
					});
				} else {
					EmiClient.sendFillRecipe(this, screen, screen.getScreenHandler().syncId, action.id, stacks, recipe);
				}
				return true;
			}
		}
		return false;
	}

	@Override
	default boolean craft(EmiRecipe recipe, EmiCraftContext<T> context) {
		return performFill(recipe, context.getScreen(), switch (context.getDestination()) {
			case NONE -> EmiFillAction.FILL;
			case INVENTORY -> EmiFillAction.QUICK_MOVE;
			case CURSOR -> EmiFillAction.CURSOR;
		}, context.getAmount());
	}

	@Deprecated
	default List<ItemStack> mutateFill(EmiRecipe recipe, HandledScreen<T> screen, List<ItemStack> stacks) {
		return stacks;
	}
}
