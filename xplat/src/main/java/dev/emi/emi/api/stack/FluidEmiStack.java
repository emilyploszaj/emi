package dev.emi.emi.api.stack;

import java.util.List;
import java.util.stream.Collectors;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import dev.emi.emi.EmiPort;
import dev.emi.emi.api.render.EmiRender;
import dev.emi.emi.api.render.EmiTooltipComponents;
import dev.emi.emi.platform.EmiAgnos;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.fluid.Fluid;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@ApiStatus.Internal
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
	public void render(DrawContext draw, int x, int y, float delta, int flags) {
		if ((flags & RENDER_ICON) != 0) {
			EmiAgnos.renderFluid(this, draw.getMatrices(), x, y, delta);
		}
		if ((flags & RENDER_REMAINDER) != 0) {
			EmiRender.renderRemainderIcon(this, draw, x, y);
		}
	}

	@Override
	public List<Text> getTooltipText() {
		return EmiAgnos.getFluidTooltip(fluid, nbt);
	}

	@Override
	public List<TooltipComponent> getTooltip() {
		List<TooltipComponent> list = getTooltipText().stream().map(EmiTooltipComponents::of).collect(Collectors.toList());
		if (amount > 1) {
			list.add(EmiTooltipComponents.getAmount(this));
		}
		String namespace = EmiPort.getFluidRegistry().getId(fluid).getNamespace();
		EmiTooltipComponents.appendModName(list, namespace);
		list.addAll(super.getTooltip());
		return list;
	}

	@Override
	public Text getName() {
		return EmiAgnos.getFluidName(fluid, nbt);
	}

	static class FluidEntry {
	}
}
