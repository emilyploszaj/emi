package dev.emi.emi.registry;

import static net.minecraft.commands.arguments.ResourceLocationArgument.id;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

import org.jetbrains.annotations.Nullable;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;

import dev.emi.emi.network.CommandS2CPacket;
import dev.emi.emi.network.EmiNetwork;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class EmiCommands {
	public static final byte VIEW_RECIPE = 0x01;
	public static final byte VIEW_TREE = 0x02;
	public static final byte TREE_GOAL = 0x11;
	public static final byte TREE_RESOLUTION = 0x12;
	
	public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(literal("emi")
			.requires(source -> source.hasPermission(2))
			.then(
				literal("view")
				.then(
					literal("recipe")
					.then(
						argument("id", id())
						.executes(context -> {
							send(context.getSource().getPlayer(), VIEW_RECIPE, context.getArgument("id", ResourceLocation.class));
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
						argument("id", id())
						.executes(context -> {
							send(context.getSource().getPlayer(), TREE_GOAL, context.getArgument("id", ResourceLocation.class));
							return Command.SINGLE_SUCCESS;
						})
					)
				)
				.then(
					literal("resolution")
					.then(
						argument("id", id())
						.executes(context -> {
							send(context.getSource().getPlayer(), TREE_RESOLUTION, context.getArgument("id", ResourceLocation.class));
							return Command.SINGLE_SUCCESS;
						})
					)
				)
			)
		);
	}

	private static void send(ServerPlayer player, byte type, @Nullable ResourceLocation id) {
		EmiNetwork.sendToClient(player, new CommandS2CPacket(type, id));
	}
}
