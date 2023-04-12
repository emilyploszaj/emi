package dev.emi.emi;

import static net.minecraft.command.argument.IdentifierArgumentType.identifier;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import org.jetbrains.annotations.Nullable;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;

import dev.emi.emi.network.CommandS2CPacket;
import dev.emi.emi.network.EmiNetwork;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class EmiCommands {
	public static final byte VIEW_RECIPE = 0x01;
	public static final byte VIEW_TREE = 0x02;
	public static final byte TREE_GOAL = 0x11;
	public static final byte TREE_RESOLUTION = 0x12;
	
	public static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(literal("emi")
			.requires(source -> source.hasPermissionLevel(2))
			.then(
				literal("view")
				.then(
					literal("recipe")
					.then(
						argument("id", identifier())
						.executes(context -> {
							send(context.getSource().getPlayer(), VIEW_RECIPE, context.getArgument("id", Identifier.class));
							return Command.SINGLE_SUCCESS;
						})
					)
				)
				.then(
					literal("tree")
					.executes(context -> {
						send(context.getSource().getPlayer(), VIEW_TREE, null);
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
							send(context.getSource().getPlayer(), TREE_GOAL, context.getArgument("id", Identifier.class));
							return Command.SINGLE_SUCCESS;
						})
					)
				)
				.then(
					literal("resolution")
					.then(
						argument("id", identifier())
						.executes(context -> {
							send(context.getSource().getPlayer(), TREE_RESOLUTION, context.getArgument("id", Identifier.class));
							return Command.SINGLE_SUCCESS;
						})
					)
				)
			)
		);
	}

	private static void send(ServerPlayerEntity player, byte type, @Nullable Identifier id) {
		EmiNetwork.sendToClient(player, new CommandS2CPacket(type, id));
	}
}
