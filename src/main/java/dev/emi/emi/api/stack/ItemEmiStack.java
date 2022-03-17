package dev.emi.emi.api.stack;

import java.util.List;

import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.EmiUtil;
import dev.emi.emi.api.stack.comparison.ItemStackComparison;
import dev.emi.emi.screen.FakeScreen;
import dev.emi.emi.screen.tooltip.RemainderTooltipComponent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.registry.Registry;

public class ItemEmiStack extends EmiStack {
	private static final MinecraftClient client = MinecraftClient.getInstance();
	private final ItemStackEntry entry;
	public final ItemStack stack;

	public ItemEmiStack(ItemStack stack) {
		this(stack, stack.getCount());
	}

	public ItemEmiStack(ItemStack stack, int amount) {
		this.stack = stack.copy();
		this.stack.setCount(1);
		entry = new ItemStackEntry(this.stack);
		this.comparison = new ItemStackComparison();
		this.amount = amount;
	}

	@Override
	public ItemStack getItemStack() {
		return stack;
	}

	@Override
	public EmiStack copy() {
		EmiStack e = new ItemEmiStack(stack.copy());
		e.setRemainder(getRemainder().copy());
		e.comparison = comparison.copy();
		return e;
	}

	@Override
	public boolean isEmpty() {
		return stack.isEmpty();
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
	public void renderIcon(MatrixStack matrices, int x, int y, float delta) {
		client.getItemRenderer().renderInGui(stack, x, y);
		String count = "";
		if (amount != 1) {
			count += amount;
		}
		client.getItemRenderer().renderGuiItemOverlay(client.textRenderer, stack, x, y, count);
	}

	@Override
	public void renderOverlay(MatrixStack matrices, int x, int y, float delta) {
		EmiRenderHelper.renderRemainder(this, matrices, x, y);
	}

	@Override
	public List<Text> getTooltipText() {
		return FakeScreen.INSTANCE.getTooltipFromItem(stack);
	}

	@Override
	public List<TooltipComponent> getTooltip() {
		if (!stack.isEmpty()) {
			List<TooltipComponent> list = FakeScreen.INSTANCE.getTooltipComponentListFromItem(stack);
			String namespace = Registry.ITEM.getId(stack.getItem()).getNamespace();
			String mod = EmiUtil.getModName(namespace);
			list.add(TooltipComponent.of(new LiteralText(mod).formatted(Formatting.BLUE, Formatting.ITALIC).asOrderedText()));
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
		return stack.getName();
	}

	public static class ItemStackEntry extends Entry<ItemStack> {

		public ItemStackEntry(ItemStack stack) {
			super(stack);
		}

		@Override
		Class<ItemStack> getType() {
			return ItemStack.class;
		}
	}
}