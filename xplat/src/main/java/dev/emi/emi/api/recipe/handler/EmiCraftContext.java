package dev.emi.emi.api.recipe.handler;

import org.jetbrains.annotations.ApiStatus;

import dev.emi.emi.api.recipe.EmiPlayerInventory;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.ScreenHandler;

public class EmiCraftContext<T extends ScreenHandler> {
	private final HandledScreen<T> screen;
	private final EmiPlayerInventory inventory;
	private final Type type;
	private final Destination destination;
	private final int amount;

	@ApiStatus.Internal
	public EmiCraftContext(HandledScreen<T> screen, EmiPlayerInventory inventory, Type type, Destination destination, int amount) {
		this.screen = screen;
		this.inventory = inventory;
		this.type = type;
		this.destination = destination;
		this.amount = amount;
	}

	@ApiStatus.Internal
	public EmiCraftContext(HandledScreen<T> screen, EmiPlayerInventory inventory, Type type) {
		this(screen, inventory, type, Destination.NONE, 1);
	}

	public HandledScreen<T> getScreen() {
		return screen;
	}

	public T getScreenHandler() {
		return screen.getScreenHandler();
	}

	public EmiPlayerInventory getInventory() {
		return inventory;
	}

	public Type getType() {
		return type;
	}

	public Destination getDestination() {
		return destination;
	}

	public int getAmount() {
		return amount;
	}
	
	public static enum Type {
		/**
		 * A fill from the recipe screen's fill button
		 */
		FILL_BUTTON,
		/**
		 * A fill from a stack in a sidebar with recipe context
		 */
		CRAFTABLE
	}

	public static enum Destination {
		/**
		 * No output, simply move the ingredients to where they belong.
		 */
		NONE,
		/**
		 * If possible, immediately craft the recipe and move it into the player's cursor.
		 * If this action is not supported, this should do nothing more than FILL.
		 */
		CURSOR,
		/**
		 * If possible, immediately craft the recipe and move it into the player's inventory.
		 * If this action is not supported, this should do nothing more than FILL.
		 */
		INVENTORY,
	}
}
