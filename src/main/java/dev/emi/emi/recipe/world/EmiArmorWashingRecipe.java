package dev.emi.emi.recipe.world;

import java.util.List;

import dev.emi.emi.EmiUtil;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.DyeableItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;

public class EmiArmorWashingRecipe extends EmiCustomWorldRecipe {
	private static final EmiStack CAULDRON = EmiStack.of(Items.CAULDRON);
	private static final EmiStack WATER = EmiStack.of(FluidVariant.of(Fluids.WATER), 81_000 / 3);
	private final EmiStack emiArmor;
	private final Item armor;
	private final int unique = EmiUtil.RANDOM.nextInt();

	static {
		CAULDRON.setRemainder(CAULDRON);
	}

	public EmiArmorWashingRecipe(Item armor, Identifier id) {
		super(id);
		this.armor = armor;
		emiArmor = EmiStack.of(armor);
	}

	@Override
	public List<EmiIngredient> getInputs() {
		return List.of(emiArmor, WATER);
	}

	@Override
	public List<EmiIngredient> getCatalysts() {
		return List.of(CAULDRON);
	}

	@Override
	public List<EmiStack> getOutputs() {
		return List.of(emiArmor);
	}

	@Override
	public void addWidgets(WidgetHolder widgets) {
		widgets.addTexture(EmiTexture.PLUS, 23, 3);
		widgets.addTexture(EmiTexture.EMPTY_ARROW, 79, 1);
		widgets.addGeneratedSlot(r -> {
			ItemStack stack = new ItemStack(armor);
			((DyeableItem) armor).setColor(stack, r.nextInt(0xFFFFFF + 1));
			return EmiStack.of(stack);
		}, unique, 0, 0);
		widgets.addSlot(CAULDRON, 40, 0);
		widgets.addSlot(WATER, 58, 0);
		widgets.addSlot(emiArmor, 107, 0).recipeContext(this);
	}

	@Override
	public boolean supportsRecipeTree() {
		return false;
	}
}
