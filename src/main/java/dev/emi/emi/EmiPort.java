package dev.emi.emi;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Collection;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.mojang.brigadier.CommandDispatcher;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.Version;
import net.minecraft.block.entity.BannerPattern;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.resource.ResourceManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class EmiPort {
	static Impl impl;

	static {
		try {
			ModContainer container = FabricLoader.getInstance().getModContainer("minecraft").get();
			if (container.getMetadata().getVersion().compareTo(Version.parse("1.18.99")) <= 0) {
				Class<?> clazz = MethodHandles.lookup().findClass("dev.emi.emi.EmiPort1_18");
				MethodHandles.lookup().findStatic(clazz, "init", MethodType.methodType(void.class)).invoke();
			} else {
				Class<?> clazz = MethodHandles.lookup().findClass("dev.emi.emi.EmiPort1_19");
				MethodHandles.lookup().findStatic(clazz, "init", MethodType.methodType(void.class)).invoke();
			}
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}
	
	public static MutableText literal(String s) {
		return impl.literal(s);
	}
	
	public static MutableText translatable(String s) {
		return impl.translatable(s);
	}
	
	public static MutableText translatable(String s, Object... objects) {
		return impl.translatable(s, objects);
	}

	public static Text fluidName(FluidVariant fluid) {
		return impl.fluidName(fluid);
	}

	public static Collection<Identifier> findResources(ResourceManager manager, String prefix, Predicate<String> pred) {
		return impl.findResources(manager, prefix, pred);
	}

	public static void registerCommand(Consumer<CommandDispatcher<ServerCommandSource>> consumer) {
		impl.registerCommand(consumer);
	}

	public static BannerPattern.Patterns addRandomBanner(BannerPattern.Patterns patterns, Random random) {
		return impl.addRandomBanner(patterns, random);
	}

	public static void upload(VertexBuffer vb, BufferBuilder bldr) {
		impl.upload(vb, bldr);
		//bldr.end();
		//vb.upload(bldr);
	}

	public static void draw(BufferBuilder bufferBuilder) {
		impl.draw(bufferBuilder);
		//bufferBuilder.end();
		//BufferRenderer.drawWithoutShader(bufferBuilder);
	}

	public static abstract class Impl {

		public abstract MutableText literal(String s);
		
		public abstract MutableText translatable(String s);
		
		public abstract MutableText translatable(String s, Object... objects);

		public abstract Text fluidName(FluidVariant fluid);

		public abstract Collection<Identifier> findResources(ResourceManager manager, String prefix, Predicate<String> pred);

		public abstract void registerCommand(Consumer<CommandDispatcher<ServerCommandSource>> consumer);

		public abstract BannerPattern.Patterns addRandomBanner(BannerPattern.Patterns patterns, Random random);

		public abstract void upload(VertexBuffer vb, BufferBuilder bldr);

		public abstract void draw(BufferBuilder bufferBuilder);
	}
}
