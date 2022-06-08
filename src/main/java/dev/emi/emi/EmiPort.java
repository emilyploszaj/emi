package dev.emi.emi;

import java.util.Collection;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.mojang.brigadier.CommandDispatcher;

import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.block.entity.BannerPattern;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.resource.ResourceManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;

public class EmiPort {
	public static final String VERSION = "1.18.2";

	public static MutableText literal(String s) {
		return new LiteralText(s);
	}
	
	public static MutableText translatable(String s) {
		return new TranslatableText(s);
	}
	
	public static MutableText translatable(String s, Object... objects) {
		return new TranslatableText(s, objects);
	}

	public static Text fluidName(FluidVariant fluid) {
		return FluidVariantRendering.getName(fluid);
	}

	public static Collection<Identifier> findResources(ResourceManager manager, String prefix, Predicate<String> pred) {
		return manager.findResources(prefix, pred);
	}

	public static void registerCommand(Consumer<CommandDispatcher<ServerCommandSource>> consumer) {
		CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> consumer.accept(dispatcher));
	}

	public static BannerPattern.Patterns addRandomBanner(BannerPattern.Patterns patterns, Random random) {
		return patterns.add(BannerPattern.values()[random.nextInt(BannerPattern.values().length)],
				DyeColor.values()[random.nextInt(DyeColor.values().length)]);
	}

	public static void upload(VertexBuffer vb, BufferBuilder bldr) {
		bldr.end();
		vb.upload(bldr);
	}

	public static void draw(BufferBuilder bufferBuilder) {
		bufferBuilder.end();
		BufferRenderer.draw(bufferBuilder);
	}
	
}
