package dev.emi.emi.mixin.conversion;

import org.spongepowered.asm.mixin.Mixin;

import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.EmiStackConvertible;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;

@Mixin(ItemConvertible.class)
public interface ItemConvertibleMixin extends EmiStackConvertible {

	@Override
	default EmiStack emi() {
		return EmiStack.of((Item) (Object) this);
	}

	@Override
	default EmiStack emi(long amount) {
		return EmiStack.of((Item) (Object) this, amount);
	}
}
