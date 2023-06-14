package dev.emi.emi.jemi;

import dev.emi.emi.EmiPort;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.runtime.EmiDrawContext;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.text.Text;

public class JemiCategory extends EmiRecipeCategory {
	public IRecipeCategory<?> category;

	public JemiCategory(IRecipeCategory<?> category) {
		super(category.getRecipeType().getUid(), (raw, x, y, delta) -> {
			EmiDrawContext context = EmiDrawContext.wrap(raw);
			IDrawable icon = category.getIcon();
			if (icon != null) {
				icon.draw(context.raw(), x + (16 - icon.getWidth()) / 2, y + (16 - icon.getHeight()) / 2);
			} else {
				String title = category.getTitle().getString();
				context.drawCenteredTextWithShadow(EmiPort.literal(title.substring(0, Math.min(2, title.length()))), x + 8, y + 2);
			}
		});
		this.category = category;
	}

	@Override
	public Text getName() {
		return category.getTitle();
	}
}
