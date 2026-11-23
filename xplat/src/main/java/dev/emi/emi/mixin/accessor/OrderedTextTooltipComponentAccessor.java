package dev.emi.emi.mixin.accessor;

import net.minecraft.client.gui.screens.inventory.tooltip.ClientTextTooltip;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientTextTooltip.class)
public interface OrderedTextTooltipComponentAccessor {
	
	@Accessor("text")
    public FormattedCharSequence getText();
}
