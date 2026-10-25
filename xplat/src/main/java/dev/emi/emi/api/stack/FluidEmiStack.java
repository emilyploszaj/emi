package dev.emi.emi.api.stack;

import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;

import dev.emi.emi.EmiPort;
import dev.emi.emi.api.render.EmiRender;
import dev.emi.emi.api.render.EmiTooltipComponents;
import dev.emi.emi.platform.EmiAgnos;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.screen.tooltip.EmiTextTooltipWrapper;

@ApiStatus.Internal
public class FluidEmiStack extends EmiStack {
	private final Fluid fluid;
	private final DataComponentPatch componentChanges;

	public FluidEmiStack(Fluid fluid) {
		this(fluid, DataComponentPatch.EMPTY);
	}

	public FluidEmiStack(Fluid fluid, DataComponentPatch componentChanges) {
		this(fluid, componentChanges, 0);
	}

	public FluidEmiStack(Fluid fluid, DataComponentPatch componentChanges, long amount) {
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
	public DataComponentPatch getComponentChanges() {
		return componentChanges;
	}

	@Override
	public Object getKey() {
		return fluid;
	}

	@Override
	public ResourceLocation getId() {
		return EmiPort.getFluidRegistry().getKey(fluid);
	}

	@Override
	public void render(GuiGraphics raw, int x, int y, float delta, int flags) {
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
	public List<Component> getTooltipText() {
		return EmiAgnos.getFluidTooltip(fluid, componentChanges);
	}

	@Override
	public List<ClientTooltipComponent> getTooltip() {
		List<ClientTooltipComponent> list = Lists.newArrayList();
		List<Component> text = getTooltipText();
		if (!text.isEmpty()) {
			list.add(new EmiTextTooltipWrapper(this, EmiPort.ordered(text.get(0))));
		}
		list.addAll(text.stream().skip(1).map(EmiTooltipComponents::of).collect(Collectors.toList()));
		if (amount > 1) {
			list.add(EmiTooltipComponents.getAmount(this));
		}
		String namespace = EmiPort.getFluidRegistry().getKey(fluid).getNamespace();
		EmiTooltipComponents.appendModName(list, namespace);
		list.addAll(super.getTooltip());
		return list;
	}

	@Override
	public Component getName() {
		return EmiAgnos.getFluidName(fluid, componentChanges);
	}

	static class FluidEntry {
	}
}
