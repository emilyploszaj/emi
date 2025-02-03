package dev.emi.emi.config;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class SidebarPages {
	public final SidebarSettings settings;
	public List<SidebarPage> pages = Lists.newArrayList();

	public SidebarPages(List<SidebarPage> pages, SidebarSettings settings) {
		this.pages.addAll(pages);
		this.settings = settings;
		unique();
	}

	public void unique() {
		Set<SidebarType> types = Sets.newHashSet();
		for (int i = 0; i < pages.size(); i++) {
			if (pages.get(i).type == SidebarType.NONE || types.contains(pages.get(i).type)) {
				pages.remove(i--);
			} else {
				types.add(pages.get(i).type);
			}
		}
		if (settings.subpanels() != null) {
			List<SidebarSubpanels.Subpanel> subpanels = settings.subpanels().subpanels;
			for (int i = 0; i < subpanels.size(); i++) {
				if (subpanels.get(i).type == SidebarType.NONE || types.contains(subpanels.get(i).type)) {
					subpanels.remove(i--);
				} else {
					types.add(subpanels.get(i).type);
				}
			}
		}
	}

	public boolean canShowChess() {
		return settings.size().values.getInt(0) == 8
			&& settings.size().values.getInt(1) == 8
			&& settings.theme() == SidebarTheme.MODERN;
	}

	public static class SidebarPage {
		public SidebarType type;

		public SidebarPage(SidebarType type) {
			this.type = type;
		}
	}
}
