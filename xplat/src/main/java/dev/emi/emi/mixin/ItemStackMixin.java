package dev.emi.emi.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiUtil;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.search.EmiSearch;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

@Mixin(value = ItemStack.class, priority = 500)
public class ItemStackMixin {
	
	@Inject(at = @At("RETURN"), method = "getTooltip")
	private void getTooltip(PlayerEntity player, TooltipContext context, CallbackInfoReturnable<List<Text>> info) {
		List<Text> text = info.getReturnValue();
		if (EmiConfig.appendItemModId && EmiConfig.appendModId && Thread.currentThread() != EmiSearch.searchThread && text != null && !text.isEmpty()) {
			String namespace = EmiPort.getItemRegistry().getId(((ItemStack) (Object) this).getItem()).getNamespace();
			String mod = EmiUtil.getModName(namespace);
			text.add(EmiPort.literal(mod, Formatting.BLUE, Formatting.ITALIC));
		}
	}
}
