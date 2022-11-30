package dev.emi.emi.config;

import java.util.List;
import java.util.Set;
import java.util.function.BooleanSupplier;

import org.apache.commons.compress.utils.Lists;

import com.google.common.collect.Sets;

public class SidebarPages {
	public List<SidebarPage> pages = Lists.newArrayList();
	public BooleanSupplier showChess;

	public SidebarPages(List<SidebarPage> pages, BooleanSupplier showChess) {
		this.pages.addAll(pages);
		this.showChess = showChess;
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
	}

	public static class SidebarPage {
		public SidebarType type;

		public SidebarPage(SidebarType type) {
			this.type = type;
		}
	}
}
