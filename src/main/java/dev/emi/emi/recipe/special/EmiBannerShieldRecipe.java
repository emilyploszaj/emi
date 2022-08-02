package dev.emi.emi.recipe.special;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import dev.emi.emi.EmiPort;
import dev.emi.emi.api.recipe.EmiPatternCraftingRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.GeneratedSlotWidget;
import dev.emi.emi.api.widget.SlotWidget;
import net.minecraft.block.entity.BannerPattern;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BannerItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

public class EmiBannerShieldRecipe extends EmiPatternCraftingRecipe {
	public static final List<Item> BANNERS = List.of(
		Items.WHITE_BANNER, Items.ORANGE_BANNER, Items.MAGENTA_BANNER, Items.LIGHT_BLUE_BANNER,
		Items.YELLOW_BANNER, Items.LIME_BANNER, Items.PINK_BANNER, Items.GRAY_BANNER,
		Items.LIGHT_GRAY_BANNER, Items.CYAN_BANNER, Items.PURPLE_BANNER, Items.BLUE_BANNER,
		Items.BROWN_BANNER, Items.GREEN_BANNER, Items.RED_BANNER, Items.BLACK_BANNER
	);
	private static final List<EmiStack> EMI_BANNERS = BANNERS.stream().map(i -> EmiStack.of(i)).collect(Collectors.toList());
	public static final EmiStack SHIELD = EmiStack.of(Items.SHIELD);

	@SuppressWarnings("unchecked")
	public EmiBannerShieldRecipe(Identifier id) {
		super((List<EmiIngredient>) (List<?>) Stream.concat(Stream.of(SHIELD), EMI_BANNERS.stream()).toList(), EmiStack.of(Items.SHIELD), id);
	}

	@Override
	public SlotWidget getInputWidget(int slot, int x, int y) {
		if (slot == 0) {
			return new SlotWidget(SHIELD, x, y);
		} else if (slot == 1) {
			return new GeneratedSlotWidget(r -> getPattern(r, null), unique, x, y);
		}
		return new SlotWidget(EmiStack.EMPTY, x, y);
	}

	@Override
	public SlotWidget getOutputWidget(int x, int y) {
		return new GeneratedSlotWidget(r -> getPattern(r, Items.SHIELD), unique, x, y);
	}
	
	public EmiStack getPattern(Random random, Item item) {
		int base = random.nextInt(BANNERS.size());
		if (item == null) {
			item = BANNERS.get(base);
		}
		ItemStack stack = new ItemStack(item);
		int patterns = 1 + Math.max(random.nextInt(5), random.nextInt(3));
		BannerPattern.Patterns pattern = new BannerPattern.Patterns();
		for (int i = 0; i < patterns; i++) {
			pattern = EmiPort.addRandomBanner(pattern, random);
		}
		NbtCompound tag = new NbtCompound();
		tag.put("Patterns", pattern.toNbt());
		if (item == Items.SHIELD) {
			tag.putInt("Base", ((BannerItem) BANNERS.get(base)).getColor().getId());
		}
        BlockItem.setBlockEntityNbt(stack, BlockEntityType.BANNER, tag);
		//stack.setNbt(tag);
		return EmiStack.of(stack);
	}
}
