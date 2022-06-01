package dev.emi.emi.api.stack;

import java.util.List;
import java.util.stream.Collectors;

import com.mojang.blaze3d.systems.RenderSystem;

import dev.emi.emi.EmiConfig;
import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.EmiUtil;
import dev.emi.emi.screen.tooltip.RemainderTooltipComponent;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.registry.Registry;

public class FluidEmiStack extends EmiStack {
	private final FluidEntry entry;
	private final FluidVariant fluid;

	public FluidEmiStack(FluidVariant fluid) {
		this(fluid, 0);
	}

	public FluidEmiStack(FluidVariant fluid, long amount) {
		entry = new FluidEntry(fluid);
		this.fluid = fluid;
		this.amount = amount;
	}

	@Override
	public EmiStack copy() {
		EmiStack e = new FluidEmiStack(fluid);
		e.setRemainder(getRemainder().copy());
		e.comparison = comparison;
		return e;
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

	@Override
	public NbtCompound getNbt() {
		return fluid.getNbt();
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
	public Identifier getId() {
		return Registry.FLUID.getId(fluid.getFluid());
	}

	@Override
	public void render(MatrixStack matrices, int x, int y, float delta, int flags) {
		if ((flags & RENDER_ICON) != 0) {
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
			EmiPort.draw(bufferBuilder);
		}
		if ((flags & RENDER_REMAINDER) != 0) {
			EmiRenderHelper.renderRemainder(this, matrices, x, y);
		}

		/*
		matrices.push();
		MinecraftClient client = MinecraftClient.getInstance();
		
		float scale = (float) client.getWindow().getScaleFactor();
		float invScale = 1 / scale;
		int s = (int) scale;
		int size = s * 16;
		matrices.scale(invScale, invScale, 1);
		matrices.translate(0, 0, 300);

		Text text = getTranslatedAmount();
		client.textRenderer.draw(matrices, text, x * s + size - client.textRenderer.getWidth(text) - 2,
			y * s + size - client.textRenderer.fontHeight - 2, -1);
		matrices.pop();*/
	}

	@Override
	public List<Text> getTooltipText() {
		return FluidVariantRendering.getTooltip(fluid);
	}

	@Override
	public List<TooltipComponent> getTooltip() {
		List<TooltipComponent> list = getTooltipText().stream().map(Text::asOrderedText).map(TooltipComponent::of)
			.collect(Collectors.toList());
		if (amount > 1) {
			list.add(TooltipComponent.of(getAmountText(amount).asOrderedText()));
		}
		String namespace = Registry.FLUID.getId(fluid.getFluid()).getNamespace();
		String mod = EmiUtil.getModName(namespace);
		list.add(TooltipComponent.of(EmiPort.literal(mod).formatted(Formatting.BLUE, Formatting.ITALIC).asOrderedText()));
		if (!getRemainder().isEmpty()) {
			list.add(new RemainderTooltipComponent(this));
		}
		return list;
	}

	@Override
	public Text getAmountText(float amount) {
		if (amount != 0) {
			return EmiConfig.fluidUnit.translate(amount);
		}
		return EmiPort.literal("");
	}

	@Override
	public Text getName() {
		return EmiPort.fluidName(fluid);
	}

	public static class FluidEntry extends Entry<FluidVariant> {

		public FluidEntry(FluidVariant value) {
			super(value);
		}

		@Override
		public Class<FluidVariant> getType() {
			return FluidVariant.class;
		}

		@Override
		public boolean equals(Object obj) {
			return obj instanceof FluidEntry e && getValue().getFluid().equals(e.getValue().getFluid());
		}
	}
}
