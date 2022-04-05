package dev.emi.emi.api.stack;

import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;

import org.apache.commons.compress.utils.Lists;

import dev.emi.emi.EmiClient;
import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.EmiUtil;
import dev.emi.emi.mixin.accessor.ItemRendererAccessor;
import dev.emi.emi.screen.tooltip.RemainderTooltipComponent;
import dev.emi.emi.screen.tooltip.TagTooltipComponent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tag.ItemTags;
import net.minecraft.tag.Tag;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

public class EmiTagIngredient implements EmiIngredient {
	private final Identifier id;
	private List<EmiStack> stacks;
	public final Tag<Item> tag;

	public EmiTagIngredient(Tag<Item> tag) {
		this.id = ItemTags.getTagGroup().getUncheckedTagId(tag);
		this.tag = tag;
		this.stacks = tag.values().stream().map(ItemStack::new).map(EmiStack::of).toList();
	}

	@Override
	public List<EmiStack> getEmiStacks() {
		return stacks;
	}

	@Override
	public void render(MatrixStack matrices, int x, int y, float delta) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (!EmiClient.MODELED_TAGS.contains(id)) {
			if (tag.values().size() > 0) {
				EmiStack.of(tag.values().get(0)).renderIcon(matrices, x, y, delta);
			}
		} else {
			BakedModel model = client.getBakedModelManager()
				.getModel(new ModelIdentifier("emi:tags/" + id.getNamespace() + "/" + id.getPath() + "#inventory"));
				
			MatrixStack vs = RenderSystem.getModelViewStack();
			vs.push();
			vs.translate(x, y, 100.0f);
			vs.translate(8.0, 8.0, 0.0);
			vs.scale(1.0f, -1.0f, 1.0f);
			vs.scale(16.0f, 16.0f, 16.0f);
			RenderSystem.applyModelViewMatrix();
			
			MatrixStack ms = new MatrixStack();
			model.getTransformation().getTransformation(ModelTransformation.Mode.GUI).apply(false, ms);
			ms.translate(-0.5, -0.5, -0.5);
			
			if (!model.isSideLit()) {
				DiffuseLighting.disableGuiDepthLighting();
			}
			VertexConsumerProvider.Immediate immediate = client.getBufferBuilders().getEntityVertexConsumers();
			((ItemRendererAccessor) client.getItemRenderer())
				.invokeRenderBakedItemModel(model,
					ItemStack.EMPTY, LightmapTextureManager.MAX_LIGHT_COORDINATE, OverlayTexture.DEFAULT_UV, ms, 
					ItemRenderer.getDirectItemGlintConsumer(immediate,
					TexturedRenderLayers.getItemEntityTranslucentCull(), true, false));
			immediate.draw();

			if (!model.isSideLit()) {
				DiffuseLighting.enableGuiDepthLighting();
			}

			vs.pop();
			RenderSystem.applyModelViewMatrix();
		}

		EmiRenderHelper.renderTag(this, matrices, x, y);
		EmiRenderHelper.renderRemainder(this, matrices, x, y);
	}

	@Override
	public List<TooltipComponent> getTooltip() {
		String translation = EmiUtil.translateId("tag.", id);
		List<TooltipComponent> list = Lists.newArrayList();
		if (I18n.hasTranslation(translation)) {
			list.add(TooltipComponent.of(new TranslatableText(translation).asOrderedText()));
		} else {
			list.add(TooltipComponent.of(new LiteralText("#" + id).asOrderedText()));
		}
		list.add(new TagTooltipComponent(tag));
		for (EmiStack stack : stacks) {
			if (!stack.getRemainder().isEmpty()) {
				list.add(new RemainderTooltipComponent(this));
				break;
			}
		}
		return list;
	}
}