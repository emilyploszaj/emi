package dev.emi.emi.config;

import dev.emi.emi.EmiPort;
import net.minecraft.text.Text;

public enum EffectLocation implements ConfigEnum {
	TOP("top", false),
	LEFT_COMPRESSED("left-compressed", true),
	RIGHT_COMPRESSED("right-compressed", true),
	LEFT("left", false),
	RIGHT("right", false),
	;

	public final String name;
	public final boolean compressed;

	private EffectLocation(String name, boolean compressed) {
		this.name = name;
		this.compressed = compressed;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Text getText() {
		return EmiPort.translatable("emi.effect_location." + name.replace("-", "_"));
	}
}
