package dev.emi.emi.jemi.runtime;

import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import dev.emi.emi.api.stack.EmiStackInteraction;
import dev.emi.emi.config.SidebarType;
import dev.emi.emi.jemi.JemiUtil;
import dev.emi.emi.screen.EmiScreenManager;
import dev.emi.emi.screen.EmiScreenManager.SidebarPanel;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.common.filter.IFilterTextSource;
import mezz.jei.common.gui.GuiScreenHelper;
import mezz.jei.common.gui.overlay.IIngredientGridSource;
import mezz.jei.common.gui.overlay.IngredientGridWithNavigation;
import mezz.jei.common.gui.overlay.IngredientListOverlay;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.ingredients.RegisteredIngredients;
import mezz.jei.common.input.IKeyBindings;
import mezz.jei.common.network.IConnectionToServer;
import mezz.jei.core.config.IClientConfig;
import mezz.jei.core.config.IWorldConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;


/**
 * The extension of main work class instead of implementing interface is needed, as JEI do not have build in overwrite
 * functions yet. A lot of rely on implementation class.
 */
public class JemiIngredientListOverlay extends IngredientListOverlay {

	/**
	 * Simpler access to GuiScreenHelper.
	 */
	private final GuiScreenHelper guiScreenHelper;

	public JemiIngredientListOverlay(IIngredientGridSource ingredientGridSource,
		IFilterTextSource filterTextSource,
		RegisteredIngredients registeredIngredients,
		GuiScreenHelper guiScreenHelper,
		IngredientGridWithNavigation contents,
		IClientConfig clientConfig,
		IWorldConfig worldConfig,
		IConnectionToServer connectionToServer,
		Textures textures,
		IKeyBindings keyBindings) {
		super(ingredientGridSource,
			filterTextSource,
			registeredIngredients,
			guiScreenHelper,
			contents,
			clientConfig,
			worldConfig,
			connectionToServer,
			textures,
			keyBindings);

		this.guiScreenHelper = guiScreenHelper;
	}


	@Override
	public Optional<ITypedIngredient<?>> getIngredientUnderMouse() {
		EmiStackInteraction stack = EmiScreenManager.getHoveredStack(EmiScreenManager.lastMouseX, EmiScreenManager.lastMouseY, false);
		if (stack instanceof EmiScreenManager.SidebarEmiStackInteraction sesi && sesi.getType() == SidebarType.INDEX && sesi.getStack().getEmiStacks().size() == 1) {
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

	@Override
	public boolean isListDisplayed() {
		return !EmiScreenManager.isDisabled() && EmiScreenManager.hasSidebarAvailable(SidebarType.INDEX);
	}

	@Override
	public boolean hasKeyboardFocus() {
		return EmiScreenManager.search.isFocused();
	}

	@Override
	public <T> List<T> getVisibleIngredients(IIngredientType<T> ingredientType) {
		SidebarPanel panel = EmiScreenManager.getPanelFor(SidebarType.INDEX);
		if (panel != null) {
			return panel.space.getPage(panel.page).stream()
				.map(i -> JemiUtil.getTyped(i.getEmiStacks().get(0)))
				.filter(Optional::isPresent).map(Optional::get)
				.map(i -> i.getIngredient(ingredientType))
				.filter(Optional::isPresent).map(Optional::get)
				.toList();
		}
		return List.of();
	}


    public GuiScreenHelper getGuiScreenHelper() {
        return this.guiScreenHelper;
    }


	/**
	 * Disables JEI screen updating
	 */
	@Override
	public void updateScreen(@Nullable Screen guiScreen, boolean exclusionAreasChanged) {
	}


	/**
	 * Disables JEI screen drawing.
	 */
	@Override
	public void drawOnForeground(MinecraftClient minecraft,
		MatrixStack poseStack,
		HandledScreen<?> gui,
		int mouseX,
		int mouseY) {
	}


	/**
	 * Disabled JEI screen drawing
	 */
	@Override
	public void drawScreen(MinecraftClient minecraft, MatrixStack poseStack, int mouseX, int mouseY, float partialTicks) {
	}


	/**
	 * Disables JEI tooltip drawing
	 */
	@Override
	public void drawTooltips(MinecraftClient minecraft, MatrixStack poseStack, int mouseX, int mouseY) {
	}


	/**
	 * Disables JEI text field ticking
	 */
	@Override
	public void handleTick() {
	}
}
