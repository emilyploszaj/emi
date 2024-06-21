package dev.emi.emi.api.stack;

import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.component.ComponentChanges;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.ComponentMapImpl;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import dev.emi.emi.EmiPort;
import dev.emi.emi.api.render.EmiRender;
import dev.emi.emi.api.render.EmiTooltipComponents;
import dev.emi.emi.platform.EmiAgnos;
import dev.emi.emi.runtime.EmiDrawContext;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.fluid.Fluid;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@ApiStatus.Internal
public class FluidEmiStack extends EmiStack {
	private final Fluid fluid;
	private final ComponentChanges componentChanges;

	public FluidEmiStack(Fluid fluid) {
		this(fluid, ComponentChanges.EMPTY);
	}

	public FluidEmiStack(Fluid fluid, ComponentChanges componentChanges) {
		this(fluid, componentChanges, 0);
	}

	public FluidEmiStack(Fluid fluid, ComponentChanges componentChanges, long amount) {
		this.fluid = fluid;
		this.componentChanges = componentChanges;
		this.amount = amount;
	}

	@Override
	public EmiStack copy() {
		EmiStack e = new FluidEmiStack(fluid, componentChanges, amount);
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
	public ComponentChanges getComponentChanges() {
		return componentChanges;
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
	public void render(DrawContext raw, int x, int y, float delta, int flags) {
		EmiDrawContext context = EmiDrawContext.wrap(raw);
		if ((flags & RENDER_ICON) != 0) {
			context.push();
			context.matrices().translate(0, 0, 100);
			EmiAgnos.renderFluid(this, context.matrices(), x, y, delta);
			context.pop();
		}
		if ((flags & RENDER_REMAINDER) != 0) {
			EmiRender.renderRemainderIcon(this, context.raw(), x, y);
		}
	}

	@Override
	public List<Text> getTooltipText() {
		return EmiAgnos.getFluidTooltip(fluid, componentChanges);
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
		return EmiAgnos.getFluidName(fluid, componentChanges);
	}

	static class FluidEntry {
	}
}
