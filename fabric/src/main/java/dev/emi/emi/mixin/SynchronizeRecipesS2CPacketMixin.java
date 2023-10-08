package dev.emi.emi.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import dev.emi.emi.runtime.EmiLog;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.SynchronizeRecipesS2CPacket;
import net.minecraft.recipe.Recipe;
import net.minecraft.util.Identifier;

@Mixin(SynchronizeRecipesS2CPacket.class)
public class SynchronizeRecipesS2CPacketMixin {
	
	@Inject(at = @At("RETURN"), method = "readRecipe", locals = LocalCapture.CAPTURE_FAILHARD)
	private static void readRecipe(PacketByteBuf buf, CallbackInfoReturnable<Recipe<?>> info, Identifier serializer, Identifier id) {
		Recipe<?> recipe = info.getReturnValue();
		if (recipe == null) {
			EmiLog.error("Recipe " + id + " was deserialized as null using recipe serializer " + serializer
				+ ", this is breaking vanilla recipe sync, and will prevent EMI from loading recipes");
		}
	}
}
