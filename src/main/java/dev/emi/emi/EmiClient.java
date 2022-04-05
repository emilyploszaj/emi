package dev.emi.emi;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.data.RecipeDefaultLoader;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

public class EmiClient implements ClientModInitializer {
	public static final Map<Consumer<ItemUsageContext>, List<EmiStack>> HOE_ACTIONS = Maps.newHashMap();
	public static final Set<Identifier> MODELED_TAGS = Sets.newHashSet();

	@Override
	public void onInitializeClient() {
		EmiConfig.load();
		ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new RecipeDefaultLoader());
		ModelLoadingRegistry.INSTANCE.registerModelProvider((manager, consumer) -> {
			for (Identifier id : manager.findResources("models/item/tags", s -> s.endsWith(".json"))) {
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
			}
		});
	}
	
	
	public static void sendFillRecipe(int syncId, int action, List<ItemStack> stacks) {
		PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
		buf.writeInt(syncId);
		buf.writeByte(action);
		buf.writeVarInt(stacks.size());
		for (ItemStack stack : stacks) {
			buf.writeItemStack(stack);
		}
		ClientPlayNetworking.send(EmiMain.FILL_RECIPE, buf);
	}
}
