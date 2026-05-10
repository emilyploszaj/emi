package dev.emi.emi.screen;

import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;

import dev.emi.emi.EmiPort;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.config.FluidUnit;
import dev.emi.emi.runtime.EmiDrawContext;
import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;
import net.minecraft.util.Identifier;

public class MicroTextRenderer {
	private static final Identifier TEXTURE = EmiPort.id("emi", "textures/gui/microfont.png");
	private static final Char2ObjectMap<MicroChar> MICRO_CHARS = new Char2ObjectOpenHashMap<>();
	private static final List<UnitScale> QUANTITY_SCALES = List.of(
		new UnitScale("", 0),
		new UnitScale("k", 3),
		new UnitScale("M", 6),
		new UnitScale("B", 9),
		new UnitScale("T", 12),
		new UnitScale("Q", 15)
	);
	private static final List<UnitScale> BUCKET_VOLUME_SCALES = List.of(
		new UnitScale("mB", 0),
		new UnitScale("B", 3),
		new UnitScale("kB", 6),
		new UnitScale("MB", 9),
		new UnitScale("GB", 12),
		new UnitScale("TB", 15),
		new UnitScale("PB", 18)
	);
	private static final List<UnitScale> LITER_VOLUME_SCALES = List.of(
		new UnitScale("mL", 0),
		new UnitScale("L", 3),
		new UnitScale("kL", 6),
		new UnitScale("ML", 9),
		new UnitScale("GL", 12),
		new UnitScale("TL", 15),
		new UnitScale("PL", 18)
	);
	private static final List<UnitScale> DROPLET_VOLUME_SCALES = List.of(
		new UnitScale("d", 0),
		new UnitScale("kd", 3),
		new UnitScale("Md", 6),
		new UnitScale("Gd", 9),
		new UnitScale("Td", 12),
		new UnitScale("Pd", 15)
	);

	static {
		for (int i = 0; i <= 9; i++) {
			addMicroChar(new MicroChar((char) ('0' + i), 5, i * 5, 0));
		}
		addMicroChar(new MicroChar('.', 3, 0, 14));
		addMicroChar(new MicroChar('k', 5, 3, 14));
		addMicroChar(new MicroChar('M', 5, 8, 14));
		addMicroChar(new MicroChar('B', 5, 13, 14)); // Also B for buckets
		addMicroChar(new MicroChar('T', 5, 18, 14));
		addMicroChar(new MicroChar('Q', 5, 23, 14));
	
		addMicroChar(new MicroChar('L', 5, 0, 28));
		addMicroChar(new MicroChar('d', 5, 5, 28));
		addMicroChar(new MicroChar('m', 5, 10, 28));
		addMicroChar(new MicroChar('G', 5, 15, 28));
		addMicroChar(new MicroChar('P', 5, 20, 28));
	}

	private static void addMicroChar(MicroChar c) {
		MICRO_CHARS.put(c.c, c);
	}
	
	public static void render(EmiDrawContext context, long amount, boolean volume, int constraint, int right, int bottom) {
		render(context, amount, volume, constraint, right, bottom, -1);
	}

	public static void render(EmiDrawContext context, long amount, boolean volume, int constraint, int right, int bottom, int color) {
		List<UnitScale> scales;
		if (volume) {
			if (EmiConfig.fluidUnit != FluidUnit.DROPLETS) {
				amount /= FluidUnit.literDivisor();
			}
			scales = switch (EmiConfig.fluidUnit) {
				case MILLIBUCKETS -> BUCKET_VOLUME_SCALES;
				case LITERS -> LITER_VOLUME_SCALES;
				case DROPLETS -> DROPLET_VOLUME_SCALES;
			};
		} else {
			scales = QUANTITY_SCALES;
		}
		String string = getConstrainedAmount(amount, constraint, scales);
		int width = measure(string);
		int height = 7;
		int x = right - width;
		int y = bottom - height;
		float a = (((color & 0xFF000000) >> 24) & 0xFF) / 255f;
		float r = (((color & 0x00FF0000) >> 16) & 0xFF) / 255f;
		float g = (((color & 0x0000FF00) >>  8) & 0xFF) / 255f;
		float b = (((color & 0x000000FF) >>  0) & 0xFF) / 255f;
		context.push();
		context.matrices().translate(0, 0, 300);
		context.disableBlend();
		for (int i = 0; i < string.length(); i++) {
			MicroChar c = MICRO_CHARS.get(string.charAt(i));
			if (c == null) {
				x += 1;
				continue;
			}
			context.setColor(r, g, b, a);
			context.drawTexture(TEXTURE, x, y, c.u, c.v, c.width, 7);
			context.resetColor();
			context.drawTexture(TEXTURE, x, y, c.u, c.v + 7, c.width, 7);
			x += c.width - 1;
		}
		context.pop();
	}


	private static String getConstrainedAmount(long amount, int constraint, List<UnitScale> scales) {
		String astring = "" + amount;
		for (UnitScale scale : scales) {
			if (astring.length() <= scale.shift + 3 /*1_000_000*/) {
				for (int i = 2; i >= 0; i--) {
					String construct = construct(astring, scale.shift, i, scale.unit);
					if (measure(construct) <= constraint) {
						return construct;
					}
				}
				if (astring.length() <= scale.shift + 2) {
					return "+";
				}
			}
		}
		return "+";
	}

	private static String construct(String amount, int shift, int afterDecimal, String unit) {
		while (amount.length() < shift) {
			amount = "0" + amount;
		}
		int start = amount.length() - shift;
		String before = amount.substring(0, start);
		if (afterDecimal <= 0) {
			return before + unit;
		}
		String after = amount.substring(start, Math.min(start + afterDecimal, amount.length()));
		while (after.length() > 0 && after.charAt(after.length() - 1) == '0') {
			after = after.substring(0, after.length() - 1);
		}
		if (after.isEmpty()) {
			return before + unit;
		} else {
			return before + "." + after + unit;
		}
	}

	private static int measure(String string) {
		int total = 0;
		int refund = 0;
		for (int i = 0; i < string.length(); i++) {
			MicroChar c = MICRO_CHARS.get(string.charAt(i));
			int amount = 1;
			if (c != null) {
				amount = c.width;
			}
			total += amount - refund;
			refund = 1;
		}
		return total;
	}

	private static record MicroChar(char c, int width, int u, int v) {
	}

	private static record UnitScale(String unit, int shift) {
	}
}
