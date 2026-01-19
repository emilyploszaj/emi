package dev.emi.emi.screen;

import dev.emi.emi.api.EmiScreenTransformer;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.EmiScreenBaseBounds;
import dev.emi.emi.mixin.accessor.HandledScreenAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.recipebook.RecipeBookProvider;
import net.minecraft.screen.ScreenHandler;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class EmiScreenBase {

	protected static List<EmiScreenTransformer> transformers = new ArrayList<>();

	private final Screen screen;
	private final Bounds bounds;

	private EmiScreenBase(Screen screen, Bounds bounds) {
		this.screen = screen;
		this.bounds = bounds;
	}

	public Screen screen() {
		return screen;
	}

	public Bounds bounds() {
		return bounds;
	}

	public boolean isEmpty() {
		return screen == null;
	}
	
	public static EmiScreenBase getCurrent() {
		MinecraftClient client = MinecraftClient.getInstance();
		return of(client.currentScreen);
	}

	public static void addtransformers(EmiScreenTransformer transformer) {
		transformers.add(transformer);
	}

	public static void cleartransformers() {
		transformers.clear();
	}

	public static void sortTransformers() {
		transformers.sort(Comparator.comparingInt(EmiScreenTransformer::getPriority).reversed());
	}

	public static EmiScreenBase of(Screen screen) {
		if (screen instanceof HandledScreen hs) {
			HandledScreenAccessor hsa = (HandledScreenAccessor) hs;
			ScreenHandler sh = hs.getScreenHandler();
			if (sh.slots != null && !sh.slots.isEmpty()) {
				int extra = 0;
				if (hs instanceof RecipeBookProvider provider) {
					if (provider.getRecipeBookWidget().isOpen()) {
						extra = 177;
					}
				}
				Bounds bounds = new Bounds(hsa.getX() - extra, hsa.getY(), hsa.getBackgroundWidth() + extra, hsa.getBackgroundHeight());
				return new EmiScreenBase(screen, bounds);
			}
		} else if (screen instanceof RecipeScreen rs) {
			return new EmiScreenBase(rs, rs.getBounds());
		} else {
			for (EmiScreenTransformer transformer : transformers) {
				if (!transformer.canTransform(screen)) {
					continue;
				}
				EmiScreenBaseBounds bounds = transformer.transform(screen);
				if (bounds != null && !bounds.isEmpty()) {
					return new EmiScreenBase(bounds.screen(), bounds.bounds());
				}
			}
		}
		return new EmiScreenBase(null, Bounds.EMPTY);
	}
}
