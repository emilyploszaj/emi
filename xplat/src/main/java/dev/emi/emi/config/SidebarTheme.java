package dev.emi.emi.config;

import dev.emi.emi.EmiPort;
import net.minecraft.text.Text;

public enum SidebarTheme implements ConfigEnum {
	TRANSPARENT("transparent", 0, 0),
	VANILLA("vanilla", 9, 9),
	MODERN("modern", 0, 0),
	;

	private final String name;
	public final int horizontalPadding, verticalPadding;

	private SidebarTheme(String name, int horizontalPadding, int verticalPadding) {
		this.name = name;
		this.horizontalPadding = horizontalPadding;
		this.verticalPadding = verticalPadding;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Text getText() {
		return EmiPort.translatable("emi.sidebar.theme." + name);
	}
}
