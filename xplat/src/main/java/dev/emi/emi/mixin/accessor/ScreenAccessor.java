package dev.emi.emi.mixin.accessor;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.gui.tooltip.TooltipPositioner;
import net.minecraft.client.util.math.MatrixStack;

@Mixin(Screen.class)
public interface ScreenAccessor {
	
	@Invoker("renderTooltipFromComponents")
	void invokeRenderTooltipFromComponents(MatrixStack matrices, List<TooltipComponent> components, int x, int y, TooltipPositioner positioner);

	/*
	@Invoker("method_32635")
	static void emi$addTooltipComponent(List<TooltipComponent> components, TooltipData data) {
		throw new AbstractMethodError();
	}*/
}
