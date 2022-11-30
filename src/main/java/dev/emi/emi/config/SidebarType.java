package dev.emi.emi.config;

import dev.emi.emi.EmiPort;
import net.minecraft.text.Text;

public enum SidebarType implements ConfigEnum {
	NONE("none", 0, 0),
	INDEX("index", 0, 146),
	CRAFTABLES("craftables", 16, 146),
	FAVORITES("favorites", 32, 146),
	CHESS("chess", 48, 146),
	;
	
	private final String name;
	public final int u, v;

	private SidebarType(String name, int u, int v) {
		this.name = name;
		this.u = u;
		this.v = v;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Text getText() {
		return EmiPort.translatable("emi.sidebar.type." + name);
	}

	public static SidebarType fromName(String name) {
		for (SidebarType type : values()) {
			if (type.getName().equals(name)) {
				return type;
			}
		}
		return NONE;
	}
}
