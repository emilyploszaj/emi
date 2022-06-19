package dev.emi.emi.api.recipe;

import java.util.List;

import com.google.common.collect.Lists;

import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiUtil;
import dev.emi.emi.api.render.EmiRenderable;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class EmiRecipeCategory implements EmiRenderable {
	public Identifier id;
	public EmiRenderable icon, simplified;
	
	/**
	 * A constructor to use only a single renderable for both the icon and simplified icon.
	 * It is generally recommended that simplified icons be unique and follow the style of vanilla icons.
	 * 
	 * {@link EmiStack} instances can be passed as {@link EmiRenderable}
	 */
	public EmiRecipeCategory(Identifier id, EmiRenderable icon) {
		this(id, icon, icon);
	}

	/**
	 * {@link EmiStack} instances can be passed as {@link EmiRenderable}
	 */
	public EmiRecipeCategory(Identifier id, EmiRenderable icon, EmiRenderable simplified) {
		this.id = id;
		this.icon = icon;
		this.simplified = simplified;
	}

	public Identifier getId() {
		return id;
	}

	@Override
	public void render(MatrixStack matrices, int x, int y, float delta) {
		icon.render(matrices, x, y, delta);
	}

	public void renderSimplified(MatrixStack matrices, int x, int y, float delta) {
		simplified.render(matrices, x, y, delta);
	}

	public List<TooltipComponent> getTooltip() {
		List<TooltipComponent> list = Lists.newArrayList();
		list.add(TooltipComponent.of(EmiPort.ordered(EmiPort.translatable(EmiUtil.translateId("emi.category.", getId())))));
		list.add(TooltipComponent.of(EmiPort.ordered(EmiPort.literal(EmiUtil.getModName(getId().getNamespace()),
			Formatting.BLUE, Formatting.ITALIC))));
		return list;
	}
}
