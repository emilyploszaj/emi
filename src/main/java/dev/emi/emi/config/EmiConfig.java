package dev.emi.emi.config;

import java.io.File;
import java.io.FileWriter;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.lwjgl.glfw.GLFW;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import dev.emi.emi.EmiLog;
import dev.emi.emi.EmiUtil;
import dev.emi.emi.bind.EmiBind;
import dev.emi.emi.com.unascribed.qdcss.QDCSS;
import it.unimi.dsi.fastutil.ints.IntList;
import joptsimple.internal.Strings;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.font.TextHandler;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;

public class EmiConfig {
	private static final Map<Class<?>, Setter> SETTERS = Maps.newHashMap();
	private static final Map<Class<?>, Writer<?>> WRITERS = Maps.newHashMap();
	private static final Map<Class<?>, MultiWriter<?>> MULTI_WRITERS = Maps.newHashMap();
	private static final Map<String, List<String>> unparsed = Maps.newHashMap();
	public static final Map<String, Predicate<?>> FILTERS = Maps.newHashMap();
	public static final String DEFAULT_CONFIG;
	public static String startupConfig;

	// General
	@Comment("Whether EMI is enabled and visible.")
	@ConfigValue("general.enabled")
	public static boolean enabled = true;

	@Comment("Whether cheating in items is enabled.")
	@ConfigValue("general.cheat-mode")
	public static boolean cheatMode = false;

	@ConfigGroup("general.search")
	@Comment("Whether normal search queries should include the tooltip.")
	@ConfigValue("general.search-tooltip-by-default")
	public static boolean searchTooltipByDefault = true;

	@Comment("Whether normal search queries should include the mod name.")
	@ConfigValue("general.search-mod-name-by-default")
	public static boolean searchModNameByDefault = false;

	@ConfigGroupEnd
	@Comment("Whether normal search queries should include the stack's tags.")
	@ConfigValue("general.search-tags-by-default")
	public static boolean searchTagsByDefault = false;

	// UI
	@Comment("Whether to move status effects to the top of the screen.")
	@ConfigValue("ui.move-effects")
	public static boolean moveEffects = true;

	@Comment("Whether to have the search bar in the center of the screen, instead of to the side.")
	@ConfigValue("ui.center-search-bar")
	public static boolean centerSearchBar = true;

	@Comment("Whether to display a gray overlay when hovering over a stack.")
	@ConfigValue("ui.show-hover-overlay")
	public static boolean showHoverOverlay = true;

	@Comment("Whether to add mod name to item tooltips, in case another mod provides behavior")
	@ConfigValue("ui.append-item-mod-id")
	public static boolean appendItemModId = true;

	@ConfigFilter("ui.search-sidebar-focus")
	private static Predicate<SidebarType> searchSidebarFocusFilter = type -> {
		return type != SidebarType.CHESS;
	};

	@Comment("Which sidebar type to switch to when searching.")
	@ConfigValue("ui.search-sidebar-focus")
	public static SidebarType searchSidebarFocus = SidebarType.INDEX;

	@ConfigFilter("ui.empty-search-sidebar-focus")
	private static Predicate<SidebarType> emptySearchSidebarFocusFilter = type -> {
		return type != SidebarType.CHESS;
	};

	@Comment("Which sidebar type to focus when the search is empty.")
	@ConfigValue("ui.empty-search-sidebar-focus")
	public static SidebarType emptySearchSidebarFocus = SidebarType.NONE;

	// Left sidebar

	@ConfigGroup("ui.left-sidebar")
	@Comment("The pages in the left sidebar")
	@ConfigValue("ui.left-sidebar-pages")
	public static SidebarPages leftSidebarPages = new SidebarPages(List.of(
		new SidebarPages.SidebarPage(SidebarType.FAVORITES)
	), () -> {
		return EmiConfig.leftSidebarSize.values.getInt(0) == 8
			&& EmiConfig.leftSidebarSize.values.getInt(1) == 8
			&& EmiConfig.leftSidebarTheme == SidebarTheme.MODERN;
	});
	
	@Comment("How many columns and rows of ingredients to limit the left sidebar to")
	@ConfigValue("ui.left-sidebar-size")
	public static IntGroup leftSidebarSize = new IntGroup(
		"emi.sidebar.size.",
		List.of("columns", "rows"),
		IntList.of(12, 100)
	);

	@Comment("How much space to maintain between the left sidebar and obstructions, in pixels")
	@ConfigValue("ui.left-sidebar-margins")
	public static Margins leftSidebarMargins = new Margins(2, 2, 2, 2);

	@Comment("Where to position the left sidebar")
	@ConfigValue("ui.left-sidebar-align")
	public static ScreenAlign leftSidebarAlign = new ScreenAlign(ScreenAlign.Horizontal.LEFT, ScreenAlign.Vertical.TOP);

	@Comment("Whether to render the header buttons and page count for the left sidebar")
	@ConfigValue("ui.left-sidebar-header")
	public static HeaderType leftSidebarHeader = HeaderType.VISIBLE;
	
	@ConfigGroupEnd
	@Comment("Which theme to use for the left sidebar")
	@ConfigValue("ui.left-sidebar-theme")
	public static SidebarTheme leftSidebarTheme = SidebarTheme.TRANSPARENT;


	// Right sidebar

	@ConfigGroup("ui.right-sidebar")
	@Comment("The pages in the right sidebar")
	@ConfigValue("ui.right-sidebar-pages")
	public static SidebarPages rightSidebarPages = new SidebarPages(List.of(
		new SidebarPages.SidebarPage(SidebarType.INDEX),
		new SidebarPages.SidebarPage(SidebarType.CRAFTABLES)
	), () -> {
		return EmiConfig.rightSidebarSize.values.getInt(0) == 8
			&& EmiConfig.rightSidebarSize.values.getInt(1) == 8
			&& EmiConfig.rightSidebarTheme == SidebarTheme.MODERN;
	});

	@Comment("How many columns and rows of ingredients to limit the right sidebar to")
	@ConfigValue("ui.right-sidebar-size")
	public static IntGroup rightSidebarSize = new IntGroup(
		"emi.sidebar.size.",
		List.of("columns", "rows"),
		IntList.of(12, 100)
	);

	@Comment("How much space to maintain between the right sidebar and obstructions, in pixels")
	@ConfigValue("ui.right-sidebar-margins")
	public static Margins rightSidebarMargins = new Margins(2, 2, 2, 2);

	@Comment("Where to position the right sidebar")
	@ConfigValue("ui.right-sidebar-align")
	public static ScreenAlign rightSidebarAlign = new ScreenAlign(ScreenAlign.Horizontal.RIGHT, ScreenAlign.Vertical.TOP);

	@Comment("Whether to render the header buttons and page count for the right sidebar")
	@ConfigValue("ui.right-sidebar-header")
	public static HeaderType rightSidebarHeader = HeaderType.VISIBLE;
	
	@ConfigGroupEnd
	@Comment("Which theme to use for the right sidebar")
	@ConfigValue("ui.right-sidebar-theme")
	public static SidebarTheme rightSidebarTheme = SidebarTheme.TRANSPARENT;

	// Top sidebar

	@ConfigGroup("ui.top-sidebar")
	@Comment("The pages in the top sidebar")
	@ConfigValue("ui.top-sidebar-pages")
	public static SidebarPages topSidebarPages = new SidebarPages(List.of(
	), () -> {
		return EmiConfig.topSidebarSize.values.getInt(0) == 8
			&& EmiConfig.topSidebarSize.values.getInt(1) == 8
			&& EmiConfig.topSidebarTheme == SidebarTheme.MODERN;
	});

	@Comment("How many columns and rows of ingredients to limit the top sidebar to")
	@ConfigValue("ui.top-sidebar-size")
	public static IntGroup topSidebarSize = new IntGroup(
		"emi.sidebar.size.",
		List.of("columns", "rows"),
		IntList.of(9, 9)
	);

	@Comment("How much space to maintain between the top sidebar and obstructions, in pixels")
	@ConfigValue("ui.top-sidebar-margins")
	public static Margins topSidebarMargins = new Margins(2, 2, 2, 2);

	@Comment("Where to position the top sidebar")
	@ConfigValue("ui.top-sidebar-align")
	public static ScreenAlign topSidebarAlign = new ScreenAlign(ScreenAlign.Horizontal.CENTER, ScreenAlign.Vertical.CENTER);

	@Comment("Whether to render the header buttons and page count for the top sidebar")
	@ConfigValue("ui.top-sidebar-header")
	public static HeaderType topSidebarHeader = HeaderType.VISIBLE;
	
	@ConfigGroupEnd
	@Comment("Which theme to use for the top sidebar")
	@ConfigValue("ui.top-sidebar-theme")
	public static SidebarTheme topSidebarTheme = SidebarTheme.TRANSPARENT;

	// Bottom sidebar

	@ConfigGroup("ui.bottom-sidebar")
	@Comment("The pages in the bottom sidebar")
	@ConfigValue("ui.bottom-sidebar-pages")
	public static SidebarPages bottomSidebarPages = new SidebarPages(List.of(
	), () -> {
		return EmiConfig.bottomSidebarSize.values.getInt(0) == 8
			&& EmiConfig.bottomSidebarSize.values.getInt(1) == 8
			&& EmiConfig.bottomSidebarTheme == SidebarTheme.MODERN;
	});

	@Comment("How many columns and rows of ingredients to limit the bottom sidebar to")
	@ConfigValue("ui.bottom-sidebar-size")
	public static IntGroup bottomSidebarSize = new IntGroup(
		"emi.sidebar.size.",
		List.of("columns", "rows"),
		IntList.of(9, 9)
	);

	@Comment("How much space to maintain between the bottom sidebar and obstructions, in pixels")
	@ConfigValue("ui.bottom-sidebar-margins")
	public static Margins bottomSidebarMargins = new Margins(2, 2, 2, 2);

	@Comment("Where to position the bottom sidebar")
	@ConfigValue("ui.bottom-sidebar-align")
	public static ScreenAlign bottomSidebarAlign = new ScreenAlign(ScreenAlign.Horizontal.CENTER, ScreenAlign.Vertical.CENTER);

	@Comment("Whether to render the header buttons and page count for the bottom sidebar")
	@ConfigValue("ui.bottom-sidebar-header")
	public static HeaderType bottomSidebarHeader = HeaderType.VISIBLE;
	
	@ConfigGroupEnd
	@Comment("Which theme to use for the bottom sidebar")
	@ConfigValue("ui.bottom-sidebar-theme")
	public static SidebarTheme bottomSidebarTheme = SidebarTheme.TRANSPARENT;

	//

	@Comment("The amount of vertical margin to give in the recipe screen.")
	@ConfigValue("ui.vertical-margin")
	public static int verticalMargin = 20;

	@Comment("Prevents recipes being quick crafted from shifting around under the cursor.")
	@ConfigValue("ui.miscraft-prevention")
	public static boolean miscraftPrevention = true;

	@Comment("The unit to display fluids as.")
	@ConfigValue("ui.fluid-unit")
	public static FluidUnit fluidUnit = FluidUnit.LITERS;

	@Comment("Whether to use the batched render system. Batching is faster, but may have incompatibilities"
		+ " with shaders or other mods.")
	@ConfigValue("ui.use-batched-renderer")
	public static boolean useBatchedRenderer = true;

	@Comment("Display cost per batch when hovering a recipe output")
	@ConfigValue("ui.show-cost-per-batch")
	public static boolean showCostPerBatch = true;

	@ConfigGroup("ui.recipe-buttons")
	@Comment("Whether recipes should have a button to set as default.")
	@ConfigValue("ui.recipe-default-button")
	public static boolean recipeDefaultButton = true;

	@Comment("Whether recipes should have a button to show the recipe tree.")
	@ConfigValue("ui.recipe-tree-button")
	public static boolean recipeTreeButton = true;

	@Comment("Whether recipes should have a button to fill the ingredients in a handler.")
	@ConfigValue("ui.recipe-fill-button")
	public static boolean recipeFillButton = true;

	@ConfigGroupEnd
	@Comment("Whether recipes should have a button to take a screenshot of the recipe.")
	@ConfigValue("ui.recipe-screenshot-button")
	public static boolean recipeScreenshotButton = false;

	@Comment("The GUI scale at which recipe screenshots are saved. Use 0 to use the current GUI scale.")
	@ConfigValue("ui.recipe-screenshot-scale")
	public static int recipeScreenshotScale = 0;


	// Binds
	@Comment("Toggle the visibility of EMI.")
	@ConfigValue("binds.toggle-visibility")
	public static EmiBind toggleVisibility = new EmiBind("key.emi.toggle_visibility", EmiUtil.CONTROL_MASK, GLFW.GLFW_KEY_O);

	@Comment("Focuse the search bar.")
	@ConfigValue("binds.focus-search")
	public static EmiBind focusSearch = new EmiBind("key.emi.focus_search", EmiUtil.CONTROL_MASK, GLFW.GLFW_KEY_F);

	@Comment("Clears the search bar.")
	@ConfigValue("binds.clear-search")
	public static EmiBind clearSearch = new EmiBind("key.emi.clear_search", InputUtil.UNKNOWN_KEY.getCode());

	@Comment("Display the recipes for creating an item.")
	@ConfigValue("binds.view-recipes")
	public static EmiBind viewRecipes = new EmiBind("key.emi.view_recipes",
		new EmiBind.ModifiedKey(InputUtil.Type.KEYSYM.createFromCode(GLFW.GLFW_KEY_R), 0),
		new EmiBind.ModifiedKey(InputUtil.Type.MOUSE.createFromCode(0), 0));

	@Comment("Display the recipes that can be created using an item.")
	@ConfigValue("binds.view-uses")
	public static EmiBind viewUses = new EmiBind("key.emi.view_uses",
		new EmiBind.ModifiedKey(InputUtil.Type.KEYSYM.createFromCode(GLFW.GLFW_KEY_U), 0),
		new EmiBind.ModifiedKey(InputUtil.Type.MOUSE.createFromCode(1), 0));

	@Comment("Favorite the item to display on the side of the screen opposite of recipies for quick access.")
	@ConfigValue("binds.favorite")
	public static EmiBind favorite = new EmiBind("key.emi.favorite", GLFW.GLFW_KEY_A);

	@Comment("Display the recipe tree for a given item.")
	@ConfigValue("binds.view-stack-tree")
	public static EmiBind viewStackTree = new EmiBind("key.emi.view_stack_tree", InputUtil.UNKNOWN_KEY.getCode());

	@Comment("Display the recipe tree.")
	@ConfigValue("binds.view-tree")
	public static EmiBind viewTree = new EmiBind("key.emi.view_tree", InputUtil.UNKNOWN_KEY.getCode());

	@Comment("Return to the previous page in EMI.")
	@ConfigValue("binds.back")
	public static EmiBind back = new EmiBind("key.emi.back", GLFW.GLFW_KEY_BACKSPACE);

	@ConfigGroup("binds.crafts")
	@Comment("When on a stack with an associated recipe:\n"
		+ "Move ingredients for a single result.")
	@ConfigValue("binds.craft-one")
	public static EmiBind craftOne = new EmiBind("key.emi.craft_one",
		new EmiBind.ModifiedKey(InputUtil.Type.MOUSE.createFromCode(0), 0));

	@Comment("When on a stack with an associated recipe:\n"
		+ "Move ingredients for as many results as possible.")
	@ConfigValue("binds.craft-all")
	public static EmiBind craftAll = new EmiBind("key.emi.craft_all", 
		new EmiBind.ModifiedKey(InputUtil.Type.MOUSE.createFromCode(0), EmiUtil.SHIFT_MASK));

	@Comment("When on a stack with an associated recipe:\n"
		+ "Move ingredients for a single result and put in inventory if possible.")
	@ConfigValue("binds.craft-one-to-inventory")
	public static EmiBind craftOneToInventory = new EmiBind("key.emi.craft_one_to_inventory", InputUtil.UNKNOWN_KEY.getCode());

	@Comment("When on a stack with an associated recipe:\n"
		+ "Move ingredients for as many results as possible and put in inventory if possible.")
	@ConfigValue("binds.craft-all-to-inventory")
	public static EmiBind craftAllToInventory = new EmiBind("key.emi.craft_all_to_inventory", InputUtil.UNKNOWN_KEY.getCode());

	@Comment("When on a stack with an associated recipe:\n"
		+ "Move ingredients for a single result and put in cursor if possible.")
	@ConfigValue("binds.craft-one-to-cursor")
	public static EmiBind craftOneToCursor = new EmiBind("key.emi.craft_one_to_cursor", 
		new EmiBind.ModifiedKey(InputUtil.Type.MOUSE.createFromCode(0), EmiUtil.CONTROL_MASK));

	@ConfigGroupEnd
	@Comment("Display the recipe that will be used to craft on a stack with no recipe context.")
	@ConfigValue("binds.show-craft")
	public static EmiBind showCraft = new EmiBind("key.emi.show_craft", GLFW.GLFW_KEY_LEFT_SHIFT);

	@ConfigGroup("binds.cheats")
	@Comment("Cheat in one of an item into the inventory.")
	@ConfigValue("binds.cheat-one-to-inventory")
	public static EmiBind cheatOneToInventory = new EmiBind("key.emi.cheat_one_to_inventory",
		new EmiBind.ModifiedKey(InputUtil.Type.MOUSE.createFromCode(1), EmiUtil.CONTROL_MASK));

	@Comment("Cheat in a stack of an item into the inventory.")
	@ConfigValue("binds.cheat-stack-to-inventory")
	public static EmiBind cheatStackToInventory = new EmiBind("key.emi.cheat_stack_to_inventory",
		new EmiBind.ModifiedKey(InputUtil.Type.MOUSE.createFromCode(0), EmiUtil.CONTROL_MASK));
	
	@Comment("Cheat in one of an item into the cursor.")
	@ConfigValue("binds.cheat-one-to-cursor")
	public static EmiBind cheatOneToCursor = new EmiBind("key.emi.cheat_one_to_cursor",
		new EmiBind.ModifiedKey(InputUtil.Type.MOUSE.createFromCode(2), EmiUtil.CONTROL_MASK));
	
	@ConfigGroupEnd
	@Comment("Cheat in a stack of an item into the cursor.")
	@ConfigValue("binds.cheat-stack-to-cursor")
	public static EmiBind cheatStackToCursor = new EmiBind("key.emi.cheat_stack_to_cursor", InputUtil.UNKNOWN_KEY.getCode());
	
	// Dev
	@Comment("Whether certain development functions should be enabled. Not recommended for general play.")
	@ConfigValue("dev.dev-mode")
	public static boolean devMode = FabricLoader.getInstance().isDevelopmentEnvironment();

	@Comment("Whether to log untranslated tags as warnings.")
	@ConfigValue("dev.log-untranslated-tags")
	public static boolean logUntranslatedTags = FabricLoader.getInstance().isDevelopmentEnvironment();

	@Comment("Whether hovering the output of a recipe should show the recipe's EMI ID.")
	@ConfigValue("dev.show-recipe-ids")
	public static boolean showRecipeIds = false;

	@Comment("Whether stacks in the index should display a highlight if they have a recipe default.")
	@ConfigValue("dev.highlight-defaulted")
	public static boolean highlightDefaulted = false;

	@Comment("Whether to display exclusion areas")
	@ConfigValue("dev.highlight-exclusion-areas")
	public static boolean highlightExclusionAreas = false;

	// Persistent (currently empty)

	public static void loadConfig() {
		try {
			File config = getConfigFile();
			if (config.exists() && config.isFile()) {
				QDCSS css = QDCSS.load(config);
				loadConfig(css);
			}
			if (startupConfig == null) {
				startupConfig = getSavedConfig();
			}
			writeConfig();
		} catch (Exception e) {
			EmiLog.error("Error reading config");
			e.printStackTrace();
		}
	}

	public static void loadConfig(QDCSS css) {
		try {
			Set<String> consumed = Sets.newHashSet();
			for (Field field : EmiConfig.class.getFields()) {
				ConfigValue annot = field.getAnnotation(ConfigValue.class);
				if (annot != null) {
					if (css.containsKey(annot.value())) {
						consumed.add(annot.value());
						assignField(css, annot.value(), field);
					}
				}
			}
			for (String key : css.keySet()) {
				if (!consumed.contains(key)) {
					unparsed.put(key, css.getAll(key));
				}
			}
		} catch (Exception e) {
			EmiLog.error("Error reading config");
			e.printStackTrace();
		}
	}

	public static void writeConfig() {
		try {
			FileWriter writer = new FileWriter(getConfigFile());
			writer.write(getSavedConfig());
			writer.close();
		} catch (Exception e) {
			EmiLog.error("Error writing config");
			e.printStackTrace();
		}
	}

	public static String getSavedConfig() {
		Map<String, List<String>> unparsed = Maps.newLinkedHashMap();
		TextHandler wrapper = new TextHandler((point, style) -> 1);
		for (Field field : EmiConfig.class.getFields()) {
			ConfigValue annot = field.getAnnotation(ConfigValue.class);
			if (annot != null) {
				String[] parts = annot.value().split("\\.");
				String group = parts[0];
				String key = parts[1];
				Comment comment = field.getAnnotation(Comment.class);
				String commentText = "";
				if (comment != null) {
					commentText += "\t/**\n";
					for (StringVisitable line : wrapper.wrapLines(comment.value(), 80, Style.EMPTY)) {
						commentText += "\t * ";
						commentText += line.getString();
						commentText += "\n";
					}
					commentText += "\t */\n";
				}
				String text = commentText;
				try {
					text += writeField(key, field);
				} catch (Exception e) {
					EmiLog.error("Error serializing config");
					e.printStackTrace();
				}
				unparsed.computeIfAbsent(group, g -> Lists.newArrayList()).add(text);
			}
		}
		for (Map.Entry<String, List<String>> entry : EmiConfig.unparsed.entrySet()) {
			String[] parts = entry.getKey().split("\\.");
			String group = parts[0];
			String key = parts[1];
			for (String value : entry.getValue()) {
				unparsed.computeIfAbsent(group, g -> Lists.newArrayList()).add("\t/** unparsed */\n\t" + key + ": "
					+ value + ";\n");
			}
		}
		String ret = "";
		ret += "/** EMI Config */\n\n";
		boolean firstCategory = true;
		for (Map.Entry<String, List<String>> category : unparsed.entrySet()) {
			if (!firstCategory) {
				ret += "\n";
			}
			firstCategory = false;

			ret += "#" + category.getKey() + " {\n";
			ret += Strings.join(category.getValue(), "\n");
			ret += "}\n";
		}
		return ret;
	}

	private static File getConfigFile() {
		String s = System.getProperty("emi.config");
		if (s != null) {
			File f = new File(s);
			if (f.exists() && f.isFile()) {
				return f;
			}
			EmiLog.error("System property 'emi.config' set to '" + s + "' but does not point to real file, using default config.");
		}
		return new File(FabricLoader.getInstance().getConfigDir().toFile(), "emi.css");
	}

	private static void assignField(QDCSS css, String annot, Field field) throws IllegalAccessException {
		Class<?> type = field.getType();
		Setter setter = SETTERS.get(type);
		if (setter != null) {
			setter.setValue(css, annot, field);
		} else if (ConfigEnum.class.isAssignableFrom(type)) {
			SETTERS.get(ConfigEnum.class).setValue(css, annot, field);
		} else {
			throw new RuntimeException("[emi] Unknown parsing type: " + type);
		}
	}

	@SuppressWarnings("unchecked")
	private static String writeField(String key, Field field) throws IllegalAccessException {
		String text = "";
		Class<?> type = field.getType();
		if (MULTI_WRITERS.containsKey(type)) {
			for (String line : ((MultiWriter<Object>) MULTI_WRITERS.get(type)).writeValue(field.get(null))) {
				text += "\t" + key + ": " + line + ";\n";
			}
		} else if (WRITERS.containsKey(type)) {
			text += "\t" + key + ": " + ((Writer<Object>) WRITERS.get(type)).writeValue(field.get(null)) + ";\n";
		} else if (ConfigEnum.class.isAssignableFrom(type)) {
			text += "\t" + key + ": " + ((Writer<Object>) WRITERS.get(ConfigEnum.class)).writeValue(field.get(null)) + ";\n";
		}
		return text;
	}

	private static void defineType(Class<?> clazz, Setter setter, Writer<?> writer) {
		SETTERS.put(clazz, setter);
		WRITERS.put(clazz, writer);
	}

	private static void defineType(Class<?> clazz, Setter setter) {
		defineType(clazz, setter, field -> field.toString());
	}

	private static void defineMultiType(Class<?> clazz, Setter setter, MultiWriter<?> writer) {
		SETTERS.put(clazz, setter);
		MULTI_WRITERS.put(clazz, writer);
	}

	static {
		defineType(boolean.class, (css, annot, field) -> field.setBoolean(null, css.getBoolean(annot).get()));
		defineType(int.class, (css, annot, field) -> field.setInt(null, css.getInt(annot).get()));
		defineType(double.class, (css, annot, field) -> field.setDouble(null, css.getDouble(annot).get()));
		defineType(String.class,
			(css, annot, field) -> {
				String s = css.get(annot).get();
				s = s.substring(1, s.length() - 1);
				field.set(null, s);
			},
			(String field) -> "\"" + field + "\"");
		defineMultiType(EmiBind.class,
			(css, annot, field) -> {
				List<String> strings = Lists.newArrayList(css.getAll(annot));
				for (int i = 0; i < strings.size(); i++) {
					String s = strings.get(i);
					strings.set(i, s.substring(1, s.length() - 1));
				}
				((EmiBind) field.get(null)).setKey(strings);
			},
			(EmiBind field) -> {
				List<String> list = Lists.newArrayList();
				for (EmiBind.ModifiedKey key : field.boundKeys) {
					if (!key.isUnbound() || field.boundKeys.size() == 1) {
						list.add("\"" + key.toName() + "\"");
					}
				}
				return list;
			});
		defineType(ScreenAlign.class,
			(css, annot, field) -> {
				String[] parts = css.get(annot).get().split(",");
				if (parts.length == 2) {
					((ScreenAlign) field.get(null)).horizontal = ScreenAlign.Horizontal.fromName(parts[0].strip());
					((ScreenAlign) field.get(null)).vertical = ScreenAlign.Vertical.fromName(parts[1].strip());
				} else {
					((ScreenAlign) field.get(null)).horizontal = ScreenAlign.Horizontal.CENTER;
					((ScreenAlign) field.get(null)).vertical = ScreenAlign.Vertical.CENTER;
				}
			},
			(ScreenAlign field) -> field.horizontal.getName() + ", " + field.vertical.getName());
		defineType(SidebarPages.class,
			(css, annot, field) -> {
				String[] parts = css.get(annot).get().split(",");
				SidebarPages pages = (SidebarPages) field.get(null);
				pages.pages.clear();
				for (String s : parts) {
					pages.pages.add(new SidebarPages.SidebarPage(SidebarType.fromName(s.trim().toLowerCase())));
				}
				pages.unique();
			},
			(SidebarPages field) -> {
				if (field.pages.isEmpty()) {
					return "none";
				} else {
					return field.pages.stream().map(p -> p.type.getName()).collect(Collectors.joining(", "));
				}
			});
		defineType(IntGroup.class, (css, annot, field) -> {
			((IntGroup) field.get(null)).deserialize(css.get(annot).get());
		}, (IntGroup group) -> group.serialize());
		defineType(Margins.class, (css, annot, field) -> {
			((Margins) field.get(null)).deserialize(css.get(annot).get());
		}, (Margins group) -> group.serialize());
		defineType(ConfigEnum.class, (css, annot, field) -> {
			String name = css.get(annot).get();
			for (ConfigEnum e : (ConfigEnum[]) field.getType().getEnumConstants()) {
				if (e.getName().equals(name)) {
					field.set(null, e);
					break;
				}
			}
		}, (ConfigEnum c) -> c.getName());
		try {
			for (Field field : EmiConfig.class.getDeclaredFields()) {
				ConfigFilter annot = field.getAnnotation(ConfigFilter.class);
				if (annot != null) {
					Predicate<?> predicate = (Predicate<?>) field.get(null);
					FILTERS.put(annot.value(), predicate);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		DEFAULT_CONFIG = getSavedConfig();
	}
	
	@Target(ElementType.FIELD)
	@Retention(RetentionPolicy.RUNTIME)
	public static @interface ConfigValue {
		public String value();
	}

	@Target(ElementType.FIELD)
	@Retention(RetentionPolicy.RUNTIME)
	public static @interface ConfigFilter {
		public String value();
	}
	
	@Target(ElementType.FIELD)
	@Retention(RetentionPolicy.RUNTIME)
	public static @interface Comment {
		public String value();
	}
	
	@Target(ElementType.FIELD)
	@Retention(RetentionPolicy.RUNTIME)
	public static @interface ConfigGroup {
		public String value();
	}
	
	@Target(ElementType.FIELD)
	@Retention(RetentionPolicy.RUNTIME)
	public static @interface ConfigGroupEnd {
	}

	private static interface Setter {
		void setValue(QDCSS css, String annot, Field field) throws IllegalAccessException ;
	}

	private static interface Writer<T> {
		String writeValue(T value);
	}

	private static interface MultiWriter<T> {
		List<String> writeValue(T value);
	}
}
