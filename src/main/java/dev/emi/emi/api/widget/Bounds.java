package dev.emi.emi.api.widget;

public record Bounds(int x, int y, int width, int height) {
	public static final Bounds EMPTY = new Bounds(0, 0, 0, 0);

	public int left() {
		return x;
	}

	public int right() {
		return x + width;
	}

	public int top() {
		return y;
	}

	public int bottom() {
		return y + height;
	}
	
	public boolean contains(int x, int y) {
		return x >= this.x() && x < this.x() + this.width() && y >= this.y() && y < this.y() + this.height();
	}

	public boolean empty() {
		return width <= 0 || height <= 0;
	}

	public Bounds overlap(Bounds another) {
		int left = Math.max(left(), another.left());
		int top = Math.max(top(), another.top());
		Bounds b = new Bounds(
			left, top,
			Math.min(right(), another.right()) - left,
			Math.min(bottom(), another.bottom()) - top
		);
		return b;
	}
}
