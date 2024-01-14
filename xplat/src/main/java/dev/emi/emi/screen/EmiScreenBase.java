package dev.emi.emi.screen;

import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.mixin.accessor.HandledScreenAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.recipebook.RecipeBookProvider;
import net.minecraft.screen.ScreenHandler;

public class EmiScreenBase {
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
	
	public static EmiScreenBase getCurrent() {
		MinecraftClient client = MinecraftClient.getInstance();
		Screen screen = client.currentScreen;
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
		}
		return null;
	}
}
