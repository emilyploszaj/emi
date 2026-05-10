package dev.emi.emi.screen;

import com.google.common.collect.Lists;
import dev.emi.emi.api.EmiScreenBoundsProvider;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.mixin.accessor.HandledScreenAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.recipebook.RecipeBookProvider;
import net.minecraft.screen.ScreenHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmiScreenBase {

	private static final Map<Class<?>, List<EmiScreenBoundsProvider<?>>> PROVIDERS_BY_CLASS = new HashMap<>();
	private static final List<EmiScreenBoundsProvider<Screen>> GENERIC_PROVIDERS = new ArrayList<>();

	private final Screen screen;
	private final Bounds bounds;

	private static final EmiScreenBase EMPTY = new EmiScreenBase(null, Bounds.EMPTY);

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

	public static <T extends Screen> void addScreenBoundsProvider(Class<T> clazz, EmiScreenBoundsProvider<T> provider) {
		PROVIDERS_BY_CLASS.computeIfAbsent(clazz, k -> Lists.newArrayList()).add(provider);
	}

	public static void addGenericScreenBoundsProvider(EmiScreenBoundsProvider<Screen> provider) {
		GENERIC_PROVIDERS.add(provider);
	}

	public static void clearScreenBoundsProviders() {
		PROVIDERS_BY_CLASS.clear();
		GENERIC_PROVIDERS.clear();
	}

	public static EmiScreenBase of(Screen screen) {
		if (screen == null) {
			return EMPTY;
		}

		Class<?> screenClass = screen.getClass();
		List<EmiScreenBoundsProvider<?>> classProviders = PROVIDERS_BY_CLASS.get(screenClass);
		if (classProviders != null) {
			for (EmiScreenBoundsProvider<?> provider : classProviders) {
				@SuppressWarnings("unchecked")
				Bounds bounds = ((EmiScreenBoundsProvider<Screen>) provider).getBounds(screen);
				if (bounds != null && !bounds.empty()) {
					return new EmiScreenBase(screen, bounds);
				}
			}
		}
		for (EmiScreenBoundsProvider<Screen> provider : GENERIC_PROVIDERS) {
			Bounds bounds = provider.getBounds(screen);
			if (bounds != null && !bounds.empty()) {
				return new EmiScreenBase(screen, bounds);
			}
		}
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
		return EMPTY;
	}
}
