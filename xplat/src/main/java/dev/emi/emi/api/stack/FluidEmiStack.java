package dev.emi.emi.api.stack;

import java.util.List;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiUtil;
import dev.emi.emi.api.render.EmiRender;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.platform.EmiAgnos;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.fluid.Fluid;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class FluidEmiStack extends EmiStack {
	private final Fluid fluid;
	private final NbtCompound nbt;

	public FluidEmiStack(Fluid fluid) {
		this(fluid, null);
	}

	public FluidEmiStack(Fluid fluid, @Nullable NbtCompound nbt) {
		this(fluid, nbt, 0);
	}

	public FluidEmiStack(Fluid fluid, @Nullable NbtCompound nbt, long amount) {
		this.fluid = fluid;
		this.nbt = nbt;
		this.amount = amount;
	}

	@Override
	public EmiStack copy() {
		EmiStack e = new FluidEmiStack(fluid, nbt, amount);
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
		return nbt;
	}

	@Override
	public Object getKey() {
		return fluid;
	}

	@Override
	public Identifier getId() {
		return EmiPort.getFluidRegistry().getId(fluid);
	}

	@Override
	public void render(MatrixStack matrices, int x, int y, float delta, int flags) {
		if ((flags & RENDER_ICON) != 0) {
			EmiAgnos.renderFluid(this, matrices, x, y, delta);
		}
		if ((flags & RENDER_REMAINDER) != 0) {
			EmiRender.renderRemainderIcon(this, matrices, x, y);
		}
	}

	@Override
	public List<Text> getTooltipText() {
		return EmiAgnos.getFluidTooltip(fluid, nbt);
	}

	@Override
	public List<TooltipComponent> getTooltip() {
		List<TooltipComponent> list = getTooltipText().stream().map(EmiPort::ordered).map(TooltipComponent::of)
			.collect(Collectors.toList());
		if (amount > 1) {
			list.add(TooltipComponent.of(EmiPort.ordered(getAmountText(amount))));
		}
		String namespace = EmiPort.getFluidRegistry().getId(fluid).getNamespace();
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
		return EmiAgnos.getFluidName(fluid, nbt);
	}

	static class FluidEntry {
	}
}
