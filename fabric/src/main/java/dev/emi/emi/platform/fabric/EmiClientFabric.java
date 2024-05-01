package dev.emi.emi.platform.fabric;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

import com.google.common.collect.Lists;

import dev.emi.emi.data.EmiData;
import dev.emi.emi.network.CommandS2CPacket;
import dev.emi.emi.network.EmiChessPacket;
import dev.emi.emi.network.EmiNetwork;
import dev.emi.emi.network.EmiPacket;
import dev.emi.emi.network.PingS2CPacket;
import dev.emi.emi.platform.EmiClient;
import dev.emi.emi.registry.EmiTags;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.PreparableModelLoadingPlugin;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
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

		PreparableModelLoadingPlugin.<List<Identifier>>register((manager, executor) -> {
			return CompletableFuture.supplyAsync(() -> {
				List<Identifier> ids = Lists.newArrayList();
				EmiTags.registerTagModels(manager, id -> ids.add(id));
				return ids;
			}, executor);
		}, (ids, context) -> {
			context.addModels(ids);
		});

		EmiNetwork.initClient(packet -> {
			PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
			packet.write(buf);
			ClientPlayNetworking.send(packet.getId(), buf);
		});

		registerPacketReader(EmiNetwork.PING, PingS2CPacket::new);
		registerPacketReader(EmiNetwork.COMMAND, CommandS2CPacket::new);
		registerPacketReader(EmiNetwork.CHESS, EmiChessPacket.S2C::new);
	}

	private void registerPacketReader(Identifier id, Function<PacketByteBuf, EmiPacket> create) {
		ClientPlayNetworking.registerGlobalReceiver(id, (client, handler, buf, sender) -> {
			EmiPacket packet = create.apply(buf);
			client.execute(() -> {
				packet.apply(client.player);
			});
		});
	}
}
