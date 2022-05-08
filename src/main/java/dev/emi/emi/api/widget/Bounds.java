package dev.emi.emi.api.widget;

public record Bounds(int x, int y, int width, int height) {
	
	public boolean contains(int x, int y) {
		return x >= this.x() && x < this.x() + this.width() && y >= this.y() && y < this.y() + this.height();
	}
}
