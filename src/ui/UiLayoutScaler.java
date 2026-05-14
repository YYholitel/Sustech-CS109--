package ui;

import java.awt.Rectangle;

public final class UiLayoutScaler {
    private final int baseWidth;
    private final int baseHeight;

    public UiLayoutScaler(int baseWidth, int baseHeight) {
        this.baseWidth = Math.max(1, baseWidth);
        this.baseHeight = Math.max(1, baseHeight);
    }

    public double getScaleFactor(int currentWidth, int currentHeight) {
        double widthScale = currentWidth / (double) baseWidth;
        double heightScale = currentHeight / (double) baseHeight;
        return Math.min(widthScale, heightScale);
    }

    public int scale(int baseValue, double factor) {
        return Math.max(1, (int) Math.round(baseValue * factor));
    }

    public Rectangle scaleRect(int x, int y, int width, int height, double factor) {
        return new Rectangle(scale(x, factor), scale(y, factor), scale(width, factor), scale(height, factor));
    }

    public int centerX(int currentWidth, int baseElementWidth, double factor) {
        return (currentWidth - scale(baseElementWidth, factor)) / 2;
    }
}