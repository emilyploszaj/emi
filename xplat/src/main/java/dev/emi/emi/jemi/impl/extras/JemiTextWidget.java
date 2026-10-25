package dev.emi.emi.jemi.impl.extras;

import java.util.List;

import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.placement.VerticalAlignment;
import mezz.jei.api.gui.widgets.ITextWidget;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.FormattedText;

public class JemiTextWidget extends JemiPlaceable<ITextWidget> implements ITextWidget {
	public int color = 0xffffffff;
	public boolean shadow = true;
	public int spacing = 0;
	public HorizontalAlignment horizontal = HorizontalAlignment.LEFT;
	public VerticalAlignment vertical = VerticalAlignment.TOP;
	public List<FormattedText> text;

	public JemiTextWidget(List<FormattedText> text, int width, int height) {
		super(width, height);
		this.text = text;
	}

	@Override
	public ITextWidget setFont(Font font) {
		// Unimplemented
		return this;
	}

	@Override
	public ITextWidget setColor(int color) {
		this.color = color;
		return this;
	}

	@Override
	public ITextWidget setLineSpacing(int spacing) {
		this.spacing = spacing;
		return this;
	}

	@Override
	public ITextWidget setShadow(boolean shadow) {
		this.shadow = shadow;
		return this;
	}

	@Override
	public ITextWidget setTextAlignment(HorizontalAlignment horizontalAlignment) {
		this.horizontal = horizontalAlignment;
		return this;
	}

	@Override
	public ITextWidget setTextAlignment(VerticalAlignment verticalAlignment) {
		this.vertical = verticalAlignment;
		return this;
	}
	
}
