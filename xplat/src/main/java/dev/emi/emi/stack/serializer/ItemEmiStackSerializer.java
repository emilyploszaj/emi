package dev.emi.emi.stack.serializer;

import dev.emi.emi.EmiPort;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.ItemEmiStack;
import dev.emi.emi.api.stack.serializer.EmiStackSerializer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

public class ItemEmiStackSerializer implements EmiStackSerializer<ItemEmiStack> {

	@Override
	public String getType() {
		return "item";
	}

	@Override
	public EmiStack create(Identifier id, NbtCompound nbt, long amount) {
		ItemStack stack = new ItemStack(EmiPort.getItemRegistry().get(id));
		stack.setNbt(nbt);
		return EmiStack.of(stack, amount);
	}
}
