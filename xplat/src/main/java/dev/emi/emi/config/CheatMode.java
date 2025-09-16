package dev.emi.emi.config;

import dev.emi.emi.EmiPort;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public enum CheatMode implements ConfigEnum {
    ALWAYS("always"),
    NEVER("never"),
    CREATIVE_ONLY("creative_only")
    ;

    public final String name;

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
        switch (this) {
            case ALWAYS -> {
                return true;
            }
            case CREATIVE_ONLY -> {
                return client.player == null || client.player.isInCreativeMode();
            }
            default -> {
                return false;
            }
        }
    }
}