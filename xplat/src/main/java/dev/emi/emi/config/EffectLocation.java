package dev.emi.emi.config;

import dev.emi.emi.EmiPort;
import net.minecraft.network.chat.Component;

public enum EffectLocation implements ConfigEnum {
	TOP("top", false),
	LEFT_COMPRESSED("left-compressed", true),
	RIGHT_COMPRESSED("right-compressed", true),
	LEFT("left", false),
	RIGHT("right", false),
	HIDDEN("hidden", false),
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
	public Component getText() {
		return EmiPort.translatable("emi.effect_location." + name.replace("-", "_"));
	}
}
