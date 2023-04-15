package dev.emi.emi.platform;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.handler.StandardRecipeHandler;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.network.EmiNetwork;
import dev.emi.emi.network.FillRecipeC2SPacket;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;

public class EmiClient {
	public static final Map<Consumer<ItemUsageContext>, List<ItemConvertible>> HOE_ACTIONS = Maps.newHashMap();
	public static boolean onServer = false;
	public static Set<Identifier> excludedTags = Sets.newHashSet();

	public static void init() {
		EmiConfig.loadConfig();
	}

	public static <T extends ScreenHandler> void sendFillRecipe(StandardRecipeHandler<T> handler, HandledScreen<T> screen,
			int syncId, int action, List<ItemStack> stacks, EmiRecipe recipe) {
		T screenHandler = screen.getScreenHandler();
		List<Slot> crafting = handler.getCraftingSlots(recipe, screenHandler);
		Slot output = handler.getOutputSlot(screenHandler);
		EmiNetwork.sendToServer(new FillRecipeC2SPacket(screenHandler, action, handler.getInputSources(screenHandler), crafting, output, stacks));
	}
}
