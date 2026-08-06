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
import java.util.Set;

import net.minecraft.client.render.BuiltBuffer;

import org.joml.Matrix3x2fc;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.VertexSorter;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.runtime.EmiLog;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;

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
				.findStatic(clazz, "markSpriteActive", MethodType.methodType(void.class, Sprite.class));
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
		void renderForBatch(VertexConsumerProvider vcp, DrawContext draw, int x, int y, int z, float delta);
	}

	private final BatcherVertexConsumerProvider imm;
	private final VertexConsumerProvider unlitFacade;
	private final Map<RenderLayer, GpuBuffer> buffers = new LinkedHashMap<>();
	private final Set<Sprite> spritesToUpdate = Sets.newHashSet();
	private boolean populated = false;
	private boolean dirty = false;
	private int x;
	private int y;
	private int z;

	public static final List<RenderLayer> EXTRA_RENDER_LAYERS = Lists.newArrayList();

	public static boolean isEnabled() {
		return EmiConfig.useBatchedRenderer;
	}

	public StackBatcher() {
		Map<RenderLayer, BufferAllocator> buffers = new HashMap<>();
		assign(buffers, RenderLayers.solid());
		assign(buffers, RenderLayers.cutout());
//		assign(buffers, RenderLayer.getTranslucent());
		assign(buffers, TexturedRenderLayers.getEntitySolid());
		assign(buffers, TexturedRenderLayers.getEntityCutout());
//		assign(buffers, TexturedRenderLayers.getEntityTranslucentCull());
		assign(buffers, RenderLayers.glint());
		//assign(buffers, RenderLayer.getDirectGlint());
		assign(buffers, RenderLayers.entityGlint());
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
				batchable.renderForBatch(batchable.isSideLit() ? imm : unlitFacade, draw, x-this.x, y+this.y, z, delta);
			} catch (Throwable t) {
				if (EmiConfig.devMode) {
					EmiLog.error("Batchable threw exception during batched rendering. See log for info", t);
				}
				batchable.setUnbatchable();
			}
		}
	}

	public void render(EmiIngredient stack, DrawContext draw, int x, int y, float delta) {
		render(stack, draw, x, y, delta, ~EmiIngredient.RENDER_AMOUNT);
	}

	public void render(EmiIngredient stack, DrawContext draw, int x, int y, float delta, int flags) {
		if (stack instanceof Batchable b && !b.isUnbatchable() && isEnabled() && (flags & EmiIngredient.RENDER_ICON) != 0) {
			if (!populated) {
				try {
					b.renderForBatch(b.isSideLit() ? imm : unlitFacade, draw, x-this.x, y + this.y, z, delta);
					if (sodiumSpriteHandle != null && !stack.isEmpty()) {
						ItemStack is = stack.getEmiStacks().getFirst().getItemStack();
						MinecraftClient client = MinecraftClient.getInstance();
//						BakedModel model = client.getItemRenderer().getModels().getModel(is);
//						if (model != null) {
//							List<BakedQuad> quads = EmiPort.getQuads(model);
//							for (BakedQuad quad : quads) {
//								if (quad != null) {
//									spritesToUpdate.add(quad.sprite());
//								}
//							}
//						}
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
//		RenderSystem.enableDepthTest();
//		DiffuseLighting.enableGuiDepthLighting();
		Matrix4f mat = new Matrix4f(RenderSystem.getModelViewMatrix());
		mat.mul(new Matrix4f().translation(x, y, 0));
		for (Map.Entry<RenderLayer, GpuBuffer> en : buffers.entrySet()) {
//			en.getKey().startDrawing();
//			EmiPort.setShader(en.getValue(), mat);
//			en.getKey().endDrawing();
		}
//		BufferRenderer.reset();
	}
	
	private void bake() {
		imm.drawCurrentLayer();
//		buffers.values().forEach(VertexBuffer::close);
		buffers.clear();
		for (Map.Entry<RenderLayer, BufferBuilder> entry : imm.getPendingLayerBuffers().entrySet()) {
			bake(entry.getKey(), entry.getValue());
		}
		imm.getPendingLayerBuffers().clear();
	}

	public void bake(RenderLayer layer, BufferBuilder bldr) {
//		BuiltBuffer builtBuffer = bldr.endNullable();
//		if (builtBuffer == null) {
//			return;
//		}
//		VertexBuffer vb = RenderSystem.get
//		vb.bind();
//		vb.upload(builtBuffer);
//		buffers.put(layer, vb);

//        try (BuiltBuffer builtBuffer = bldr.endNullable()) {
//            if (builtBuffer == null) {
//                return;
//            }
//
//            BuiltBuffer.Contents contents = builtBuffer.contents();
//            if (contents == null) return;
//
//            GpuBuffer vb = new GpuBuffer(
//                    GpuBuffer.Usage.DYNAMIC,
//                    contents.format(),
//                    contents.indexCount(),
//                    contents.vertexCount()
//            );
//
//            vb.bind();
//            vb.upload(contents);
//
//            buffers.put(layer, vb);
//        }
	}

	// Apparently BufferBuilder leaks memory in vanilla. Go figure
	public static class ClaimedCollection {
		private final Set<StackBatcher> claimed = Sets.newHashSet();
		private final List<StackBatcher> unclaimed = Lists.newArrayList();

		public StackBatcher claim() {
			synchronized (this) {
				StackBatcher batcher;
				if (unclaimed.isEmpty()) {
					batcher = new StackBatcher();
				} else {
					batcher = unclaimed.removeLast();
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
                unclaimed.addAll(claimed);
				claimed.clear();
			}
		}
	}

	/*
	 * This class is mostly a copy of a 1.21 implementation of VertexConsumerProvider.Immediate
	 * The reimplementation allows compatibility with shader mods, as well as less hackery.
	 */
	private static class BatcherVertexConsumerProvider implements VertexConsumerProvider {
		protected final BufferAllocator fallbackBuffer;
		protected final Map<RenderLayer, BufferAllocator> layerBuffers;
		protected final Map<RenderLayer, BufferBuilder> pending = new HashMap<>();
		protected RenderLayer currentLayer = null;

		protected BatcherVertexConsumerProvider(BufferAllocator fallbackBuffer, Map<RenderLayer, BufferAllocator> layerBuffers) {
			this.fallbackBuffer = fallbackBuffer;
			this.layerBuffers = layerBuffers;
		}

		@Override
		public VertexConsumer getBuffer(RenderLayer renderLayer) {
			BufferBuilder bufferBuilder = this.pending.get(renderLayer);

			if (bufferBuilder == null) {
				BufferAllocator allocator = this.layerBuffers.get(renderLayer);
				if (allocator != null) {
					// Dedicated layer buffer, we can make a new buffer builder safely
					bufferBuilder = new BufferBuilder(allocator, renderLayer.getDrawMode(), renderLayer.getVertexFormat());
				} else {
					// Not dedicated, flush previous layer first
					if (this.currentLayer != null) {
						this.draw(this.currentLayer);
					}
					bufferBuilder = new BufferBuilder(this.fallbackBuffer, renderLayer.getDrawMode(), renderLayer.getVertexFormat());
					this.currentLayer = renderLayer;
				}

				this.pending.put(renderLayer, bufferBuilder);
			}

			return bufferBuilder;
		}

		private BufferAllocator getBufferInternal(RenderLayer layer) {
			return this.layerBuffers.getOrDefault(layer, this.fallbackBuffer);
		}

		public void drawCurrentLayer() {
			if (this.currentLayer != null) {
				RenderLayer renderLayer = this.currentLayer;
				if (!this.layerBuffers.containsKey(renderLayer)) {
					this.draw(renderLayer);
				}
				this.currentLayer = null;
			}
		}

		public void draw(RenderLayer layer) {
			BufferAllocator bufferAllocator = this.getBufferInternal(layer);
			boolean isSameAsCurrentLayer = Objects.equals(this.currentLayer, layer);
			if (!isSameAsCurrentLayer && bufferAllocator == this.fallbackBuffer) {
				return;
			}
			BufferBuilder builder = this.pending.remove(layer);
			if (builder == null) {
				return;
			}
			BuiltBuffer buffer = builder.endNullable();
			if (buffer != null) {
				// TODO: do we actually need to sort quads still?
				buffer.sortQuads(bufferAllocator, VertexSorter.BY_Z);
				layer.draw(buffer);
			}
			if (isSameAsCurrentLayer) {
				this.currentLayer = null;
			}
		}

		public Map<RenderLayer, BufferBuilder> getPendingLayerBuffers() {
			return pending;
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

        private record Consumer(VertexConsumer delegate) implements VertexConsumer {

            @Override
            public VertexConsumer normal(float x, float y, float z) {
                delegate.normal(0, -1, 0); // this is the change
                return this;
            }

            @Override
            public VertexConsumer normal(MatrixStack.Entry matrix, float x, float y, float z) {
                delegate.normal(matrix, 0, -1, 0); // this is the change
                return this;
            }

            @Override
            public VertexConsumer normal(MatrixStack.Entry matrix, Vector3f vec) {
                delegate.normal(matrix, 0, -1, 0); // this is the change
                return this;
            }

            @Override
            public void vertex(float x, float y, float z, int color, float u, float v, int overlay, int light,
                               float normalX, float normalY, float normalZ) {
                delegate.vertex(x, y, z, color, u, v, overlay, light, 0, -1, 0); // this is the change
            }

            // all other methods are direct delegation

            @Override
            public VertexConsumer lineWidth(float width) {
                delegate.lineWidth(width);
                return this;
            }

            @Override
            public VertexConsumer color(float red, float green, float blue, float alpha) {
                delegate.color(red, green, blue, alpha);
                return this;
            }

            @Override
            public VertexConsumer light(int uv) {
                delegate.light(uv);
                return this;
            }

            @Override
            public VertexConsumer overlay(int uv) {
                delegate.overlay(uv);
                return this;
            }

            @Override
            public void quad(MatrixStack.Entry matrixEntry, BakedQuad quad, float red, float green, float blue,
                             float alpha, int light, int overlay) {
                delegate.quad(matrixEntry, quad, red, green, blue, alpha, light, overlay);
            }

            @Override
            public void quad(MatrixStack.Entry matrixEntry, BakedQuad quad, float[] brightnesses, float red,
                             float green, float blue, float alpha, int[] lights, int overlay) {
                delegate.quad(matrixEntry, quad, brightnesses, red, green, blue, alpha, lights, overlay);
            }

            @Override
            public VertexConsumer vertex(Vector3fc vec) {
                delegate.vertex(vec);
                return this;
            }

            @Override
            public VertexConsumer vertex(MatrixStack.Entry matrix, Vector3f vec) {
                delegate.vertex(matrix, vec);
                return this;
            }

            @Override
            public VertexConsumer vertex(MatrixStack.Entry matrix, float x, float y, float z) {
                delegate.vertex(matrix, x, y, z);
                return this;
            }

            @Override
            public VertexConsumer vertex(Matrix4fc matrix, float x, float y, float z) {
                delegate.vertex(matrix, x, y, z);
                return this;
            }

            @Override
            public VertexConsumer vertex(Matrix3x2fc matrix, float x, float y) {
                delegate.vertex(matrix, x, y);
                return this;
            }

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

            @Override
            public VertexConsumer color(int argb) {
                delegate.color(argb);
                return this;
            }
        }

    }

}
