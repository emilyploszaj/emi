package dev.emi.emi.api.stack;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.compress.utils.Lists;

import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.tag.ItemTags;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;

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

	public static EmiIngredient of(Tag<Item> tag) {
		return new EmiTagIngredient(tag);
	}

	public static EmiIngredient of(Ingredient ingredient) {
		List<Item> items = Arrays.stream(ingredient.getMatchingStacks()).map(i -> i.getItem()).collect(Collectors.toList());
		if (items.size() == 0) {
			return EmiStack.EMPTY;
		} else if (items.size() == 1) {
			return EmiStack.of(items.get(0));
		}
		List<Tag<Item>> tags = Lists.newArrayList();
		for (Map.Entry<Identifier, Tag<Item>> entry : ItemTags.getTagGroup().getTags().entrySet().stream()
				.sorted((a, b) -> Integer.compare(b.getValue().values().size(), a.getValue().values().size())).toList()) {
			Tag<Item> tag = entry.getValue();
			if (tag.values().size() < 2) {
				continue;
			}
			if (items.containsAll(tag.values())) {
				items.removeAll(tag.values());
				tags.add(tag);
			}
			if (items.size() == 0) {
				break;
			}
		}
		if (tags.isEmpty()) {
			return new EmiIngredientList(Arrays.stream(ingredient.getMatchingStacks()).map(EmiStack::of).toList());
		} else if (items.isEmpty()) {
			if (tags.size() == 1) {
				return new EmiTagIngredient(tags.get(0));
			} else {
				return new EmiIngredientList(tags.stream().map(EmiTagIngredient::new).toList());
			}
		} else {
			return new EmiIngredientList(List.of(items.stream().map(ItemStack::new).map(EmiStack::of).toList(),
					tags.stream().map(EmiTagIngredient::new).toList())
				.stream().flatMap(a -> a.stream()).toList());
		}

	}

	public static EmiIngredient of(List<? extends EmiIngredient> list) {
		if (list.size() == 0) {
			return EmiStack.EMPTY;
		} else if (list.size() == 1) {
			return list.get(0);
		} else {
			return new EmiIngredientList(list);
		}
	}
}
