package com.example.snakegame;

import android.graphics.Rect;

public class Food {
    Rect rect;
    float x = 0.0F;
    float y = 0.0F;

    int width = 100;
    int height = 100;

    public Food(float x, float y) {
        this.x = x;
        this.y = y;
        rect = new Rect((int) x, (int) y, (int) x + width, (int) y + height);
    }

    public Rect getRect() {
        return rect;
    }

    public void setPos(float x, float y) {
        this.x = x;
        this.y = y;
        rect = new Rect((int) x, (int) y, (int) x + width, (int) y + height);
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }
}
