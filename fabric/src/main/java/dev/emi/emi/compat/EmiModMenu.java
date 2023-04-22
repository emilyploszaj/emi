package dev.emi.emi.compat;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import dev.emi.emi.screen.ConfigScreen;

public class EmiModMenu implements ModMenuApi {

	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return (s) -> new ConfigScreen(s);
	}
}
