package dev.emi.emi;

import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.recipe.EmiCookingRecipe;
import dev.emi.emi.recipe.EmiShapedRecipe;
import dev.emi.emi.recipe.EmiShapelessRecipe;
import dev.emi.emi.recipe.EmiSmithingRecipe;
import dev.emi.emi.recipe.EmiStonecuttingRecipe;
import dev.emi.emi.recipe.EmiTagRecipe;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.recipe.BlastingRecipe;
import net.minecraft.recipe.CampfireCookingRecipe;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.recipe.ShapelessRecipe;
import net.minecraft.recipe.SmeltingRecipe;
import net.minecraft.recipe.SmithingRecipe;
import net.minecraft.recipe.SmokingRecipe;
import net.minecraft.recipe.StonecuttingRecipe;
import net.minecraft.tag.ItemTags;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;

public class VanillaPlugin implements EmiPlugin {
	public static EmiRecipeCategory CRAFTING = new EmiRecipeCategory(new Identifier("minecraft:crafting"),
		EmiStack.of(Items.CRAFTING_TABLE));
	public static EmiRecipeCategory SMELTING = new EmiRecipeCategory(new Identifier("minecraft:smelting"),
		EmiStack.of(Items.FURNACE));
	public static EmiRecipeCategory BLASTING = new EmiRecipeCategory(new Identifier("minecraft:blasting"),
		EmiStack.of(Items.BLAST_FURNACE));
	public static EmiRecipeCategory SMOKING = new EmiRecipeCategory(new Identifier("minecraft:smoking"),
		EmiStack.of(Items.SMOKER));
	public static EmiRecipeCategory CAMPFIRE_COOKING = new EmiRecipeCategory(new Identifier("minecraft:campfire_cooking"),
		EmiStack.of(Items.CAMPFIRE));
	public static EmiRecipeCategory STONECUTTING = new EmiRecipeCategory(new Identifier("minecraft:stonecutting"),
		EmiStack.of(Items.STONECUTTER));
	public static EmiRecipeCategory SMITHING = new EmiRecipeCategory(new Identifier("minecraft:smithing"),
		EmiStack.of(Items.SMITHING_TABLE));
	public static EmiRecipeCategory TAG = new EmiRecipeCategory(new Identifier("minecraft:tag"),
		EmiStack.of(Items.NAME_TAG));

	// brewing, composting, fuel, anvil repairing
	// beacon stuff? world processing?
	
	// Synthetic
	public static EmiRecipeCategory INGREDIENT = new EmiRecipeCategory(new Identifier("emi:ingredient"),
		EmiStack.of(Items.COMPASS));

	@Override
	public void register(EmiRegistry registry) {
		registry.addCategory(CRAFTING);
		registry.addCategory(SMELTING);
		registry.addCategory(BLASTING);
		registry.addCategory(SMOKING);
		registry.addCategory(CAMPFIRE_COOKING);
		registry.addCategory(STONECUTTING);
		registry.addCategory(SMITHING);
		registry.addCategory(TAG);
		registry.addCategory(INGREDIENT);

		registry.addWorkstation(CRAFTING, CRAFTING.icon);
		registry.addWorkstation(SMELTING, SMELTING.icon);
		registry.addWorkstation(BLASTING, BLASTING.icon);
		registry.addWorkstation(SMOKING, SMOKING.icon);
		registry.addWorkstation(CAMPFIRE_COOKING, CAMPFIRE_COOKING.icon);
		registry.addWorkstation(STONECUTTING, STONECUTTING.icon);
		registry.addWorkstation(SMITHING, SMITHING.icon);

		registry.addRecipeHandler(CRAFTING, EmiMain.CRAFTING);
		registry.addRecipeHandler(CRAFTING, EmiMain.INVENTORY);
		registry.addRecipeHandler(SMELTING, EmiMain.COOKING);
		registry.addRecipeHandler(BLASTING, EmiMain.COOKING);
		registry.addRecipeHandler(SMOKING, EmiMain.COOKING);

		for (CraftingRecipe recipe : registry.getRecipeManager().listAllOfType(RecipeType.CRAFTING)) {
			if (recipe instanceof ShapedRecipe shaped) {
				registry.addRecipe(new EmiShapedRecipe(shaped));
			} else if (recipe instanceof ShapelessRecipe shapeless) {
				registry.addRecipe(new EmiShapelessRecipe(shapeless));
			}
		}
		for (SmeltingRecipe recipe : registry.getRecipeManager().listAllOfType(RecipeType.SMELTING)) {
			registry.addRecipe(new EmiCookingRecipe(recipe, SMELTING, 1, false));
		}
		for (BlastingRecipe recipe : registry.getRecipeManager().listAllOfType(RecipeType.BLASTING)) {
			registry.addRecipe(new EmiCookingRecipe(recipe, BLASTING, 2, false));
		}
		for (SmokingRecipe recipe : registry.getRecipeManager().listAllOfType(RecipeType.SMOKING)) {
			registry.addRecipe(new EmiCookingRecipe(recipe, SMOKING, 2, false));
		}
		for (CampfireCookingRecipe recipe : registry.getRecipeManager().listAllOfType(RecipeType.CAMPFIRE_COOKING)) {
			registry.addRecipe(new EmiCookingRecipe(recipe, CAMPFIRE_COOKING, 1, true));
		}
		for (SmithingRecipe recipe : registry.getRecipeManager().listAllOfType(RecipeType.SMITHING)) {
			registry.addRecipe(new EmiSmithingRecipe(recipe));
		}
		for (StonecuttingRecipe recipe : registry.getRecipeManager().listAllOfType(RecipeType.STONECUTTING)) {
			registry.addRecipe(new EmiStonecuttingRecipe(recipe));
		}
		for (Tag<Item> tag : ItemTags.getTagGroup().getTags().values()) {
			registry.addRecipe(new EmiTagRecipe(tag));
		}
	}
}