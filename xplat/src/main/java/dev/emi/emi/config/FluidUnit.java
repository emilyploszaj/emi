package dev.emi.emi.config;

import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.platform.EmiAgnos;
import it.unimi.dsi.fastutil.doubles.Double2ObjectFunction;
import net.minecraft.text.Text;

public enum FluidUnit implements ConfigEnum {
	LITERS("liters", a -> EmiPort.translatable("emi.fluid.amount.liters",
		EmiRenderHelper.TEXT_FORMAT.format((int) (a / literDivisor())))),
	MILLIBUCKETS("millibuckets", a -> EmiPort.translatable("emi.fluid.amount.millibuckets",
		EmiRenderHelper.TEXT_FORMAT.format((int) (a / literDivisor())))),
	DROPLETS("droplets", a -> EmiPort.translatable("emi.fluid.amount.droplets",
		EmiRenderHelper.TEXT_FORMAT.format((int) a))),
	;

	public static final int BUCKET = EmiAgnos.isForge() ? 1000 : 81_000;
	public static final int BOTTLE = EmiAgnos.isForge() ? 250 : 27_000;

	private final String name;
	private final Text translation;
	private final Double2ObjectFunction<Text> translator;

	private FluidUnit(String name, Double2ObjectFunction<Text> translator) {
		this.name = name;
		translation = EmiPort.translatable("emi.unit." + name);
		this.translator = translator;
	}

	private static int literDivisor() {
		if (EmiAgnos.isForge()) {
			return 1;
		} else {
			return 81;
		}
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
