package dev.emi.emi.screen;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import dev.emi.emi.EmiPort;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.item.TooltipData;
import net.minecraft.item.ItemStack;

public class FakeScreen extends Screen {
	public static final FakeScreen INSTANCE = new FakeScreen();

	protected FakeScreen() {
		super(EmiPort.literal(""));
		this.client = MinecraftClient.getInstance();
		this.itemRenderer = client.getItemRenderer();
		this.textRenderer = client.textRenderer;
		this.width = Integer.MAX_VALUE;
		this.height = Integer.MAX_VALUE;
	}

	public List<TooltipComponent> getTooltipComponentListFromItem(ItemStack stack) {
		List<TooltipComponent> list = this.getTooltipFromItem(stack)
			.stream().map(EmiPort::ordered).map(TooltipComponent::of).collect(Collectors.toList());
		Optional<TooltipData> data = stack.getTooltipData();
		if (data.isPresent()) {
			try {
				list.add(TooltipComponent.of(data.get()));
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
		return list;
	}
}
