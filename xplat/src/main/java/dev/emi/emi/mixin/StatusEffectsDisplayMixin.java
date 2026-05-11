package dev.emi.emi.mixin;

import com.google.common.collect.Ordering;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.emi.emi.config.EffectLocation;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.mixin.accessor.HandledScreenAccessor;
import dev.emi.emi.platform.EmiAgnos;
import dev.emi.emi.runtime.EmiDrawContext;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.StatusEffectsDisplay;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.text.Text;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Mixin(StatusEffectsDisplay.class)
public abstract class StatusEffectsDisplayMixin {

    @Shadow
    @Final
    private HandledScreen<?> parent;

    @Shadow
    @Final
    private MinecraftClient client;

    @Unique
    private static final boolean emi$hasInventoryTabs = EmiAgnos.isModLoaded("inventorytabs");

    @Shadow
    protected abstract int drawStatusEffectBackgrounds(DrawContext context, TextRenderer textRenderer, Text description,
                                                       Text duration, int x, int y, boolean ambient, int width);

    @Shadow
    protected abstract Text getStatusEffectDescription(StatusEffectInstance statusEffect);

    @Shadow
    protected abstract void drawTexts(DrawContext context, Text description, Text duration, TextRenderer textRenderer,
                                      int x, int y, int width, int height, int mouseX, int mouseY);

    @Inject(at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screen/ingame/StatusEffectsDisplay;drawStatusEffects(Lnet/minecraft/client/gui/DrawContext;Ljava/util/Collection;IIIII)V"),
            method = "render", cancellable = true)
    private void drawStatusEffects(DrawContext context, int mouseX, int mouseY, CallbackInfo ci) {
        if (EmiConfig.effectLocation == EffectLocation.TOP) {
            emi$drawCenteredEffects(context, mouseX, mouseY);
            ci.cancel();
        } else if (EmiConfig.effectLocation == EffectLocation.HIDDEN) {
            ci.cancel();
        }
    }

	@Unique
    private void emi$drawCenteredEffects(DrawContext raw, int mouseX, int mouseY) {
		EmiDrawContext context = EmiDrawContext.wrap(raw);
		context.resetColor();
		Collection<StatusEffectInstance> effects = Ordering.natural().sortedCopy(this.client.player.getStatusEffects());
		int size = effects.size();
		if (size == 0) {
			return;
		}

        int screenX = ((HandledScreenAccessor) parent).getX();
        int screenY = ((HandledScreenAccessor) parent).getY();
        int backgroundWidth = ((HandledScreenAccessor) parent).getBackgroundWidth();

		boolean wide = size == 1;

		int y = screenY - 34;
		if (parent instanceof CreativeInventoryScreen || emi$hasInventoryTabs) {
			y -= 28;
			if (parent instanceof CreativeInventoryScreen && EmiAgnos.isForge()) {
				y -= 22;
			}
		}

		int xOff = 34;
		if (wide) {
			xOff = 122;
		} else if (size > 5) {
			xOff = (backgroundWidth - 32) / (size - 1);
		}

		int width = (size - 1) * xOff + (wide ? 120 : 32);
		int x = screenX + (backgroundWidth - width) / 2;
		StatusEffectInstance hovered = null;

        for (StatusEffectInstance inst : effects) {
            int ew = wide ? 120 : 32;

            TextRenderer textRenderer = this.parent.getTextRenderer();
            Text description = this.getStatusEffectDescription(inst);
            Text duration = StatusEffectUtil.getDurationText(inst, 1.0F, this.client.world.getTickManager()
                    .getTickRate());
            boolean isAmbient = inst.isAmbient();

            int textWidth = this.drawStatusEffectBackgrounds(context.raw(), textRenderer, description, duration, x, y, isAmbient, ew);
            raw.drawGuiTexture(RenderPipelines.GUI_TEXTURED, InGameHud.getEffectTexture(inst.getEffectType()), x + 7, y + 7, 18, 18);
            if (wide) {
                this.drawTexts(raw, description, duration, textRenderer, x, y, textWidth, 32, mouseX, mouseY);
            }

            if (mouseX >= x && mouseX < x + ew && mouseY >= y && mouseY < y + 32) {
                hovered = inst;
            }
            x += xOff;
        }

		if (hovered != null && size > 1) {
			List<Text> list = List.of(this.getStatusEffectDescription(hovered), StatusEffectUtil.getDurationText(hovered, 1.0f, client.world.getTickManager().getTickRate()));
			context.raw().drawTooltip(client.textRenderer, list, Optional.empty(), mouseX, Math.max(mouseY, 16));
		}
	}

	@ModifyVariable(at = @At("HEAD"), method = "drawStatusEffects", ordinal = 4, argsOnly = true)
	private int squishEffects(int original) {
		return EmiConfig.effectLocation.compressed ? 32 : original;
	}

    @ModifyVariable(at = @At("HEAD"), method = "drawStatusEffects", ordinal = 0, argsOnly = true)
	private int changeEffectSpace(int original) {
        int screenX = ((HandledScreenAccessor) parent).getX();
		return switch (EmiConfig.effectLocation) {
			case RIGHT, RIGHT_COMPRESSED, HIDDEN -> original;
			case TOP -> screenX;
			case LEFT_COMPRESSED -> screenX - 2- 32;
			case LEFT -> screenX - 2 - 120;
		};
	}

}
