package dev.emi.emi.mixin.conversion;

import org.spongepowered.asm.mixin.Mixin;

import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.EmiStackConvertible;
import net.minecraft.item.ItemStack;

@Mixin(ItemStack.class)
public class ItemStackMixin implements EmiStackConvertible {

	@Override
	public EmiStack emi() {
		return EmiStack.of((ItemStack) (Object) this);
	}

	@Override
	public EmiStack emi(long amount) {
		return EmiStack.of((ItemStack) (Object) this, amount);
	}
}
