package dev.emi.emi.config;

import java.util.function.Supplier;

public class SidebarSettings {
	public static final SidebarSettings LEFT = new SidebarSettings(
		() -> EmiConfig.leftSidebarPages,
		() -> EmiConfig.leftSidebarSubpanels,
		() -> EmiConfig.leftSidebarSize,
		() -> EmiConfig.leftSidebarMargins,
		() -> EmiConfig.leftSidebarAlign,
		() -> EmiConfig.leftSidebarHeader,
		() -> EmiConfig.leftSidebarTheme
	);
	public static final SidebarSettings RIGHT = new SidebarSettings(
		() -> EmiConfig.rightSidebarPages,
		() -> EmiConfig.rightSidebarSubpanels,
		() -> EmiConfig.rightSidebarSize,
		() -> EmiConfig.rightSidebarMargins,
		() -> EmiConfig.rightSidebarAlign,
		() -> EmiConfig.rightSidebarHeader,
		() -> EmiConfig.rightSidebarTheme
	);
	public static final SidebarSettings TOP = new SidebarSettings(
		() -> EmiConfig.topSidebarPages,
		() -> EmiConfig.topSidebarSubpanels,
		() -> EmiConfig.topSidebarSize,
		() -> EmiConfig.topSidebarMargins,
		() -> EmiConfig.topSidebarAlign,
		() -> EmiConfig.topSidebarHeader,
		() -> EmiConfig.topSidebarTheme
	);
	public static final SidebarSettings BOTTOM = new SidebarSettings(
		() -> EmiConfig.bottomSidebarPages,
		() -> EmiConfig.bottomSidebarSubpanels,
		() -> EmiConfig.bottomSidebarSize,
		() -> EmiConfig.bottomSidebarMargins,
		() -> EmiConfig.bottomSidebarAlign,
		() -> EmiConfig.bottomSidebarHeader,
		() -> EmiConfig.bottomSidebarTheme
	);
	private Supplier<SidebarPages> pages;
	private Supplier<SidebarSubpanels> subpanels;
	private Supplier<IntGroup> size;
	private Supplier<Margins> margins;
	private Supplier<ScreenAlign> align;
	private Supplier<HeaderType> header;
	private Supplier<SidebarTheme> theme;

	public SidebarSettings(Supplier<SidebarPages> pages, Supplier<SidebarSubpanels> subpanels,
			Supplier<IntGroup> size, Supplier<Margins> margins, Supplier<ScreenAlign> align,
			Supplier<HeaderType> header, Supplier<SidebarTheme> theme) {
		this.pages = pages;
		this.subpanels = subpanels;
		this.size = size;
		this.margins = margins;
		this.align = align;
		this.header = header;
		this.theme = theme;
	}

	public SidebarPages pages() {
		return pages.get();
	}

	public SidebarSubpanels subpanels() {
		return subpanels.get();
	}

	public IntGroup size() {
		return size.get();
	}

	public Margins margins() {
		return margins.get();
	}

	public ScreenAlign align() {
		return align.get();
	}

	public HeaderType header() {
		return header.get();
	}

	public SidebarTheme theme() {
		return theme.get();
	}
}
