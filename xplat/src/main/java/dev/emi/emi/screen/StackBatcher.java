package dev.emi.emi.screen;


import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.SequencedMap;
import java.util.SequencedSet;
import java.util.Set;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.util.BufferAllocator;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.VertexSorter;

import dev.emi.emi.EmiPort;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.platform.EmiAgnos;
import dev.emi.emi.runtime.EmiLog;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.texture.Sprite;
import net.minecraft.item.ItemStack;

/**
 * @author Una "unascribed" Thompson
 */
public class StackBatcher {
	private static MethodHandle sodiumSpriteHandle;
	private static boolean isIncompatibleSodiumLoaded;

	static {
		try {
			Class<?> clazz = null;
			// TODO this is a 1.18 -> 1.19 refactor for Sodium
			try {
				clazz = Class.forName("net.caffeinemc.sodium.render.texture.SpriteUtil");
			} catch (Throwable t) {
			}
			if (clazz == null) {
				clazz = Class.forName("me.jellysquid.mods.sodium.client.render.texture.SpriteUtil");
			}
			sodiumSpriteHandle = MethodHandles.lookup()
				.findStatic(clazz, "markSpriteActive", MethodType.methodType(void.class, Sprite.class));
			if (sodiumSpriteHandle != null) {
				EmiLog.info("Discovered Sodium");
			}

			if(EmiAgnos.isModLoaded("sodium") || EmiAgnos.isModLoaded("rubidium")) {
				// Check for the modern VertexBufferWriter API. If so, we are likely on Sodium 0.5+ (or a derivative),
				// which can generally handle a custom VertexConsumer properly.
				try {
					Class.forName("net.caffeinemc.mods.sodium.api.vertex.buffer.VertexBufferWriter");
				} catch(Throwable t) {
					// Check for the legacy VBW API which *cannot* handle this
					try {
						Class.forName("me.jellysquid.mods.sodium.client.render.vertex.VertexBufferWriter");
						// Success means the class exists
						isIncompatibleSodiumLoaded = true;
						EmiLog.info("Batching stack renderer disabled for compatibility with legacy Sodium");
					} catch(Throwable t2) {
						// Old enough Sodiums shouldn't have an issue
					}
				}
			}
		} catch (Throwable e) {
		}
	}

	public interface Batchable {
		boolean isSideLit();
		boolean isUnbatchable();
		void setUnbatchable();
		void renderForBatch(VertexConsumerProvider vcp, DrawContext draw, int x, int y, int z, float delta);
	}

	private final BatcherVertexConsumerProvider imm;
	private final VertexConsumerProvider unlitFacade;
	private final Map<RenderLayer, VertexBuffer> buffers = new LinkedHashMap<>();
	private final Set<Sprite> spritesToUpdate = Sets.newHashSet();
	private boolean populated = false;
	private boolean dirty = false;
	private int x;
	private int y;
	private int z;

	public static final List<RenderLayer> EXTRA_RENDER_LAYERS = Lists.newArrayList();

	private static boolean isEnabled() {
		return EmiConfig.useBatchedRenderer && !isIncompatibleSodiumLoaded;
	}

	public StackBatcher() {
		SequencedMap<RenderLayer, BufferAllocator> buffers = new Object2ObjectLinkedOpenHashMap<>();
		assign(buffers, RenderLayer.getSolid());
		assign(buffers, RenderLayer.getCutout());
		assign(buffers, RenderLayer.getTranslucent());
		assign(buffers, TexturedRenderLayers.getEntitySolid());
		assign(buffers, TexturedRenderLayers.getEntityCutout());
		assign(buffers, TexturedRenderLayers.getEntityTranslucentCull());
		assign(buffers, RenderLayer.getGlint());
		assign(buffers, RenderLayer.getEntityGlint());
		for (RenderLayer layer : EXTRA_RENDER_LAYERS) {
			assign(buffers, layer);
		}
		imm = new BatcherVertexConsumerProvider(new BufferAllocator(256), buffers);
		unlitFacade = new UnlitFacade(imm);
	}

	private void assign(Map<RenderLayer, BufferAllocator> buffers, RenderLayer layer) {
		buffers.put(layer, new BufferAllocator(layer.getExpectedBufferSize()));
	}

	public boolean isPopulated() {
		return populated;
	}

	public void repopulate() {
		dirty = true;
	}

	public void begin(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
		if (dirty) {
			populated = false;
			dirty = false;
			spritesToUpdate.clear();
		}
	}

	public void render(Batchable batchable, DrawContext draw, int x, int y, float delta) {
		if (!populated) {
			try {
				batchable.renderForBatch(batchable.isSideLit() ? imm : unlitFacade, draw, x-this.x, -y+this.y, z, delta);
			} catch (Throwable t) {
				if (EmiConfig.devMode) {
					EmiLog.error("Batchable threw exception during batched rendering. See log for info");
					t.printStackTrace();
				}
				batchable.setUnbatchable();
			}
		}
	}

	public void render(EmiIngredient stack, DrawContext draw, int x, int y, float delta) {
		render(stack, draw, x, y, delta, -1 ^ EmiIngredient.RENDER_AMOUNT);
	}

	public void render(EmiIngredient stack, DrawContext draw, int x, int y, float delta, int flags) {
		if (stack instanceof Batchable b && !b.isUnbatchable() && isEnabled() && (flags & EmiIngredient.RENDER_ICON) != 0) {
			if (!populated) {
				try {
					b.renderForBatch(b.isSideLit() ? imm : unlitFacade, draw, x-this.x, y + this.y, z, delta);
					if (sodiumSpriteHandle != null && !stack.isEmpty()) {
						ItemStack is = stack.getEmiStacks().get(0).getItemStack();
						MinecraftClient client = MinecraftClient.getInstance();
						BakedModel model = client.getItemRenderer().getModels().getModel(is);
						if (model != null) {
							List<BakedQuad> quads = EmiPort.getQuads(model);
							for (BakedQuad quad : quads) {
								if (quad != null) {
									spritesToUpdate.add(quad.getSprite());
								}
							}
						}
					}
				} catch (Throwable t) {
					if (EmiConfig.devMode) {
						EmiLog.error("Stack threw exception during batched rendering. See log for info");
						t.printStackTrace();
					}
					b.setUnbatchable();
				}
			}
			stack.render(draw, x, y, delta, flags & (~EmiIngredient.RENDER_ICON));
		} else {
			stack.render(draw, x, y, delta, flags);
		}
	}

	public void draw() {
		if (!isEnabled()) {
			return;
		}
		if (sodiumSpriteHandle != null) {
			try {
				for (Sprite sprite : spritesToUpdate) {
					sodiumSpriteHandle.invoke(sprite);
				}
			} catch (Throwable t) {
			}
		}
		if (!populated) {
			bake();
			populated = true;
		}
		RenderSystem.enableDepthTest();
		DiffuseLighting.enableGuiDepthLighting();
		Matrix4f mat = new Matrix4f(RenderSystem.getModelViewMatrix());
		mat.mul(new Matrix4f().translation(x, y, 0));
		for (Map.Entry<RenderLayer, VertexBuffer> en : buffers.entrySet()) {
			en.getKey().startDrawing();
			EmiPort.setShader(en.getValue(), mat);
			en.getKey().endDrawing();
		}
		BufferRenderer.reset();
	}
	
	private void bake() {
		imm.drawCurrentLayer();
		buffers.values().forEach(VertexBuffer::close);
		buffers.clear();
		for (var entry : imm.getPendingLayerBuffers().entrySet()) {
			bake(entry.getKey(), entry.getValue());
		}
	}

	public void bake(RenderLayer layer, BufferBuilder bldr) {
		VertexBuffer vb = new VertexBuffer(VertexBuffer.Usage.DYNAMIC);
		EmiPort.upload(vb, bldr);
		buffers.put(layer, vb);
	}

	// Apparently BufferBuilder leaks memory in vanilla. Go figure
	public static class ClaimedCollection {
		private Set<StackBatcher> claimed = Sets.newHashSet();
		private List<StackBatcher> unclaimed = Lists.newArrayList();

		public StackBatcher claim() {
			StackBatcher batcher;
			if (unclaimed.isEmpty()) {
				batcher = new StackBatcher();
			} else {
				batcher = unclaimed.remove(unclaimed.size() - 1);
			}
			claimed.add(batcher);
			return batcher;
		}

		public void unclaim(StackBatcher batcher) {
			claimed.remove(batcher);
			unclaimed.add(batcher);
		}

		public void unclaimAll() {
			for (StackBatcher batcher : claimed) {
				unclaimed.add(batcher);
			}
			claimed.clear();
		}
	}

	/*
	 * This class is mostly a copy of a 1.21 implementation of VertexConsumerProvider.Immediate
	 * The reimplementation allows compatibility with shader mods, as well as less hackery.
	 */
	private static class BatcherVertexConsumerProvider implements VertexConsumerProvider {
		protected final BufferAllocator allocator;
		protected final SequencedMap<RenderLayer, BufferAllocator> layerBuffers;
		protected final Map<RenderLayer, BufferBuilder> pending = new HashMap<>();
		@Nullable
		protected RenderLayer currentLayer;

		protected BatcherVertexConsumerProvider(BufferAllocator allocator, SequencedMap<RenderLayer, BufferAllocator> layerBuffers) {
			this.allocator = allocator;
			this.layerBuffers = layerBuffers;
		}

		@Override
		public VertexConsumer getBuffer(RenderLayer renderLayer) {
			BufferBuilder bufferBuilder = this.pending.get(renderLayer);

			if (bufferBuilder != null && !renderLayer.areVerticesNotShared()) {
				this.draw(renderLayer, bufferBuilder);
				bufferBuilder = null;
			}

			if (bufferBuilder == null) {
				BufferAllocator bufferAllocator = this.layerBuffers.get(renderLayer);
				if (bufferAllocator != null) {
					bufferBuilder = new BufferBuilder(bufferAllocator, renderLayer.getDrawMode(), renderLayer.getVertexFormat());
				} else {
					if (this.currentLayer != null) {
						this.draw(this.currentLayer);
					}

					bufferBuilder = new BufferBuilder(this.allocator, renderLayer.getDrawMode(), renderLayer.getVertexFormat());
					this.currentLayer = renderLayer;
				}

				this.pending.put(renderLayer, bufferBuilder);
			}

			return bufferBuilder;
		}

		public void drawCurrentLayer() {
			if (this.currentLayer != null) {
				this.draw(this.currentLayer);
				this.currentLayer = null;
			}
		}

		public void draw(RenderLayer layer) {
			BufferBuilder bufferBuilder = this.pending.remove(layer);
			if (bufferBuilder != null) {
				this.draw(layer, bufferBuilder);
			}
		}

		public void draw(RenderLayer layer, BufferBuilder bufferBuilder) {
			BuiltBuffer builtBuffer = bufferBuilder.endNullable();
			if (builtBuffer != null) {
				BufferAllocator bufferAllocator = this.layerBuffers.getOrDefault(layer, this.allocator);
				builtBuffer.sortQuads(bufferAllocator, VertexSorter.BY_Z);
				layer.draw(builtBuffer);
			}

			if (layer.equals(this.currentLayer)) {
				this.currentLayer = null;
			}
		}

		public Map<RenderLayer, BufferBuilder> getPendingLayerBuffers() {
			return this.pending;
		}
	}

	private static class UnlitFacade implements VertexConsumerProvider {
		private final VertexConsumerProvider delegate;
		private final IdentityHashMap<VertexConsumer, VertexConsumer> cache = new IdentityHashMap<>();

		public UnlitFacade(VertexConsumerProvider delegate) {
			this.delegate = delegate;
		}

		@Override
		public VertexConsumer getBuffer(RenderLayer layer) {
			return cache.computeIfAbsent(delegate.getBuffer(layer), Consumer::new);
		}

		private static final class Consumer implements VertexConsumer {
			private final VertexConsumer delegate;

			private Consumer(VertexConsumer delegate) {
				this.delegate = delegate;
			}

			@Override
			public VertexConsumer normal(float x, float y, float z) {
				delegate.normal(0, -1, 0); // this is the change
				return this;
			}
			
			// all other methods are direct delegation

			@Override
			public VertexConsumer vertex(float x, float y, float z) {
				delegate.vertex(x, y, z);
				return this;
			}

			@Override
			public VertexConsumer texture(float u, float v) {
				delegate.texture(u, v);
				return this;
			}

			@Override
			public VertexConsumer overlay(int u, int v) {
				delegate.overlay(u, v);
				return this;
			}

			@Override
			public VertexConsumer light(int u, int v) {
				delegate.light(u, v);
				return this;
			}

			@Override
			public VertexConsumer color(int r, int g, int b, int a) {
				delegate.color(r, g, b, a);
				return this;
			}
		}
	}

}
