package dev.emi.emi.config;

import dev.emi.emi.EmiPort;
import net.minecraft.text.Text;

public enum CheatMode implements ConfigEnum {
    ALWAYS("always"),
    NEVER("never"),
    CREATIVE("creative")
    ;

    private final String name;

    CheatMode(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Text getText() {
        return EmiPort.translatable("emi.cheat_mode." + name.replace("-", "_"));
    }
}
