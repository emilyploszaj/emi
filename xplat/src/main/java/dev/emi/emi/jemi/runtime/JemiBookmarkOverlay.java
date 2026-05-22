package dev.emi.emi.jemi.runtime;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import dev.emi.emi.api.stack.EmiStackInteraction;
import dev.emi.emi.config.SidebarType;
import dev.emi.emi.jemi.JemiUtil;
import dev.emi.emi.screen.EmiScreenManager;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.common.bookmarks.BookmarkList;
import mezz.jei.common.gui.GuiScreenHelper;
import mezz.jei.common.gui.overlay.IngredientGridWithNavigation;
import mezz.jei.common.gui.overlay.bookmarks.BookmarkOverlay;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.input.IKeyBindings;
import mezz.jei.common.network.IConnectionToServer;
import mezz.jei.core.config.IClientConfig;
import mezz.jei.core.config.IWorldConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;


/**
 * The extension of main work class instead of implementing interface is needed, as JEI do not have build in overwrite
 * functions yet. A lot of rely on implementation class.
 */
public class JemiBookmarkOverlay extends BookmarkOverlay {

	public JemiBookmarkOverlay(BookmarkList bookmarkList,
		Textures textures,
		IngredientGridWithNavigation contents,
		IClientConfig clientConfig,
		IWorldConfig worldConfig,
		GuiScreenHelper guiScreenHelper,
		IConnectionToServer serverConnection,
		IKeyBindings keyBindings) {
		super(bookmarkList,
			textures,
			contents,
			clientConfig,
			worldConfig,
			guiScreenHelper,
			serverConnection,
			keyBindings);
	}


	@Override
	public Optional<ITypedIngredient<?>> getIngredientUnderMouse() {
		EmiStackInteraction stack = EmiScreenManager.getHoveredStack(EmiScreenManager.lastMouseX, EmiScreenManager.lastMouseY, false);
		if (stack instanceof EmiScreenManager.SidebarEmiStackInteraction sesi && sesi.getType() == SidebarType.FAVORITES && sesi.getStack().getEmiStacks().size() == 1) {
			return JemiUtil.getTyped(stack.getStack().getEmiStacks().get(0));
		}
		return Optional.empty();
	}

	@Override
	public <T> @Nullable T getIngredientUnderMouse(IIngredientType<T> ingredientType) {
		Optional<ITypedIngredient<?>> opt = getIngredientUnderMouse();
		if (opt.isPresent()) {
			return opt.get().getIngredient(ingredientType).orElseGet(() -> null);
		}
		return null;
	}


	/**
	 * Disable JEI screen drawing code.
	 */
	@Override
	public void drawScreen(MinecraftClient minecraft, MatrixStack poseStack, int mouseX, int mouseY, float partialTicks) {
	}


	/**
	 * Disable JEI tooltip drawing code.
	 */
	@Override
	public void drawTooltips(MinecraftClient minecraft, MatrixStack poseStack, int mouseX, int mouseY) {
	}
}
