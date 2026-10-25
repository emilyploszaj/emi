package dev.emi.emi.stack.serializer;

import dev.emi.emi.EmiPort;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.ItemEmiStack;
import dev.emi.emi.api.stack.serializer.EmiStackSerializer;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class ItemEmiStackSerializer implements EmiStackSerializer<ItemEmiStack> {

	@Override
	public String getType() {
		return "item";
	}

	@Override
	public EmiStack create(ResourceLocation id, DataComponentPatch componentChanges, long amount) {
		ItemStack stack = new ItemStack(EmiPort.getItemRegistry().getHolder(id).orElseThrow(), 1, componentChanges);
		return EmiStack.of(stack, amount);
	}
}
