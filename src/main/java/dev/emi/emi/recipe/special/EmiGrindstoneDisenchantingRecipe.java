package dev.emi.emi.recipe.special;

import java.util.List;
import java.util.Random;

import org.apache.commons.compress.utils.Lists;

import dev.emi.emi.EmiUtil;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class EmiGrindstoneDisenchantingRecipe implements EmiRecipe {
	private static final Identifier BACKGROUND = new Identifier("minecraft", "textures/gui/container/grindstone.png");
	private final int uniq = EmiUtil.RANDOM.nextInt();
	private final Item tool;

	public EmiGrindstoneDisenchantingRecipe(Item tool) {
		this.tool = tool;
	}

	@Override
	public EmiRecipeCategory getCategory() {
		return VanillaEmiRecipeCategories.GRINDING;
	}

	@Override
	public Identifier getId() {
		return null;
	}

	@Override
	public List<EmiIngredient> getInputs() {
		return List.of(EmiStack.of(tool));
	}

	@Override
	public List<EmiStack> getOutputs() {
		return List.of(EmiStack.of(tool));
	}

	@Override
	public boolean supportsRecipeTree() {
		return false;
	}

	@Override
	public int getDisplayWidth() {
		return 130;
	}

	@Override
	public int getDisplayHeight() {
		return 61;
	}

	@Override
	public void addWidgets(WidgetHolder widgets) {
		widgets.addTexture(BACKGROUND, 0, 0, 130, 61, 16, 14);

		int notUniq= uniq;
		widgets.addGeneratedSlot(r -> getTool(r, true), notUniq,32, 4).drawBack(false);
		widgets.addGeneratedSlot(r -> getTool(r, false), notUniq, 112, 19).drawBack(false).recipeContext(this);
	}

	private EmiStack getTool(Random random, Boolean enchanted){
		ItemStack itemStack = new ItemStack(tool);
		int enchantments = 1 + Math.max(random.nextInt(5), random.nextInt(3));

		List<Enchantment> list = Lists.newArrayList();

		outer:
		for (int i = 0; i < enchantments; i++) {
			Enchantment enchantment = getEnchantment(random);

			int maxLvl = enchantment.getMaxLevel();
			int minLvl = enchantment.getMinLevel();
			int lvl = random.nextInt(maxLvl) + 1;

			if (lvl < minLvl) {
				lvl = minLvl;
			}
			
			for (Enchantment e : list) {
				if (e == enchantment || !e.canCombine(enchantment)) {
					continue outer;
				}
			}
			list.add(enchantment);

			if (enchantment.isCursed()) {
				itemStack.addEnchantment(enchantment, lvl);
			} else if (enchanted) {
				itemStack.addEnchantment(enchantment, lvl);
			}
		}
		return EmiStack.of(itemStack);
	}

	private Enchantment getEnchantment(Random random){
		List<Enchantment> enchantments = Registry.ENCHANTMENT.stream().filter(i -> i.isAcceptableItem(tool.getDefaultStack())).toList();
		int enchantment = random.nextInt(enchantments.size());
		return enchantments.get(enchantment);
	}
}
