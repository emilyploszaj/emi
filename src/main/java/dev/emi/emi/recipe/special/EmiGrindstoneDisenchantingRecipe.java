package dev.emi.emi.recipe.special;

import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiUtil;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.client.gui.screen.ingame.GrindstoneScreen;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.util.Identifier;

import java.util.List;

public class EmiGrindstoneDisenchantingRecipe implements EmiRecipe {
    private static final Identifier BACKGROUND = new Identifier("minecraft", "textures/gui/container/grindstone.png");
    private final Item tool;
    private final Enchantment enchantment;
    private final int level;
    private final int uniq = EmiUtil.RANDOM.nextInt();

    public EmiGrindstoneDisenchantingRecipe(Item tool, Enchantment enchantment, int level) {
        this.tool = tool;
        this.enchantment = enchantment;
        this.level = level;
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return VanillaEmiRecipeCategories.GRINDING;
    }

    @Override
    public Identifier getId() {
        return null;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return List.of(checkBook());
    }

    @Override
    public List<EmiStack> getOutputs() {
        return List.of(getTool());
    }

    @Override
    public boolean supportsRecipeTree() {
        return false;
    }

    @Override
    public int getDisplayWidth() {
        return 130;
    }

    @Override
    public int getDisplayHeight() {
        return 61;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addTexture(BACKGROUND, 0, 0, 130, 61, 16, 14);

        widgets.addText(getExp(), 78, 39, -1, true);
        widgets.addSlot(getEnchantedTool(), 32, 4);
        widgets.addSlot(getTool(), 112, 19).recipeContext(this);

    }

    private EmiStack getEnchantedTool() {
        ItemStack enchantedTool = tool.getDefaultStack();
        enchantedTool.addEnchantment(enchantment, level);

        if (tool == Items.ENCHANTED_BOOK){
            ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
            NbtCompound tag = new NbtCompound();
            NbtList StoredEnchantments = new NbtList();
            NbtCompound enchant = new NbtCompound();
            String id = enchantedTool.getNbt().getList("Enchantments", NbtElement.COMPOUND_TYPE).getCompound(0).getString("id");

            enchant.putString("id", id);
            enchant.putShort("lvl", (short) level);
            StoredEnchantments.add(enchant);
            tag.put("StoredEnchantments", StoredEnchantments);
            book.setNbt(tag);

            return EmiStack.of(book);
        }

        return EmiStack.of(enchantedTool);
    }

    private EmiStack getTool(){
        if (tool == Items.ENCHANTED_BOOK){
            return EmiStack.of(Items.BOOK);
        }

        return  EmiStack.of(tool);
    }

    private EmiStack checkBook(){
        if (tool == Items.ENCHANTED_BOOK){
            return getEnchantedTool();
        }
        
        return EmiStack.of(tool);
    }

    private OrderedText getExp(){
        int minPower = enchantment.getMinPower(level);
        int minXP = (int)Math.ceil((double)minPower / 2.0);
        int maxXP = 2 * minXP - 1;
        return EmiPort.ordered(EmiPort.translatable("emi.grinding.experience", minXP, maxXP));
    }
}
