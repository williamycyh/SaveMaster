package com.savemaster.savefromfb.player.event;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.savemaster.savefromfb.R;

public class CustomBottomSheetBehavior extends BottomSheetBehavior<FrameLayout> {

    public CustomBottomSheetBehavior(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    Rect globalRect = new Rect();
    private boolean skippingInterception = false;
    private final List<Integer> skipInterceptionOfElements = Arrays.asList(
			R.id.detail_content_root_layout, R.id.detail_related_streams_view,
			R.id.playQueuePanel, R.id.bottomControls);

    @Override
    public boolean onInterceptTouchEvent(@NonNull final CoordinatorLayout parent, @NonNull final FrameLayout child, final MotionEvent event) {
        // Drop following when action ends
        if (event.getAction() == MotionEvent.ACTION_CANCEL || event.getAction() == MotionEvent.ACTION_UP) {
            skippingInterception = false;
        }

        // Found that user still swiping, continue following
        if (skippingInterception || getState() == BottomSheetBehavior.STATE_SETTLING) {
            return false;
        }

        // Don't need to do anything if bottomSheet isn't expanded
        if (getState() == BottomSheetBehavior.STATE_EXPANDED && event.getAction() == MotionEvent.ACTION_DOWN) {
            // Without overriding scrolling will not work when user touches these elements
            for (final Integer element : skipInterceptionOfElements) {
                final View view = child.findViewById(element);
                if (view != null) {
                    final boolean visible = view.getGlobalVisibleRect(globalRect);
                    if (visible && globalRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                        // Makes bottom part of the player draggable in portrait when
                        // playbackControlRoot is hidden
                        if (element == R.id.bottomControls && child.findViewById(R.id.playbackControlRoot).getVisibility() != View.VISIBLE) {
                            return super.onInterceptTouchEvent(parent, child, event);
                        }
                        skippingInterception = true;
                        return false;
                    }
                }
            }
        }

        return super.onInterceptTouchEvent(parent, child, event);
    }
}
