package dev.emi.emi.recipe;

import java.util.List;

import dev.emi.emi.VanillaPlugin;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiIngredientList;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.EmiTagIngredient;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.Widget;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tag.ItemTags;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;

public class EmiTagRecipe implements EmiRecipe {
	private final List<EmiStack> stacks;
	public final Tag<Item> tag;

	public EmiTagRecipe(Tag<Item> tag) {
		stacks = tag.values().stream().map(ItemStack::new).map(EmiStack::of).toList();
		this.tag = tag;
	}

	@Override
	public EmiRecipeCategory getCategory() {
		return VanillaPlugin.TAG;
	}

	@Override
	public Identifier getId() {
		Identifier id = ItemTags.getTagGroup().getUncheckedTagId(tag);
		return id == null ? null : new Identifier("emi", "tag/" + id.getNamespace() + "/" + id.getPath());
	}

	@Override
	public List<EmiIngredient> getInputs() {
		return List.of(new EmiIngredientList(stacks));
	}

	@Override
	public List<EmiStack> getOutputs() {
		return List.of();
	}

	@Override
	public int getDisplayHeight() {
		return Math.min((stacks.size() - 1) / 8 + 1, 7) * 18 + 24;
	}

	@Override
	public int getDisplayWidth() {
		return 144;
	}

	@Override
	public void addWidgets(List<Widget> widgets, int x, int y) {
		widgets.add(new SlotWidget(new EmiTagIngredient(tag), x + 63, y));
		for (int i = 0; i < stacks.size() && i < 7 * 8; i++) {
			widgets.add(new SlotWidget(stacks.get(i), x + i % 8 * 18, y + i / 8 * 18 + 24));
		}
	}
}
