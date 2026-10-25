package dev.emi.emi.mixin.accessor;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractContainerScreen.class)
public interface HandledScreenAccessor {
	
	@Accessor("hoveredSlot")
	Slot getFocusedSlot();

	@Accessor("leftPos")
	int getX();

	@Accessor("topPos")
	int getY();

	@Accessor("imageWidth")
	int getBackgroundWidth();

	@Accessor("imageHeight")
	int getBackgroundHeight();

	@Invoker("getHoveredSlot")
	Slot invokeGetSlotAt(double x, double y);
}
