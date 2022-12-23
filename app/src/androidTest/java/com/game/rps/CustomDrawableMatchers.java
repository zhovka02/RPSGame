package com.game.rps;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.VectorDrawable;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class CustomDrawableMatchers {
    public static Matcher<View> withDrawableId(@DrawableRes final int id) {
        return new DrawableMatcher(id);
    }


    public static class DrawableMatcher extends TypeSafeMatcher<View> {

        private final int expectedId;

        public DrawableMatcher(@DrawableRes int expectedId) {
            super(View.class);
            this.expectedId = expectedId;
        }

        private static boolean sameBitmap(Context context, Drawable drawable, int expectedId) {
            Drawable expectedDrawable = ContextCompat.getDrawable(context, expectedId);
            if (drawable == null || expectedDrawable == null) {
                return false;
            }

            if (drawable instanceof StateListDrawable && expectedDrawable instanceof StateListDrawable) {
                drawable = drawable.getCurrent();
                expectedDrawable = expectedDrawable.getCurrent();
            }
            if (drawable instanceof BitmapDrawable) {
                Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
                Bitmap otherBitmap = ((BitmapDrawable) expectedDrawable).getBitmap();
                return bitmap.sameAs(otherBitmap);
            }

            if (drawable instanceof VectorDrawable ||
                    drawable instanceof VectorDrawableCompat ||
                    drawable instanceof GradientDrawable) {
                Rect drawableRect = drawable.getBounds();
                Bitmap bitmap = Bitmap.createBitmap(drawableRect.width(), drawableRect.height(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                drawable.draw(canvas);

                Bitmap otherBitmap = Bitmap.createBitmap(drawableRect.width(), drawableRect.height(), Bitmap.Config.ARGB_8888);
                Canvas otherCanvas = new Canvas(otherBitmap);
                expectedDrawable.setBounds(0, 0, otherCanvas.getWidth(), otherCanvas.getHeight());
                expectedDrawable.draw(otherCanvas);
                return bitmap.sameAs(otherBitmap);
            }
            return false;
        }

        @Override
        protected boolean matchesSafely(View target) {
            if (!(target instanceof ImageView)) {
                return false;
            }
            ImageView imageView = (ImageView) target;
            return sameBitmap(target.getContext(), imageView.getDrawable(), this.expectedId);
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("with drawable from resource id: ");
            description.appendValue(this.expectedId);
        }
    }
}
