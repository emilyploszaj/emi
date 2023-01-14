package dev.emi.emi.api.stack;

import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;

import dev.emi.emi.EmiPort;
import dev.emi.emi.api.render.EmiRender;
import dev.emi.emi.screen.FakeScreen;
import dev.emi.emi.screen.StackBatcher.Batchable;
import dev.emi.emi.screen.tooltip.RemainderTooltipComponent;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.impl.transfer.item.ItemVariantImpl;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ItemEmiStack extends EmiStack implements Batchable {
	private final ItemEntry entry;
	private final ItemStack stack;
	@Deprecated
	public final ItemVariant item;
	private boolean unbatchable;

	public ItemEmiStack(ItemStack stack) {
		this(stack, stack.getCount());
	}

	public ItemEmiStack(ItemStack stack, long amount) {
		stack = stack.copy();
		stack.setCount((int) amount);
		this.stack = stack;
		this.entry = new ItemEntry(new ItemVariantImpl(stack.getItem(), stack.getNbt()));
		this.item = entry.getValue();
		this.amount = amount;
	}
	
	public ItemEmiStack(ItemVariant item, long amount) {
		this(item.toStack((int) amount), amount);
	}

	@Override
	public ItemStack getItemStack() {
		return stack;
	}

	@Override
	public EmiStack copy() {
		EmiStack e = new ItemEmiStack(stack.copy(), amount);
		e.setRemainder(getRemainder().copy());
		e.comparison = comparison;
		return e;
	}

	@Override
	public boolean isEmpty() {
		return amount == 0 || stack.isEmpty();
	}

	@Override
	public NbtCompound getNbt() {
		return stack.getNbt();
	}

	@Override
	public Object getKey() {
		return stack.getItem();
	}

	@Override
	public Entry<?> getEntry() {
		return entry;
	}

	@Override
	public Identifier getId() {
		return EmiPort.getItemRegistry().getId(stack.getItem());
	}

	@Override
	public void render(MatrixStack matrices, int x, int y, float delta, int flags) {
		MinecraftClient client = MinecraftClient.getInstance();
		ItemStack stack = getItemStack();
		if ((flags & RENDER_ICON) != 0) {
			MatrixStack view = RenderSystem.getModelViewStack();
			view.push();
			view.multiplyPositionMatrix(matrices.peek().getPositionMatrix());
			RenderSystem.applyModelViewMatrix();
			client.getItemRenderer().renderInGui(stack, x, y);
			view.pop();
			RenderSystem.applyModelViewMatrix();
		}
		if ((flags & RENDER_AMOUNT) != 0) {
			String count = "";
			if (amount != 1) {
				count += amount;
			}
			client.getItemRenderer().renderGuiItemOverlay(client.textRenderer, stack, x, y, count);
		}
		if ((flags & RENDER_REMAINDER) != 0) {
			EmiRender.renderRemainderIcon(this, matrices, x, y);
		}
	}
	
	@Override
	public boolean isSideLit() {
		return MinecraftClient.getInstance().getItemRenderer().getModel(getItemStack(), null, null, 0).isSideLit();
	}
	
	@Override
	public boolean isUnbatchable() {
		ItemStack stack = getItemStack();
		return unbatchable || stack.hasGlint() || ColorProviderRegistry.ITEM.get(stack.getItem()) != null
			|| MinecraftClient.getInstance().getItemRenderer().getModel(getItemStack(), null, null, 0).isBuiltin();
	}
	
	@Override
	public void setUnbatchable() {
		this.unbatchable = true;
	}
	
	@Override
	public void renderForBatch(VertexConsumerProvider vcp, MatrixStack matrices, int x, int y, int z, float delta) {
		ItemStack stack = getItemStack();
		ItemRenderer ir = MinecraftClient.getInstance().getItemRenderer();
		BakedModel model = ir.getModel(stack, null, null, 0);
		matrices.push();
		try {
			matrices.translate(x, y, 100.0f + z + (model.hasDepth() ? 50 : 0));
			matrices.translate(8.0, 8.0, 0.0);
			matrices.scale(16.0f, 16.0f, 16.0f);
			ir.renderItem(stack, ModelTransformation.Mode.GUI, false, matrices, vcp, LightmapTextureManager.MAX_LIGHT_COORDINATE, OverlayTexture.DEFAULT_UV, model);
		} finally {
			matrices.pop();
		}
	}

	@Override
	public List<Text> getTooltipText() {
		MinecraftClient client = MinecraftClient.getInstance();
		return getItemStack().getTooltip(client.player, TooltipContext.Default.NORMAL);
	}

	@Override
	public List<TooltipComponent> getTooltip() {
		ItemStack stack = getItemStack();
		if (!isEmpty()) {
			List<TooltipComponent> list = FakeScreen.INSTANCE.getTooltipComponentListFromItem(stack);
			//String namespace = EmiPort.getItemRegistry().getId(stack.getItem()).getNamespace();
			//String mod = EmiUtil.getModName(namespace);
			//list.add(TooltipComponent.of(EmiLang.literal(mod, Formatting.BLUE, Formatting.ITALIC)));
			if (!getRemainder().isEmpty()) {
				list.add(new RemainderTooltipComponent(this));
			}
			return list;
		} else {
			return List.of();
		}
	}

	@Override
	public Text getName() {
		if (isEmpty()) {
			return EmiPort.literal("");
		}
		return getItemStack().getName();
	}

	public static class ItemEntry extends Entry<ItemVariant> {

		public ItemEntry(ItemVariant variant) {
			super(variant);
		}

		@Override
		public Class<ItemVariant> getType() {
			return ItemVariant.class;
		}

		@Override
		public boolean equals(Object obj) {
			return obj instanceof ItemEntry e && getValue().getItem().equals(e.getValue().getItem());
		}
	}
}