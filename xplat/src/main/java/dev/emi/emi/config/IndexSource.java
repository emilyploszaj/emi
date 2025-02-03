package dev.emi.emi.config;

import dev.emi.emi.EmiPort;
import net.minecraft.text.Text;

public enum IndexSource implements ConfigEnum {
	CREATIVE("creative"),
	REGISTERED("registered"),
	CREATIVE_PLUS_REGISTERED("creative-plus-registered"),
	;

	public final String name;

	private IndexSource(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Text getText() {
		return EmiPort.translatable("emi.index_source." + name.replace("-", "_"));
	}
}
