package dev.emi.emi.handler;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;

import dev.emi.emi.EmiPort;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.recipe.handler.EmiCraftContext;
import dev.emi.emi.api.recipe.handler.StandardRecipeHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.StonecuttingRecipe;
import net.minecraft.recipe.input.SingleStackRecipeInput;
import net.minecraft.screen.StonecutterScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.world.World;

public class StonecuttingRecipeHandler implements StandardRecipeHandler<StonecutterScreenHandler> {

	@Override
	public List<Slot> getInputSources(StonecutterScreenHandler handler) {
		List<Slot> list = Lists.newArrayList();
		list.add(handler.getSlot(0));
		int invStart = 2;
		for (int i = invStart; i < invStart + 36; i++) { 
			list.add(handler.getSlot(i));
		}
		return list;
	}

	@Override
	public List<Slot> getCraftingSlots(StonecutterScreenHandler handler) {
		return List.of(handler.slots.get(0));
	}

	@Override
	public boolean supportsRecipe(EmiRecipe recipe) {
		return recipe.getCategory() == VanillaEmiRecipeCategories.STONECUTTING;
	}

	@Override
	public @Nullable Slot getOutputSlot(StonecutterScreenHandler handler) {
		return handler.getSlot(1);
	}

	@Override
	public boolean craft(EmiRecipe recipe, EmiCraftContext<StonecutterScreenHandler> context) {
		boolean action = StandardRecipeHandler.super.craft(recipe, context);
		MinecraftClient client = MinecraftClient.getInstance();
		World world = client.world;
		SingleStackRecipeInput inv = new SingleStackRecipeInput(recipe.getInputs().get(0).getEmiStacks().get(0).getItemStack());
		List<StonecuttingRecipe> recipes = world.getRecipeManager().getAllMatches(RecipeType.STONECUTTING, inv, world).stream().map(RecipeEntry::value).toList();
		for (int i = 0; i < recipes.size(); i++) {
			if (EmiPort.getId(recipes.get(i)) != null && EmiPort.getId(recipes.get(i)).equals(recipe.getId())) {
				StonecutterScreenHandler sh = context.getScreenHandler();
				client.interactionManager.clickButton(sh.syncId, i);
				if (context.getDestination() == EmiCraftContext.Destination.CURSOR) {
					client.interactionManager.clickSlot(sh.syncId, 1, 0, SlotActionType.PICKUP, client.player);
				} else if (context.getDestination() == EmiCraftContext.Destination.INVENTORY) {
					client.interactionManager.clickSlot(sh.syncId, 1, 0, SlotActionType.QUICK_MOVE, client.player);
				}
				break;
			}
		}
		return action;
	}
}
