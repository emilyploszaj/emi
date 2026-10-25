package dev.emi.emi.mixin;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiUtil;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.search.EmiSearch;

@Mixin(value = ItemStack.class, priority = 500)
public class ItemStackMixin {
	
	@Inject(at = @At("RETURN"), method = "getTooltipLines")
	private void getTooltip(Item.TooltipContext context, @Nullable Player player, TooltipFlag type, CallbackInfoReturnable<List<Component>> info) {
		List<Component> text = info.getReturnValue();
		if (EmiConfig.appendItemModId && EmiConfig.appendModId && Thread.currentThread() != EmiSearch.searchThread && text != null && !text.isEmpty()) {
			String namespace = EmiPort.getItemRegistry().getKey(((ItemStack) (Object) this).getItem()).getNamespace();
			String mod = EmiUtil.getModName(namespace);
			text.add(EmiPort.literal(mod, ChatFormatting.BLUE, ChatFormatting.ITALIC));
		}
	}
}
