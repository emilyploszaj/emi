package dev.emi.emi.mixin;

import java.util.Optional;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.runtime.EmiSidebars;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.screen.slot.CraftingResultSlot;
import net.minecraft.world.World;

@Mixin(CraftingResultSlot.class)
public class CraftingResultSlotMixin {
	@Shadow @Final
	private RecipeInputInventory input;
	@Shadow @Final
	private PlayerEntity player;
	
	@Inject(at = @At("HEAD"), method = "onCrafted(Lnet/minecraft/item/ItemStack;)V")
	private void onCrafted(ItemStack stack, CallbackInfo info) {
		World world = player.getWorld();
		if (world.isClient) {
			Optional<CraftingRecipe> opt = world.getRecipeManager().getFirstMatch(RecipeType.CRAFTING, input, world);
			if (opt.isPresent()) {
				EmiRecipe recipe = EmiApi.getRecipeManager().getRecipe(opt.get().getId());
				if (recipe != null) {
					EmiSidebars.craft(recipe);
				}
			}
		}
	}
}
