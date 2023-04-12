package dev.emi.emi;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import dev.emi.emi.data.EmiData;
import dev.emi.emi.network.CommandS2CPacket;
import dev.emi.emi.network.EmiChessPacket;
import dev.emi.emi.network.EmiNetwork;
import dev.emi.emi.network.EmiPacket;
import dev.emi.emi.network.PingS2CPacket;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

public class EmiClientFabric implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		EmiClient.init();
		EmiData.init(reloader -> {
			ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new IdentifiableResourceReloadListener() {

				@Override
				public CompletableFuture<Void> reload(Synchronizer var1, ResourceManager var2, Profiler var3,
						Profiler var4, Executor var5, Executor var6) {
					return reloader.reload(var1, var2, var3, var4, var5, var6);
				}

				@Override
				public String getName() {
					return reloader.getName();
				}

				@Override
				public Identifier getFabricId() {
					return reloader.getEmiId();
				}
			});
		});
		ModelLoadingRegistry.INSTANCE.registerModelProvider((manager, consumer) -> {
			EmiClient.MODELED_TAGS.clear();
			for (Identifier id : EmiPort.findResources(manager, "models/item/tags", s -> s.endsWith(".json"))) {
				String path = id.getPath();
				path = path.substring(0, path.length() - 5);
				String[] parts = path.substring(17).split("/");
				if (parts.length > 1) {
					EmiClient.MODELED_TAGS.add(new Identifier(parts[0], path.substring(18 + parts[0].length())));
					if (id.getNamespace().equals("emi")) {
						consumer.accept(new ModelIdentifier(id.getNamespace(), path.substring(12), "inventory"));
					}
				}
			}
		});

		EmiNetwork.initClient((clazz, id, create) -> {
			ClientPlayNetworking.registerGlobalReceiver(id, (client, handler, buf, sender) -> {
				EmiPacket packet = create.apply(buf);
				client.execute(() -> {
					packet.apply(client.player);
				});
			});
		}, packet -> {
			PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
			packet.write(buf);
			ClientPlayNetworking.send(packet.getId(), buf);
		});

		ClientPlayNetworking.registerGlobalReceiver(EmiNetwork.PING, (client, handler, buf, sender) -> {
			new PingS2CPacket(buf).apply(client.player);
		});

		ClientPlayNetworking.registerGlobalReceiver(EmiNetwork.COMMAND, (client, handler, buf, sender) -> {
			EmiPacket packet = new CommandS2CPacket(buf);
			client.execute(() -> {
				packet.apply(client.player);
			});
		});

		ClientPlayNetworking.registerGlobalReceiver(EmiNetwork.CHESS, (client, handler, buf, sender) -> {
			EmiPacket packet = new EmiChessPacket.S2C(buf);
			client.execute(() -> {
				packet.apply(client.player);
			});
		});
	}
}
