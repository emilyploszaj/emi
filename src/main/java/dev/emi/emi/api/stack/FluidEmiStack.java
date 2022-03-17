package dev.emi.emi.api.stack;

import java.util.List;
import java.util.stream.Collectors;

import com.mojang.blaze3d.systems.RenderSystem;

import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.EmiUtil;
import dev.emi.emi.api.stack.comparison.FluidStackComparison;
import dev.emi.emi.screen.tooltip.RemainderTooltipComponent;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.registry.Registry;

public class FluidEmiStack extends EmiStack {
	private final FluidEntry entry;
	private final FluidVariant fluid;

	public FluidEmiStack(FluidVariant fluid) {
		entry = new FluidEntry(fluid);
		this.fluid = fluid;
		this.comparison = new FluidStackComparison();
	}

	@Override
	public EmiStack copy() {
		EmiStack e = new FluidEmiStack(fluid);
		e.setRemainder(getRemainder().copy());
		e.comparison = comparison.copy();
		return e;
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

	@Override
	public Object getKey() {
		return fluid;
	}
	
	@Override
	public Entry<?> getEntry() {
		return entry;
	}

	@Override
	public void renderIcon(MatrixStack matrices, int x, int y, float delta) {
		Sprite sprite = FluidVariantRendering.getSprite(fluid);
		if (sprite == null) {
			return;
		}
		RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
		RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
		RenderSystem.setShaderTexture(0, sprite.getAtlas().getId());
		
		int color = FluidVariantRendering.getColor(fluid);
		float r = ((color >> 16) & 255) / 256f;
		float g = ((color >> 8) & 255) / 256f;
		float b = (color & 255) / 256f;
		
		BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
		bufferBuilder.begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE);
		float xMin = (float) x;
		float yMin = (float) y;
		float xMax = xMin + 16;
		float yMax = yMin + 16;
		float uMin = sprite.getMinU();
		float vMin = sprite.getMinV();
		float uMax = sprite.getMaxU();
		float vMax = sprite.getMaxV();
		Matrix4f model = matrices.peek().getPositionMatrix();
		bufferBuilder.vertex(model, xMin, yMax, 1).color(r, g, b, 1).texture(uMin, vMax).next();
		bufferBuilder.vertex(model, xMax, yMax, 1).color(r, g, b, 1).texture(uMax, vMax).next();
		bufferBuilder.vertex(model, xMax, yMin, 1).color(r, g, b, 1).texture(uMax, vMin).next();
		bufferBuilder.vertex(model, xMin, yMin, 1).color(r, g, b, 1).texture(uMin, vMin).next();
		bufferBuilder.end();
		BufferRenderer.draw(bufferBuilder);
	}

	@Override
	public void renderOverlay(MatrixStack matrices, int x, int y, float delta) {
		EmiRenderHelper.renderRemainder(this, matrices, x, y);
	}

	@Override
	public List<Text> getTooltipText() {
		return FluidVariantRendering.getTooltip(fluid);
	}

	@Override
	public List<TooltipComponent> getTooltip() {
		List<TooltipComponent> list = getTooltipText().stream().map(Text::asOrderedText).map(TooltipComponent::of)
			.collect(Collectors.toList());
		String namespace = Registry.FLUID.getId(fluid.getFluid()).getNamespace();
		String mod = EmiUtil.getModName(namespace);
		list.add(TooltipComponent.of(new LiteralText(mod).formatted(Formatting.BLUE, Formatting.ITALIC).asOrderedText()));
		if (!getRemainder().isEmpty()) {
			list.add(new RemainderTooltipComponent(this));
		}
		return list;
	}

	@Override
	public Text getName() {
		return FluidVariantRendering.getName(fluid);
	}

	public static class FluidEntry extends Entry<FluidVariant> {

		public FluidEntry(FluidVariant value) {
			super(value);
		}

		@Override
		Class<FluidVariant> getType() {
			return FluidVariant.class;
		}
	}
}
