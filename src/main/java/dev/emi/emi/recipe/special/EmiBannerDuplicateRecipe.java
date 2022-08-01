package dev.emi.emi.recipe.special;

import java.util.List;
import java.util.Random;

import dev.emi.emi.EmiPort;
import dev.emi.emi.api.recipe.EmiPatternCraftingRecipe;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.GeneratedSlotWidget;
import dev.emi.emi.api.widget.SlotWidget;

import net.minecraft.block.entity.BannerPattern;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

public class EmiBannerDuplicateRecipe extends EmiPatternCraftingRecipe {

    public static final List<Item> BANNERS = List.of(
            Items.WHITE_BANNER, Items.ORANGE_BANNER, Items.MAGENTA_BANNER, Items.LIGHT_BLUE_BANNER,
            Items.YELLOW_BANNER, Items.LIME_BANNER, Items.PINK_BANNER, Items.GRAY_BANNER,
            Items.LIGHT_GRAY_BANNER, Items.CYAN_BANNER, Items.PURPLE_BANNER, Items.BLUE_BANNER,
            Items.BROWN_BANNER, Items.GREEN_BANNER, Items.RED_BANNER, Items.BLACK_BANNER
    );

    private final Item banner;

    public EmiBannerDuplicateRecipe(Item banner, Identifier id) {
        super(List.of(
                EmiStack.of(banner),
                EmiStack.of(banner).setRemainder(EmiStack.of(banner))),
                EmiStack.of(banner), id);
        this.banner = banner;
    }

    @Override
    public SlotWidget getInputWidget(int slot, int x, int y) {
        if (slot == 0) {
            return new SlotWidget(EmiStack.of(banner), x, y);
        } else if (slot == 1) {
            return new GeneratedSlotWidget(r -> getPattern(r, true), unique, x, y);
        }
        return new SlotWidget(EmiStack.EMPTY, x, y);
    }

    @Override
    public SlotWidget getOutputWidget(int x, int y) {
        return new GeneratedSlotWidget(r -> getPattern(r, false) , unique, x, y);
    }

    public EmiStack getPattern(Random random, boolean reminder) {
        ItemStack stack = new ItemStack(banner);
        int patterns = 1 + Math.max(random.nextInt(5), random.nextInt(3));
        BannerPattern.Patterns pattern = new BannerPattern.Patterns();
        for (int i = 0; i < patterns; i++) {
            pattern = EmiPort.addRandomBanner(pattern, random);
        }

        NbtCompound tag = new NbtCompound();
        tag.put("Patterns", pattern.toNbt());

        BlockItem.setBlockEntityNbt(stack, BlockEntityType.BANNER, tag);
        //stack.setNbt(tag);
        EmiStack emiStack = EmiStack.of(stack);
        if (reminder) {
            emiStack.setRemainder(EmiStack.of(stack));
        }
        return emiStack;
    }
}
