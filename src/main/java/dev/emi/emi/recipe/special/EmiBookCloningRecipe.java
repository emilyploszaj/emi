package dev.emi.emi.recipe.special;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.GeneratedSlotWidget;
import dev.emi.emi.api.widget.SlotWidget;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.WrittenBookItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Identifier;

public class EmiBookCloningRecipe extends EmiPatternCraftingRecipe {
	// TODO more book stuff
	private static final List<String> AUTHORS = List.of(
		"Emi", "A Rabbit", "[REDACTED]", "Jeb", "The Multiversal Author's Guild", "Another Book", "A Collection of Branches",
		"Unknown"
	);
	private static final List<String> MOD_AUTHORS = FabricLoader.getInstance().getAllMods().stream()
		.flatMap(c -> c.getMetadata().getAuthors().stream()).map(p -> p.getName()).distinct().toList();
	private static final List<String> MOD = FabricLoader.getInstance().getAllMods().stream()
		.map(c -> c.getMetadata().getName()).toList();
	private static final List<String> NOUN = List.of(
		"Bunnies", "Apples", "Rocks", "Antimemetics", "a Rabbit", "Deers", "Mice", "a Dog", "Bnuuy", "Kitties",
		"Reconstruction", "Dawn", "Time", "Night", "the Sky",
		"Diamonds", "Pickaxes", "Planks", "Stone", "Slime", "Creepers", "Iron", "Redstone",
		"Trinkets", "Fungiculture", "Floralisia", "Chime", "Yttr", "Mnemonics", "Quilt"
	);
	private static final List<String> OBJECT = Stream.concat(List.of(
		"Fighting Monsters"
	).stream(), NOUN.stream()).toList();
	private static final List<String> ADJECTIVE = List.of(
		"Cool", "Introductory", "Diffused", "Wooden", "Slimey", "Dark", "Antimemetic", "Fun", "Fuzzy", "Tall",
		"Rotund", "Jovial", ""
	);
	private static final List<String> IMPERATIVE = List.of(
		"A Guide to", "Introduction to", "Discussing", "The Making of"
	);
	private static final List<Template> TEMPLATES = List.of(
		Template.of(IMPERATIVE, ADJECTIVE, OBJECT),
		Template.of(ADJECTIVE, OBJECT, "101"),
		Template.of(IMPERATIVE, OBJECT),
		Template.of(NOUN, "and", NOUN),
		Template.of(NOUN, "1st Edition"),
		Template.of(IMPERATIVE, MOD)
	);
	private static final EmiStack BOOK_AND_QUILL = EmiStack.of(Items.WRITABLE_BOOK);
	private static final EmiStack WRITTEN_BOOK = EmiStack.of(Items.WRITTEN_BOOK);

	public EmiBookCloningRecipe(Identifier id) {
		super(List.of(WRITTEN_BOOK, BOOK_AND_QUILL), WRITTEN_BOOK.copy().setAmount(2), id);
	}

	@Override
	public SlotWidget getInputWidget(int slot, int x, int y) {
		if (slot == 0) {
			return new GeneratedSlotWidget(r -> getWrittenBook(r, false), unique, x, y);
		} else if (slot == 1) {
			return new SlotWidget(BOOK_AND_QUILL, x, y);
		}
		return new SlotWidget(EmiStack.EMPTY, x, y);
	}

	@Override
	public SlotWidget getOutputWidget(int x, int y) {
		return new GeneratedSlotWidget(r -> getWrittenBook(r, true), unique, x, y);
	}
	
	private EmiStack getWrittenBook(Random random, boolean copy) {
		ItemStack stack = new ItemStack(Items.WRITTEN_BOOK);
		NbtCompound tag = new NbtCompound();
		String title = TEMPLATES.get(random.nextInt(TEMPLATES.size())).resolve(random);
		if (title.length() > 0) {
			title = Character.toUpperCase(title.charAt(0)) + title.substring(1, title.length());
		}
		tag.putString(WrittenBookItem.TITLE_KEY, title);
		String author;
		if (random.nextInt(20) < 5) {
			author = MOD_AUTHORS.get(random.nextInt(MOD_AUTHORS.size()));
		} else {
			author = AUTHORS.get(random.nextInt(AUTHORS.size()));
		}
		tag.putString(WrittenBookItem.AUTHOR_KEY, author);
		tag.putInt(WrittenBookItem.GENERATION_KEY, (copy ? 1 : 0) + random.nextInt(2));
		tag.put("pages", new NbtList());
		stack.setNbt(tag);
		return EmiStack.of(stack);
	}

	public static class Template {
		private List<Object> parts;

		private Template(List<Object> parts) {
			this.parts = parts;
		}

		public static Template of(Object... parts) {
			return new Template(Arrays.asList(parts));
		}

		public String resolve(Random random) {
			String ret = resolvePart(random, parts.get(0));
			for (int i = 1; i < parts.size(); i++) {
				ret += " " + resolvePart(random, parts.get(i));
			}
			return ret;
		}

		private String resolvePart(Random random, Object part) {
			if (part instanceof String s) {
				return s;
			} else if (part instanceof List<?> l) {
				return (String) l.get(random.nextInt(l.size()));
			}
			return "";
		}
	}
}
