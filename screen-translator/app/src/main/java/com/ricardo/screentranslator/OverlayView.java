package com.ricardo.screentranslator;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/** Pinta los textos traducidos encima de los textos originales. */
public class OverlayView extends View {

    public static class Box {
        final Rect rect;
        final String text;
        StaticLayout layout;

        public Box(Rect rect, String text) {
            this.rect = rect;
            this.text = text;
        }
    }

    private final List<Box> boxes = new ArrayList<>();
    private final Paint bg = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final float density;

    public OverlayView(Context context) {
        super(context);
        density = context.getResources().getDisplayMetrics().density;
        bg.setColor(0xE6000000);
    }

    private float dp(float v) {
        return v * density;
    }

    public void setBoxes(List<Box> list) {
        boxes.clear();
        for (Box b : list) {
            fit(b);
            boxes.add(b);
        }
        invalidate();
    }

    public void clear() {
        boxes.clear();
        invalidate();
    }

    public boolean hasBoxes() {
        return !boxes.isEmpty();
    }

    private void fit(Box b) {
        int w = Math.max(b.rect.width(), (int) dp(60));
        float min = dp(9);
        float s = dp(22);
        StaticLayout l;
        while (true) {
            TextPaint p = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            p.setColor(Color.WHITE);
            p.setTextSize(s);
            l = StaticLayout.Builder.obtain(b.text, 0, b.text.length(), p, w)
                    .setIncludePad(false)
                    .build();
            if (l.getHeight() <= b.rect.height() || s <= min) {
                break;
            }
            s -= dp(1);
        }
        b.layout = l;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float pad = dp(3);
        for (Box b : boxes) {
            if (b.layout == null) {
                continue;
            }
            int w = b.layout.getWidth();
            int h = Math.max(b.rect.height(), b.layout.getHeight());
            canvas.drawRect(b.rect.left - pad, b.rect.top - pad,
                    b.rect.left + w + pad, b.rect.top + h + pad, bg);
            canvas.save();
            canvas.translate(b.rect.left, b.rect.top);
            b.layout.draw(canvas);
            canvas.restore();
        }
    }
}
