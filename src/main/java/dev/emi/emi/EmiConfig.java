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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.lwjgl.glfw.GLFW;

import dev.emi.emi.bind.EmiBind;
import dev.emi.emi.com.unascribed.qdcss.QDCSS;
import joptsimple.internal.Strings;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public class EmiConfig {
	private static Map<Class<?>, Setter> SETTERS = Maps.newHashMap();
	private static Map<Class<?>, Writer<?>> WRITERS = Maps.newHashMap();
	private static Map<Class<?>, MultiWriter<?>> MULTI_WRITERS = Maps.newHashMap();
	private static Map<String, String> unparsed = Maps.newHashMap();

	// General

	// UI
	@Comment("Whether to move status effects to the left of the screen.")
	@ConfigValue("ui.move-effects")
	public static boolean moveEffects = true;

	@Comment("The amount of vertical padding to give in the recipe screen.")
	@ConfigValue("ui.vertical-padding")
	public static int verticalPadding = 20;

	//@Comment("Maximum columns")
	//@ConfigValue("ui.max-columns")
	//public static int maxColumns = 14;

	@Comment("The unit to display fluids as.")
	@ConfigValue("ui.fluid-unit")
	public static FluidUnit fluidUnit = FluidUnit.LITERS;

	@Comment("Whether recipes should have a button to favorite the result.")
	@ConfigValue("ui.recipe-favorite-button")
	public static boolean recipeFavoriteButton = true;

	@Comment("Whether recipes should have a button to set as default.")
	@ConfigValue("ui.recipe-default-button")
	public static boolean recipeDefaultButton = true;

	@Comment("Whether recipes should have a button to show the recipe tree.")
	@ConfigValue("ui.recipe-tree-button")
	public static boolean recipeTreeButton = true;

	@Comment("Whether recipes should have a button to fill the ingredients in a handler.")
	@ConfigValue("ui.recipe-fill-button")
	public static boolean recipeFillButton = true;

	// Binds
	@Comment("Toggles the visibility of EMI.")
	@ConfigValue("binds.toggle-visibility")
	public static EmiBind toggleVisibility = new EmiBind("key.emi.toggle_visibility", EmiUtil.CONTROL_MASK, GLFW.GLFW_KEY_O);

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

	@Comment("Define a recipe's output as the default way to obtain it.\nUsed for constructing recipe trees.")
	@ConfigValue("binds.set-default")
	public static EmiBind setDefault = new EmiBind("key.emi.set_default", InputUtil.UNKNOWN_KEY.getCode());

	@Comment("Display the recipe tree for a given item.")
	@ConfigValue("binds.view-tree")
	public static EmiBind viewTree = new EmiBind("key.emi.view_tree", InputUtil.UNKNOWN_KEY.getCode());

	@Comment("When on a stack with an associated recipe.\n"
		+ "Move ingredients for a single result.")
	@ConfigValue("binds.craft-one")
	public static EmiBind craftOne = new EmiBind("key.emi.craft_one", InputUtil.UNKNOWN_KEY.getCode());

	@Comment("When on a stack with an associated recipe.\n"
		+ "Move ingredients for as many results as possible.")
	@ConfigValue("binds.craft-all")
	public static EmiBind craftAll = new EmiBind("key.emi.craft_all", 
		new EmiBind.ModifiedKey(InputUtil.Type.MOUSE.createFromCode(0), EmiUtil.SHIFT_MASK));

	@Comment("When on a stack with an associated recipe.\n"
		+ "Move ingredients for a single result and put in inventory if possible.")
	@ConfigValue("binds.craft-one-to-inventory")
	public static EmiBind craftOneToInventory = new EmiBind("key.emi.craft_one_to_inventory", InputUtil.UNKNOWN_KEY.getCode());

	@Comment("When on a stack with an associated recipe.\n"
		+ "Move ingredients for as many results as possible and put in inventory if possible.")
	@ConfigValue("binds.craft-all-to-inventory")
	public static EmiBind craftAllToInventory = new EmiBind("key.emi.craft_all_to_inventory", InputUtil.UNKNOWN_KEY.getCode());

	@Comment("When on a stack with an associated recipe.\n"
		+ "Move ingredients for a single result and put in cursor if possible.")
	@ConfigValue("binds.craft-one-to-cursor")
	public static EmiBind craftOneToCursor = new EmiBind("key.emi.craft_one_to_cursor", 
		new EmiBind.ModifiedKey(InputUtil.Type.MOUSE.createFromCode(0), EmiUtil.CONTROL_MASK));
	
	// Dev
	@Comment("Whether certain development functions should be enabled.\nNot recommended for general play.")
	@ConfigValue("dev.dev-mode")
	public static boolean devMode = FabricLoader.getInstance().isDevelopmentEnvironment();

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
						unparsed.put(key, css.get(key).get());
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
			for (Map.Entry<String, String> entry : EmiConfig.unparsed.entrySet()) {
				String[] parts = entry.getKey().split("\\.");
				String group = parts[0];
				String key = parts[1];
				unparsed.computeIfAbsent(group, g -> Lists.newArrayList()).add("\t/** unparsed */\n\t" + key + ": "
					+ entry.getValue() + ";\n");
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
		LITERS("liters"),
		MILLIBUCKETS("millibuckets"),
		;

		private final String name;
		private final Text translation;

		private FluidUnit(String name) {
			this.name = name;
			translation = new TranslatableText("emi.unit." + name);
		}

		@Override
		public String toString() {
			return name;
		}

		@Override
		public String getName() {
			return name;
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
