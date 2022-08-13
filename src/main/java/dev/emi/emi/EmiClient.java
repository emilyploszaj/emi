package dev.emi.emi;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.apache.commons.compress.utils.Lists;

import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.EmiRecipeHandler;
import dev.emi.emi.api.recipe.EmiPlayerInventory;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.bom.BoM;
import dev.emi.emi.data.EmiTagExclusionsLoader;
import dev.emi.emi.data.RecipeDefaultLoader;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.resource.ResourceType;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;

public class EmiClient implements ClientModInitializer {
	public static final Map<Consumer<ItemUsageContext>, List<EmiStack>> HOE_ACTIONS = Maps.newHashMap();
	public static final Set<Identifier> MODELED_TAGS = Sets.newHashSet();
	public static boolean onServer = false;
	public static Set<Identifier> excludedTags = Sets.newHashSet();
	public static List<TagKey<Item>> itemTags = List.of();
	// Flag used by a mixin
	public static boolean shiftTooltipsLeft = false;
	public static Map<EmiIngredient, Boolean> availableForCrafting = Maps.newHashMap();

	public static void getAvailable(EmiRecipe recipe) {
		availableForCrafting = new IdentityHashMap<>();
		MinecraftClient client = MinecraftClient.getInstance();
		List<Boolean> list = new EmiPlayerInventory(client.player).getCraftAvailability(recipe);
		List<EmiIngredient> inputs = recipe.getInputs();
		if (list.size() != inputs.size()) {
			return;
		}
		for (int i = 0; i < list.size(); i++) {
			availableForCrafting.put(inputs.get(i), list.get(i));
		}
	}

	@Override
	public void onInitializeClient() {
		EmiConfig.load();
		ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new RecipeDefaultLoader());
		ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new EmiTagExclusionsLoader());
		ModelLoadingRegistry.INSTANCE.registerModelProvider((manager, consumer) -> {
			MODELED_TAGS.clear();
			for (Identifier id : EmiPort.findResources(manager, "models/item/tags", s -> s.endsWith(".json"))) {
				String path = id.getPath();
				path = path.substring(0, path.length() - 5);
				String[] parts = path.substring(17).split("/");
				if (parts.length > 1) {
					MODELED_TAGS.add(new Identifier(parts[0], path.substring(18 + parts[0].length())));
					if (id.getNamespace().equals("emi")) {
						consumer.accept(new ModelIdentifier(id.getNamespace(), path.substring(12), "inventory"));
					}
				}
			}
		});


		ClientPlayConnectionEvents.INIT.register((handler, client) -> {
			EmiReloadManager.clear();
			onServer = false;
		});

		ClientPlayNetworking.registerGlobalReceiver(EmiMain.PING, (client, handler, buf, sender) -> {
			onServer = true;
		});

		ClientPlayNetworking.registerGlobalReceiver(EmiMain.COMMAND, (client, handler, buf, sender) -> {
			byte type = buf.readByte();
			if (type == EmiCommands.VIEW_RECIPE) {
				Identifier id = buf.readIdentifier();
				client.execute(() -> {
					EmiRecipe recipe = EmiRecipes.byId.get(id);
					if (recipe != null) {
						EmiApi.displayRecipe(recipe);
					}
				});
			} else if (type == EmiCommands.VIEW_TREE) {
				client.execute(() -> {
					EmiApi.viewRecipeTree();
				});
			} else if (type == EmiCommands.TREE_GOAL) {
				Identifier id = buf.readIdentifier();
				client.execute(() -> {
					EmiRecipe recipe = EmiRecipes.byId.get(id);
					if (recipe != null) {
						BoM.setGoal(recipe);
					}
				});
			} else if (type == EmiCommands.TREE_RESOLUTION) {
				Identifier id = buf.readIdentifier();
				client.execute(() -> {
					EmiRecipe recipe = EmiRecipes.byId.get(id);
					if (recipe != null && BoM.tree != null) {
						for (EmiStack stack : recipe.getOutputs()) {
							BoM.tree.addResolution(stack, recipe);
						}
					}
				});
			}
		});
	}
	
	private static void writeCompressedSlots(List<Slot> slots, PacketByteBuf buf) {
		List<Integer> list = slots.stream()
			.map(s -> s.id)
			.sorted()
			.distinct()
			.toList();
		List<Consumer<PacketByteBuf>> postWrite = Lists.newArrayList();
		int groups = 0;
		int i = 0;
		while (i < list.size()) {
			groups++;
			int start = i;
			int startValue = list.get(start);
			while (i < list.size() && i - start == list.get(i) - startValue) {
				i++;
			}
			int end = i - 1;
			postWrite.add(b -> {
				b.writeVarInt(startValue);
				b.writeVarInt(list.get(end));
			});
		}
		buf.writeVarInt(groups);
		for (Consumer<PacketByteBuf> consumer : postWrite) {
			consumer.accept(buf);
		}
	}
	
	public static <T extends ScreenHandler> void sendFillRecipe(EmiRecipeHandler<T> handler, HandledScreen<T> screen,
			int syncId, int action, List<ItemStack> stacks) {
		T screenHandler = screen.getScreenHandler();
		PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
		buf.writeInt(syncId);
		buf.writeByte(action);
		writeCompressedSlots(handler.getInputSources(screenHandler), buf);
		List<Slot> crafting = handler.getCraftingSlots(screenHandler);
		buf.writeVarInt(crafting.size());
		for (Slot s : crafting) {
			buf.writeVarInt(s == null ? -1 : s.id);
		}
		Slot output = handler.getOutputSlot(screenHandler);
		if (output != null) {
			buf.writeBoolean(true);
			buf.writeVarInt(output.id);
		} else {
			buf.writeBoolean(false);
		}
		buf.writeVarInt(stacks.size());
		for (ItemStack stack : stacks) {
			buf.writeItemStack(stack);
		}
		ClientPlayNetworking.send(EmiMain.FILL_RECIPE, buf);
	}
}
