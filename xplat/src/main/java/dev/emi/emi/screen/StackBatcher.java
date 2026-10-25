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
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexSorting;
import dev.emi.emi.EmiPort;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.platform.EmiAgnos;
import dev.emi.emi.runtime.EmiLog;

/**
 * @author Una "unascribed" Thompson
 */
public class StackBatcher {
	private static MethodHandle sodiumSpriteHandle;

	static {
		try {
			Class<?> clazz = null;
			try {
				// Try Sodium 0.5 name
				clazz = Class.forName("me.jellysquid.mods.sodium.client.render.texture.SpriteUtil");
			} catch (Throwable t) {
			}
			sodiumSpriteHandle = MethodHandles.lookup()
				.findStatic(clazz, "markSpriteActive", MethodType.methodType(void.class, TextureAtlasSprite.class));
			if (sodiumSpriteHandle != null) {
				EmiLog.info("Discovered Sodium");
			}
		} catch (Throwable e) {
		}
	}

	public interface Batchable {
		boolean isSideLit();
		boolean isUnbatchable();
		void setUnbatchable();
		void renderForBatch(MultiBufferSource vcp, GuiGraphics draw, int x, int y, int z, float delta);
	}

	private final BatcherVertexConsumerProvider imm;
	private final MultiBufferSource unlitFacade;
	private final Map<RenderType, VertexBuffer> buffers = new LinkedHashMap<>();
	private final Set<TextureAtlasSprite> spritesToUpdate = Sets.newHashSet();
	private boolean populated = false;
	private boolean dirty = false;
	private int x;
	private int y;
	private int z;

	public static final List<RenderType> EXTRA_RENDER_LAYERS = Lists.newArrayList();

	public static boolean isEnabled() {
		return EmiConfig.useBatchedRenderer;
	}

	public StackBatcher() {
		Map<RenderType, ByteBufferBuilder> buffers = new HashMap<>();
		assign(buffers, RenderType.solid());
		assign(buffers, RenderType.cutout());
		assign(buffers, RenderType.translucent());
		assign(buffers, Sheets.solidBlockSheet());
		assign(buffers, Sheets.cutoutBlockSheet());
		assign(buffers, Sheets.translucentCullBlockSheet());
		assign(buffers, RenderType.glint());
		//assign(buffers, RenderLayer.getDirectGlint());
		assign(buffers, RenderType.entityGlint());
		for (RenderType layer : EXTRA_RENDER_LAYERS) {
			assign(buffers, layer);
		}
		imm = new BatcherVertexConsumerProvider(new ByteBufferBuilder(256), buffers);
		unlitFacade = new UnlitFacade(imm);
	}

	private void assign(Map<RenderType, ByteBufferBuilder> buffers, RenderType layer) {
		buffers.put(layer, new ByteBufferBuilder(layer.bufferSize()));
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

	public void render(Batchable batchable, GuiGraphics draw, int x, int y, float delta) {
		if (!populated) {
			try {
				batchable.renderForBatch(batchable.isSideLit() ? imm : unlitFacade, draw, x-this.x, y+this.y, z, delta);
			} catch (Throwable t) {
				if (EmiConfig.devMode) {
					EmiLog.error("Batchable threw exception during batched rendering. See log for info", t);
				}
				batchable.setUnbatchable();
			}
		}
	}

	public void render(EmiIngredient stack, GuiGraphics draw, int x, int y, float delta) {
		render(stack, draw, x, y, delta, -1 ^ EmiIngredient.RENDER_AMOUNT);
	}

	public void render(EmiIngredient stack, GuiGraphics draw, int x, int y, float delta, int flags) {
		if (stack instanceof Batchable b && !b.isUnbatchable() && isEnabled() && (flags & EmiIngredient.RENDER_ICON) != 0) {
			if (!populated) {
				try {
					b.renderForBatch(b.isSideLit() ? imm : unlitFacade, draw, x-this.x, y + this.y, z, delta);
					if (sodiumSpriteHandle != null && !stack.isEmpty()) {
						ItemStack is = stack.getEmiStacks().get(0).getItemStack();
						Minecraft client = Minecraft.getInstance();
						BakedModel model = client.getItemRenderer().getItemModelShaper().getItemModel(is);
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
						EmiLog.error("Stack threw exception during batched rendering. See log for info", t);
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
				for (TextureAtlasSprite sprite : spritesToUpdate) {
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
		Lighting.setupFor3DItems();
		Matrix4f mat = new Matrix4f(RenderSystem.getModelViewMatrix());
		mat.mul(new Matrix4f().translation(x, y, 0));
		for (Map.Entry<RenderType, VertexBuffer> en : buffers.entrySet()) {
			en.getKey().setupRenderState();
			EmiPort.setShader(en.getValue(), mat);
			en.getKey().clearRenderState();
		}
		BufferUploader.reset();
	}
	
	private void bake() {
		imm.drawCurrentLayer();
		buffers.values().forEach(VertexBuffer::close);
		buffers.clear();
		for (Map.Entry<RenderType, BufferBuilder> entry : imm.getPendingLayerBuffers().entrySet()) {
			bake(entry.getKey(), entry.getValue());
		}
		imm.getPendingLayerBuffers().clear();
	}

	public void bake(RenderType layer, BufferBuilder bldr) {
		MeshData builtBuffer = bldr.build();
		if (builtBuffer == null) {
			return;
		}
		VertexBuffer vb = new VertexBuffer(VertexBuffer.Usage.DYNAMIC);
		vb.bind();
		vb.upload(builtBuffer);
		buffers.put(layer, vb);
	}

	// Apparently BufferBuilder leaks memory in vanilla. Go figure
	public static class ClaimedCollection {
		private Set<StackBatcher> claimed = Sets.newHashSet();
		private List<StackBatcher> unclaimed = Lists.newArrayList();

		public StackBatcher claim() {
			synchronized (this) {
				StackBatcher batcher;
				if (unclaimed.isEmpty()) {
					batcher = new StackBatcher();
				} else {
					batcher = unclaimed.remove(unclaimed.size() - 1);
				}
				if (batcher == null) {
					batcher = new StackBatcher();
				}
				claimed.add(batcher);
				return batcher;
			}
		}

		public void unclaim(StackBatcher batcher) {
			synchronized (this) {
				claimed.remove(batcher);
				unclaimed.add(batcher);
			}
		}

		public void unclaimAll() {
			synchronized (this) {
				for (StackBatcher batcher : claimed) {
					unclaimed.add(batcher);
				}
				claimed.clear();
			}
		}
	}

	/*
	 * This class is mostly a copy of a 1.21 implementation of VertexConsumerProvider.Immediate
	 * The reimplementation allows compatibility with shader mods, as well as less hackery.
	 */
	private static class BatcherVertexConsumerProvider implements MultiBufferSource {
		protected final ByteBufferBuilder fallbackBuffer;
		protected final Map<RenderType, ByteBufferBuilder> layerBuffers;
		protected final Map<RenderType, BufferBuilder> pending = new HashMap<>();
		protected RenderType currentLayer = null;

		protected BatcherVertexConsumerProvider(ByteBufferBuilder fallbackBuffer, Map<RenderType, ByteBufferBuilder> layerBuffers) {
			this.fallbackBuffer = fallbackBuffer;
			this.layerBuffers = layerBuffers;
		}

		@Override
		public VertexConsumer getBuffer(RenderType renderLayer) {
			BufferBuilder bufferBuilder = this.pending.get(renderLayer);

			if (bufferBuilder == null) {
				ByteBufferBuilder allocator = this.layerBuffers.get(renderLayer);
				if (allocator != null) {
					// Dedicated layer buffer, we can make a new buffer builder safely
					bufferBuilder = new BufferBuilder(allocator, renderLayer.mode(), renderLayer.format());
				} else {
					// Not dedicated, flush previous layer first
					if (this.currentLayer != null) {
						this.draw(this.currentLayer);
					}
					bufferBuilder = new BufferBuilder(this.fallbackBuffer, renderLayer.mode(), renderLayer.format());
					this.currentLayer = renderLayer;
				}

				this.pending.put(renderLayer, bufferBuilder);
			}

			return bufferBuilder;
		}

		private ByteBufferBuilder getBufferInternal(RenderType layer) {
			return this.layerBuffers.getOrDefault(layer, this.fallbackBuffer);
		}

		public void drawCurrentLayer() {
			if (this.currentLayer != null) {
				RenderType renderLayer = this.currentLayer;
				if (!this.layerBuffers.containsKey(renderLayer)) {
					this.draw(renderLayer);
				}
				this.currentLayer = null;
			}
		}

		public void draw(RenderType layer) {
			ByteBufferBuilder bufferAllocator = this.getBufferInternal(layer);
			boolean isSameAsCurrentLayer = Objects.equals(this.currentLayer, layer);
			if (!isSameAsCurrentLayer && bufferAllocator == this.fallbackBuffer) {
				return;
			}
			BufferBuilder builder = this.pending.remove(layer);
			if (builder == null) {
				return;
			}
			MeshData buffer = builder.build();
			if (buffer != null) {
				// TODO: do we actually need to sort quads still?
				buffer.sortQuads(bufferAllocator, VertexSorting.ORTHOGRAPHIC_Z);
				layer.draw(buffer);
			}
			if (isSameAsCurrentLayer) {
				this.currentLayer = null;
			}
		}

		public Map<RenderType, BufferBuilder> getPendingLayerBuffers() {
			return pending;
		}
	}

	private static class UnlitFacade implements MultiBufferSource {
		private final MultiBufferSource delegate;
		private final IdentityHashMap<VertexConsumer, VertexConsumer> cache = new IdentityHashMap<>();

		public UnlitFacade(MultiBufferSource delegate) {
			this.delegate = delegate;
		}

		@Override
		public VertexConsumer getBuffer(RenderType layer) {
			return cache.computeIfAbsent(delegate.getBuffer(layer), Consumer::new);
		}

		private static final class Consumer implements VertexConsumer {
			private final VertexConsumer delegate;

			private Consumer(VertexConsumer delegate) {
				this.delegate = delegate;
			}

			@Override
			public VertexConsumer setNormal(float x, float y, float z) {
				delegate.setNormal(0, -1, 0); // this is the change
				return this;
			}
			
			// all other methods are direct delegation

			@Override
			public VertexConsumer addVertex(float x, float y, float z) {
				delegate.addVertex(x, y, z);
				return this;
			}

			@Override
			public VertexConsumer setUv(float u, float v) {
				delegate.setUv(u, v);
				return this;
			}

			@Override
			public VertexConsumer setUv1(int u, int v) {
				delegate.setUv1(u, v);
				return this;
			}

			@Override
			public VertexConsumer setUv2(int u, int v) {
				delegate.setUv2(u, v);
				return this;
			}

			@Override
			public VertexConsumer setColor(int r, int g, int b, int a) {
				delegate.setColor(r, g, b, a);
				return this;
			}
			
		}
	}

}
