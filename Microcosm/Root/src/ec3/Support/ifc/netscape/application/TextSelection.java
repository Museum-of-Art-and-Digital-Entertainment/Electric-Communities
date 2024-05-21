// TextSelection.java
// By Ned Etcode
// Copyright 1995, 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;

import netscape.util.*;

/** Object subclass that maintains a TextView's selection information and
  * also manages its insertion point blinking.<br><br>
  * @note 1.0 changes
  */
class TextSelection implements Target {
    TextView            _owner;
    private TextParagraph       _editParagraph;
    TextPositionInfo    _insertionPointInfo, _anchorPositionInfo,
                        _endPositionInfo;
    Timer               _blinkTimer;
    long                _lastFlashTime;
    int                 _anchorPosition, _endPosition;
    boolean             _flashInsertionPoint, _insertionPointShowing,
                        _ignoreNextFlash;
    boolean             _selectionDefined;
    int                 _insertionPointDisabled;


/* constructors */
     TextSelection() {
        super();
        _selectionDefined = false;
    }

     TextSelection(TextView textView) {
        this();
        init(textView);
        _selectionDefined = false;
    }



/* initializers */


     void init(TextView owner) {
        _owner = owner;

    }

    void _startFlashing() {

        if( _insertionPointDisabled != 0)
            return;

        if(!_owner.isEditable())
            return;

        if(! _selectionDefined )
            return;
        if( _blinkTimer == null )
            _blinkTimer = new Timer(this, "blinkCaret", 700);

        if (_owner.isEditing()) {
            _blinkTimer.start();
        } else if ((System.currentTimeMillis() - _lastFlashTime > 350)) {
          /* if almost time to blink again, don't */
            _ignoreNextFlash = true;
        }
        showInsertionPoint();
    }

    void _stopFlashing() {
        hideInsertionPoint();
        if (_blinkTimer != null) {
            _blinkTimer.stop();
            _ignoreNextFlash = false;
        }
    }

    public void performCommand(String command, Object data) {
        if (_owner.isEditing() && _selectionDefined) {
            /* toggle insertion point */
            if (_ignoreNextFlash || !_flashInsertionPoint || isARange()) {
                _ignoreNextFlash = false;
                return;
            }

            _lastFlashTime = _blinkTimer.timeStamp();

            _insertionPointShowing = !_insertionPointShowing;

            _owner.drawInsertionPoint();
        } else if (_blinkTimer != null) {
            /* stop flashing */
            _blinkTimer.stop();
        }
    }

    boolean isARange() {
        return (_insertionPointInfo == null && _anchorPositionInfo != null);
    }

    void disableInsertionPoint() {
        if( _insertionPointDisabled == 0 )
            hideInsertionPoint();
        _insertionPointDisabled++;
    }

    void enableInsertionPoint() {
        _insertionPointDisabled--;
        if( _insertionPointDisabled == 0 )
            showInsertionPoint();
    }
    void showInsertionPoint() {
        if( _insertionPointDisabled != 0 )
            return;

        if(!_owner.isEditable())
            return;

        if( ! _owner.isEditing())
            return;

        _flashInsertionPoint = true;

        if (isARange() || _insertionPointShowing) {
            return;
        }

        _insertionPointShowing = true;

        _owner.drawInsertionPoint();

        _startFlashing();
    }

    void hideInsertionPoint() {
        _flashInsertionPoint = false;

        if (isARange() || !_insertionPointShowing) {
            return;
        }

        _insertionPointShowing = false;

        _owner.drawInsertionPoint();
    }

    Rect insertionPointRect() {
        if (_insertionPointInfo == null) {
            return TextView.newRect();
        }

        return TextView.newRect(_insertionPointInfo._x - 1,
                                _insertionPointInfo._y, 1,
                                _insertionPointInfo._lineHeight);
    }

    Rect _updateRectForInfo(int oldPoint, int newPoint,
                            TextPositionInfo oldInfo,
                            TextPositionInfo newInfo) {
        Rect    tmpRect, updateRect;

      /* did the selection point change? */
        if (oldPoint == newPoint) {
            return null;
        }

      /* if on the same line, just redraw the update rect */
        if (oldInfo._y == newInfo._y) {
            if (newInfo._x < oldInfo._x) {
                updateRect = TextView.newRect(newInfo._x, newInfo._y,
                                              oldInfo._x - newInfo._x,
                                              newInfo._lineHeight);
            } else {
                updateRect = TextView.newRect(oldInfo._x, newInfo._y,
                                             newInfo._x - oldInfo._x,
                                             newInfo._lineHeight);
            }
        } else {
            updateRect = oldInfo.lineBounds();
            tmpRect = newInfo.lineBounds();
            updateRect.unionWith(tmpRect);
            TextView.returnRect(tmpRect);
        }

        return updateRect;
    }



     void setRange(int selectionBegin, int selectionEnd,
                         TextPositionInfo selectionEndInfo,
                         boolean selectLineBreak,boolean fromBottom) {
        TextPositionInfo        oldStartInfo, oldEndInfo, newStartInfo,
                                newEndInfo;
        Rect                    updateRect, anchorRect, endRect, startRect,
                                tmpRect;
        int                     oldStart, oldEnd, newStart, newEnd;

        if( selectionBegin == -1 ||
            selectionEnd   == -1 )
            _selectionDefined = false;
        else
            _selectionDefined = true;

        if (_anchorPosition != _endPosition) {
            oldStart = (_anchorPosition < _endPosition ?
                                _anchorPosition : _endPosition);
            oldStartInfo = (_anchorPosition < _endPosition ?
                                _anchorPositionInfo : _endPositionInfo);
            oldEnd = (_anchorPosition > _endPosition ?
                                _anchorPosition : _endPosition);
            oldEndInfo = (_anchorPosition > _endPosition ?
                                _anchorPositionInfo : _endPositionInfo);
        } else {
            oldStart = oldEnd = -1;
            oldStartInfo = oldEndInfo = null;
        }

      /* make sure range is valid */
        if (selectionBegin < 0) {
            selectionBegin = 0;
        } else if (selectionBegin >= _owner._charCount) {
            selectionBegin = _owner._charCount - 1;
        }
        if (selectionEnd < 0) {
            selectionEnd = 0;
        } else if (selectionEnd >= _owner._charCount) {
            selectionEnd = _owner._charCount - 1;
        }

        _anchorPosition = selectionBegin;
        _endPosition = selectionEnd;

        if (_anchorPosition == _endPosition) {
            _editParagraph = _owner._paragraphForIndex(_anchorPosition);
            _insertionPointInfo = _editParagraph.infoForPosition(
                                                        _anchorPosition, -1);
            if( fromBottom && _insertionPointInfo._endOfLine && _anchorPosition < (_owner.length()-1)) {
                TextParagraph nextParagraph = _owner._paragraphForIndex(_anchorPosition+1);
                if( nextParagraph == _editParagraph ) {
                    TextPositionInfo nextPositionInfo =
                        _editParagraph.infoForPosition(_anchorPosition + 1,-1);
                    if( nextPositionInfo._y > _insertionPointInfo._y )
                        _insertionPointInfo = _editParagraph.infoForPosition(_anchorPosition,
                                                        nextPositionInfo._y);
                }
            }
            _anchorPositionInfo = _endPositionInfo = null;
        } else {
            TextParagraph p;
            p = _owner._paragraphForIndex(_anchorPosition);
            _anchorPositionInfo = p.infoForPosition(_anchorPosition, -1);
            if( _anchorPositionInfo._endOfLine && !selectLineBreak ) {
                _anchorPositionInfo = p.infoForPosition(_anchorPosition, _anchorPositionInfo.maxY());
            }
            if (selectionEndInfo == null) {
                _endPositionInfo = _owner._paragraphForIndex(
                                        _endPosition).infoForPosition(
                                                        _endPosition, -1);
            } else {
                _endPositionInfo = selectionEndInfo;
            }

            _insertionPointInfo = null;
            _editParagraph = null;
        }

        if (_anchorPosition != _endPosition) {
            newStart = (_anchorPosition < _endPosition ?
                                _anchorPosition : _endPosition);
            newStartInfo = (_anchorPosition < _endPosition ?
                                _anchorPositionInfo : _endPositionInfo);
            newEnd = (_anchorPosition > _endPosition ?
                                _anchorPosition : _endPosition);
            newEndInfo = (_anchorPosition > _endPosition ?
                                _anchorPositionInfo : _endPositionInfo);
        } else {
            newStart = newEnd = -1;
            newStartInfo = newEndInfo = null;
        }

      /* old selection was just an insertionPoint */
        if (oldStart == -1) {
          /* if still just an insertionPoint, we're done */
            if (newStart == -1) {
                _startFlashing();
                _updateCurrentFont();
                return;
            }

          /* redraw from new selection start to new selection end */
            dirtyRangeForSelection(_anchorPositionInfo,_endPositionInfo,null,null);
            _updateCurrentFont();
            return;
        } else if (newStart == -1) {
          /*
           * new selection is just an insertionPoint, so redraw from old
           * selection start to old selection end
           */
            dirtyRangeForSelection(oldStartInfo,oldEndInfo,null,null);

            _startFlashing();
            _updateCurrentFont();
            return;
        } else { /* Both ranges */
            dirtyRangeForSelection(newStartInfo,newEndInfo,oldStartInfo,oldEndInfo);
        }

        _updateCurrentFont();
     }

     void dirtyRangeForSelection(TextPositionInfo start,TextPositionInfo end,
                                 TextPositionInfo previousStart,TextPositionInfo previousEnd) {
        Range isr,nsr,dirtyRange;
        Vector dirtyRects;
        Rect rect,result = null;
        int i,c;
        Rect bounds = _owner.bounds();

        nsr = Range.rangeFromIndices(start._absPosition,end._absPosition);
        if( previousStart == null || previousEnd == null ) {
            dirtyRange = nsr;
        } else {
            isr = Range.rangeFromIndices(previousStart._absPosition,previousEnd._absPosition);
            if( isr.equals(nsr))
                return;

            if(nsr.index == isr.index ) {
                if( nsr.length > isr.length )
                    dirtyRange = new Range(nsr.index + isr.length,
                                           nsr.length - isr.length);
                else
                    dirtyRange = new Range(nsr.index + nsr.length,
                                           isr.length - nsr.length);
            } else if( (nsr.index + nsr.length) == (isr.index + isr.length)) {
                if( nsr.length > isr.length )
                    dirtyRange = new Range( nsr.index,nsr.length - isr.length);
                else
                    dirtyRange = new Range( isr.index,isr.length - nsr.length);
            } else {
                /* We are not dragging the selection. We change it.
                 * it is faster to refresh two ranges than the union
                 * of two
                 */
                dirtyRangeForSelection(start,end,null,null);
                dirtyRangeForSelection(previousStart,previousEnd,null,null);
                return;
            }
        }


        dirtyRects  = _owner.rectsForRange( dirtyRange );

        for(i=0,c=dirtyRects.count() ; i < c ; i++ ) {
            rect = (Rect) dirtyRects.elementAt(i);

            rect.x = 0;
            rect.width = bounds.width;

            if( rect.height > 0 ) {
                if( result == null )
                    result = new Rect( rect );
                else
                    result.unionWith( rect );
            }
        }

        if( result == null ) {
             return;
        }

         if(dirtyRange.contains(start._absPosition) &&
            !result.contains(start._x,start._y) && start._absPosition > 0) {
             TextPositionInfo previousLineInfo = _owner.positionInfoForIndex(
                                                     start._absPosition-1);
             if( previousLineInfo != null ) {
                 result.y -= previousLineInfo._lineHeight;
                 result.height += previousLineInfo._lineHeight;
             }
         }

         if(dirtyRange.contains(end._absPosition) &&
            !result.contains(end._x,end._y) && end._absPosition > 0) {
             TextPositionInfo previousLineInfo = _owner.positionInfoForIndex(
                                                     end._absPosition-1);
             if( previousLineInfo != null ) {
                 result.y -= previousLineInfo._lineHeight;
                 result.height += previousLineInfo._lineHeight;
             }
         }


         if(previousStart != null && dirtyRange.contains(previousStart._absPosition) &&
            !result.contains(previousStart._x,previousStart._y) &&
            previousStart._absPosition > 0) {
             TextPositionInfo previousLineInfo = _owner.positionInfoForIndex(
                                                     previousStart._absPosition-1);
             if( previousLineInfo != null ) {
                 result.y -= previousLineInfo._lineHeight;
                 result.height += previousLineInfo._lineHeight;
             }
         }

         if(previousEnd != null && dirtyRange.contains(previousEnd._absPosition) &&
            !result.contains(previousEnd._x,previousEnd._y) && previousEnd._absPosition > 0) {
             TextPositionInfo previousLineInfo = _owner.positionInfoForIndex(
                                                     previousEnd._absPosition-1);
             if( previousLineInfo != null ) {
                 result.y -= previousLineInfo._lineHeight;
                 result.height += previousLineInfo._lineHeight;
             }
         }
         //System.out.println("Updating range " + dirtyRange + " rect " + result);
         _owner.addDirtyRect( result );
     }

     void setRange(int selectionBegin, int selectionEnd,
                         TextPositionInfo selectionEndInfo,
                         boolean selectLineBreak) {
         setRange(selectionBegin,selectionEnd,selectionEndInfo,selectLineBreak,false);
     }


     void setRange(int selectionBegin, int selectionEnd) {
        setRange(selectionBegin, selectionEnd, null, false,false);
    }

     void setRange(int selectionBegin, int selectionEnd, boolean fromBottom) {
         setRange(selectionBegin,selectionEnd,null,false,fromBottom);
     }

     void clearRange() {
        hideInsertionPoint();
        setRange(-1,-1);
        _stopFlashing();
    }

    void setInsertionPoint(TextPositionInfo positionInfo) {
        Rect    updateRect = null, endRect;

        if (positionInfo == null) {
            return;
        }

        _selectionDefined = true;
        if (_anchorPosition != _endPosition) {
            updateRect = _anchorPositionInfo.lineBounds();
            endRect = _endPositionInfo.lineBounds();

            updateRect.unionWith(endRect);
            updateRect.x = 0;
            updateRect.width = _owner.bounds.width;
            TextView.returnRect(endRect);
        }

      /* make the changes */
        _anchorPosition = _endPosition = positionInfo._absPosition;
        _insertionPointInfo = positionInfo;
        _editParagraph = _insertionPointInfo._textRun._paragraph;

        _anchorPositionInfo = _endPositionInfo = null;

      /* update */
        if (updateRect != null) {
            _owner.draw(updateRect);
            TextView.returnRect(updateRect);
        }

        if (_insertionPointShowing) {
            _startFlashing();
        }
    }

    int insertionPoint() {
        if (isARange()) {
            return -1;
        }

        return _anchorPosition;
    }

    TextPositionInfo insertionPointInfo() {
        if (isARange()) {
            return null;
        }

        return _insertionPointInfo;
    }

    int selectionStart() {
        if( !_selectionDefined )
            return -1;

        if (_anchorPosition <= _endPosition) {
            return _anchorPosition;
        }
        return _endPosition;
    }

    TextPositionInfo selectionStartInfo() {
        if (!isARange()) {
            return _insertionPointInfo;
        }
        if (_anchorPosition <= _endPosition) {
            return _anchorPositionInfo;
        }

        return _endPositionInfo;
    }

    int selectionEnd() {
        if( !_selectionDefined )
            return -1;

        if (_endPosition > _anchorPosition) {
            return _endPosition;
        }

        return _anchorPosition;
    }

    TextPositionInfo selectionEndInfo() {
        if (!isARange()) {
            return _insertionPointInfo;
        }
        if (_endPosition > _anchorPosition) {
            return _endPositionInfo;
        }

        return _anchorPositionInfo;
    }

    void _updateCurrentFont() {
    }




    int orderedSelectionStart() {
        if( !_selectionDefined )
            return -1;

        return _anchorPosition;
    }

    TextPositionInfo orderedSelectionStartInfo() {
        if (!isARange()) {
            return _insertionPointInfo;
        }
        return _anchorPositionInfo;
    }

    int orderedSelectionEnd() {
        if( !_selectionDefined )
            return -1;

        return _endPosition;
    }

    TextPositionInfo orderedSelectionEndInfo() {
       if (!isARange()) {
           return _insertionPointInfo;
       }

       return _endPositionInfo;
   }
}










