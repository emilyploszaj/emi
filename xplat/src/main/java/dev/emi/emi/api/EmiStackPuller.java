package dev.emi.emi.api;

import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;

public interface EmiStackPuller<T extends ScreenHandler> {

	/**
	 * Pull stacks matching the given stack into the player inventory until toPull is reached or items run out
	 * @param stacks All valid items for pulling, for tag favorites this will contain every item in the tag
	 * @param toPull The desired number of items to pull into the players inventory
	 * @return whether or not the pull has been successfully handled by this, `true` stops execution of further handlers
	 */
	public boolean pullStack(T screenHandler, List<ItemStack> stacks, long toPull);

}
