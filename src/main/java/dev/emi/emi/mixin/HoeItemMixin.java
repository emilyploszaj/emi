package dev.emi.emi.mixin;

import java.util.List;
import java.util.function.Consumer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.emi.emi.EmiClient;
import net.minecraft.block.BlockState;
import net.minecraft.item.HoeItem;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemUsageContext;

@Mixin(HoeItem.class)
public class HoeItemMixin {

	@Inject(at = @At("RETURN"), method = "createTillAction")
	private static void createTillAction(BlockState result, CallbackInfoReturnable<Consumer<ItemUsageContext>> info) {
		EmiClient.HOE_ACTIONS.put(info.getReturnValue(), List.of(result.getBlock()));
	}

	@Inject(at = @At("RETURN"), method = "createTillAndDropAction")
	private static void createTillAndDropAction(BlockState result, ItemConvertible droppedItem,
			CallbackInfoReturnable<Consumer<ItemUsageContext>> info) {
		EmiClient.HOE_ACTIONS.put(info.getReturnValue(), List.of(droppedItem, result.getBlock()));
	}
}
