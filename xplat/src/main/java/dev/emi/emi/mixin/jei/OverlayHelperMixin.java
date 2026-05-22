package dev.emi.emi.mixin.jei;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import dev.emi.emi.jemi.runtime.JemiBookmarkOverlay;
import dev.emi.emi.jemi.runtime.JemiIngredientListOverlay;
import mezz.jei.common.bookmarks.BookmarkList;
import mezz.jei.common.filter.IFilterTextSource;
import mezz.jei.common.gui.GuiScreenHelper;
import mezz.jei.common.gui.overlay.IIngredientGridSource;
import mezz.jei.common.gui.overlay.IngredientGridWithNavigation;
import mezz.jei.common.gui.overlay.IngredientListOverlay;
import mezz.jei.common.gui.overlay.bookmarks.BookmarkOverlay;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.ingredients.RegisteredIngredients;
import mezz.jei.common.input.IKeyBindings;
import mezz.jei.common.network.IConnectionToServer;
import mezz.jei.common.startup.OverlayHelper;
import mezz.jei.core.config.IClientConfig;
import mezz.jei.core.config.IWorldConfig;


@Pseudo
@Mixin(value = OverlayHelper.class, remap = false)
public class OverlayHelperMixin {

    /**
     * Replaces JEI BookmarkOverlay with JemiBookmarkOverlay
     */
    @Redirect(method = "createBookmarkOverlay",
        at = @At(value = "NEW",
            target = "(Lmezz/jei/common/bookmarks/BookmarkList;Lmezz/jei/common/gui/textures/Textures;Lmezz/jei/common/gui/overlay/IngredientGridWithNavigation;Lmezz/jei/core/config/IClientConfig;Lmezz/jei/core/config/IWorldConfig;Lmezz/jei/common/gui/GuiScreenHelper;Lmezz/jei/common/network/IConnectionToServer;Lmezz/jei/common/input/IKeyBindings;)Lmezz/jei/common/gui/overlay/bookmarks/BookmarkOverlay;"
        )
    )
    private static BookmarkOverlay injectJemiBookmarkOverlay(BookmarkList bookmarkList,
        Textures textures,
        IngredientGridWithNavigation contents,
        IClientConfig clientConfig,
        IWorldConfig worldConfig,
        GuiScreenHelper guiScreenHelper,
        IConnectionToServer serverConnection,
        IKeyBindings keyBindings) {

        return new JemiBookmarkOverlay(bookmarkList,
            textures,
            contents,
            clientConfig,
            worldConfig,
            guiScreenHelper,
            serverConnection,
            keyBindings);
    }


    /**
     * Replaces JEI IngredientListOverlay with JemiIngredientListOverlay
     */
    @Redirect(method = "createIngredientListOverlay",
        at = @At(value = "NEW",
            target = "(Lmezz/jei/common/gui/overlay/IIngredientGridSource;Lmezz/jei/common/filter/IFilterTextSource;Lmezz/jei/common/ingredients/RegisteredIngredients;Lmezz/jei/common/gui/GuiScreenHelper;Lmezz/jei/common/gui/overlay/IngredientGridWithNavigation;Lmezz/jei/core/config/IClientConfig;Lmezz/jei/core/config/IWorldConfig;Lmezz/jei/common/network/IConnectionToServer;Lmezz/jei/common/gui/textures/Textures;Lmezz/jei/common/input/IKeyBindings;)Lmezz/jei/common/gui/overlay/IngredientListOverlay;"
        )
    )
    private static IngredientListOverlay injectJemiIngredientListOverlay(IIngredientGridSource ingredientGridSource,
        IFilterTextSource filterTextSource,
        RegisteredIngredients registeredIngredients,
        GuiScreenHelper guiScreenHelper,
        IngredientGridWithNavigation contents,
        IClientConfig clientConfig,
        IWorldConfig worldConfig,
        IConnectionToServer connectionToServer,
        Textures textures,
        IKeyBindings keyBindings) {
        return new JemiIngredientListOverlay(ingredientGridSource,
            filterTextSource,
            registeredIngredients,
            guiScreenHelper,
            contents,
            clientConfig,
            worldConfig,
            connectionToServer,
            textures,
            keyBindings);
    }
}
