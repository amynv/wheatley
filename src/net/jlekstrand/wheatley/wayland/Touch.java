package net.jlekstrand.wheatley.wayland;

import android.view.MotionEvent;

public class Touch
{
    private static final String LOG_TAG = "wheatley:wayland.Touch";

    private long _nativeHandle;

    private static native long createNative(long seatHandle);
    private static native void destroyNative(long nativeHandle);

    private static native void downOnOutputNative(long nativeHandle, int time,
            int id, long outputHandle, float x, float y);
    private static native void moveOnOutputNative(long nativeHandle,
            int id, long outputHandle, float x, float y);
    private static native void finishFrameNative(long nativeHandle, int time);
    private static native void upNative(long nativeHandle, int time, int id);
    private static native void cancelNative(long nativeHandle);

    Touch(Seat seat)
    {
        _nativeHandle = createNative(seat.getNativeHandle());
    }

    private void handleDown(MotionEvent event, Output output)
    {
        final int idx = event.getActionIndex();
        downOnOutputNative(_nativeHandle, (int)event.getEventTime(),
                event.getPointerId(idx), output.getNativeHandle(),
                event.getX(idx), event.getY(idx));
    }

    private void handleMove(MotionEvent event, Output output)
    {
        final int historySize = event.getHistorySize();
        final int pointerCount = event.getPointerCount();

        for (int h = 0; h < historySize; h++) {
            for (int p = 0; p < pointerCount; p++) {
                moveOnOutputNative(_nativeHandle,
                        event.getPointerId(p), output.getNativeHandle(),
                        event.getHistoricalX(p, h),
                        event.getHistoricalY(p, h));
            }
            finishFrameNative(_nativeHandle,
                    (int)event.getHistoricalEventTime(h));
        }

        for (int p = 0; p < pointerCount; p++) {
            moveOnOutputNative(_nativeHandle, event.getPointerId(p),
                    output.getNativeHandle(), event.getX(p), event.getY(p));
        }
        finishFrameNative(_nativeHandle, (int)event.getEventTime());
    }

    private void handleUp(MotionEvent event)
    {
        upNative(_nativeHandle, (int)event.getEventTime(),
                event.getPointerId(event.getActionIndex()));
    }

    private void handleCancel(MotionEvent event)
    {
        cancelNative(_nativeHandle);
    }

    public boolean onTouchEvent(MotionEvent event, Output output)
    {
        switch (event.getActionMasked()) {
        case MotionEvent.ACTION_DOWN:
        case MotionEvent.ACTION_POINTER_DOWN:
            handleDown(event, output);
            return true;
        case MotionEvent.ACTION_MOVE:
            handleMove(event, output);
            return true;
        case MotionEvent.ACTION_UP:
        case MotionEvent.ACTION_POINTER_UP:
            handleUp(event);
            return true;
        case MotionEvent.ACTION_CANCEL:
            handleCancel(event);
            return true;
        default:
            return false;
        }
    }

    @Override
    public void finalize() throws Throwable
    {
        if (_nativeHandle != 0)
            destroyNative(_nativeHandle);

        super.finalize();
    }
}

// vim: ts=4 sw=4 sts=4 expandtab
