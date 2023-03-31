package dev.emi.emi.screen.tooltip;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

import dev.emi.emi.EmiPort;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.util.Formatting;

public class EmiTooltip {
	public static final DecimalFormat TEXT_FORMAT = new DecimalFormat("0.##");
	
	public static TooltipComponent chance(String type, float chance) {
		return TooltipComponent.of(EmiPort.ordered(
			EmiPort.translatable("tooltip.emi.chance." + type,
				TEXT_FORMAT.format(chance * 100))
					.formatted(Formatting.GOLD)));
	}

	public static List<TooltipComponent> splitTranslate(String key) {
		return Arrays.stream(I18n.translate(key).split("\n"))
			.map(s -> TooltipComponent.of(EmiPort.ordered(EmiPort.literal(s)))).toList();
	}

	public static List<TooltipComponent> splitTranslate(String key, Object... objects) {
		return Arrays.stream(I18n.translate(key, objects).split("\n"))
			.map(s -> TooltipComponent.of(EmiPort.ordered(EmiPort.literal(s)))).toList();
	}
}
