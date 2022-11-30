package dev.emi.emi.config;

import dev.emi.emi.EmiPort;
import net.minecraft.text.Text;

public class ScreenAlign {
	public Horizontal horizontal;
	public Vertical vertical;

	public ScreenAlign(Horizontal horizontal, Vertical vertical) {
		this.horizontal = horizontal;
		this.vertical = vertical;
	}
	
	public static enum Horizontal implements ConfigEnum {
		LEFT("left"),
		CENTER("center"),
		RIGHT("right"),
		;

		private final String name;

		private Horizontal(String name) {
			this.name = name;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public Text getText() {
			return EmiPort.translatable("emi.align.horizontal." + name);
		}

		public static Horizontal fromName(String name) {
			for (Horizontal s : values()) {
				if (s.getName().equals(name)) {
					return s;
				}
			}
			return Horizontal.CENTER;
		}
	}
	
	public static enum Vertical implements ConfigEnum {
		TOP("top"),
		CENTER("center"),
		BOTTOM("bottom"),
		;

		private final String name;

		private Vertical(String name) {
			this.name = name;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public Text getText() {
			return EmiPort.translatable("emi.align.vertical." + name);
		}

		public static Vertical fromName(String name) {
			for (Vertical s : values()) {
				if (s.getName().equals(name)) {
					return s;
				}
			}
			return Vertical.CENTER;
		}
	}
}
