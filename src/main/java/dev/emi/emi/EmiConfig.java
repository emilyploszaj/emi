package dev.emi.emi;

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

import org.lwjgl.glfw.GLFW;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import dev.emi.emi.bind.EmiBind;
import dev.emi.emi.com.unascribed.qdcss.QDCSS;
import it.unimi.dsi.fastutil.doubles.Double2ObjectFunction;
import joptsimple.internal.Strings;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;

public class EmiConfig {
	private static Map<Class<?>, Setter> SETTERS = Maps.newHashMap();
	private static Map<Class<?>, Writer<?>> WRITERS = Maps.newHashMap();
	private static Map<Class<?>, MultiWriter<?>> MULTI_WRITERS = Maps.newHashMap();
	private static Map<String, List<String>> unparsed = Maps.newHashMap();

	// General
	@Comment("Whether EMI is enabled and visible.")
	@ConfigValue("general.enabled")
	public static boolean enabled = true;

	@Comment("Whether cheating in items is enabled.")
	@ConfigValue("general.cheat-mode")
	public static boolean cheatMode = false;

	// UI
	@Comment("Whether to move status effects to the\nleft of the screen.")
	@ConfigValue("ui.move-effects")
	public static boolean moveEffects = true;

	@Comment("Whether to have the search bar in the\ncenter of the screen, instead of to the side.")
	@ConfigValue("ui.center-search-bar")
	public static boolean centerSearchBar = true;

	@Comment("Whether to display a gray overlay when\nhovering over a stack.")
	@ConfigValue("ui.show-hover-overlay")
	public static boolean showHoverOverlay = false;

	@Comment("Whether to add mod name to item tooltips")
	@ConfigValue("ui.append-item-mod-id")
	public static boolean appendItemModId = true;

	@Comment("Whether an empty search should display\ncraftable recipes, instead of the index.")
	@ConfigValue("ui.empty-search-craftable")
	public static boolean emptySearchCraftable = false;

	@Comment("The amount of vertical margin to\ngive in the recipe screen.")
	@ConfigValue("ui.vertical-margin")
	public static int verticalMargin = 20;

	@Comment("Prevents recipes being quick crafted\nfrom shifting around under the cursor.")
	@ConfigValue("ui.miscraft-prevention")
	public static boolean miscraftPrevention = true;

	//@Comment("Maximum columns")
	//@ConfigValue("ui.max-columns")
	//public static int maxColumns = 9;

	@Comment("The unit to display fluids as.")
	@ConfigValue("ui.fluid-unit")
	public static FluidUnit fluidUnit = FluidUnit.LITERS;

	@Comment("Display cost per batch when hovering\na recipe output")
	@ConfigValue("ui.show-cost-per-batch")
	public static boolean showCostPerBatch = true;

	@Comment("Whether recipes should have a button to\nset as default.")
	@ConfigValue("ui.recipe-default-button")
	public static boolean recipeDefaultButton = true;

	@Comment("Whether recipes should have a button to\nshow the recipe tree.")
	@ConfigValue("ui.recipe-tree-button")
	public static boolean recipeTreeButton = true;

	@Comment("Whether recipes should have a button to\nfill the ingredients in a handler.")
	@ConfigValue("ui.recipe-fill-button")
	public static boolean recipeFillButton = true;

	@Comment("Whether to use the batched render system.\nBatching is faster, but may have incompatibilities"
		+ "\nwith shaders or other mods.")
	@ConfigValue("ui.use-batched-renderer")
	public static boolean useBatchedRenderer = true;


	// Binds
	@Comment("Toggle the visibility of EMI.")
	@ConfigValue("binds.toggle-visibility")
	public static EmiBind toggleVisibility = new EmiBind("key.emi.toggle_visibility", EmiUtil.CONTROL_MASK, GLFW.GLFW_KEY_O);

	@Comment("Focuse the search bar.")
	@ConfigValue("binds.focus-search")
	public static EmiBind focusSearch = new EmiBind("key.emi.focus_search", EmiUtil.CONTROL_MASK, GLFW.GLFW_KEY_F);

	@Comment("Display the recipes for creating an item.")
	@ConfigValue("binds.view-recipes")
	public static EmiBind viewRecipes = new EmiBind("key.emi.view_recipes",
		new EmiBind.ModifiedKey(InputUtil.Type.KEYSYM.createFromCode(GLFW.GLFW_KEY_R), 0),
		new EmiBind.ModifiedKey(InputUtil.Type.MOUSE.createFromCode(0), 0));

	@Comment("Display the recipes that can be created\nusing an item.")
	@ConfigValue("binds.view-uses")
	public static EmiBind viewUses = new EmiBind("key.emi.view_uses",
		new EmiBind.ModifiedKey(InputUtil.Type.KEYSYM.createFromCode(GLFW.GLFW_KEY_U), 0),
		new EmiBind.ModifiedKey(InputUtil.Type.MOUSE.createFromCode(1), 0));

	@Comment("Favorite the item to display on the\nside of the screen opposite of\nrecipies for quick access.")
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

	@Comment("Toggle between index and craftable\nsearch modes.")
	@ConfigValue("binds.toggle-craftable")
	public static EmiBind toggleCraftable = new EmiBind("key.emi.toggle_craftable", InputUtil.UNKNOWN_KEY.getCode());

	@Comment("Toggle craftable filter between\nall recipes and current workstation.")
	@ConfigValue("binds.toggle-local-craftable")
	public static EmiBind toggleLocalCraftable = new EmiBind("key.emi.toggle_local_craftable", InputUtil.UNKNOWN_KEY.getCode());

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
		+ "Move ingredients for a single result\nand put in inventory if possible.")
	@ConfigValue("binds.craft-one-to-inventory")
	public static EmiBind craftOneToInventory = new EmiBind("key.emi.craft_one_to_inventory", InputUtil.UNKNOWN_KEY.getCode());

	@Comment("When on a stack with an associated recipe:\n"
		+ "Move ingredients for as many results as possible\nand put in inventory if possible.")
	@ConfigValue("binds.craft-all-to-inventory")
	public static EmiBind craftAllToInventory = new EmiBind("key.emi.craft_all_to_inventory", InputUtil.UNKNOWN_KEY.getCode());

	@Comment("When on a stack with an associated recipe:\n"
		+ "Move ingredients for a single result\nand put in cursor if possible.")
	@ConfigValue("binds.craft-one-to-cursor")
	public static EmiBind craftOneToCursor = new EmiBind("key.emi.craft_one_to_cursor", 
		new EmiBind.ModifiedKey(InputUtil.Type.MOUSE.createFromCode(0), EmiUtil.CONTROL_MASK));

	@Comment("Cheat in one of an item into the inventory.")
	@ConfigValue("binds.cheat-one-to-inventory")
	public static EmiBind cheatOneToInventory = new EmiBind("key.emi.cheat_one_to_inventory",
		new EmiBind.ModifiedKey(InputUtil.Type.MOUSE.createFromCode(1), EmiUtil.CONTROL_MASK));

	@Comment("Cheat in a stack of an item into the inventory.")
	@ConfigValue("binds.cheat-stack-to-inventory")
	public static EmiBind cheatStackToInventory = new EmiBind("key.emi.cheat_stack_to_inventory",
		new EmiBind.ModifiedKey(InputUtil.Type.MOUSE.createFromCode(0), EmiUtil.CONTROL_MASK));
	
	@Comment("Cheat in one of an item into the cursor.")
	@ConfigValue("binds.cheat-one-cursor")
	public static EmiBind cheatOneToCursor = new EmiBind("key.emi.cheat_one_to_cursor",
		new EmiBind.ModifiedKey(InputUtil.Type.MOUSE.createFromCode(2), EmiUtil.CONTROL_MASK));
	
	@Comment("Cheat in a stack of an item into the cursor.")
	@ConfigValue("binds.cheat-stack-cursor")
	public static EmiBind cheatStackToCursor = new EmiBind("key.emi.cheat_stack_to_cursor", InputUtil.UNKNOWN_KEY.getCode());
	
	// Dev
	@Comment("Whether certain development functions should be enabled.\nNot recommended for general play.")
	@ConfigValue("dev.dev-mode")
	public static boolean devMode = FabricLoader.getInstance().isDevelopmentEnvironment();

	@Comment("Whether to log untranslated tags as warnings.")
	@ConfigValue("dev.log-untranslated-tags")
	public static boolean logUntranslatedTags = FabricLoader.getInstance().isDevelopmentEnvironment();

	@Comment("Whether hovering the output of a recipe should show\nthe recipe's EMI ID.")
	@ConfigValue("dev.show-recipe-ids")
	public static boolean showRecipeIds = false;

	@Comment("Whether stacks in the index should display a highlight\nif they have a recipe default.")
	@ConfigValue("dev.highlight-defaulted")
	public static boolean highlightDefaulted = false;

	// Persistent
	@ConfigValue("persistent.craftable")
	public static boolean craftable = false;

	@ConfigValue("persistent.local-craftable")
	public static boolean localCraftable = true;

	public static void load() {
		try {
			File config = getConfigFile();
			if (config.exists() && config.isFile()) {
				QDCSS css = QDCSS.load(config);
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
			}
			writeConfig();
		} catch (Exception e) {
			System.err.print("[emi] Error reading config");
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	public static void writeConfig() {
		try {
			Map<String, List<String>> unparsed = Maps.newLinkedHashMap();
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
						for (String line : comment.value().split("\n")) {
							commentText += "\t * ";
							commentText += line;
							commentText += "\n";
						}
						commentText += "\t */\n";
					}
					String text = commentText;
					if (MULTI_WRITERS.containsKey(field.getType())) {
						for (String line : ((MultiWriter<Object>) MULTI_WRITERS.get(field.getType())).writeValue(field.get(null))) {
							text += "\t" + key + ": " + line + ";\n";
						}
					} else {
						text += "\t" + key + ": " + ((Writer<Object>) WRITERS.get(field.getType())).writeValue(field.get(null)) + ";\n";
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
			FileWriter writer = new FileWriter(getConfigFile());
			writer.write("/** EMI Config */\n\n");
			boolean firstCategory = true;
			for (Map.Entry<String, List<String>> category : unparsed.entrySet()) {
				if (!firstCategory) {
					writer.write("\n");
				}
				firstCategory = false;

				writer.write("#" + category.getKey() + " {\n");
				writer.write(Strings.join(category.getValue(), "\n"));
				writer.write("}\n");
			}
			writer.close();
		} catch (Exception e) {
			System.err.print("[emi] Error writing config");
			e.printStackTrace();
		}
	}

	private static File getConfigFile() {
		String s = System.getProperty("emi.config");
		if (s != null) {
			File f = new File(s);
			if (f.exists() && f.isFile()) {
				return f;
			}
			System.err.println("[emi] System property 'emi.config' set to '" + s + "' but does not point to real file, using default config.");
		}
		return new File(FabricLoader.getInstance().getConfigDir().toFile(), "emi.css");
	}

	private static void assignField(QDCSS css, String annot, Field field) throws IllegalAccessException {
		Class<?> type = field.getType();
		Setter setter = SETTERS.get(type);
		if (setter != null) {
			setter.setValue(css, annot, field);
		} else {
			throw new RuntimeException("[emi] Unknown parsing type: " + type);
		}
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
		defineType(FluidUnit.class, (css, annot, field) -> field.set(null, FluidUnit.fromName(css.get(annot).get())));
	}
	
	@Target(ElementType.FIELD)
	@Retention(RetentionPolicy.RUNTIME)
	public static @interface ConfigValue {
		public String value();
	}
	
	@Target(ElementType.FIELD)
	@Retention(RetentionPolicy.RUNTIME)
	public static @interface Comment {
		public String value();
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

	public static interface ConfigEnum {

		public String getName();

		public Text getText();

		public ConfigEnum next();
	}

	public static enum FluidUnit implements ConfigEnum {
		LITERS("liters", a -> EmiPort.translatable("emi.fluid.amount.liters", (int) (a / 81))),
		MILLIBUCKETS("millibuckets", a -> EmiPort.translatable("emi.fluid.amount.millibuckets", (int) (a / 81))),
		DROPLETS("droplets", a -> EmiPort.translatable("emi.fluid.amount.droplets", (int) a)),
		;

		private final String name;
		private final Text translation;
		private final Double2ObjectFunction<Text> translator;

		private FluidUnit(String name, Double2ObjectFunction<Text> translator) {
			this.name = name;
			translation = EmiPort.translatable("emi.unit." + name);
			this.translator = translator;
		}

		@Override
		public String toString() {
			return name;
		}

		@Override
		public String getName() {
			return name;
		}

		public Text translate(double amount) {
			return translator.apply(Double.valueOf(amount));
		}

		@Override
		public Text getText() {
			return translation;
		}

		@Override
		public ConfigEnum next() {
			int i = this.ordinal() + 1;
			if (i < values().length) {
				return values()[i];
			} else {
				return values()[0];
			}
		}

		public static FluidUnit fromName(String name) {
			for (FluidUnit u : values()) {
				if (u.getName().equals(name)) {
					return u;
				}
			}
			return FluidUnit.LITERS;
		}
	}
}
