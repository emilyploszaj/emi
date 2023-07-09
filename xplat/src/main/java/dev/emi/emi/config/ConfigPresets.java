package dev.emi.emi.config;

import java.util.List;

import org.lwjgl.glfw.GLFW;

import dev.emi.emi.com.unascribed.qdcss.QDCSS;
import dev.emi.emi.config.EmiConfig.Comment;
import dev.emi.emi.config.EmiConfig.ConfigGroup;
import dev.emi.emi.config.EmiConfig.ConfigValue;
import dev.emi.emi.input.EmiBind;
import dev.emi.emi.input.EmiInput;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;

public class ConfigPresets {

	// Sidebars

	@ConfigGroup("presets.sidebars")
	@Comment("Index on the right, a smaller set of craftables on the left, and favorites in the top left."
		+ " Ideal for getting the most out of EMI's features")
	@ConfigValue("presets.productive")
	public static Runnable productive = () -> {
		setPages(EmiConfig.rightSidebarPages, List.of(
			new SidebarPages.SidebarPage(SidebarType.INDEX)
		));

		setPages(EmiConfig.leftSidebarPages, List.of(
			new SidebarPages.SidebarPage(SidebarType.CRAFTABLES)
		));

		setPages(EmiConfig.topSidebarPages, List.of(
			new SidebarPages.SidebarPage(SidebarType.FAVORITES)
		));

		setPages(EmiConfig.bottomSidebarPages, List.of());

		EmiConfig.leftSidebarTheme = SidebarTheme.MODERN;
		EmiConfig.leftSidebarHeader = HeaderType.VISIBLE;

		MinecraftClient client = MinecraftClient.getInstance();
		if (client.getWindow().getScaledHeight() < 260) {
			EmiConfig.leftSidebarSize.values.set(0, 10);
			EmiConfig.leftSidebarSize.values.set(1, 8);
		} else {
			EmiConfig.leftSidebarSize.values.set(0, 10);
			EmiConfig.leftSidebarSize.values.set(1, 10);
		}

		EmiConfig.leftSidebarAlign = new ScreenAlign(
			ScreenAlign.Horizontal.RIGHT,
			ScreenAlign.Vertical.CENTER
		);

		EmiConfig.rightSidebarTheme = SidebarTheme.MODERN;
		EmiConfig.rightSidebarHeader = HeaderType.VISIBLE;
		
		EmiConfig.rightSidebarSize.values.set(0, 12);
		EmiConfig.rightSidebarSize.values.set(1, 100);

		EmiConfig.rightSidebarAlign = new ScreenAlign(
			ScreenAlign.Horizontal.RIGHT,
			ScreenAlign.Vertical.TOP
		);

		EmiConfig.topSidebarTheme = SidebarTheme.TRANSPARENT;
		EmiConfig.topSidebarHeader = HeaderType.INVISIBLE;
		
		EmiConfig.topSidebarSize.values.set(0, 12);
		EmiConfig.topSidebarSize.values.set(1, 4);

		EmiConfig.topSidebarAlign = new ScreenAlign(
			ScreenAlign.Horizontal.LEFT,
			ScreenAlign.Vertical.TOP
		);
	};

	@Comment("Use a smaller, recipe book styled panel on the left for craftables and favorites,"
		+ " and use a classic index on the right.")
	@ConfigValue("presets.recipe-book-plus")
	public static Runnable recipeBookPlus = () -> {
		setPages(EmiConfig.rightSidebarPages, List.of(
			new SidebarPages.SidebarPage(SidebarType.INDEX)
		));

		setPages(EmiConfig.leftSidebarPages, List.of(
			new SidebarPages.SidebarPage(SidebarType.CRAFTABLES),
			new SidebarPages.SidebarPage(SidebarType.FAVORITES)
		));

		setPages(EmiConfig.topSidebarPages, List.of());

		setPages(EmiConfig.bottomSidebarPages, List.of());

		EmiConfig.leftSidebarTheme = SidebarTheme.VANILLA;
		EmiConfig.leftSidebarHeader = HeaderType.VISIBLE;
		
		EmiConfig.leftSidebarSize.values.set(0, 8);
		EmiConfig.leftSidebarSize.values.set(1, 7);

		EmiConfig.leftSidebarAlign = new ScreenAlign(
			ScreenAlign.Horizontal.RIGHT,
			ScreenAlign.Vertical.CENTER
		);

		EmiConfig.rightSidebarTheme = SidebarTheme.MODERN;
		EmiConfig.rightSidebarHeader = HeaderType.VISIBLE;
		
		EmiConfig.rightSidebarSize.values.set(0, 12);
		EmiConfig.rightSidebarSize.values.set(1, 100);

		EmiConfig.rightSidebarAlign = new ScreenAlign(
			ScreenAlign.Horizontal.RIGHT,
			ScreenAlign.Vertical.TOP
		);
	};

	@Comment("Use index and craftables on the right panel, craftable by default, and index when searching")
	@ConfigValue("presets.empty-search-craftable")
	public static Runnable emptySearchCraftable = () -> {
		EmiConfig.searchSidebarFocus = SidebarType.INDEX;
		EmiConfig.emptySearchSidebarFocus = SidebarType.CRAFTABLES;
		
		setPages(EmiConfig.rightSidebarPages, List.of(
			new SidebarPages.SidebarPage(SidebarType.INDEX),
			new SidebarPages.SidebarPage(SidebarType.CRAFTABLES)
		));
		
		setPages(EmiConfig.leftSidebarPages, List.of(
			new SidebarPages.SidebarPage(SidebarType.FAVORITES)
		));

		setPages(EmiConfig.topSidebarPages, List.of());

		setPages(EmiConfig.bottomSidebarPages, List.of());
	};

	// Binds

	@ConfigGroup("presets.binds")
	@Comment("Use some of the binds EMI's author uses, do they know best?")
	@ConfigValue("presets.author-binds")
	public static Runnable authorBinds = () -> {
		EmiConfig.toggleVisibility.setBinds(
			EmiBind.ModifiedKey.of(GLFW.GLFW_KEY_O, EmiInput.CONTROL_MASK)
		);
		EmiConfig.focusSearch.setBinds(
			EmiBind.ModifiedKey.of(GLFW.GLFW_KEY_F, EmiInput.CONTROL_MASK)
		);
		EmiConfig.clearSearch.setBinds(
			EmiBind.ModifiedKey.of(GLFW.GLFW_KEY_D, EmiInput.CONTROL_MASK)
		);
		EmiConfig.viewRecipes.setToDefault();
		EmiConfig.viewUses.setToDefault();
		EmiConfig.favorite.setToDefault();
		EmiConfig.viewStackTree.setToDefault();
		EmiConfig.viewTree.setBinds(
			EmiBind.ModifiedKey.of(GLFW.GLFW_KEY_C, 0)
		);
		EmiConfig.back.setToDefault();
		EmiConfig.craftOne.setToDefault();
		EmiConfig.craftAll.setBinds();
		EmiConfig.craftOneToInventory.setBinds(
			new EmiBind.ModifiedKey(InputUtil.Type.MOUSE.createFromCode(1), EmiInput.SHIFT_MASK)
		);
		EmiConfig.craftAllToInventory.setBinds(
			new EmiBind.ModifiedKey(InputUtil.Type.MOUSE.createFromCode(0), EmiInput.SHIFT_MASK)
		);
		EmiConfig.showCraft.setToDefault();
		EmiConfig.cheatOneToInventory.setToDefault();
		EmiConfig.cheatStackToInventory.setToDefault();
		EmiConfig.cheatOneToCursor.setToDefault();
		EmiConfig.cheatStackToCursor.setToDefault();
	};

	// Resets

	@ConfigGroup("presets.restore")
	@Comment("Restore all settings to backup from most recently launch")
	@ConfigValue("presets.backup")
	public static Runnable backup = () -> {
		EmiConfig.loadConfig(QDCSS.load("backup", EmiConfig.startupConfig));
	};

	@Comment("Restore all default settings")
	@ConfigValue("presets.defaults")
	public static Runnable defaults = () -> {
		EmiConfig.loadConfig(QDCSS.load("defaults", EmiConfig.DEFAULT_CONFIG));
	};

	private static void setPages(SidebarPages pages, List<SidebarPages.SidebarPage> list) {
		pages.pages.clear();
		pages.pages.addAll(list);
	}
}
