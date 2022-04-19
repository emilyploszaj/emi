package dev.emi.emi.recipe;

import java.util.List;

import org.apache.commons.compress.utils.Lists;

import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.VanillaPlugin;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.screen.AbstractRecipeScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.Identifier;

public class EmiShapedRecipe implements EmiRecipe {
	private final List<EmiIngredient> input;
	private final EmiStack output;
	private final ShapedRecipe recipe;
	
	public EmiShapedRecipe(ShapedRecipe recipe) {
		input = recipe.getIngredients().stream().map(i -> EmiIngredient.of(i)).toList();
		for (int i = 0; i < input.size(); i++) {
			CraftingInventory inv = new CraftingInventory(new ScreenHandler(null, -1) {

				@Override
				public boolean canUse(PlayerEntity player) {
					return false;
				}
			}, recipe.getWidth(), recipe.getHeight());
			for (int j = 0; j < input.size(); j++) {
				if (j == i) {
					continue;
				}
				if (!input.get(j).isEmpty()) {
					inv.setStack(j, input.get(j).getEmiStacks().get(0).getItemStack().copy());
				}
			}
			List<EmiStack> stacks = input.get(i).getEmiStacks();
			for (EmiStack stack : stacks) {
				inv.setStack(i, stack.getItemStack().copy());
				ItemStack remainder = recipe.getRemainder(inv).get(i);
				if (!remainder.isEmpty()) {
					stack.setRemainder(EmiStack.of(remainder));
				}
			}
		}
		output = EmiStack.of(recipe.getOutput());
		this.recipe = recipe;
	}

	@Override
	public EmiRecipeCategory getCategory() {
		return VanillaPlugin.CRAFTING;
	}

	@Override
	public Identifier getId() {
		return recipe.getId();
	}

	@Override
	public List<EmiIngredient> getInputs() {
		return input;
	}

	@Override
	public List<EmiStack> getOutputs() {
		return List.of(output);
	}

	@Override
	public int getDisplayWidth() {
		return 118;
	}

	@Override
	public int getDisplayHeight() {
		return 54;
	}

	@Override
	public void addWidgets(WidgetHolder widgets) {
		int w = recipe.getWidth();
		int h = recipe.getHeight();
		widgets.addTexture(EmiRenderHelper.WIDGETS, 60, 18, 24, 17, 44, 0);
		for (int i = 0; i < input.size(); i++) {
			widgets.addSlot(input.get(i), i % w * 18, i / w * 18);
		}
		for (int sx = 0; sx < 3; sx++) {
			for (int sy = 0; sy < 3; sy++) {
				if (sx >= w || sy >= h) {
					widgets.addSlot(EmiStack.of(ItemStack.EMPTY), sx * 18, sy * 18);
				}
			}
		}
		widgets.addSlot(output, 92, 14).output(true).recipeContext(this);
	}

	@Override
	public boolean canFill(HandledScreen<?> hs) {
		ScreenHandler sh = hs.getScreenHandler();
		if (sh instanceof AbstractRecipeScreenHandler<?> arsh) {
			return recipe.getWidth() <= arsh.getCraftingWidth() && recipe.getHeight() <= arsh.getCraftingHeight()
				&& EmiRecipe.super.canFill(hs);
		}
		return false;
	}

	@Override
	public List<ItemStack> getFill(HandledScreen<?> hs, boolean all) {
		List<ItemStack> list = EmiRecipe.super.getFill(hs, all);

		ScreenHandler sh = hs.getScreenHandler();
		if (list != null && sh instanceof AbstractRecipeScreenHandler<?> arsh) {
			int w = recipe.getWidth();
			int h = recipe.getHeight();
			
			List<ItemStack> mut = Lists.newArrayList();
	
			int i = 0;
			for (int sy = 0; sy < arsh.getCraftingHeight(); sy++) {
				for (int sx = 0; sx < arsh.getCraftingWidth(); sx++) {
					if (sx >= w || sy >= h) {
						mut.add(ItemStack.EMPTY);
					} else {
						mut.add(list.get(i++));
					}
				}
			}
			return mut;
		}
		return list;
	}
}
