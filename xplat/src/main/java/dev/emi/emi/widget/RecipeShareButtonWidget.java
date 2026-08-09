package dev.emi.emi.widget;

import dev.emi.emi.EmiPort;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.runtime.EmiShareRecipe;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;

import java.util.List;

public class RecipeShareButtonWidget extends RecipeButtonWidget{

    public boolean visible = true;

    public RecipeShareButtonWidget(int x, int y, EmiRecipe recipe) {
        super(x, y, 84, 0, recipe);

        if(visible && !EmiShareRecipe.isSupportedRecipe(recipe)) {
            this.visible = false;
        }

    }

    @Override
    public void render(DrawContext raw, int mouseX, int mouseY, float delta) {
        if(visible){
        super.render(raw, mouseX, mouseY, delta);
        }
    }

    @Override
    public List<TooltipComponent> getTooltip(int mouseX, int mouseY) {
        return List.of(TooltipComponent.of(EmiPort.ordered(EmiPort.translatable("tooltip.emi.recipe_share_widget"))));
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        this.playButtonSound();

        EmiShareRecipe.sendMessage(recipe);
        return true;
    }
}
