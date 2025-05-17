package dev.emi.emi.api.stack;

import com.google.common.collect.Lists;
import dev.emi.emi.EmiPort;
import dev.emi.emi.api.render.EmiRender;
import dev.emi.emi.screen.tooltip.EmiTextTooltipWrapper;
import dev.emi.emi.screen.tooltip.IngredientTooltipComponent;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;

@ApiStatus.Internal
public class SearchEmiIngredient implements EmiIngredient {
    private final List<? extends EmiIngredient> results;
    private final List<EmiStack> fullResults;

    public final String content;

    public SearchEmiIngredient(String content, List<? extends EmiIngredient> results) {
        this.results = results;
        this.fullResults = results.stream().flatMap(i -> i.getEmiStacks().stream()).toList();
        if (fullResults.isEmpty()) {
            throw new IllegalArgumentException("SearchEmiIngredient cannot be and empty search");
        }

        this.content = content;
    }

    @Override
    public void render(DrawContext draw, int x, int y, float delta, int flags) {
        int item = (int) (System.currentTimeMillis() / 1000 % results.size());
        EmiIngredient current = results.get(item);
        if ((flags & RENDER_ICON) != 0) {
            current.render(draw, x, y, delta, -1 ^ RENDER_AMOUNT);
        }
        if ((flags & RENDER_INGREDIENT) != 0) {
            EmiRender.renderIngredientIcon(this, draw, x, y);
        }

//        // Maybe render a couple of letters of the search above the icon
//        EmiDrawContext context = EmiDrawContext.wrap(draw);
//        EmiRenderHelper.renderText(context, x, y, EmiPort.literal(content.substring(0, 3)));
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof SearchEmiIngredient ingredient && ingredient.content.equals(this.content);
    }

    @Override
    public EmiIngredient copy() {
        return new SearchEmiIngredient(content, results);
    }

    @Override
    public long getAmount() {
        return 1;
    }

    @Override
    public EmiIngredient setAmount(long amount) {
        return null;
    }

    @Override
    public float getChance() {
        return 1;
    }

    @Override
    public EmiIngredient setChance(float chance) {
        return null;
    }

    @Override
    public List<EmiStack> getEmiStacks() {
        return EmiStack.EMPTY.getEmiStacks();
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public List<TooltipComponent> getTooltip() {
        List<TooltipComponent> tooltip = Lists.newArrayList();
        tooltip.add(new EmiTextTooltipWrapper(this, EmiPort.ordered(EmiPort.literal(content))));
        tooltip.add(new IngredientTooltipComponent(results));
        int item = (int) (System.currentTimeMillis() / 1000 % results.size());
        tooltip.addAll(results.get(item).copy().setAmount(1).getTooltip());
        return tooltip;
    }

    @ApiStatus.Internal
    public String getContent() {
        return content;
    }

    @ApiStatus.Internal
    public List<? extends EmiIngredient> getResults() {
        return results;
    }
}
