package dev.emi.emi.api.stack;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.compress.utils.Lists;

import dev.emi.emi.EmiClient;
import dev.emi.emi.EmiUtil;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.tag.TagKey;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

public interface EmiIngredient {
	public static final DecimalFormat TEXT_FORMAT = new DecimalFormat("0.##");
	public static final Text EMPTY_TEXT = new LiteralText("");
	public static final int RENDER_ICON = 1;
	public static final int RENDER_AMOUNT = 2;
	public static final int RENDER_INGREDIENT = 4;
	public static final int RENDER_REMAINDER = 8;
	
	List<EmiStack> getEmiStacks();

	default boolean isEmpty() {
		for (EmiStack stack : getEmiStacks()) {
			if (!stack.isEmpty()) {
				return false;
			}
		}
		return true;
	}

	int getAmount();

	default void render(MatrixStack matrices, int x, int y, float delta) {
		render(matrices, x, y, delta, -1);
	}

	void render(MatrixStack matrices, int x, int y, float delta, int flags);

	default Text getAmountText(float amount) {
		if (amount == 0) {
			return EMPTY_TEXT;
		} else {
			return new LiteralText(TEXT_FORMAT.format(amount));
		}
	}

	List<TooltipComponent> getTooltip();

	public static boolean areEqual(EmiIngredient a, EmiIngredient b) {
		List<EmiStack> as = a.getEmiStacks();
		List<EmiStack> bs = b.getEmiStacks();
		if (as.size() != bs.size()) {
			return false;
		}
		for (int i = 0; i < as.size(); i++) {
			if (!as.get(i).isEqual(bs.get(i))) {
				return false;
			}
		}
		return true;
	}

	public static EmiIngredient of(TagKey<Item> key) {
		return of(key, 1);
	}
	
	public static EmiIngredient of(TagKey<Item> key, int amount) {
		return new TagEmiIngredient(key, EmiUtil.values(key).map(ItemStack::new).map(EmiStack::of).toList(), amount);
	}

	public static EmiIngredient of(Ingredient ingredient) {
		return of(ingredient, 1);
	}
	
	public static EmiIngredient of(Ingredient ingredient, int amount) {
		if (ingredient == null) {
			return EmiStack.EMPTY;
		}
		List<Item> items = Arrays.stream(ingredient.getMatchingStacks())
			.filter(s -> !s.isEmpty())
			.map(i -> i.getItem())
			.distinct()
			.collect(Collectors.toList());
		if (items.size() == 0) {
			return EmiStack.EMPTY;
		} else if (items.size() == 1) {
			return EmiStack.of(ingredient.getMatchingStacks()[0]);
		}
		List<TagKey<Item>> keys = Lists.newArrayList();
		for (TagKey<Item> key : EmiClient.itemTags) {
			List<Item> values = EmiUtil.values(key).map(i -> i.value()).toList();
			if (values.size() < 2) {
				continue;
			}
			if (items.containsAll(values)) {
				items.removeAll(values);
				keys.add(key);
			}
			if (items.size() == 0) {
				break;
			}
		}
		if (keys.isEmpty()) {
			return new ListEmiIngredient(Arrays.stream(ingredient.getMatchingStacks()).map(EmiStack::of).toList(), amount);
		} else if (items.isEmpty()) {
			if (keys.size() == 1) {
				return new TagEmiIngredient(keys.get(0), amount);
			} else {
				return new ListEmiIngredient(keys.stream().map(k -> new TagEmiIngredient(k, amount)).toList(), amount);
			}
		} else {
			return new ListEmiIngredient(List.of(items.stream().map(ItemStack::new).map(EmiStack::of).toList(),
					keys.stream().map(k -> new TagEmiIngredient(k, amount)).toList())
				.stream().flatMap(a -> a.stream()).toList(), amount);
		}

	}

	public static EmiIngredient of(List<? extends EmiIngredient> list) {
		return of(list, 1);
	}

	public static EmiIngredient of(List<? extends EmiIngredient> list, int amount) {
		if (list.size() == 0) {
			return EmiStack.EMPTY;
		} else if (list.size() == 1) {
			return list.get(0);
		} else {
			return new ListEmiIngredient(list, amount);
		}
	}
}
