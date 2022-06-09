package dev.emi.emi.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.emi.emi.EmiConfig;
import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiUtil;
import dev.emi.emi.search.EmiSearch;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.registry.Registry;

@Mixin(ItemStack.class)
public class ItemStackMixin {
	
	@Inject(at = @At("RETURN"), method = "getTooltip")
	private void getTooltip(PlayerEntity player, TooltipContext context, CallbackInfoReturnable<List<Text>> info) {
		if (EmiConfig.appendItemModId && Thread.currentThread() != EmiSearch.thread) {
			List<Text> text = info.getReturnValue();
			String namespace = Registry.ITEM.getId(((ItemStack) (Object) this).getItem()).getNamespace();
			String mod = EmiUtil.getModName(namespace);
			text.add(EmiPort.literal(mod).formatted(Formatting.BLUE, Formatting.ITALIC));
		}
	}
}
