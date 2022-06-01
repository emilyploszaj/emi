package dev.emi.emi.api.recipe;

import java.util.List;

import com.google.common.collect.Lists;

import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiUtil;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class EmiRecipeCategory {
	public Identifier id;
	public Renderer renderer, simplified;
	
	public EmiRecipeCategory(Identifier id, EmiStack icon) {
		this(id, new Renderer() {

			public void render(MatrixStack matrices, int x, int y, float delta) {
				icon.render(matrices, x, y, delta, EmiIngredient.RENDER_ICON);
			}
		});
	}

	public EmiRecipeCategory(Identifier id, Renderer renderer) {
		this(id, renderer, renderer);
	}

	public EmiRecipeCategory(Identifier id, EmiStack icon, Renderer simplified) {
		this(id, new Renderer() {

			public void render(MatrixStack matrices, int x, int y, float delta) {
				icon.render(matrices, x, y, delta, EmiIngredient.RENDER_ICON);
			}
		}, simplified);
	}

	public EmiRecipeCategory(Identifier id, Renderer renderer, Renderer simplified) {
		this.id = id;
		this.renderer = renderer;
		this.simplified = simplified;
	}

	public Identifier getId() {
		return id;
	}

	public void render(MatrixStack matrices, int x, int y, float delta) {
		renderer.render(matrices, x, y, delta);
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

	public static interface Renderer {

		void render(MatrixStack matrices, int x, int y, float delta);
	}
}
