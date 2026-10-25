package dev.emi.emi.api.stack;

import java.util.List;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTextTooltip;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.ApiStatus;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.Lighting;
import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.api.render.EmiRender;
import dev.emi.emi.platform.EmiAgnos;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.screen.StackBatcher.Batchable;
import dev.emi.emi.screen.tooltip.EmiTextTooltipWrapper;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Internal
public class ItemEmiStack extends EmiStack implements Batchable {
	private static final Minecraft client = Minecraft.getInstance();

	private final Item item;
	private final DataComponentPatch componentChanges;

	private boolean unbatchable;

	public ItemEmiStack(ItemStack stack) {
		this(stack, stack.getCount());
	}

	public ItemEmiStack(ItemStack stack, long amount) {
		this(stack.getItem(), stack.getComponentsPatch(), amount);
	}

	public ItemEmiStack(Item item, DataComponentPatch components, long amount) {
		this.item = item;
		this.componentChanges = components;
		this.amount = amount;
	}

	@Override
	public ItemStack getItemStack() {
		return new ItemStack(EmiPort.getItemRegistry().wrapAsHolder(this.item), (int) this.amount, componentChanges);
	}

	@Override
	public EmiStack copy() {
		EmiStack e = new ItemEmiStack(item, componentChanges, amount);
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
	public DataComponentPatch getComponentChanges() {
		return this.componentChanges;
	}

	@Override
	public <T> @Nullable T get(DataComponentType<? extends T> type) {
		// Check the changes first
		var changedOpt = this.componentChanges.get(type);
		//noinspection OptionalAssignedToNull
		if(changedOpt != null) {
			return changedOpt.orElse(null);
		}
		// Check the item's default components
		return this.item.components().get(type);
	}

	@Override
	public Object getKey() {
		return item;
	}

	@Override
	public ResourceLocation getId() {
		return EmiPort.getItemRegistry().getKey(item);
	}

	@Override
	public void render(GuiGraphics draw, int x, int y, float delta, int flags) {
		EmiDrawContext context = EmiDrawContext.wrap(draw);
		ItemStack stack = getItemStack();
		if ((flags & RENDER_ICON) != 0) {
			Lighting.setupFor3DItems();
			draw.renderFakeItem(stack, x, y);
			draw.renderItemDecorations(client.font, stack, x, y, "");
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
		return client.getItemRenderer().getModel(getItemStack(), null, null, 0).usesBlockLight();
	}
	
	@Override
	public boolean isUnbatchable() {
		ItemStack stack = getItemStack();
		return unbatchable || stack.hasFoil() || stack.isDamaged() || !EmiAgnos.canBatch(stack)
			|| client.getItemRenderer().getModel(getItemStack(), null, null, 0).isCustomRenderer();
	}
	
	@Override
	public void setUnbatchable() {
		this.unbatchable = true;
	}
	
	@Override
	public void renderForBatch(MultiBufferSource vcp, GuiGraphics draw, int x, int y, int z, float delta) {
		EmiDrawContext context = EmiDrawContext.wrap(draw);
		ItemStack stack = getItemStack();
		ItemRenderer ir = client.getItemRenderer();
		BakedModel model = ir.getModel(stack, null, null, 0);
		context.push();
		try {
			context.matrices().translate(x, y, 100.0f + z + (model.isGui3d() ? 50 : 0));
			context.translate(8, 8);
			context.matrices().scale(16.0f, -16.0f, 16.0f);
			ir.render(stack, ItemDisplayContext.GUI, false, context.matrices(), vcp, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, model);
		} finally {
			context.pop();
		}
	}

	@Override
	public List<Component> getTooltipText() {
		if (client.isSameThread()) {
			return getItemStack().getTooltipLines(Item.TooltipContext.of(client.level), client.player, TooltipFlag.NORMAL);
		} else {
			// Don't provide world or entity as context, as they are not thread safe
			return getItemStack().getTooltipLines(Item.TooltipContext.of(client.level.registryAccess()), null, TooltipFlag.NORMAL);
		}
	}

	@Override
	public List<ClientTooltipComponent> getTooltip() {
		ItemStack stack = getItemStack();
		List<ClientTooltipComponent> list = Lists.newArrayList();
		if (!isEmpty()) {
			list.addAll(EmiAgnos.getItemTooltip(stack));
			if (!list.isEmpty() && list.get(0) instanceof ClientTextTooltip ottc) {
				list.set(0, new EmiTextTooltipWrapper(this, ottc));
			}
			//String namespace = EmiPort.getItemRegistry().getId(stack.getItem()).getNamespace();
			//String mod = EmiUtil.getModName(namespace);
			//list.add(TooltipComponent.of(EmiLang.literal(mod, Formatting.BLUE, Formatting.ITALIC)));
			list.addAll(super.getTooltip());
		}
		return list;
	}

	@Override
	public Component getName() {
		if (isEmpty()) {
			return EmiPort.literal("");
		}
		return getItemStack().getHoverName();
	}

	static class ItemEntry {
	}
}