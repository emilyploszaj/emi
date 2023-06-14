package dev.emi.emi.screen.widget.config;

import java.util.List;
import java.util.Random;

import com.google.common.collect.Lists;

import dev.emi.emi.EmiPort;
import dev.emi.emi.runtime.EmiDrawContext;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.text.Style;

public class EmiNameWidget implements Drawable {
	private static MinecraftClient client = MinecraftClient.getInstance();
	private List<String[]> NAMES = Lists.<String[]>newArrayList(
		"Emi Memy Imi".split(" "),
		"Exhaustively Many Ingredients".split(" "),
		"Explicitly Mandated Items".split(" "),
		"Endless Material Information".split(" "),
		"Evolving Manufacturing Index".split(" "),
		"Evidently, Many Ingredients".split(" "),
		"Earnestly Made Imitation".split(" "),
		"Even More Items".split(" "),
		"Eminence, My Inception".split(" "),
		"Explore My Inventory".split(" "),
		"Expounded Minutia Introspection".split(" "),
		"Exciting Minecraft Information".split(" "),
		"Expropriated Matter Insights".split(" "),
		"Efficiently Managed Inventory".split(" "),
		"Eerily Many Ingredients".split(" "),
		"Eventually Made Impressive".split(" "),
		"Exceptionally Motionless Interface".split(" "),
		"Emi's Magic Inventory".split(" "),
		"Egad, My Items!".split(" "),
		"Exploring Modified: Iridescent".split(" "),
		"E M I".split(" ")
	);
	public int x, y;

	public EmiNameWidget(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public IntSet pruneSet(Random rand, int bound, int portion) {
		IntList ints = new IntArrayList();
		for (int i = 0; i < bound; i++) {
			ints.add(i);
		}
		for (int i = 0; i < portion; i++) {
			if (ints.size() == 0) {
				break;
			}
			ints.removeInt(rand.nextInt(ints.size()));
		}
		return new IntOpenHashSet(ints);
	}

	public String interpolate(Random source, String first, String second, float progress) {
		TextRenderer render = client.textRenderer;
		String both = first + second;
		int fw = render.getWidth(first);
		int sw = render.getWidth(second);
		int width = fw + Math.round((sw - fw) * progress);
		String ret = "";
		Random rand = new Random(source.nextLong() + width);
		outer:
		while (true) {
			for (int i = 0; i < 10; i++) {
				char c = both.charAt(rand.nextInt(both.length()));
				int w = render.getWidth(ret + c);
				if (w > width) {
					continue;
				}
				ret += c;
				continue outer;
			}
			break;
		}
		return ret;
	}

	public String transformString(Random rand, String string, float progress) {
		String ret = "";
		IntSet glitched = pruneSet(new Random(rand.nextLong()), string.length(), Math.round(string.length() * (1 - progress)));
		for (int i = 0; i < string.length(); i++) {
			if (glitched.contains(i)) {
				ret += "§k";
			}
			ret += string.charAt(i);
		}
		return ret;
	}

	public void render(DrawContext raw, int mouseX, int mouseY, float delta) {
		EmiDrawContext context = EmiDrawContext.wrap(raw);
		long time = System.currentTimeMillis();
		long progress = time % 5_000;
		Random rand = new Random(time / 5_000);
		String[] parts = NAMES.get(rand.nextInt(NAMES.size())).clone();
		if (progress < 500) {
			float p = (progress / 500f);
			String[] orig = NAMES.get(new Random((time - 5_000) / 5_000).nextInt(NAMES.size())).clone();
			parts[0] = transformString(rand, interpolate(rand, orig[0], parts[0], p), 2);
			parts[1] = transformString(rand, interpolate(rand, orig[1], parts[1], p), 2);
			parts[2] = transformString(rand, interpolate(rand, orig[2], parts[2], p), 2);
		} else if (progress < 1_000) {
			float p = 1f - ((progress - 500) / 500f);
			parts[0] = transformString(rand, parts[0], p);
			parts[1] = transformString(rand, parts[1], p);
			parts[2] = transformString(rand, parts[2], p);
		} else if (progress >= 4_500) {
			float p = (progress - 4_500) / 500f;
			parts[0] = transformString(rand, parts[0], p);
			parts[1] = transformString(rand, parts[1], p);
			parts[2] = transformString(rand, parts[2], p);
		}

		context.drawCenteredTextWithShadow(
			EmiPort.literal(parts[0], Style.EMPTY.withColor(0xeb7bfc))
				.append(EmiPort.literal("  "))
				.append(EmiPort.literal(parts[1], Style.EMPTY.withColor(0x7bfca2)))
				.append(EmiPort.literal("  "))
				.append(EmiPort.literal(parts[2], Style.EMPTY.withColor(0x7bebfc))),
			x, y, -1);
	}
}
