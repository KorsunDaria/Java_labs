package org.example.view;

import org.example.util.Vector2D;

public class Camera {
    private double scale = 1.0;
    private int screenWidth;
    private int screenHeight;

    private static final double WORLD_WIDTH = 1300.0;
    private static final double WORLD_HEIGHT = 670.0;

    public void updateScreenSize(int width, int height) {
        this.screenWidth = width;
        this.screenHeight = height;

        double scaleX = screenWidth / WORLD_WIDTH;
        double scaleY = screenHeight / WORLD_HEIGHT;
        this.scale = Math.min(scaleX, scaleY);
    }


    public int worldToScreenX(double worldX) {
        return (int) ((worldX * scale) + (screenWidth / 2.0));
    }


    public int worldToScreenY(double worldY) {
        return (int) ((worldY * scale) + (screenHeight / 2.0));
    }


    public int scaleSize(double size) {
        return (int) (size * scale);
    }
}
