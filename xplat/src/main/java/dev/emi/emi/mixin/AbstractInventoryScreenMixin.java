package dev.emi.emi.mixin;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.EffectsInInventory;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.google.common.collect.Ordering;

import dev.emi.emi.config.EffectLocation;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.mixin.accessor.HandledScreenAccessor;
import dev.emi.emi.platform.EmiAgnos;

@Mixin(EffectsInInventory.class)
public abstract class AbstractInventoryScreenMixin {
	@Unique
	private static boolean hasInventoryTabs = EmiAgnos.isModLoaded("inventorytabs");
	@Shadow @Final
	private AbstractContainerScreen<?> screen;
	@Shadow @Final
	private Minecraft minecraft;

	@Shadow
	private void extractEffects(GuiGraphicsExtractor graphics, Collection<MobEffectInstance> activeEffects, int x0, int yStep, int mouseX, int mouseY, int maxWidth) {
		throw new UnsupportedOperationException();
	}

	@Shadow
	private int extractBackground(GuiGraphicsExtractor graphics, Font font, Component effectName, Component duration, int x0, int y0, boolean isAmbient, int maxTextureWidth) {
		throw new UnsupportedOperationException();
	}

	@Shadow
	private Component getEffectName(MobEffectInstance effect) {
		throw new UnsupportedOperationException();
	}

	@Inject(at = @At("HEAD"), method = "extractRenderState", cancellable = true)
	private void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, CallbackInfo info) {
		EffectLocation location = EmiConfig.effectLocation;
		if (location == EffectLocation.RIGHT) {
			return;
		}
		info.cancel();
		Collection<MobEffectInstance> effects = this.minecraft.player.getActiveEffects();
		if (location == EffectLocation.HIDDEN || effects.isEmpty()) {
			return;
		}
		if (location == EffectLocation.TOP) {
			emi$drawCenteredEffects(graphics, effects, mouseX, mouseY);
			return;
		}
		HandledScreenAccessor hsa = (HandledScreenAccessor) screen;
		int width = location.compressed ? 32 : 120;
		int x = switch (location) {
			case LEFT, LEFT_COMPRESSED -> hsa.getX() - 2 - width;
			default -> hsa.getX() + hsa.getBackgroundWidth() + 2;
		};
		int yStep = 33;
		if (effects.size() > 5) {
			yStep = 132 / (effects.size() - 1);
		}
		this.extractEffects(graphics, effects, x, yStep, mouseX, mouseY, width);
	}

	@Unique
	private void emi$drawCenteredEffects(GuiGraphicsExtractor graphics, Collection<MobEffectInstance> active, int mouseX, int mouseY) {
		HandledScreenAccessor hsa = (HandledScreenAccessor) screen;
		List<MobEffectInstance> effects = Ordering.natural().sortedCopy(active);
		Font font = screen.getFont();
		int size = effects.size();
		boolean wide = size == 1;
		int y = hsa.getY() - 34;
		if (screen instanceof CreativeModeInventoryScreen || hasInventoryTabs) {
			y -= 28;
			if (screen instanceof CreativeModeInventoryScreen && EmiAgnos.isForge()) {
				y -= 22;
			}
		}
		int xOff = 34;
		if (wide) {
			xOff = 122;
		} else if (size > 5) {
			xOff = (hsa.getBackgroundWidth() - 32) / (size - 1);
		}
		int width = (size - 1) * xOff + (wide ? 120 : 32);
		int x = hsa.getX() + (hsa.getBackgroundWidth() - width) / 2;
		MobEffectInstance hovered = null;
		for (MobEffectInstance inst : effects) {
			Component name = this.getEffectName(inst);
			Component duration = MobEffectUtil.formatDuration(inst, 1.0f, minecraft.level.tickRateManager().tickrate());
			int ew = this.extractBackground(graphics, font, name, duration, x, y, inst.isAmbient(), wide ? 120 : 32);
			graphics.blitSprite(RenderPipelines.GUI_TEXTURED, Gui.getMobEffectSprite(inst.getEffect()), x + 7, y + 7, 18, 18);
			if (wide) {
				graphics.text(font, name, x + 32, y + 7, -1);
				graphics.text(font, duration, x + 32, y + 16, -8355712);
			}
			if (mouseX >= x && mouseX < x + ew && mouseY >= y && mouseY < y + 32) {
				hovered = inst;
			}
			x += xOff;
		}
		if (hovered != null && !wide) {
			Component duration = MobEffectUtil.formatDuration(hovered, 1.0f, minecraft.level.tickRateManager().tickrate());
			graphics.setTooltipForNextFrame(font, List.of(this.getEffectName(hovered), duration), Optional.empty(), mouseX, Math.max(mouseY, 16));
		}
	}
}
