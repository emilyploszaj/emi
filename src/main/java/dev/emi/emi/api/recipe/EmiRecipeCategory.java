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
		list.add(TooltipComponent.of(EmiPort.translatable(EmiUtil.translateId("emi.category.", getId())).asOrderedText()));
		list.add(TooltipComponent.of(EmiPort.literal(EmiUtil.getModName(getId().getNamespace()))
			.formatted(Formatting.BLUE).formatted(Formatting.ITALIC).asOrderedText()));
		return list;
	}
}
