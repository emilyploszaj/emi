package dev.emi.emi.config;

import org.jetbrains.annotations.Nullable;

import dev.emi.emi.EmiPort;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public enum TextFormat implements ConfigEnum {
	PLAIN("plain", null),
	OBFUSCATED("obfuscated", Formatting.OBFUSCATED),
	BOLD("bold", Formatting.BOLD),
	STRIKETHROUGH("strikethrough", Formatting.STRIKETHROUGH),
	UNDERLINED("underlined", Formatting.UNDERLINE),
	ITALIC("italic", Formatting.ITALIC),
	;

	private final String name;
	private final @Nullable Formatting formatting;

	private TextFormat(String name, @Nullable Formatting formatting) {
		this.name = name;
		this.formatting = formatting;
	}

    public @Nullable Formatting getFormatting() {
        return formatting;
    }

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Text getText() {
		MutableText text = EmiPort.translatable("emi.name_format." + name.replace("-", "_"));
		if (formatting != null) {
			text = text.formatted(formatting);
		}
		return text;
	}
}
