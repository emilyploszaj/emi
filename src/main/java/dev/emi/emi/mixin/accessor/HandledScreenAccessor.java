package dev.emi.emi.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;

@Mixin(HandledScreen.class)
public interface HandledScreenAccessor {
	
	@Accessor("focusedSlot")
	Slot getFocusedSlot();

	@Accessor("x")
	int getX();

	@Accessor("y")
	int getY();

	@Accessor("backgroundWidth")
	int getBackgroundWidth();
}
