package dev.emi.emi.api.stack;

import java.util.List;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import org.jetbrains.annotations.ApiStatus;

import com.google.common.collect.Lists;

import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.api.render.EmiRender;
import dev.emi.emi.platform.EmiAgnos;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.screen.StackBatcher.Batchable;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@ApiStatus.Internal
public class ItemEmiStack extends EmiStack implements Batchable {
	private static final MinecraftClient client = MinecraftClient.getInstance();

	private final Item item;
	private final NbtCompound nbt;

	private boolean unbatchable;

	public ItemEmiStack(ItemStack stack) {
		this(stack, stack.getCount());
	}

	public ItemEmiStack(ItemStack stack, long amount) {
		this(stack.getItem(), stack.getNbt(), amount);
	}

	public ItemEmiStack(Item item, NbtCompound nbt, long amount) {
		this.item = item;
		this.nbt = nbt != null ? nbt.copy() : null;
		this.amount = amount;
	}

	@Override
	public ItemStack getItemStack() {
		ItemStack stack = new ItemStack(this.item, (int) this.amount);
		if (this.nbt != null) {
			stack.setNbt(this.nbt);
		}
		return stack;
	}

	@Override
	public EmiStack copy() {
		EmiStack e = new ItemEmiStack(item, nbt, amount);
		e.setChance(chance);
		e.setRemainder(getRemainder().copy());
		e.comparison = comparison;
		return e;
	}

	@Override
	public boolean isEmpty() {
		return amount == 0 || item == Items.AIR;
	}

	@Override
	public NbtCompound getNbt() {
		return nbt;
	}

	@Override
	public Object getKey() {
		return item;
	}

	@Override
	public Identifier getId() {
		return EmiPort.getItemRegistry().getId(item);
	}

	@Override
	public void render(DrawContext draw, int x, int y, float delta, int flags) {
		EmiDrawContext context = EmiDrawContext.wrap(draw);
		ItemStack stack = getItemStack();
		if ((flags & RENDER_ICON) != 0) {
			DiffuseLighting.enableGuiDepthLighting();
			draw.drawItemWithoutEntity(stack, x, y);
			draw.drawItemInSlot(client.textRenderer, stack, x, y, "");
		}
		if ((flags & RENDER_AMOUNT) != 0) {
			String count = "";
			if (amount != 1) {
				count += amount;
			}
			EmiRenderHelper.renderAmount(context, x, y, EmiPort.literal(count));
		}
		if ((flags & RENDER_REMAINDER) != 0) {
			EmiRender.renderRemainderIcon(this, context.raw(), x, y);
		}
	}
	
	@Override
	public boolean isSideLit() {
		return client.getItemRenderer().getModel(getItemStack(), null, null, 0).isSideLit();
	}
	
	@Override
	public boolean isUnbatchable() {
		ItemStack stack = getItemStack();
		return unbatchable || stack.hasGlint() || stack.isDamaged() || !EmiAgnos.canBatch(stack)
			|| client.getItemRenderer().getModel(getItemStack(), null, null, 0).isBuiltin();
	}
	
	@Override
	public void setUnbatchable() {
		this.unbatchable = true;
	}
	
	@Override
	public void renderForBatch(VertexConsumerProvider vcp, DrawContext draw, int x, int y, int z, float delta) {
		EmiDrawContext context = EmiDrawContext.wrap(draw);
		ItemStack stack = getItemStack();
		ItemRenderer ir = client.getItemRenderer();
		BakedModel model = ir.getModel(stack, null, null, 0);
		context.push();
		try {
			context.matrices().translate(x, y, 100.0f + z + (model.hasDepth() ? 50 : 0));
			context.matrices().translate(8.0, 8.0, 0.0);
			context.matrices().scale(16.0f, 16.0f, 16.0f);
			ir.renderItem(stack, ModelTransformationMode.GUI, false, context.matrices(), vcp, LightmapTextureManager.MAX_LIGHT_COORDINATE, OverlayTexture.DEFAULT_UV, model);
		} finally {
			context.pop();
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
			list.addAll(EmiAgnos.getItemTooltip(stack));
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