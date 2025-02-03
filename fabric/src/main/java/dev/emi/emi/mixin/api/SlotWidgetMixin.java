package dev.emi.emi.mixin.api;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import dev.emi.emi.api.widget.SlotWidget;
import net.minecraft.util.Identifier;

@Mixin(SlotWidget.class)
public abstract class SlotWidgetMixin {
	@Shadow(remap = false)
	public abstract SlotWidget large(boolean large);
	@Shadow
	public abstract SlotWidget customBackground(Identifier id, int u, int v, int width, int height);

	public SlotWidget output(boolean output) {
		return large(output);
	}

	public void custom(Identifier id, int u, int v, int width, int height) {
		customBackground(id, u, v, width, height);
	}
}
