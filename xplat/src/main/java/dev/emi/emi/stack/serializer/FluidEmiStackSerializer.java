package dev.emi.emi.stack.serializer;

import dev.emi.emi.EmiPort;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.FluidEmiStack;
import dev.emi.emi.api.stack.serializer.EmiStackSerializer;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.resources.ResourceLocation;

public class FluidEmiStackSerializer implements EmiStackSerializer<FluidEmiStack> {

	@Override
	public String getType() {
		return "fluid";
	}

	@Override
	public EmiStack create(ResourceLocation id, DataComponentPatch componentChanges, long amount) {
		return EmiStack.of(EmiPort.getFluidRegistry().get(id), componentChanges, amount);
	}
}
