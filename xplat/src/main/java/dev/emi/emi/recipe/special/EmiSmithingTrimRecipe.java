package dev.emi.emi.recipe.special;

import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import dev.emi.emi.EmiUtil;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.render.EmiTooltipComponents;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.emi.emi.recipe.EmiSmithingRecipe;
import dev.emi.emi.runtime.ProxyRecipeManager;

public class EmiSmithingTrimRecipe extends EmiSmithingRecipe {
	private final SmithingRecipe recipe;
	private final int uniq = EmiUtil.RANDOM.nextInt();

	public EmiSmithingTrimRecipe(EmiIngredient template, EmiIngredient input, EmiIngredient addition, EmiStack output, SmithingRecipe recipe) {
		super(template, input, addition, output, ProxyRecipeManager.getId(recipe));
		this.recipe = recipe;
	}

	@Override
	public boolean supportsRecipeTree() {
		return false;
	}
	
	@Override
	public void addWidgets(WidgetHolder widgets) {
		widgets.addTexture(EmiTexture.EMPTY_ARROW, 62, 1);
		widgets.addSlot(template, 0, 0);
		widgets.addGeneratedSlot(r -> getStack(r, 0), uniq, 18, 0).appendTooltip(() -> EmiTooltipComponents.getIngredientTooltipComponent(input.getEmiStacks()));
		widgets.addGeneratedSlot(r -> getStack(r, 1), uniq, 36, 0).appendTooltip(() -> EmiTooltipComponents.getIngredientTooltipComponent(addition.getEmiStacks()));
		widgets.addGeneratedSlot(r -> getStack(r, 2), uniq, 94, 0).recipeContext(this);
	}

	private EmiStack getStack(Random r, int i) {
		EmiStack input = this.input.getEmiStacks().get(r.nextInt(this.input.getEmiStacks().size()));
		EmiStack addition = this.addition.getEmiStacks().get(r.nextInt(this.addition.getEmiStacks().size()));
		SmithingRecipeInput inv = new SmithingRecipeInput(template.getEmiStacks().get(0).getItemStack(), input.getItemStack(), addition.getItemStack());
		Minecraft client = Minecraft.getInstance();
		return new EmiStack[] {
			input,
			addition,
			EmiStack.of(recipe.assemble(inv))
		}[i];
	}
}
