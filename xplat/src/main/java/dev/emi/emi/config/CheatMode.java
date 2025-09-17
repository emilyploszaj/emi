package dev.emi.emi.config;

import dev.emi.emi.EmiPort;
import net.minecraft.client.MinecraftClient;
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

    public boolean isEnabled(MinecraftClient client) {
        return switch (this) {
            case ALWAYS -> true;
            case CREATIVE -> client.player == null || client.player.isInCreativeMode();
            case NEVER -> false;
        };
    }
}
