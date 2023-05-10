package dev.emi.emi.jemi;

import java.util.List;

import dev.emi.emi.EmiPort;
import dev.emi.emi.api.stack.EmiStack;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class JemiStack<T> extends EmiStack {
	private final IIngredientType<T> type;
	private final IIngredientHelper<T> helper;
	private final IIngredientRenderer<T> renderer;
	private final T ingredient;

	public JemiStack(IIngredientType<T> type, IIngredientHelper<T> helper, IIngredientRenderer<T> renderer, T ingredient) {
		this.type = type;
		this.helper = helper;
		this.renderer = renderer;
		this.ingredient = ingredient;
	}

	@Override
	public void render(MatrixStack matrices, int x, int y, float delta, int flags) {
		int xOff = (16 - renderer.getWidth()) / 2;
		int yOff = (16 - renderer.getHeight()) / 2;
		matrices.push();
		matrices.translate(x + xOff, y + yOff, 0);
		renderer.render(matrices, ingredient);
		matrices.pop();
	}

	@Override
	public EmiStack copy() {
		return new JemiStack<T>(type, helper, renderer, helper.copyIngredient(ingredient));
	}

	@Override
	public boolean isEmpty() {
		return !helper.isValidIngredient(ingredient);
	}

	@Override
	public NbtCompound getNbt() {
		return null;
	}

	@Override
	public Object getKey() {
		return ingredient;
	}

	@Override
	public Identifier getId() {
		return helper.getResourceLocation(ingredient);
	}

	@Override
	public List<Text> getTooltipText() {
		return renderer.getTooltip(ingredient, TooltipContext.BASIC);
	}

	@Override
	public Text getName() {
		return EmiPort.literal(helper.getDisplayName(ingredient));
	}
}
