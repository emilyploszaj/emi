package dev.emi.emi.config;

import java.util.List;

import com.google.common.collect.Lists;

public class SidebarSubpanels {
	public final SidebarSettings settings;
	public List<Subpanel> subpanels = Lists.newArrayList();

	public SidebarSubpanels(List<Subpanel> subpanels, SidebarSettings settings) {
		this.subpanels.addAll(subpanels);
		this.settings = settings;
		unique();
	}

	public void unique() {
		settings.pages().unique();
	}

	public static class Subpanel {
		public SidebarType type;
		public int rows;

		public Subpanel(SidebarType type, int rows) {
			this.type = type;
			this.rows = rows;
		}

		public int rows() {
			return Math.max(1, rows);
		}
	}
}
