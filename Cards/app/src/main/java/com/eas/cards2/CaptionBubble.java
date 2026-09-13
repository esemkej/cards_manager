package com.eas.cards2;

import android.graphics.Canvas;
import android.graphics.drawable.GradientDrawable;
import android.text.Layout;
import android.widget.TextView;

/** Follow rendered line bounds rather than TextView's available wrapping width. */
final class CaptionBubble extends GradientDrawable {
    private final TextView text;
    CaptionBubble(TextView text, int[] colors) {
        super(Orientation.TOP_BOTTOM, colors);
        this.text = text;
    }
    @Override public void draw(Canvas canvas) {
        Layout layout = text.getLayout();
        if (layout == null || layout.getLineCount() == 0) return;
        float left = Float.MAX_VALUE, right = 0;
        for (int i = 0; i < Math.min(layout.getLineCount(), text.getMaxLines()); i++) {
            left = Math.min(left, layout.getLineLeft(i));
            right = Math.max(right, layout.getLineRight(i));
        }
        int start = Math.max(0, (int) Math.floor(left));
        int end = Math.min(text.getWidth(), (int) Math.ceil(right) + text.getCompoundPaddingLeft() + text.getCompoundPaddingRight());
        if (getBounds().left != start || getBounds().right != end || getBounds().bottom != text.getHeight()) {
            setBounds(start, 0, end, text.getHeight());
        }
        super.draw(canvas);
    }
}
