package dev.emi.emi.config;

import dev.emi.emi.EmiPort;
import net.minecraft.network.chat.Component;

public enum ButtonVisibility implements ConfigEnum {
	AUTO("auto"),
	SHOWN("shown"),
	HIDDEN("hidden"),
	;

	private final String name;

	private ButtonVisibility(String name) {
		this.name = name;
	}

	public boolean resolve(boolean fallback) {
		return switch (this) {
			case AUTO -> fallback;
			case SHOWN -> true;
			case HIDDEN -> false;
		};
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Component getText() {
		return EmiPort.translatable("emi.button_visibility." + name);
	}
}
