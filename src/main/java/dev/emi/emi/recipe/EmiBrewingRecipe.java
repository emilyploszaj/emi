package dev.emi.emi.recipe;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import dev.emi.emi.VanillaPlugin;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.AnimatedTextureWidget;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.TextureWidget;
import dev.emi.emi.api.widget.Widget;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.item.Items;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

public class EmiBrewingRecipe implements EmiRecipe {
	private static final Identifier BACKGROUND = new Identifier("minecraft", "textures/gui/container/brewing_stand.png");
	private static final EmiStack BLAZE_POWDER = EmiStack.of(Items.BLAZE_POWDER);
	private final EmiIngredient input, ingredient;
	private final EmiStack output, input3, output3;
	private final Identifier id;

	public EmiBrewingRecipe(EmiStack input, EmiIngredient ingredient, EmiStack output, Identifier id) {
		this.input = input;
		this.ingredient = ingredient;
		this.output = output;
		this.input3 = input.copy().setAmount(3);
		this.output3 = output.copy().setAmount(3);
		this.id = id;
	}

	@Override
	public EmiRecipeCategory getCategory() {
		return VanillaPlugin.BREWING;
	}

	@Override
	public @Nullable Identifier getId() {
		return id;
	}

	@Override
	public List<EmiIngredient> getInputs() {
		return List.of(input3, ingredient);
	}

	@Override
	public List<EmiStack> getOutputs() {
		return List.of(output3);
	}

	@Override
	public int getDisplayWidth() {
		return 120;
	}

	@Override
	public int getDisplayHeight() {
		return 61;
	}

	@Override
	public void addWidgets(List<Widget> widgets, int x, int y) {
		widgets.add(new TextureWidget(BACKGROUND, x, y, 103, 61, 16, 14));
		widgets.add(new AnimatedTextureWidget(BACKGROUND, x + 81, y + 2, 9, 28, 176, 0, 1000 * 20, false, false, false).tooltip(() -> {
			return List.of(TooltipComponent.of(new TranslatableText("emi.cooking.time", 20).asOrderedText()));
		}));
		widgets.add(new AnimatedTextureWidget(BACKGROUND, x + 47, y, 12, 29, 185, 0, 700, false, true, false));
		widgets.add(new SlotWidget(BLAZE_POWDER, x, y + 2).drawBack(false));
		widgets.add(new SlotWidget(input, x + 39, y + 36).drawBack(false));
		widgets.add(new SlotWidget(ingredient, x + 62, y + 2).drawBack(false));
		widgets.add(new SlotWidget(output, x + 85, y + 36).drawBack(false).recipeContext(this));
	}
}
