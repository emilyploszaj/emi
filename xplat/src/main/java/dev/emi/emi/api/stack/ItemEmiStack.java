package dev.emi.emi.api.stack;

import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;

import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.api.render.EmiRender;
import dev.emi.emi.platform.EmiAgnos;
import dev.emi.emi.screen.FakeScreen;
import dev.emi.emi.screen.StackBatcher.Batchable;
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
	private static final MinecraftClient client = MinecraftClient.getInstance();
	private final ItemStack stack;
	private boolean unbatchable;

	public ItemEmiStack(ItemStack stack) {
		this(stack, stack.getCount());
	}

	public ItemEmiStack(ItemStack stack, long amount) {
		stack = stack.copy();
		stack.setCount((int) amount);
		this.stack = stack;
		this.amount = amount;
	}

	@Override
	public ItemStack getItemStack() {
		return stack;
	}

	@Override
	public EmiStack copy() {
		EmiStack e = new ItemEmiStack(stack.copy(), amount);
		e.setChance(chance);
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
	public Identifier getId() {
		return EmiPort.getItemRegistry().getId(stack.getItem());
	}

	@Override
	public void render(MatrixStack matrices, int x, int y, float delta, int flags) {
		ItemStack stack = getItemStack();
		if ((flags & RENDER_ICON) != 0) {
			MatrixStack view = RenderSystem.getModelViewStack();
			view.push();
			view.multiplyPositionMatrix(matrices.peek().getPositionMatrix());
			RenderSystem.applyModelViewMatrix();
			ItemRenderer itemRenderer = client.getItemRenderer();
			float zOffset = itemRenderer.zOffset;
			itemRenderer.zOffset = 0;
			itemRenderer.renderInGui(stack, x, y);
			itemRenderer.renderGuiItemOverlay(client.textRenderer, stack, x, y, "");
			itemRenderer.zOffset = zOffset;
			view.pop();
			RenderSystem.applyModelViewMatrix();
		}
		if ((flags & RENDER_AMOUNT) != 0) {
			String count = "";
			if (amount != 1) {
				count += amount;
			}
			EmiRenderHelper.renderAmount(matrices, x, y, EmiPort.literal(count));
		}
		if ((flags & RENDER_REMAINDER) != 0) {
			EmiRender.renderRemainderIcon(this, matrices, x, y);
		}
	}
	
	@Override
	public boolean isSideLit() {
		return client.getItemRenderer().getModel(getItemStack(), null, null, 0).isSideLit();
	}
	
	@Override
	public boolean isUnbatchable() {
		ItemStack stack = getItemStack();
		return unbatchable || stack.hasGlint() || stack.isDamaged() || !EmiAgnos.canBatch(stack.getItem())
			|| client.getItemRenderer().getModel(getItemStack(), null, null, 0).isBuiltin();
	}
	
	@Override
	public void setUnbatchable() {
		this.unbatchable = true;
	}
	
	@Override
	public void renderForBatch(VertexConsumerProvider vcp, MatrixStack matrices, int x, int y, int z, float delta) {
		ItemStack stack = getItemStack();
		ItemRenderer ir = client.getItemRenderer();
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
		return getItemStack().getTooltip(client.player, TooltipContext.BASIC);
	}

	@Override
	public List<TooltipComponent> getTooltip() {
		ItemStack stack = getItemStack();
		List<TooltipComponent> list = Lists.newArrayList();
		if (!isEmpty()) {
			list.addAll(FakeScreen.INSTANCE.getTooltipComponentListFromItem(stack));
			//String namespace = EmiPort.getItemRegistry().getId(stack.getItem()).getNamespace();
			//String mod = EmiUtil.getModName(namespace);
			//list.add(TooltipComponent.of(EmiLang.literal(mod, Formatting.BLUE, Formatting.ITALIC)));
			list.addAll(super.getTooltip());
		}
		return list;
	}

	@Override
	public Text getName() {
		if (isEmpty()) {
			return EmiPort.literal("");
		}
		return getItemStack().getName();
	}

	static class ItemEntry {
	}
}