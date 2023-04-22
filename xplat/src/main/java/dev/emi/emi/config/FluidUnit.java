package dev.emi.emi.config;

import dev.emi.emi.EmiPort;
import it.unimi.dsi.fastutil.doubles.Double2ObjectFunction;
import net.minecraft.text.Text;

public enum FluidUnit implements ConfigEnum {
	LITERS("liters", a -> EmiPort.translatable("emi.fluid.amount.liters", (int) (a / 81))),
	MILLIBUCKETS("millibuckets", a -> EmiPort.translatable("emi.fluid.amount.millibuckets", (int) (a / 81))),
	DROPLETS("droplets", a -> EmiPort.translatable("emi.fluid.amount.droplets", (int) a)),
	;

	private final String name;
	private final Text translation;
	private final Double2ObjectFunction<Text> translator;

	private FluidUnit(String name, Double2ObjectFunction<Text> translator) {
		this.name = name;
		translation = EmiPort.translatable("emi.unit." + name);
		this.translator = translator;
	}

	@Override
	public String getName() {
		return name;
	}

	public Text translate(double amount) {
		return translator.apply(Double.valueOf(amount));
	}

	@Override
	public Text getText() {
		return translation;
	}
}
