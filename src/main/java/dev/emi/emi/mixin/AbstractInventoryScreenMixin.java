package dev.emi.emi.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import dev.emi.emi.EmiConfig;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.ScreenHandler;

@Mixin(AbstractInventoryScreen.class)
public abstract class AbstractInventoryScreenMixin<T extends ScreenHandler> extends HandledScreen<T> {
	
	private AbstractInventoryScreenMixin() { super(null, null, null); }
	
	@ModifyVariable(at = @At(value = "STORE", ordinal = 0),
		method = "drawStatusEffects", ordinal = 2)
	private int moveEffects(int original) {
		if (!EmiConfig.moveEffects) {
			return original;
		}
		boolean wide = this.x >= 122;
		if (wide) {
			return this.x - 122;
		} else {
			return this.x - 34;
		}
	}

	@ModifyVariable(at = @At(value = "STORE", ordinal = 0),
		method = "drawStatusEffects", ordinal = 3)
	private int changeEffectSpace(int original) {
		if (!EmiConfig.moveEffects) {
			return original;
		}
		return this.x - 2;
	}
}
