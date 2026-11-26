package dev.emi.emi.config;

import dev.emi.emi.EmiPort;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public enum TextColor implements ConfigEnum {
    BLACK("black", Formatting.BLACK),
    DARK_BLUE("dark-blue", Formatting.DARK_BLUE),
    DARK_GREEN("dark-green", Formatting.DARK_GREEN),
    DARK_AQUA("dark-aqua", Formatting.DARK_AQUA),
    DARK_RED("dark-red", Formatting.DARK_RED),
    DARK_PURPLE("dark-purple", Formatting.DARK_PURPLE),
    GOLD("gold", Formatting.GOLD),
    GRAY("gray", Formatting.GRAY),
    DARK_GRAY("dark-gray", Formatting.DARK_GRAY),
    BLUE("blue", Formatting.BLUE),
    GREEN("green", Formatting.GREEN),
    AQUA("aqua", Formatting.AQUA),
    RED("red", Formatting.RED),
    LIGHT_PURPLE("light-purple", Formatting.LIGHT_PURPLE),
    YELLOW("yellow", Formatting.YELLOW),
    WHITE("white", Formatting.WHITE),
    ;

    private final String name;
    private final Formatting formatting;

    private TextColor(String name, Formatting formatting) {
        this.name = name;
        this.formatting = formatting;
    }

    public Formatting getFormatting() {
        return formatting;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Text getText() {
		return EmiPort.translatable("emi.name_color." + name.replace("-", "_"), formatting);
    }
}
