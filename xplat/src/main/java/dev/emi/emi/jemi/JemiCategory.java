package dev.emi.emi.jemi;

import dev.emi.emi.api.recipe.EmiRecipeCategory;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.text.Text;

public class JemiCategory extends EmiRecipeCategory {
	public IRecipeCategory<?> category;

	public JemiCategory(IRecipeCategory<?> category) {
		super(category.getRecipeType().getUid(), (matrices, x, y, delta) -> {
			if (category.getIcon() != null) {
				category.getIcon().draw(matrices, x, y);
			} else {
				MinecraftClient client = MinecraftClient.getInstance();
				String title = category.getTitle().getString();
				DrawableHelper.drawCenteredTextWithShadow(matrices, client.textRenderer, title.substring(0, Math.min(2, title.length())), x + 8, y + 2, -1);
			}
		});
		this.category = category;
	}

	@Override
	public Text getName() {
		return category.getTitle();
	}
}
