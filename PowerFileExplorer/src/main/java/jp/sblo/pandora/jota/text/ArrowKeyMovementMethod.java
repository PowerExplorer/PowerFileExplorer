/*
 * Copyright (C) 2006 The Android Open Source Project
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


import java.lang.Character.UnicodeBlock;
import java.lang.reflect.Method;

import android.text.NoCopySpan;
import android.text.Spannable;
import android.text.method.MetaKeyKeyListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

// XXX this doesn't extend MetaKeyKeyListener because the signatures
// don't match.  Need to figure that out.  Meanwhile the meta keys
// won't work in fields that don't take input.

public class ArrowKeyMovementMethod extends BaseMovementMethod
{
    private boolean moveUp( TextView widget, Spannable buffer , boolean cap )
    {
        Layout layout = widget.getLayout();
        if ( cap ){
            return Selection.extendUp(buffer, layout);
        }else{
            return Selection.moveUp(buffer, layout);
        }
    }

    private boolean moveDown( TextView widget, Spannable buffer , boolean cap )
    {
        Layout layout = widget.getLayout();
        if ( cap ){
            return Selection.extendDown(buffer, layout);
        }else{
            return Selection.moveDown(buffer, layout);
        }
    }

    private boolean moveLeft( TextView widget, Spannable buffer , boolean cap )
    {
        Layout layout = widget.getLayout();
        if ( cap ){
            return Selection.extendLeft(buffer, layout);
        }else{
            return Selection.moveLeft(buffer, layout);
        }
    }

    private boolean moveRight( TextView widget, Spannable buffer , boolean cap )
    {
        Layout layout = widget.getLayout();
        if ( cap ){
            return Selection.extendRight(buffer, layout);
        }else{
            return Selection.moveRight(buffer, layout);
        }
    }

    private boolean movePageUp( TextView widget, Spannable buffer , boolean cap )
    {
        if ( cap ){
            widget.movePage(true,true);
            return true;
        }else{
            widget.movePage(true,false);
            return true;
        }
    }

    private boolean movePageDown( TextView widget, Spannable buffer , boolean cap )
    {
        if ( cap ){
            widget.movePage(false,true);
            return true;
        }else{
            widget.movePage(false,false);
            return true;
        }
    }

    private boolean moveHome(TextView widget, Spannable buffer, boolean cap )
    {
        Layout layout = widget.getLayout();
        if (cap) {
            return Selection.extendToLeftEdge(buffer, layout);
        } else {
            return Selection.moveToLeftEdge(buffer, layout);
        }
    }

    private boolean moveTop(TextView widget, Spannable buffer, boolean cap )
    {
        if (cap) {
            Selection.extendSelection(buffer, 0);
            return true;
        } else {
            Selection.setSelection(buffer, 0);
            return true;
        }
    }

    private boolean moveEnd(TextView widget, Spannable buffer, boolean cap )
    {
        Layout layout = widget.getLayout();
        if (cap) {
            return Selection.extendToRightEdge(buffer, layout);
        } else {
            return Selection.moveToRightEdge(buffer, layout);
        }
    }

    private boolean moveBottom(TextView widget, Spannable buffer, boolean cap )
    {
        if (cap) {
            Selection.extendSelection(buffer, buffer.length());
            return true;
        } else {
            Selection.setSelection(buffer, buffer.length());
            return true;
        }
    }
    public boolean doFunction(TextView widget, Spannable buffer, int function)
    {
        boolean cap = (MetaKeyKeyListener.getMetaState(buffer,
                KeyEvent.META_SHIFT_ON) == 1) ||
              (JotaTextKeyListener.getMetaStateSelecting(buffer) != 0);

        Log.d("=========================>","cap="+cap);

        switch( function )
        {
            case TextView.FUNCTION_CURSOR_LEFT:
                return moveLeft(widget, buffer, cap);
            case TextView.FUNCTION_CURSOR_RIGHT:
                return moveRight(widget, buffer, cap);
            case TextView.FUNCTION_CURSOR_UP:
                return moveUp(widget, buffer, cap);
            case TextView.FUNCTION_CURSOR_DOWN:
                return moveDown(widget, buffer, cap);
            case TextView.FUNCTION_PAGE_UP:
                return movePageUp(widget, buffer, cap);
            case TextView.FUNCTION_PAGE_DOWN:
                return movePageDown(widget, buffer, cap);
            case TextView.FUNCTION_HOME:
                return moveHome(widget, buffer, cap);
            case TextView.FUNCTION_END:
                return moveEnd(widget, buffer, cap);
            case TextView.FUNCTION_TOP:
                return moveTop(widget, buffer, cap);
            case TextView.FUNCTION_BOTTOM:
                return moveBottom(widget, buffer, cap);
        }
        return false;
    }


    private boolean up(TextView widget, Spannable buffer) {
        boolean cap = (MetaKeyKeyListener.getMetaState(buffer,
                        KeyEvent.META_SHIFT_ON) == 1) ||
                      (JotaTextKeyListener.getMetaStateSelecting(buffer) != 0);
        boolean alt = MetaKeyKeyListener.getMetaState(buffer,
                        KeyEvent.META_ALT_ON) == 1;
        Layout layout = widget.getLayout();

        if (cap) {
            if (alt) {
//                Selection.extendSelection(buffer, 0);
                widget.movePage(true,true);
                return true;
            } else {
                return Selection.extendUp(buffer, layout);
            }
        } else {
            if (alt) {
//                Selection.setSelection(buffer, 0);
                widget.movePage(true,false);
                return true;
            } else {
                return Selection.moveUp(buffer, layout);
            }
        }
    }

    private boolean down(TextView widget, Spannable buffer) {
        boolean cap = (MetaKeyKeyListener.getMetaState(buffer,
                        KeyEvent.META_SHIFT_ON) == 1) ||
                      (JotaTextKeyListener.getMetaStateSelecting(buffer) != 0);
        boolean alt = MetaKeyKeyListener.getMetaState(buffer,
                        KeyEvent.META_ALT_ON) == 1;
        Layout layout = widget.getLayout();

        if (cap) {
            if (alt) {
//                Selection.extendSelection(buffer, buffer.length());
                widget.movePage(false,true);
                return true;
            } else {
                return Selection.extendDown(buffer, layout);
            }
        } else {
            if (alt) {
//                Selection.setSelection(buffer, buffer.length());
                widget.movePage(false,false);
                return true;
            } else {
                return Selection.moveDown(buffer, layout);
            }
        }
    }

    private boolean left(TextView widget, Spannable buffer) {
        boolean cap = (MetaKeyKeyListener.getMetaState(buffer,
                        KeyEvent.META_SHIFT_ON) == 1) ||
                      (JotaTextKeyListener.getMetaStateSelecting(buffer) != 0);
        boolean alt = MetaKeyKeyListener.getMetaState(buffer,
                        KeyEvent.META_ALT_ON) == 1;
        Layout layout = widget.getLayout();

        if (cap) {
            if (alt) {
                return Selection.extendToLeftEdge(buffer, layout);
            } else {
                return Selection.extendLeft(buffer, layout);
            }
        } else {
            if (alt) {
                return Selection.moveToLeftEdge(buffer, layout);
            } else {
                return Selection.moveLeft(buffer, layout);
            }
        }
    }

    private boolean right(TextView widget, Spannable buffer) {
        boolean cap = (MetaKeyKeyListener.getMetaState(buffer,
                        KeyEvent.META_SHIFT_ON) == 1) ||
                      (JotaTextKeyListener.getMetaStateSelecting(buffer) != 0);
        boolean alt = MetaKeyKeyListener.getMetaState(buffer,
                        KeyEvent.META_ALT_ON) == 1;
        Layout layout = widget.getLayout();

        if (cap) {
            if (alt) {
                return Selection.extendToRightEdge(buffer, layout);
            } else {
                return Selection.extendRight(buffer, layout);
            }
        } else {
            if (alt) {
                return Selection.moveToRightEdge(buffer, layout);
            } else {
                return Selection.moveRight(buffer, layout);
            }
        }
    }

    private boolean movehome(TextView widget, Spannable buffer, KeyEvent event) {
        boolean cap = (MetaKeyKeyListener.getMetaState(buffer,
                        KeyEvent.META_SHIFT_ON) == 1) ||
                      (JotaTextKeyListener.getMetaStateSelecting(buffer) != 0);
        Layout layout = widget.getLayout();

        boolean ctrl = (event.getMetaState() & mShortcutCtrlKey)!=0;

        if ( ctrl ){
            if (cap) {
                Selection.extendSelection(buffer, 0);
                return true;
            } else {
                Selection.setSelection(buffer, 0);
                return true;
            }
        }else{
            if (cap) {
                return Selection.extendToLeftEdge(buffer, layout);
            } else {
                return Selection.moveToLeftEdge(buffer, layout);
            }
        }
    }

    private boolean moveend(TextView widget, Spannable buffer, KeyEvent event) {
        boolean cap = (MetaKeyKeyListener.getMetaState(buffer,
                        KeyEvent.META_SHIFT_ON) == 1) ||
                      (JotaTextKeyListener.getMetaStateSelecting(buffer) != 0);
        Layout layout = widget.getLayout();

        boolean ctrl = (event.getMetaState() & mShortcutCtrlKey)!=0;

        if ( ctrl ){
            if (cap) {
                Selection.extendSelection(buffer, buffer.length());
                return true;
            } else {
                Selection.setSelection(buffer, buffer.length());
                return true;
            }
        }else{
            if (cap) {
                return Selection.extendToRightEdge(buffer, layout);
            } else {
                return Selection.moveToRightEdge(buffer, layout);
            }
        }
    }

    private boolean volup(TextView widget, Spannable buffer) {
        boolean cap = (MetaKeyKeyListener.getMetaState(buffer,
                        KeyEvent.META_SHIFT_ON) == 1) ||
                      (JotaTextKeyListener.getMetaStateSelecting(buffer) != 0);
        if (cap) {
            widget.movePage(true,true);
            return true;
        } else {
            widget.movePage(true,false);
            return true;
        }
    }

    private boolean voldown(TextView widget, Spannable buffer) {
        boolean cap = (MetaKeyKeyListener.getMetaState(buffer,
                        KeyEvent.META_SHIFT_ON) == 1) ||
                      (JotaTextKeyListener.getMetaStateSelecting(buffer) != 0);
        if (cap) {
            widget.movePage(false,true);
            return true;
        } else {
            widget.movePage(false,false);
            return true;
        }
    }

    private int getOffset(int x, int y, TextView widget){
      // Converts the absolute X,Y coordinates to the character offset for the
      // character whose position is closest to the specified
      // horizontal position.
      x -= widget.getTotalPaddingLeft();
      y -= widget.getTotalPaddingTop();

      // Clamp the position to inside of the view.
      if (x < 0) {
          x = 0;
      } else if (x >= (widget.getWidth()-widget.getTotalPaddingRight())) {
          x = widget.getWidth()-widget.getTotalPaddingRight() - 1;
      }
      if (y < 0) {
          y = 0;
      } else if (y >= (widget.getHeight()-widget.getTotalPaddingBottom())) {
          y = widget.getHeight()-widget.getTotalPaddingBottom() - 1;
      }

      x += widget.getScrollX();
      y += widget.getScrollY();

      Layout layout = widget.getLayout();
      int line = layout.getLineForVertical(y);

      int offset = layout.getOffsetForHorizontal(line, x);
      return offset;
    }

    public boolean onKeyDown(TextView widget, Spannable buffer, int keyCode, KeyEvent event) {
        if (executeDown(widget, buffer, keyCode,event)) {
            MetaKeyKeyListener.adjustMetaAfterKeypress(buffer);
            resetLockedMeta(buffer);
            return true;
        }

        return false;
    }

    private boolean executeDown(TextView widget, Spannable buffer, int keyCode , KeyEvent event) {
        boolean handled = false;

        if ( keyCode == KEYCODE_PAGE_UP ){
            handled |= volup(widget, buffer);
        }else if ( keyCode == KEYCODE_PAGE_DOWN){
            handled |= voldown(widget, buffer);
        }else if ( keyCode == KEYCODE_MOVE_HOME){
            handled |= movehome(widget, buffer ,event);
        }else if ( keyCode == KEYCODE_MOVE_END){
            handled |= moveend(widget, buffer , event);
        }else{
            switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_UP:
                handled |= up(widget, buffer);
                break;

            case KeyEvent.KEYCODE_DPAD_DOWN:
                handled |= down(widget, buffer);
                break;

            case KeyEvent.KEYCODE_DPAD_LEFT:
                handled |= left(widget, buffer);
                break;

            case KeyEvent.KEYCODE_DPAD_RIGHT:
                handled |= right(widget, buffer);
                break;

            case KeyEvent.KEYCODE_DPAD_CENTER:
                if (JotaTextKeyListener.getMetaStateSelecting(buffer) != 0) {
                    if (widget.showContextMenu()) {
                        handled = true;
                    }
                }
                break;
            case KeyEvent.KEYCODE_VOLUME_UP:
                if ( sUseVolumeKey ){
                    handled |= volup(widget, buffer);
                }
                break;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if ( sUseVolumeKey ){
                    handled |= voldown(widget, buffer);
                }
                break;
            }
        }
        if (handled) {
            MetaKeyKeyListener.adjustMetaAfterKeypress(buffer);
            resetLockedMeta(buffer);
        }

        return handled;
    }

    public boolean onKeyUp(TextView widget, Spannable buffer, int keyCode, KeyEvent event) {
        switch (keyCode) {
        case KeyEvent.KEYCODE_VOLUME_UP:
            if ( sUseVolumeKey ){
                return true;
            }
            break;
        case KeyEvent.KEYCODE_VOLUME_DOWN:
            if ( sUseVolumeKey ){
                return true;
            }
            break;
        }
        return false;
    }

    public boolean onKeyOther(TextView view, Spannable text, KeyEvent event) {
        int code = event.getKeyCode();
        if (code != KeyEvent.KEYCODE_UNKNOWN
                && event.getAction() == KeyEvent.ACTION_MULTIPLE) {
            int repeat = event.getRepeatCount();
            boolean handled = false;
            while ((--repeat) > 0) {
                handled |= executeDown(view, text, code,event);
            }
            return handled;
        }
        return false;
    }

    public boolean onTrackballEvent(TextView widget, Spannable text,
            MotionEvent event) {
        return false;
    }

    public boolean onTouchEvent(TextView widget, Spannable buffer,
                                MotionEvent event) {
        int initialScrollX = -1, initialScrollY = -1;
        if (event.getAction() == MotionEvent.ACTION_UP) {
            initialScrollX = Touch.getInitialScrollX(widget, buffer);
            initialScrollY = Touch.getInitialScrollY(widget, buffer);
        }

        boolean handled = Touch.onTouchEvent(widget, buffer, event);

        if (widget.isFocused() && !widget.didTouchFocusSelect()) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
              boolean cap = (MetaKeyKeyListener.getMetaState(buffer,
                              KeyEvent.META_SHIFT_ON) == 1) ||
                            (JotaTextKeyListener.getMetaStateSelecting(buffer) != 0);
              int x = (int) event.getX() - sLineNumberWidth;	// Jota Text Editor
              int y = (int) event.getY();
              int offset = getOffset(x, y, widget);

              if (cap) {
                  buffer.setSpan(LAST_TAP_DOWN, offset, offset,
                                 Spannable.SPAN_POINT_POINT);

                  // Disallow intercepting of the touch events, so that
                  // users can scroll and select at the same time.
                  // without this, users would get booted out of select
                  // mode once the view detected it needed to scroll.
                  widget.getParent().requestDisallowInterceptTouchEvent(true);
              } else {
                  OnePointFiveTapState[] tap = buffer.getSpans(0, buffer.length(),
                      OnePointFiveTapState.class);

                  if (tap.length > 0) {
                      if (event.getEventTime() - tap[0].mWhen <=
                          ViewConfiguration.getDoubleTapTimeout() &&
                          sameWord(buffer, offset, Selection.getSelectionEnd(buffer))) {

                          tap[0].active = true;
                          MetaKeyKeyListener.startSelecting(widget, buffer);
                          widget.getParent().requestDisallowInterceptTouchEvent(true);
                          buffer.setSpan(LAST_TAP_DOWN, offset, offset,
                              Spannable.SPAN_POINT_POINT);
                      }

                      tap[0].mWhen = event.getEventTime();
                  } else {
                      OnePointFiveTapState newtap = new OnePointFiveTapState();
                      newtap.mWhen = event.getEventTime();
                      newtap.active = false;
                      buffer.setSpan(newtap, 0, buffer.length(),
                          Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                  }
              }
            } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                boolean cap = (MetaKeyKeyListener.getMetaState(buffer,
                                KeyEvent.META_SHIFT_ON) == 1) ||
                              (JotaTextKeyListener.getMetaStateSelecting(buffer) != 0);

                if (cap && handled) {
                    // Before selecting, make sure we've moved out of the "slop".
                    // handled will be true, if we're in select mode AND we're
                    // OUT of the slop

                    // Turn long press off while we're selecting. User needs to
                    // re-tap on the selection to enable longpress
                    widget.cancelLongPress();

                    // Update selection as we're moving the selection area.

                    // Get the current touch position
                    int x = (int) event.getX()- sLineNumberWidth;	// Jota Text Editor
                    int y = (int) event.getY();
                    int offset = getOffset(x, y, widget);

                    final OnePointFiveTapState[] tap = buffer.getSpans(0, buffer.length(),
                            OnePointFiveTapState.class);

                    if (tap.length > 0 && tap[0].active) {
                        // Get the last down touch position (the position at which the
                        // user started the selection)
                        int lastDownOffset = buffer.getSpanStart(LAST_TAP_DOWN);

                        // Compute the selection boundaries
                        int spanstart;
                        int spanend;
                        if (offset >= lastDownOffset) {
                            // Expand from word start of the original tap to new word
                            // end, since we are selecting "forwards"
                            spanstart = findWordStart(buffer, lastDownOffset);
                            spanend = findWordEnd(buffer, offset);
                        } else {
                            // Expand to from new word start to word end of the original
                            // tap since we are selecting "backwards".
                            // The spanend will always need to be associated with the touch
                            // up position, so that refining the selection with the
                            // trackball will work as expected.
                            spanstart = findWordEnd(buffer, lastDownOffset);
                            spanend = findWordStart(buffer, offset);
                        }
                        Selection.setSelection(buffer, spanstart, spanend);
                    } else {
                        Selection.extendSelection(buffer, offset);
                    }
                    return true;
                }
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                // If we have scrolled, then the up shouldn't move the cursor,
                // but we do need to make sure the cursor is still visible at
                // the current scroll offset to avoid the scroll jumping later
                // to show it.
                if ((initialScrollY >= 0 && initialScrollY != widget.getScrollY()) ||
                        (initialScrollX >= 0 && initialScrollX != widget.getScrollX())) {
//                    widget.moveCursorToVisibleOffset();
                    return true;
                }

                int x = (int) event.getX()- sLineNumberWidth;	// Jota Text Editor
                int y = (int) event.getY();
                int off = getOffset(x, y, widget);

                // XXX should do the same adjust for x as we do for the line.

                OnePointFiveTapState[] onepointfivetap = buffer.getSpans(0, buffer.length(),
                    OnePointFiveTapState.class);
                if (onepointfivetap.length > 0 && onepointfivetap[0].active &&
                    Selection.getSelectionStart(buffer) == Selection.getSelectionEnd(buffer)) {
                    // If we've set select mode, because there was a onepointfivetap,
                    // but there was no ensuing swipe gesture, undo the select mode
                    // and remove reference to the last onepointfivetap.
                    MetaKeyKeyListener.stopSelecting(widget, buffer);
                    for (int i=0; i < onepointfivetap.length; i++) {
                        buffer.removeSpan(onepointfivetap[i]);
                    }
                    buffer.removeSpan(LAST_TAP_DOWN);
                }
                boolean cap = (MetaKeyKeyListener.getMetaState(buffer,
                                KeyEvent.META_SHIFT_ON) == 1) ||
                              (JotaTextKeyListener.getMetaStateSelecting(buffer) != 0);

                DoubleTapState[] tap = buffer.getSpans(0, buffer.length(),
                                                       DoubleTapState.class);
                boolean doubletap = false;

                if (tap.length > 0) {
                    if (event.getEventTime() - tap[0].mWhen <=
                        ViewConfiguration.getDoubleTapTimeout() &&
                        sameWord(buffer, off, Selection.getSelectionEnd(buffer))) {

                        doubletap = true;
                    }

                    tap[0].mWhen = event.getEventTime();
                } else {
                    DoubleTapState newtap = new DoubleTapState();
                    newtap.mWhen = event.getEventTime();
                    buffer.setSpan(newtap, 0, buffer.length(),
                                   Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                }

                if (cap) {
                    buffer.removeSpan(LAST_TAP_DOWN);
                    if (onepointfivetap.length > 0 && onepointfivetap[0].active) {
                        // If we selecting something with the onepointfivetap-and
                        // swipe gesture, stop it on finger up.
                        MetaKeyKeyListener.stopSelecting(widget, buffer);
                    } else {
                        Selection.extendSelection(buffer, off);
                    }
                } else if (doubletap) {
                    Selection.setSelection(buffer,
                                           findWordStart(buffer, off),
                                           findWordEnd(buffer, off));
                } else {
                    Selection.setSelection(buffer, off);
                }

                MetaKeyKeyListener.adjustMetaAfterKeypress(buffer);
                resetLockedMeta(buffer);

                return true;
            }
        }

        return handled;
    }

    public static void selectWord( Spannable buffer , int off ){
        Selection.setSelection(buffer,
                findWordStart(buffer, off),
                findWordEnd(buffer, off));
    }

    public static void selectBlock( Spannable buffer , int off ){
        Selection.setSelection(buffer,
                findBlockStart(buffer, off),
                findBlockEnd(buffer, off));
    }

    private static class DoubleTapState implements NoCopySpan {
        long mWhen;
    }

    /* We check for a onepointfive tap. This is similar to
    *  doubletap gesture (where a finger goes down, up, down, up, in a short
    *  time period), except in the onepointfive tap, a users finger only needs
    *  to go down, up, down in a short time period. We detect this type of tap
    *  to implement the onepointfivetap-and-swipe selection gesture.
    *  This gesture allows users to select a segment of text without going
    *  through the "select text" option in the context menu.
    */
    private static class OnePointFiveTapState implements NoCopySpan {
        long mWhen;
        boolean active;
    }

    private static boolean sameWord(CharSequence text, int one, int two) {
        int start = findWordStart(text, one);
        int end = findWordEnd(text, one);

        if (end == start) {
            return false;
        }

        return start == findWordStart(text, two) &&
               end == findWordEnd(text, two);
    }

    // TODO: Unify with TextView.getWordForDictionary()
    private static int findWordStart(CharSequence text, int start) {
        if ( text.length() <= start ){
            return start;
        }

        UnicodeBlock c0 = UnicodeBlock.of(text.charAt(start));

        for (; start > 0; start--) {
            char c = text.charAt(start - 1);
            UnicodeBlock cb = UnicodeBlock.of(c);
            if ( c0 == UnicodeBlock.BASIC_LATIN ){
                int type = Character.getType(c);

                if (c != '\'' &&
                    type != Character.UPPERCASE_LETTER &&
                    type != Character.LOWERCASE_LETTER &&
                    type != Character.TITLECASE_LETTER &&
                    type != Character.MODIFIER_LETTER &&
                    type != Character.DECIMAL_DIGIT_NUMBER) {
                    break;
                }
            }else if ( c0 != cb ){
                break;
            }
        }

        return start;
    }

    // TODO: Unify with TextView.getWordForDictionary()
    private static int findWordEnd(CharSequence text, int end) {
        int len = text.length();

        if ( len <= end ){
            return end;
        }

        UnicodeBlock c0 = UnicodeBlock.of(text.charAt(end));

        for (; end < len; end++) {
            char c = text.charAt(end);
            UnicodeBlock cb = UnicodeBlock.of(c);
            if ( c0 == UnicodeBlock.BASIC_LATIN ){
                int type = Character.getType(c);

                if (c != '\'' &&
                    type != Character.UPPERCASE_LETTER &&
                    type != Character.LOWERCASE_LETTER &&
                    type != Character.TITLECASE_LETTER &&
                    type != Character.MODIFIER_LETTER &&
                    type != Character.DECIMAL_DIGIT_NUMBER) {
                    break;
                }
            }else if ( c0 != cb ){
                break;
            }
        }

        return end;
    }

    public static int findBlockStart(CharSequence text, int start) {
        if ( text.length() <= start ){
            return start;
        }

        for (; start > 0; start--) {
            char c = text.charAt(start - 1);
            if ( c == '\n' ){
                break;
            }
        }
        return start;
    }

    // TODO: Unify with TextView.getWordForDictionary()
    public  static int findBlockEnd(CharSequence text, int end) {
        int len = text.length();

        if ( len <= end ){
            return end;
        }

        for (; end < len; end++) {
            char c = text.charAt(end);
            if ( c == '\n' ){
                break;
            }
        }

        return end;
    }

    public boolean canSelectArbitrarily() {
        return true;
    }

    public void initialize(TextView widget, Spannable text) {
        Selection.setSelection(text, 0);
    }

    public void onTakeFocus(TextView view, Spannable text, int dir) {
//        if ((dir & (View.FOCUS_FORWARD | View.FOCUS_DOWN)) != 0) {
//            Layout layout = view.getLayout();
//
//            if (layout == null) {
//                /*
//                 * This shouldn't be null, but do something sensible if it is.
//                 */
//                Selection.setSelection(text, text.length());
//            } else {
//                /*
//                 * Put the cursor at the end of the first line, which is
//                 * either the last offset if there is only one line, or the
//                 * offset before the first character of the second line
//                 * if there is more than one line.
//                 */
//                if (layout.getLineCount() == 1) {
//                    Selection.setSelection(text, text.length());
//                } else {
//                    Selection.setSelection(text, layout.getLineStart(1) - 1);
//                }
//            }
//        } else {
//            Selection.setSelection(text, text.length());
//        }
    }

    public static void resetLockedMeta(Spannable text) {
        try{
            if ( sMethod != null ){
                sMethod.invoke(null,text);
            }
        }catch(Exception e)
        {
        }
    }

    public void cancelFling(TextView widget, Spannable buffer)
    {
        Touch.cancelFling(widget, buffer);
    }

    public static MovementMethod getInstance() {
        if (sInstance == null)
            sInstance = new ArrowKeyMovementMethod();

            try{
                Class<?> c = MetaKeyKeyListener.class;
                sMethod = c.getDeclaredMethod("resetLockedMeta", new Class[]{ Spannable.class });
                sMethod.setAccessible(true);

            }catch(Exception e){
        }
        return sInstance;
    }

	// Jota Text Editor
    public static void setUseVolumeKey( boolean useVolumeKey )
    {
        sUseVolumeKey = useVolumeKey;
    }
    // Jota Text Editor
    public static void setLineNumberWidth( int lineNumberWidth )
    {
        sLineNumberWidth = lineNumberWidth;
    }
    // Jota Text Editor
    public static void setHomeEndKeycode( int home , int end )
    {
        KEYCODE_MOVE_HOME = home;
        KEYCODE_MOVE_END = end;
    }
    // Jota Text Editor
    public static void setCtrlKey(int ctrlkey) {
        mShortcutCtrlKey = ctrlkey;
    }


    private static int sLineNumberWidth;	// Jota Text Editor
    private static boolean sUseVolumeKey = true;	// Jota Text Editor
    private static Method sMethod;
    private static final Object LAST_TAP_DOWN = new Object();
    private static ArrowKeyMovementMethod sInstance;

    private static int KEYCODE_MOVE_HOME = 122; // Jota Text Editor
    private static int KEYCODE_MOVE_END = 123;  // Jota Text Editor
    private static int KEYCODE_PAGE_UP = 92;    // Jota Text Editor
    private static int KEYCODE_PAGE_DOWN = 93;  // Jota Text Editor
    private static int mShortcutCtrlKey = 0x1000;  // Jota Text Editor
}
