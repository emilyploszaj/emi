package dev.emi.emi.api.stack;

import java.util.List;

import org.apache.commons.compress.utils.Lists;
import org.jetbrains.annotations.ApiStatus;

import com.mojang.blaze3d.systems.RenderSystem;

import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.EmiUtil;
import dev.emi.emi.api.render.EmiRender;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.mixin.accessor.BakedModelManagerAccessor;
import dev.emi.emi.mixin.accessor.ItemRendererAccessor;
import dev.emi.emi.registry.EmiTags;
import dev.emi.emi.runtime.EmiDrawContext;
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
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

@ApiStatus.Internal
public class TagEmiIngredient implements EmiIngredient {
	private final Identifier id;
	private List<EmiStack> stacks;
	public final TagKey<?> key;
	private long amount;
	private float chance = 1;

	@ApiStatus.Internal
	public TagEmiIngredient(TagKey<?> key, long amount) {
		this(key, fromKey(key), amount);
	}

	@SuppressWarnings({"unchecked"})
	private static List<EmiStack> fromKey(TagKey<?> key) {
		if (key.registry().equals(EmiPort.getItemRegistry().getKey())) {
			return EmiUtil.values((TagKey<Item>) key).map(RegistryEntry::value).map(EmiStack::of).toList();
		} else if (key.registry().equals(EmiPort.getFluidRegistry().getKey())) {
			return EmiUtil.values((TagKey<Fluid>) key).map(f -> EmiStack.of(f.value())).distinct().toList();
		}
		throw new UnsupportedOperationException("Unsupported tag registry " + key);
	}

	@ApiStatus.Internal
	public TagEmiIngredient(TagKey<?> key, List<EmiStack> stacks, long amount) {
		this.id = key.id();
		this.key = key;
		this.stacks = stacks;
		this.amount = amount;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof TagEmiIngredient tag && tag.key.equals(this.key);
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public EmiIngredient copy() {
		EmiIngredient stack = new TagEmiIngredient(key, amount);
		stack.setChance(chance);
		return stack;
	}

	@Override
	public List<EmiStack> getEmiStacks() {
		return stacks;
	}

	@Override
	public long getAmount() {
		return amount;
	}

	@Override
	public EmiIngredient setAmount(long amount) {
		this.amount = amount;
		return this;
	}

	@Override
	public float getChance() {
		return chance;
	}

	@Override
	public EmiIngredient setChance(float chance) {
		this.chance = chance;
		return this;
	}

	@Override
	public void render(MatrixStack matrices, int x, int y, float delta, int flags) {
		EmiDrawContext context = EmiDrawContext.wrap(matrices);
		MinecraftClient client = MinecraftClient.getInstance();

		if ((flags & RENDER_ICON) != 0) {
			if (!EmiTags.hasCustomModel(key)) {
				if (stacks.size() > 0) {
					stacks.get(0).render(context.raw(), x, y, delta, -1 ^ RENDER_AMOUNT);
				}
			} else {
				BakedModel model = ((BakedModelManagerAccessor) client.getBakedModelManager()).getModels()
					.getOrDefault(EmiTags.getCustomModel(key), client.getBakedModelManager().getMissingModel());
					
				MatrixStack vs = RenderSystem.getModelViewStack();
				vs.push();
				vs.multiplyPositionMatrix(context.matrices().peek().getPositionMatrix());
				vs.translate(x, y, 100.0f);
				vs.translate(8.0, 8.0, 0.0);
				vs.scale(1.0f, -1.0f, 1.0f);
				vs.scale(16.0f, 16.0f, 16.0f);
				RenderSystem.applyModelViewMatrix();
				
				MatrixStack ms = new MatrixStack();
				model.getTransformation().getTransformation(ModelTransformationMode.GUI).apply(false, ms);
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
		}
		if ((flags & RENDER_AMOUNT) != 0 && !key.registry().equals(EmiPort.getFluidRegistry().getKey())) {
			String count = "";
			if (amount != 1) {
				count += amount;
			}
			EmiRenderHelper.renderAmount(context, x, y, EmiPort.literal(count));
		}
		if ((flags & RENDER_INGREDIENT) != 0) {
			EmiRender.renderTagIcon(this, context.raw(), x, y);
		}
		if ((flags & RENDER_REMAINDER) != 0) {
			EmiRender.renderRemainderIcon(this, context.raw(), x, y);
		}
	}

	@Override
	public List<TooltipComponent> getTooltip() {
		List<TooltipComponent> list = Lists.newArrayList();
		list.add(TooltipComponent.of(EmiPort.ordered(EmiTags.getTagName(key))));
		if (EmiUtil.showAdvancedTooltips()) {
			list.add(TooltipComponent.of(EmiPort.ordered(EmiPort.literal("#" + id, Formatting.DARK_GRAY))));
		}
		if (key.registry().equals(EmiPort.getFluidRegistry().getKey()) && amount > 1) {
			list.add(TooltipComponent.of(EmiPort.ordered(EmiRenderHelper.getAmountText(this, amount))));
		}
		if (EmiConfig.appendModId) {
			String mod = EmiUtil.getModName(id.getNamespace());
			list.add(TooltipComponent.of(EmiPort.ordered(EmiPort.literal(mod, Formatting.BLUE, Formatting.ITALIC))));
		}
		list.add(new TagTooltipComponent(stacks));
		for (EmiStack stack : stacks) {
			if (!stack.getRemainder().isEmpty()) {
				list.add(new RemainderTooltipComponent(this));
				break;
			}
		}
		return list;
	}
}