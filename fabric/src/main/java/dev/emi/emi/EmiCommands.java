package dev.emi.emi;

import java.util.List;

import com.mojang.brigadier.Command;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import static net.minecraft.command.argument.IdentifierArgumentType.identifier;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class EmiCommands {
	public static final byte VIEW_RECIPE = 0x01;
	public static final byte VIEW_TREE = 0x02;
	public static final byte TREE_GOAL = 0x11;
	public static final byte TREE_RESOLUTION = 0x12;
	
	public static void init() {
		EmiFabric.registerCommand((dispatcher) -> {
			dispatcher.register(literal("emi")
				.requires(source -> source.hasPermissionLevel(2))
				.then(
					literal("view")
					.then(
						literal("recipe")
						.then(
							argument("id", identifier())
							.executes(context -> {
								send(context.getSource().getPlayer(), VIEW_RECIPE, List.of(context.getArgument("id", Identifier.class)));
								return Command.SINGLE_SUCCESS;
							})
						)
					)
					.then(
						literal("tree")
						.executes(context -> {
							send(context.getSource().getPlayer(), VIEW_TREE, List.of());
							return Command.SINGLE_SUCCESS;
						})
					)
				)
				.then(
					literal("tree")
					.then(
						literal("goal")
						.then(
							argument("id", identifier())
							.executes(context -> {
								send(context.getSource().getPlayer(), TREE_GOAL, List.of(context.getArgument("id", Identifier.class)));
								return Command.SINGLE_SUCCESS;
							})
						)
					)
					.then(
						literal("resolution")
						.then(
							argument("id", identifier())
							.executes(context -> {
								send(context.getSource().getPlayer(), TREE_RESOLUTION, List.of(context.getArgument("id", Identifier.class)));
								return Command.SINGLE_SUCCESS;
							})
						)
					)
				)
			);
		});
	}

	private static void send(ServerPlayerEntity player, byte id, List<Identifier> ids) {
		PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
		buf.writeByte(id);
		for (Identifier i : ids) {
			buf.writeIdentifier(i);
		}
		ServerPlayNetworking.send(player, EmiMain.COMMAND, buf);
	}
}
