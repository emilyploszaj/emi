package dev.emi.emi.screen;


import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.mojang.blaze3d.systems.RenderSystem;

import dev.emi.emi.EmiConfig;
import dev.emi.emi.EmiPort;
import dev.emi.emi.api.stack.EmiIngredient;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Matrix4f;

/**
 * @author Una "unascribed" Thompson
 */
public class StackBatcher {

	public interface Batchable {
		boolean isSideLit();
		boolean isUnbatchable();
		void setUnbatchable();
		void renderForBatch(VertexConsumerProvider vcp, MatrixStack matrices, int x, int y, int z, float delta);
	}

	private final AccessibleImmediateVertexConsumerProvider imm;
	private final VertexConsumerProvider unlitFacade;
	private final Map<RenderLayer, VertexBuffer> buffers = new LinkedHashMap<>();
	private boolean populated = false;
	private boolean dirty = false;
	private int x;
	private int y;
	private int z;
	private static boolean enabled = EmiPort.VERSION.equals("1.18.2");

	private static boolean isEnabled() {
		return enabled && EmiConfig.useBatchedRenderer;
	}

	public StackBatcher() {
		Map<RenderLayer, BufferBuilder> buffers = new HashMap<>();
		assign(buffers, RenderLayer.getSolid());
		assign(buffers, RenderLayer.getCutout());
		assign(buffers, RenderLayer.getTranslucent());
		assign(buffers, TexturedRenderLayers.getEntitySolid());
		assign(buffers, TexturedRenderLayers.getEntityCutout());
		assign(buffers, TexturedRenderLayers.getEntityTranslucentCull());
		assign(buffers, RenderLayer.getGlint());
		assign(buffers, RenderLayer.getDirectGlint());
		assign(buffers, RenderLayer.getEntityGlint());
		imm = new AccessibleImmediateVertexConsumerProvider(new BufferBuilder(256), buffers);
		unlitFacade = new UnlitFacade(imm);
	}

	private void assign(Map<RenderLayer, BufferBuilder> buffers, RenderLayer layer) {
		buffers.put(layer, new BufferBuilder(layer.getExpectedBufferSize()));
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
		}
	}

	public void render(EmiIngredient stack, MatrixStack matrices, int x, int y, float delta) {
		if (stack instanceof Batchable b && !b.isUnbatchable() && isEnabled()) {
			if (populated) return;
			try {
				b.renderForBatch(b.isSideLit() ? imm : unlitFacade, matrices, x-this.x, -y-this.y, z, delta);
			} catch (Throwable t) {
				if (EmiConfig.devMode) {
					System.err.println("[emi] Stack threw exception during batched rendering. See log for info");
					t.printStackTrace();
				}
				b.setUnbatchable();
			}
		} else {
			stack.render(matrices, x, y, delta, -1 ^ EmiIngredient.RENDER_AMOUNT);
		}
	}

	public void draw() {
		if (!isEnabled()) {
			return;
		}
		if (!populated) {
			bake();
			populated = true;
		}
		DiffuseLighting.enableGuiDepthLighting();
		Matrix4f mat = RenderSystem.getModelViewMatrix().copy();
		mat.multiply(Matrix4f.scale(1, -1, 1));
		mat.multiplyByTranslation(x, 0, 0);
		for (Map.Entry<RenderLayer, VertexBuffer> en : buffers.entrySet()) {
			en.getKey().startDrawing();
			en.getValue().setShader(mat, RenderSystem.getProjectionMatrix(), RenderSystem.getShader());
			en.getKey().endDrawing();
		}
		BufferRenderer.unbindAll();
	}
	
	private void bake() {
		imm.drawCurrentLayer();
		buffers.values().forEach(VertexBuffer::close);
		buffers.clear();
		for (RenderLayer layer : imm.getLayerBuffers().keySet()) {
			bake(layer);
		}
	}

	public void bake(RenderLayer layer) {
		BufferBuilder bldr = imm.getBufferInternal(layer);
		if (!imm.getActiveConsumers().remove(bldr)) return;
		VertexBuffer vb = new VertexBuffer();
		EmiPort.upload(vb, bldr);
		buffers.put(layer, vb);
		bldr.reset();
	}

	@SuppressWarnings("unused")
	private static class AccessibleImmediateVertexConsumerProvider extends VertexConsumerProvider.Immediate {

		protected AccessibleImmediateVertexConsumerProvider(BufferBuilder fallbackBuffer, Map<RenderLayer, BufferBuilder> layerBuffers) {
			super(fallbackBuffer, layerBuffers);
		}
		
		public BufferBuilder getBufferInternal(RenderLayer layer) {
			return this.layerBuffers.getOrDefault(layer, this.fallbackBuffer);
		}
		
		public Optional<RenderLayer> getCurrentLayer() {
			return currentLayer;
		}
		
		public void setCurrentLayer(Optional<RenderLayer> layer) {
			this.currentLayer = layer;
		}
		
		public BufferBuilder getFallbackBuffer() {
			return fallbackBuffer;
		}
		
		public Map<RenderLayer, BufferBuilder> getLayerBuffers() {
			return layerBuffers;
		}
		
		public Set<BufferBuilder> getActiveConsumers() {
			return activeConsumers;
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
				delegate.normal(0, 1, 0); // this is the change
				return this;
			}
			
			// all other methods are direct delegation

			@Override
			public VertexConsumer vertex(double x, double y, double z) {
				delegate.vertex(x, y, z);
				return this;
			}

			@Override
			public void unfixColor() {
				delegate.unfixColor();
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
			public void next() {
				delegate.next();
			}

			@Override
			public VertexConsumer light(int u, int v) {
				delegate.light(u, v);
				return this;
			}

			@Override
			public void fixedColor(int r, int g, int b, int a) {
				delegate.fixedColor(r, g, b, a);
			}

			@Override
			public VertexConsumer color(int r, int g, int b, int a) {
				delegate.color(r, g, b, a);
				return this;
			}
			
		}
	}

}
