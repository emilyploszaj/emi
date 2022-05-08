package dev.emi.emi.api.stack;

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

public interface EmiIngredient {
	
	List<EmiStack> getEmiStacks();

	default boolean isEmpty() {
		for (EmiStack stack : getEmiStacks()) {
			if (!stack.isEmpty()) {
				return false;
			}
		}
		return true;
	}

	void render(MatrixStack matrices, int x, int y, float delta);

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
		return new TagEmiIngredient(key, EmiUtil.values(key).map(ItemStack::new).map(EmiStack::of).toList());
	}

	public static EmiIngredient of(Ingredient ingredient) {
		List<Item> items = Arrays.stream(ingredient.getMatchingStacks()).map(i -> i.getItem()).collect(Collectors.toList());
		if (items.size() == 0) {
			return EmiStack.EMPTY;
		} else if (items.size() == 1) {
			return EmiStack.of(items.get(0));
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
			return new ListEmiIngredient(Arrays.stream(ingredient.getMatchingStacks()).map(EmiStack::of).toList());
		} else if (items.isEmpty()) {
			if (keys.size() == 1) {
				return new TagEmiIngredient(keys.get(0));
			} else {
				return new ListEmiIngredient(keys.stream().map(TagEmiIngredient::new).toList());
			}
		} else {
			return new ListEmiIngredient(List.of(items.stream().map(ItemStack::new).map(EmiStack::of).toList(),
					keys.stream().map(TagEmiIngredient::new).toList())
				.stream().flatMap(a -> a.stream()).toList());
		}

	}

	public static EmiIngredient of(List<? extends EmiIngredient> list) {
		if (list.size() == 0) {
			return EmiStack.EMPTY;
		} else if (list.size() == 1) {
			return list.get(0);
		} else {
			return new ListEmiIngredient(list);
		}
	}
}
