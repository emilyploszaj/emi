package dev.emi.emi.handler;

import java.util.List;

import com.google.common.collect.Lists;

import org.jetbrains.annotations.Nullable;

import dev.emi.emi.api.EmiRecipeHandler;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.slot.Slot;

public class InventoryRecipeHandler implements EmiRecipeHandler<PlayerScreenHandler> {

	@Override
	public List<Slot> getInputSources(PlayerScreenHandler handler) {
		List<Slot> list = Lists.newArrayList();
		for (int i = 1; i < 5; i++) { 
			list.add(handler.getSlot(i));
		}
		int invStart = 9;
		for (int i = invStart; i < invStart + 36; i++) { 
			list.add(handler.getSlot(i));
		}
		return list;
	}
	
	@Override
	public List<Slot> getCraftingSlots(PlayerScreenHandler handler) {
		List<Slot> list = Lists.newArrayList();
		for (int i = 1; i < 5; i++) { 
			list.add(handler.getSlot(i));
		}
		return list;
	}

	@Override
	public @Nullable Slot getOutputSlot(PlayerScreenHandler handler) {
		return handler.slots.get(0);
	}
}
