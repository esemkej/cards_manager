package com.eas.cards2;

import android.graphics.Canvas;
import android.text.Layout;
import android.widget.TextView;

/** Follow rendered line bounds rather than TextView's available wrapping width. */
final class CaptionBubble extends LiquidGlassDrawable {
    private final TextView text;
    CaptionBubble(TextView text, int[] colors) {
        super(text, (android.view.View) text.getParent().getParent(), true, colors);
        this.text = text;
    }
    @Override public void draw(Canvas canvas) {
        Layout layout = text.getLayout();
        if (layout == null || layout.getLineCount() == 0) return;
        float lineWidth = 0;
        for (int i = 0; i < Math.min(layout.getLineCount(), text.getMaxLines()); i++) {
            lineWidth = Math.max(lineWidth, layout.getLineWidth(i));
        }
        // Captions are centered. Single-line TextViews can use a very wide scrolling
        // Layout, so its absolute line offsets are not coordinates in this view.
        int width = Math.min(text.getWidth(), (int) Math.ceil(lineWidth)
                + text.getCompoundPaddingLeft() + text.getCompoundPaddingRight());
        int start = (text.getWidth() - width) / 2;
        int end = start + width;
        if (getBounds().left != start || getBounds().right != end || getBounds().bottom != text.getHeight()) {
            setBounds(start, 0, end, text.getHeight());
        }
        super.draw(canvas);
    }
}
