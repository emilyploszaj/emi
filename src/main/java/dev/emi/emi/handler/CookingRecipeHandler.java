package dev.emi.emi.handler;

import java.util.List;

import com.google.common.collect.Lists;

import dev.emi.emi.api.EmiRecipeHandler;
import net.minecraft.screen.AbstractFurnaceScreenHandler;
import net.minecraft.screen.slot.Slot;

public class CookingRecipeHandler implements EmiRecipeHandler<AbstractFurnaceScreenHandler> {

	@Override
	public List<Slot> getInputSources(AbstractFurnaceScreenHandler handler) {
		List<Slot> list = Lists.newArrayList();
		list.add(handler.getSlot(0));
		int invStart = 3;
		for (int i = invStart; i < invStart + 36; i++) { 
			list.add(handler.getSlot(i));
		}
		return list;
	}
	
	@Override
	public List<Slot> getCraftingSlots(AbstractFurnaceScreenHandler handler) {
		return List.of(handler.slots.get(0));
	}
}
