package dev.emi.emi.api.stack;

import java.util.List;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.systems.RenderSystem;

import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiUtil;
import dev.emi.emi.api.render.EmiRender;
import dev.emi.emi.config.EmiConfig;
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
import net.minecraft.fluid.Fluid;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.registry.Registry;

public class FluidEmiStack extends EmiStack {
	private final FluidEntry entry;
	private final FluidVariant fluid;

	public FluidEmiStack(Fluid fluid) {
		this(FluidVariant.of(fluid));
	}

	public FluidEmiStack(Fluid fluid, @Nullable NbtCompound nbt) {
		this(FluidVariant.of(fluid, nbt));
	}

	public FluidEmiStack(Fluid fluid, @Nullable NbtCompound nbt, long amount) {
		this(FluidVariant.of(fluid, nbt), amount);
	}

	@Deprecated
	public FluidEmiStack(FluidVariant fluid) {
		this(fluid, 0);
	}

	@Deprecated
	public FluidEmiStack(FluidVariant fluid, long amount) {
		entry = new FluidEntry(fluid);
		this.fluid = fluid;
		this.amount = amount;
	}

	@Override
	public EmiStack copy() {
		EmiStack e = new FluidEmiStack(fluid, amount);
		e.setChance(chance);
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
		return fluid.getFluid();
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
			Sprite[] sprites = FluidVariantRendering.getSprites(fluid);
			if (sprites == null || sprites.length < 1 || sprites[0] == null) {
				return;
			}
			Sprite sprite = sprites[0];
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
			EmiRender.renderRemainderIcon(this, matrices, x, y);
		}
	}

	@Override
	public List<Text> getTooltipText() {
		return FluidVariantRendering.getTooltip(fluid);
	}

	@Override
	public List<TooltipComponent> getTooltip() {
		List<TooltipComponent> list = getTooltipText().stream().map(EmiPort::ordered).map(TooltipComponent::of)
			.collect(Collectors.toList());
		if (amount > 1) {
			list.add(TooltipComponent.of(EmiPort.ordered(getAmountText(amount))));
		}
		String namespace = EmiPort.getFluidRegistry().getId(fluid.getFluid()).getNamespace();
		if (EmiConfig.appendModId) {
			String mod = EmiUtil.getModName(namespace);
			list.add(TooltipComponent.of(EmiPort.ordered(EmiPort.literal(mod, Formatting.BLUE, Formatting.ITALIC))));
		}
		list.addAll(super.getTooltip());
		return list;
	}

	@Override
	public Text getAmountText(double amount) {
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
