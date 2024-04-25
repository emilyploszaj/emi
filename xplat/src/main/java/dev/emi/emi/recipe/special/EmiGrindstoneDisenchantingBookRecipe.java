package dev.emi.emi.recipe.special;

import dev.emi.emi.EmiPort;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.emi.emi.api.widget.TextWidget.Alignment;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.OrderedText;
import net.minecraft.util.Identifier;

import java.util.List;

public class EmiGrindstoneDisenchantingBookRecipe implements EmiRecipe {
	private static final Identifier BACKGROUND = new Identifier("minecraft", "textures/gui/container/grindstone.png");
	private final Enchantment enchantment;
	private final int level;
	private final Identifier id;

	public EmiGrindstoneDisenchantingBookRecipe(Enchantment enchantment, int level, Identifier id) {
		this.enchantment = enchantment;
		this.level = level;
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
		return List.of(getBook());
	}

	@Override
	public List<EmiStack> getOutputs() {
		return List.of(EmiStack.of(Items.BOOK));
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

		widgets.addText(getExp(), 114, 39, -1, true).horizontalAlign(Alignment.END);
		widgets.addSlot(getBook(), 18, 3).drawBack(false);
		widgets.addSlot(EmiStack.of(Items.BOOK), 98, 18).drawBack(false).recipeContext(this);
	}

	private EmiStack getBook() {
		ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);

		var enchBuilder = new ItemEnchantmentsComponent.Builder(ItemEnchantmentsComponent.DEFAULT);
		enchBuilder.add(enchantment, level);
		book.set(DataComponentTypes.STORED_ENCHANTMENTS, enchBuilder.build());

		return EmiStack.of(book);
	}

	private OrderedText getExp(){
		int minPower = enchantment.getMinPower(level);
		int minXP = (int)Math.ceil((double)minPower / 2.0);
		int maxXP = 2 * minXP - 1;
		return EmiPort.ordered(EmiPort.translatable("emi.grinding.experience", minXP, maxXP));
	}
}
