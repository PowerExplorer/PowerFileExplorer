/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jp.sblo.pandora.jota.text;

import jp.sblo.pandora.jota.ApiWrapper;
import android.text.Spannable;
import android.text.method.MetaKeyKeyListener;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;

/**
 * Base classes for movement methods.
 */
abstract public class BaseMovementMethod implements MovementMethod {


    @Override
    public boolean onGenericMotionEvent(TextView widget, Spannable text, MotionEvent event) {
        if ((ApiWrapper.getSourceOfEvent(event) & InputDevice.SOURCE_CLASS_POINTER) != 0) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_SCROLL: {
                    final float vscroll;
                    final float hscroll;
                    if ((event.getMetaState() & KeyEvent.META_SHIFT_ON) != 0) {
                        vscroll = 0;
                        hscroll = ApiWrapper.getAxisValue(event,MotionEvent.AXIS_VSCROLL);
                    } else {
                        vscroll = -ApiWrapper.getAxisValue(event,MotionEvent.AXIS_VSCROLL);
                        hscroll = ApiWrapper.getAxisValue(event,MotionEvent.AXIS_HSCROLL);
                    }

                    boolean handled = false;
                    if (hscroll < 0) {
                        handled |= scrollLeft(widget, text, (int)Math.ceil(-hscroll));
                    } else if (hscroll > 0) {
                        handled |= scrollRight(widget, text, (int)Math.ceil(hscroll));
                    }
                    if (vscroll < 0) {
                        handled |= scrollUp(widget, text, (int)Math.ceil(-vscroll));
                        if ( handled ){
                        	widget.moveCursorToVisibleOffset();
                        }
                    } else if (vscroll > 0) {
                        handled |= scrollDown(widget, text, (int)Math.ceil(vscroll));
                        if ( handled ){
                        	widget.moveCursorToVisibleOffset();
                        }
                    }
                    return handled;
                }
            }
        }
        return false;
    }

//    /**
//     * Gets the meta state used for movement using the modifiers tracked by the text
//     * buffer as well as those present in the key event.
//     *
//     * The movement meta state excludes the state of locked modifiers or the SHIFT key
//     * since they are not used by movement actions (but they may be used for selection).
//     *
//     * @param buffer The text buffer.
//     * @param event The key event.
//     * @return The keyboard meta states used for movement.
//     */
//    protected int getMovementMetaState(Spannable buffer, KeyEvent event) {
//        // We ignore locked modifiers and SHIFT.
//        int metaState = (event.getMetaState() | MetaKeyKeyListener.getMetaState(buffer))
//                & ~(MetaKeyKeyListener.META_ALT_LOCKED | MetaKeyKeyListener.META_SYM_LOCKED);
//        return KeyEvent.normalizeMetaState(metaState) & ~KeyEvent.META_SHIFT_MASK;
//    }

    private int getTopLine(TextView widget) {
        return widget.getLayout().getLineForVertical(widget.getScrollY());
    }

    private int getBottomLine(TextView widget) {
        return widget.getLayout().getLineForVertical(widget.getScrollY() + getInnerHeight(widget));
    }

    private int getInnerWidth(TextView widget) {
        return widget.getWidth() - widget.getTotalPaddingLeft() - widget.getTotalPaddingRight();
    }

    private int getInnerHeight(TextView widget) {
        return widget.getHeight() - widget.getTotalPaddingTop() - widget.getTotalPaddingBottom();
    }

    private int getCharacterWidth(TextView widget) {
        return (int) Math.ceil(widget.getPaint().getFontSpacing());
    }

    private int getScrollBoundsLeft(TextView widget) {
        final Layout layout = widget.getLayout();
        final int topLine = getTopLine(widget);
        final int bottomLine = getBottomLine(widget);
        if (topLine > bottomLine) {
            return 0;
        }
        int left = Integer.MAX_VALUE;
        for (int line = topLine; line <= bottomLine; line++) {
            final int lineLeft = (int) Math.floor(layout.getLineLeft(line));
            if (lineLeft < left) {
                left = lineLeft;
            }
        }
        return left;
    }

    private int getScrollBoundsRight(TextView widget) {
        final Layout layout = widget.getLayout();
        final int topLine = getTopLine(widget);
        final int bottomLine = getBottomLine(widget);
        if (topLine > bottomLine) {
            return 0;
        }
        int right = Integer.MIN_VALUE;
        for (int line = topLine; line <= bottomLine; line++) {
            final int lineRight = (int) Math.ceil(layout.getLineRight(line));
            if (lineRight > right) {
                right = lineRight;
            }
        }
        return right;
    }

    /**
     * Performs a scroll left action.
     * Scrolls left by the specified number of characters.
     *
     * @param widget The text view.
     * @param buffer The text buffer.
     * @param amount The number of characters to scroll by.  Must be at least 1.
     * @return True if the event was handled.
     * @hide
     */
    protected boolean scrollLeft(TextView widget, Spannable buffer, int amount) {
        final int minScrollX = getScrollBoundsLeft(widget);
        int scrollX = widget.getScrollX();
        if (scrollX > minScrollX) {
            scrollX = Math.max(scrollX - getCharacterWidth(widget) * amount, minScrollX);
            widget.scrollTo(scrollX, widget.getScrollY());
            return true;
        }
        return false;
    }

    /**
     * Performs a scroll right action.
     * Scrolls right by the specified number of characters.
     *
     * @param widget The text view.
     * @param buffer The text buffer.
     * @param amount The number of characters to scroll by.  Must be at least 1.
     * @return True if the event was handled.
     * @hide
     */
    protected boolean scrollRight(TextView widget, Spannable buffer, int amount) {
        final int maxScrollX = getScrollBoundsRight(widget) - getInnerWidth(widget);
        int scrollX = widget.getScrollX();
        if (scrollX < maxScrollX) {
            scrollX = Math.min(scrollX + getCharacterWidth(widget) * amount, maxScrollX);
            widget.scrollTo(scrollX, widget.getScrollY());
            return true;
        }
        return false;
    }

    /**
     * Performs a scroll up action.
     * Scrolls up by the specified number of lines.
     *
     * @param widget The text view.
     * @param buffer The text buffer.
     * @param amount The number of lines to scroll by.  Must be at least 1.
     * @return True if the event was handled.
     * @hide
     */
    protected boolean scrollUp(TextView widget, Spannable buffer, int amount) {
        final Layout layout = widget.getLayout();
        final int top = widget.getScrollY();
        int topLine = layout.getLineForVertical(top);
        if (layout.getLineTop(topLine) == top) {
            // If the top line is partially visible, bring it all the way
            // into view; otherwise, bring the previous line into view.
            topLine -= 1;
        }
        if (topLine >= 0) {
            topLine = Math.max(topLine - amount + 1, 0);
            Touch.scrollTo(widget, layout, widget.getScrollX(), layout.getLineTop(topLine));
            return true;
        }
        return false;
    }

    /**
     * Performs a scroll down action.
     * Scrolls down by the specified number of lines.
     *
     * @param widget The text view.
     * @param buffer The text buffer.
     * @param amount The number of lines to scroll by.  Must be at least 1.
     * @return True if the event was handled.
     * @hide
     */
    protected boolean scrollDown(TextView widget, Spannable buffer, int amount) {
        final Layout layout = widget.getLayout();
        final int innerHeight = getInnerHeight(widget);
        final int bottom = widget.getScrollY() + innerHeight;
        int bottomLine = layout.getLineForVertical(bottom);
        if (layout.getLineTop(bottomLine + 1) < bottom + 1) {
            // Less than a pixel of this line is out of view,
            // so we must have tried to make it entirely in view
            // and now want the next line to be in view instead.
            bottomLine += 1;
        }
        final int limit = layout.getLineCount() - 1;
        if (bottomLine <= limit) {
            bottomLine = Math.min(bottomLine + amount - 1, limit);
            Touch.scrollTo(widget, layout, widget.getScrollX(),
                    layout.getLineTop(bottomLine + 1) - innerHeight);
            return true;
        }
        return false;
    }

    /**
     * Performs a scroll page up action.
     * Scrolls up by one page.
     *
     * @param widget The text view.
     * @param buffer The text buffer.
     * @return True if the event was handled.
     * @hide
     */
    protected boolean scrollPageUp(TextView widget, Spannable buffer) {
        final Layout layout = widget.getLayout();
        final int top = widget.getScrollY() - getInnerHeight(widget);
        int topLine = layout.getLineForVertical(top);
        if (topLine >= 0) {
            Touch.scrollTo(widget, layout, widget.getScrollX(), layout.getLineTop(topLine));
            return true;
        }
        return false;
    }

    /**
     * Performs a scroll page up action.
     * Scrolls down by one page.
     *
     * @param widget The text view.
     * @param buffer The text buffer.
     * @return True if the event was handled.
     * @hide
     */
    protected boolean scrollPageDown(TextView widget, Spannable buffer) {
        final Layout layout = widget.getLayout();
        final int innerHeight = getInnerHeight(widget);
        final int bottom = widget.getScrollY() + innerHeight + innerHeight;
        int bottomLine = layout.getLineForVertical(bottom);
        if (bottomLine <= layout.getLineCount() - 1) {
            Touch.scrollTo(widget, layout, widget.getScrollX(),
                    layout.getLineTop(bottomLine + 1) - innerHeight);
            return true;
        }
        return false;
    }

    /**
     * Performs a scroll to top action.
     * Scrolls to the top of the document.
     *
     * @param widget The text view.
     * @param buffer The text buffer.
     * @return True if the event was handled.
     * @hide
     */
    protected boolean scrollTop(TextView widget, Spannable buffer) {
        final Layout layout = widget.getLayout();
        if (getTopLine(widget) >= 0) {
            Touch.scrollTo(widget, layout, widget.getScrollX(), layout.getLineTop(0));
            return true;
        }
        return false;
    }

    /**
     * Performs a scroll to bottom action.
     * Scrolls to the bottom of the document.
     *
     * @param widget The text view.
     * @param buffer The text buffer.
     * @return True if the event was handled.
     * @hide
     */
    protected boolean scrollBottom(TextView widget, Spannable buffer) {
        final Layout layout = widget.getLayout();
        final int lineCount = layout.getLineCount();
        if (getBottomLine(widget) <= lineCount - 1) {
            Touch.scrollTo(widget, layout, widget.getScrollX(),
                    layout.getLineTop(lineCount) - getInnerHeight(widget));
            return true;
        }
        return false;
    }

    /**
     * Performs a scroll to line start action.
     * Scrolls to the start of the line.
     *
     * @param widget The text view.
     * @param buffer The text buffer.
     * @return True if the event was handled.
     * @hide
     */
    protected boolean scrollLineStart(TextView widget, Spannable buffer) {
        final int minScrollX = getScrollBoundsLeft(widget);
        int scrollX = widget.getScrollX();
        if (scrollX > minScrollX) {
            widget.scrollTo(minScrollX, widget.getScrollY());
            return true;
        }
        return false;
    }

    /**
     * Performs a scroll to line end action.
     * Scrolls to the end of the line.
     *
     * @param widget The text view.
     * @param buffer The text buffer.
     * @return True if the event was handled.
     * @hide
     */
    protected boolean scrollLineEnd(TextView widget, Spannable buffer) {
        final int maxScrollX = getScrollBoundsRight(widget) - getInnerWidth(widget);
        int scrollX = widget.getScrollX();
        if (scrollX < maxScrollX) {
            widget.scrollTo(maxScrollX, widget.getScrollY());
            return true;
        }
        return false;
    }
}
