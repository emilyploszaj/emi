package dev.emi.emi.recipe.special;

import java.util.List;
import java.util.Random;

import com.google.common.collect.Lists;

import dev.emi.emi.EmiPort;
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

public class EmiGrindstoneDisenchantingRecipe implements EmiRecipe {
	private static final Identifier BACKGROUND = EmiPort.id("minecraft", "textures/gui/container/grindstone.png");
	private final int uniq = EmiUtil.RANDOM.nextInt();
	private final Item tool;
	private final Identifier id;

	public EmiGrindstoneDisenchantingRecipe(Item tool, Identifier id) {
		this.tool = tool;
		this.id = id;
	}

	@Override
	public EmiRecipeCategory getCategory() {
		return VanillaEmiRecipeCategories.GRINDING;
	}

	@Override
	public Identifier getId() {
		return id;
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
		return 116;
	}

	@Override
	public int getDisplayHeight() {
		return 56;
	}

	@Override
	public void addWidgets(WidgetHolder widgets) {
		widgets.addTexture(BACKGROUND, 0, 0, 116, 56, 30, 15);

		widgets.addGeneratedSlot(r -> getTool(r, true), uniq, 18, 3).drawBack(false);
		widgets.addGeneratedSlot(r -> getTool(r, false), uniq, 98, 18).drawBack(false).recipeContext(this);
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
			// Some enchantments are returning zero for max level? I don't want to think about it
			int lvl = maxLvl > 0 ? random.nextInt(maxLvl) + 1 : 0;

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
		List<Enchantment> enchantments = EmiPort.getEnchantmentRegistry().stream().filter(i -> i.isAcceptableItem(tool.getDefaultStack())).toList();
		int enchantment = random.nextInt(enchantments.size());
		return enchantments.get(enchantment);
	}
}
