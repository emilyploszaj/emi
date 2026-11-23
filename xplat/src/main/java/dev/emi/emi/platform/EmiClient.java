package dev.emi.emi.platform;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ItemLike;
import com.google.common.collect.Maps;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.handler.StandardRecipeHandler;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.network.EmiNetwork;
import dev.emi.emi.network.FillRecipeC2SPacket;

public class EmiClient {
	public static final Map<Consumer<UseOnContext>, List<ItemLike>> HOE_ACTIONS = Maps.newHashMap();
	public static boolean onServer = false;

	public static void init() {
		EmiConfig.loadConfig();
	}

	public static <T extends AbstractContainerMenu> void sendFillRecipe(StandardRecipeHandler<T> handler, AbstractContainerScreen<T> screen,
			int syncId, int action, List<ItemStack> stacks, EmiRecipe recipe) {
		T screenHandler = screen.getMenu();
		List<Slot> crafting = handler.getCraftingSlots(recipe, screenHandler);
		Slot output = handler.getOutputSlot(screenHandler);
		EmiNetwork.sendToServer(new FillRecipeC2SPacket(screenHandler, action, handler.getInputSources(screenHandler), crafting, output, stacks));
	}
}
