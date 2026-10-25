package dev.emi.emi.screen;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import dev.emi.emi.EmiPort;
import dev.emi.emi.runtime.EmiLog;

public class FakeScreen extends Screen {
	public static final FakeScreen INSTANCE = new FakeScreen();

	protected FakeScreen() {
		super(EmiPort.literal(""));
		this.minecraft = Minecraft.getInstance();
		this.font = minecraft.font;
		this.width = Integer.MAX_VALUE;
		this.height = Integer.MAX_VALUE;
	}

	public List<ClientTooltipComponent> getTooltipComponentListFromItem(ItemStack stack) {
		List<ClientTooltipComponent> list = Screen.getTooltipFromItem(minecraft, stack)
			.stream().map(EmiPort::ordered).map(ClientTooltipComponent::create).collect(Collectors.toList());
		Optional<TooltipComponent> data = stack.getTooltipImage();
		if (data.isPresent()) {
			try {
				list.add(ClientTooltipComponent.create(data.get()));
			} catch (Throwable e) {
				EmiLog.error("Exception converting TooltipComponent", e);
			}
		}
		return list;
	}
}
