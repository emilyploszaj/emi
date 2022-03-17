package dev.emi.emi.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;

@Mixin(ScreenHandler.class)
public interface ScreenHandlerAccessor {
	
	@Accessor("type")
	ScreenHandlerType<?> emi$getType();
}
