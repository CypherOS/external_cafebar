package com.danimahardhika.cafebar;

/*
 * CafeBar
 *
 * Copyright (c) 2017 Dani Mahardhika
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed getTo in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatDrawableManager;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Locale;

class CafeBarUtil {

    static void adjustCustomView(@NonNull CafeBar.Builder builder, @NonNull View view) {
        ViewGroup viewGroup;
        try {
            viewGroup = (ViewGroup) view;
        } catch (ClassCastException e) {
            LogUtil.d(Log.getStackTraceString(e));
            return;
        }

        viewGroup.setClickable(true);

        if (!builder.isAdjustCustomView()) {
            LogUtil.d("isAdjustCustomView = false, leave custom view as it is");
            return;
        }

        LogUtil.d("CafeBar has getCustomView adjusting padding, setup content, button etc ignored");

        int left = view.getPaddingLeft();
        int top = view.getPaddingTop();
        int right = view.getPaddingRight();
        int bottom = view.getPaddingBottom();

        boolean tabletMode = builder.getContext().getResources().getBoolean(R.bool.cafebar_tablet_mode);

        if (builder.isFitSystemWindow() && !builder.isFloating()) {
            Configuration configuration = builder.getContext().getResources().getConfiguration();
            int navBar = getNavigationBarHeight(builder.getContext());

            if (tabletMode || configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                viewGroup.setPadding(left, top, right, (bottom + navBar));
            } else {
                viewGroup.setPadding(left, top, (right + navBar), bottom);
            }
        }

        int index = -1;
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            if (viewGroup.getChildAt(i) instanceof TextView) {
                index = i;
                LogUtil.d("CafeBar with getCustomView found textview at index " +i);
                LogUtil.d("CafeBar always consider fist textview found as content");
                break;
            }
        }

        if (index < 0) return;

        TextView content = (TextView) viewGroup.getChildAt(index);

        if (tabletMode || builder.isFloating()) {
            ViewGroup.LayoutParams params = content.getLayoutParams();
            params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;

            content.setLayoutParams(params);

            content.setMinWidth(builder.getContext().getResources()
                    .getDimensionPixelSize(R.dimen.cafebar_floating_min_width));
            content.setMaxWidth(builder.getContext().getResources()
                    .getDimensionPixelSize(R.dimen.cafebar_floating_max_width));
        }
    }

    @NonNull
    static View getBaseCafeBarView(@NonNull CafeBar.Builder builder) {
        int color = builder.getTheme().getColor();
        int titleColor = builder.getTheme().getTitleColor();

        CafeBarTheme.Custom customTheme = builder.getTheme();
        if (customTheme != null) {
            color = customTheme.getColor();
            titleColor = customTheme.getTitleColor();
        }

        //Creating LinearLayout as rootView
        LinearLayout root = new LinearLayout(builder.getContext());
        root.setId(R.id.cafebar_root);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_VERTICAL);
        root.setBackgroundColor(color);
        root.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        root.setClickable(true);

        //Creating another LinearLayout for getContent container
        LinearLayout contentBase = new LinearLayout(builder.getContext());
        contentBase.setId(R.id.cafebar_content_base);
        contentBase.setOrientation(LinearLayout.HORIZONTAL);
        contentBase.setGravity(Gravity.CENTER_VERTICAL);
        contentBase.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        Drawable drawable = null;
        if (builder.getIcon() != null) {
            drawable = getResizedDrawable(
                    builder.getContext(),
                    builder.getIcon(),
                    titleColor,
                    builder.isTintIcon());
        }

        //Creating TextView for getContent as childView
        TextView content = new TextView(builder.getContext());
        content.setId(R.id.cafebar_content);
        content.setMaxLines(builder.getMaxLines());
        content.setEllipsize(TextUtils.TruncateAt.END);
        content.setTextColor(titleColor);
        content.setTextSize(TypedValue.COMPLEX_UNIT_PX, builder.getContext().getResources()
                .getDimension(R.dimen.cafebar_content_text));

        if (builder.getContentTypeface() != null) {
            content.setTypeface(builder.getContentTypeface());
        }

        if (builder.getSpannableStringBuilder() != null) {
            content.setText(builder.getSpannableStringBuilder(), TextView.BufferType.SPANNABLE);
        } else {
            content.setText(builder.getContent());
        }

        content.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        content.setGravity(Gravity.CENTER_VERTICAL);

        boolean tabletMode = builder.getContext().getResources().getBoolean(R.bool.cafebar_tablet_mode);
        if (tabletMode || builder.isFloating()) {
            content.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            content.setMinWidth(builder.getContext().getResources()
                    .getDimensionPixelSize(R.dimen.cafebar_floating_min_width));
            content.setMaxWidth(builder.getContext().getResources()
                    .getDimensionPixelSize(R.dimen.cafebar_floating_max_width));
        }

        int side = builder.getContext().getResources().getDimensionPixelSize(R.dimen.cafebar_content_padding_side);
        int top = builder.getContext().getResources().getDimensionPixelSize(R.dimen.cafebar_content_padding_top);

        if (drawable != null) {
            content.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
            content.setCompoundDrawablePadding(top);
        }

        boolean multiLines = isContentMultiLines(builder);
        boolean containsPositive = builder.getPositiveText() != null;
        boolean containsNegative = builder.getNegativeText() != null;
        boolean longNeutralAction = isLongAction(builder.getNeutralText());

        if (multiLines || containsPositive || containsNegative || longNeutralAction) {
            top = side;
            builder.longContent(true);
        }

        root.setPadding(side, top, side, top);

        if (builder.getPositiveText() == null && builder.getNegativeText() == null) {
            if (builder.isFitSystemWindow() && !builder.isFloating()) {
                Configuration configuration = builder.getContext().getResources().getConfiguration();
                int navBar = getNavigationBarHeight(builder.getContext());

                if (tabletMode || configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                    root.setPadding(side, top, side, (top + navBar));
                } else {
                    root.setPadding(side, top, (side + navBar), top);
                }
            }

            //Adding getContent getTo container
            contentBase.addView(content);

            //Adding childView getTo rootView
            root.addView(contentBase);

            //Returning rootView
            return root;
        }

        //Creating another linear layout for button container
        LinearLayout buttonBase = new LinearLayout(builder.getContext());
        buttonBase.setId(R.id.cafebar_button_base);
        buttonBase.setOrientation(LinearLayout.HORIZONTAL);
        buttonBase.setGravity(Gravity.END);
        buttonBase.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        //Adding button

        String neutralText = builder.getNeutralText();
        if (neutralText != null) {
            TextView neutral = getActionView(builder, neutralText, builder.getNeutralColor());
            neutral.setId(R.id.cafebar_button_neutral);

            if (builder.getPositiveTypeface() != null) {
                neutral.setTypeface(builder.getNeutralTypeface());
            }

            buttonBase.addView(neutral);
        }

        String negativeText = builder.getNegativeText();
        if (negativeText != null) {
            TextView negative = getActionView(builder, negativeText, builder.getNegativeColor());
            negative.setId(R.id.cafebar_button_negative);

            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) negative.getLayoutParams();
            params.setMargins(
                    params.leftMargin + builder.getContext().getResources().getDimensionPixelSize(R.dimen.cafebar_button_margin),
                    params.topMargin,
                    params.rightMargin,
                    params.bottomMargin);

            if (builder.getPositiveTypeface() != null) {
                negative.setTypeface(builder.getNegativeTypeface());
            }

            buttonBase.addView(negative);
        }

        String positiveText = builder.getPositiveText();
        if (positiveText != null) {
            int positiveColor = CafeBarUtil.getAccentColor(builder.getContext(), builder.getPositiveColor());
            TextView positive = getActionView(builder, positiveText, positiveColor);
            positive.setId(R.id.cafebar_button_positive);

            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) positive.getLayoutParams();
            params.setMargins(
                    params.leftMargin + builder.getContext().getResources().getDimensionPixelSize(R.dimen.cafebar_button_margin),
                    params.topMargin,
                    params.rightMargin,
                    params.bottomMargin);

            if (builder.getPositiveTypeface() != null) {
                positive.setTypeface(builder.getPositiveTypeface());
            }

            buttonBase.addView(positive);
        }

        //Adjust padding
        int buttonPadding = builder.getContext().getResources().getDimensionPixelSize(
                R.dimen.cafebar_button_padding);
        root.setPadding(side, top, (side - buttonPadding), (top - buttonPadding));

        if (builder.isFitSystemWindow() && !builder.isFloating()) {
            Configuration configuration = builder.getContext().getResources().getConfiguration();
            int navBar = getNavigationBarHeight(builder.getContext());

            if (tabletMode || configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                root.setPadding(side, top, (side - buttonPadding), (top - buttonPadding + navBar));
            } else {
                root.setPadding(side, top, (side - buttonPadding + navBar), top);
            }
        }

        //Adding getContent getTo container
        content.setPadding(0, 0, buttonPadding, 0);
        contentBase.addView(content);

        //Adding childView getTo rootView
        root.addView(contentBase);

        //Adding button container getTo root
        root.addView(buttonBase);

        //Returning rootView
        return root;
    }

    @Nullable
    static Snackbar getBaseSnackBar(@NonNull View cafeBarLayout,
                                    @NonNull CafeBar.Builder builder) {
        View view = builder.getTo();
        if (view == null) {
            view = ((Activity) builder.getContext()).getWindow().getDecorView().findViewById(android.R.id.content);
        }

        Snackbar snackBar = Snackbar.make(view, "", builder.isAutoDismiss() ?
                builder.getDuration() : Snackbar.LENGTH_INDEFINITE);
        Snackbar.SnackbarLayout snackBarLayout = (Snackbar.SnackbarLayout) snackBar.getView();
        snackBarLayout.setPadding(0, 0, 0, 0);
        snackBarLayout.setBackgroundColor(Color.TRANSPARENT);
        snackBarLayout.setClickable(false);

        try {
            if (snackBarLayout.getLayoutParams() instanceof CoordinatorLayout.LayoutParams) {
                CoordinatorLayout.LayoutParams snackBarParams = (CoordinatorLayout.LayoutParams)
                        snackBarLayout.getLayoutParams();
                snackBarParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
                snackBarLayout.setLayoutParams(snackBarParams);
            } else {
                Snackbar.SnackbarLayout.LayoutParams snackBarParams = (Snackbar.SnackbarLayout.LayoutParams)
                        snackBarLayout.getLayoutParams();
                snackBarParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
                snackBarLayout.setLayoutParams(snackBarParams);
            }
        } catch (ClassCastException e) {
            LogUtil.e(Log.getStackTraceString(e));
            return null;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            snackBarLayout.setElevation(0);
        }

        TextView textView = (TextView) snackBarLayout.findViewById(
                android.support.design.R.id.snackbar_text);
        if (textView != null) textView.setVisibility(View.INVISIBLE);

        boolean tabletMode = builder.getContext().getResources().getBoolean(R.bool.cafebar_tablet_mode);
        if (tabletMode || builder.isFloating()) {
            int shadow = builder.getContext().getResources().getDimensionPixelSize(R.dimen.cafebar_shadow_around);
            int padding = builder.getContext().getResources().getDimensionPixelSize(R.dimen.cafebar_floating_padding);

            CardView cardView = (CardView) View.inflate(builder.getContext(), R.layout.cafebar_floating_base, null);
            Snackbar.SnackbarLayout.LayoutParams params = new Snackbar.SnackbarLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.gravity = builder.getGravity().getGravity();

            int bottom = builder.isFloating() ? padding : 0;
            snackBarLayout.setClipToPadding(false);
            snackBarLayout.setPadding(padding, shadow, padding, bottom);

            if (builder.isFitSystemWindow() && builder.isFloating()) {
                Configuration configuration = builder.getContext().getResources().getConfiguration();
                int navBar = getNavigationBarHeight(builder.getContext());

                if (tabletMode || configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                    snackBarLayout.setPadding(padding, shadow, padding, (bottom + navBar));
                } else {
                    snackBarLayout.setPadding(0, 0, navBar, 0);
                }
            }

            cardView.setLayoutParams(params);
            cardView.setClickable(true);

            if (builder.isShowShadow()) {
                cardView.setCardElevation(builder.getContext().getResources().getDimension(R.dimen.cafebar_shadow_around));
            }

            cardView.addView(cafeBarLayout);
            snackBarLayout.addView(cardView, 0);
            return snackBar;
        }

        LinearLayout root = new LinearLayout(builder.getContext());
        root.setOrientation(LinearLayout.VERTICAL);
        root.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        if (builder.isShowShadow()) {
            View shadow = new View(builder.getContext());
            shadow.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    builder.getContext().getResources().getDimensionPixelSize(
                            R.dimen.cafebar_shadow_top)));
            shadow.setBackgroundResource(R.drawable.cafebar_shadow_top);
            root.addView(shadow);
        }

        root.addView(cafeBarLayout);
        snackBarLayout.addView(root, 0);
        return snackBar;
    }

    @NonNull
    static TextView getActionView(@NonNull CafeBar.Builder builder, @NonNull String action, int color) {
        boolean longAction = isLongAction(action);
        int res = R.layout.cafebar_action_button_dark;

        CafeBarTheme.Custom customTheme = builder.getTheme();
        int titleColor = customTheme.getTitleColor();
        boolean dark = titleColor != Color.WHITE;
        if (dark) {
            res = R.layout.cafebar_action_button;
        }

        int padding = builder.getContext().getResources().getDimensionPixelSize(
                R.dimen.cafebar_button_padding);

        TextView button = (TextView) View.inflate(builder.getContext(), res, null);
        button.setText(action.toUpperCase(Locale.getDefault()));
        button.setMaxLines(1);
        button.setEllipsize(TextUtils.TruncateAt.END);
        button.setTextColor(color);
        button.setPadding(padding, padding, padding, padding);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        int side = builder.getContext().getResources().getDimensionPixelSize(
                R.dimen.cafebar_content_padding_side);
        int margin = builder.getContext().getResources().getDimensionPixelSize(
                R.dimen.cafebar_button_margin_start);
        params.setMargins(margin, 0, 0, 0);

        if (longAction) {
            params.setMargins(0, (side - padding), 0, 0);
        }

        if (builder.getPositiveText() != null || builder.getNegativeText() != null) {
            longAction = true;
            params.setMargins(0, (side - padding), 0, 0);
        } else {
            params.gravity = Gravity.CENTER_VERTICAL | Gravity.END;
        }

        button.setLayoutParams(params);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            button.setBackgroundResource(dark ? R.drawable.cafebar_action_button_selector_dark :
                    R.drawable.cafebar_action_button_selector);
            return button;
        }

        TypedValue outValue = new TypedValue();
        builder.getContext().getTheme().resolveAttribute(longAction ?
                        R.attr.selectableItemBackground : R.attr.selectableItemBackgroundBorderless,
                outValue, true);
        button.setBackgroundResource(outValue.resourceId);
        return button;
    }

    static boolean isContentMultiLines(@NonNull CafeBar.Builder builder) {
        DisplayMetrics metrics = new DisplayMetrics();
        ((Activity) builder.getContext()).getWindowManager().getDefaultDisplay().getMetrics(metrics);

        boolean tabletMode = builder.getContext().getResources().getBoolean(R.bool.cafebar_tablet_mode);
        int padding = (builder.getContext().getResources().getDimensionPixelSize(R.dimen.cafebar_content_padding_side) * 2);

        if (builder.getNeutralText() != null && builder.getNegativeText() == null && builder.getPositiveText() == null &&
                !isLongAction(builder.getNeutralText())) {
            padding += builder.getContext().getResources().getDimensionPixelSize(R.dimen.cafebar_button_margin_start);

            int actionPadding = builder.getContext().getResources().getDimensionPixelSize(R.dimen.cafebar_button_padding);
            TextView action = new TextView(builder.getContext());
            action.setTextSize(TypedValue.COMPLEX_UNIT_PX, builder.getContext().getResources()
                    .getDimension(R.dimen.cafebar_content_text));
            if (builder.getNeutralTypeface() != null) {
                action.setTypeface(builder.getContentTypeface());
            }
            action.setPadding(actionPadding, 0, actionPadding, 0);
            action.setText(builder.getNeutralText().substring(0,
                    builder.getNeutralText().length() > 10 ? 10 : builder.getNeutralText().length()));

            int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(metrics.widthPixels, View.MeasureSpec.AT_MOST);
            int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
            action.measure(widthMeasureSpec, heightMeasureSpec);

            LogUtil.d("measured action width: " +action.getMeasuredWidth());
            padding += action.getMeasuredWidth();
        }

        if (builder.getIcon() != null) {
            int icon = builder.getContext().getResources().getDimensionPixelSize(R.dimen.cafebar_icon_size);
            icon += builder.getContext().getResources().getDimensionPixelSize(R.dimen.cafebar_content_padding_top);
            padding += icon;
        }

        if (builder.isFloating() || tabletMode) {
            padding += builder.getContext().getResources().getDimensionPixelSize(R.dimen.cafebar_floating_padding);
        }

        TextView textView = new TextView(builder.getContext());
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, builder.getContext().getResources()
                .getDimension(R.dimen.cafebar_content_text));
        textView.setPadding(padding, 0, 0, 0);
        if (builder.getContentTypeface() != null) {
            textView.setTypeface(builder.getContentTypeface());
        }

        if (builder.getSpannableStringBuilder() != null) {
            textView.setText(builder.getSpannableStringBuilder(), TextView.BufferType.SPANNABLE);
        } else {
            textView.setText(builder.getContent());
        }

        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(metrics.widthPixels, View.MeasureSpec.AT_MOST);
        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        textView.measure(widthMeasureSpec, heightMeasureSpec);

        LogUtil.d("line count: " +textView.getLineCount());
        return textView.getLineCount() > 1;
    }

    @Nullable
    static Drawable getDrawable(@NonNull Context context, @DrawableRes int res) {
        try {
            Drawable drawable = AppCompatDrawableManager.get().getDrawable(context, res);
            return drawable.mutate();
        } catch (OutOfMemoryError e) {
            return null;
        }
    }

    @Nullable
    static Drawable toDrawable(@NonNull Context context, @Nullable Bitmap bitmap) {
        try {
            if (bitmap == null) return null;
            return new BitmapDrawable(context.getResources(), bitmap);
        } catch (OutOfMemoryError e) {
            return null;
        }
    }

    @Nullable
    private static Drawable getResizedDrawable(@NonNull Context context, @Nullable Drawable drawable,
                                               int color, boolean tint) {
        try {
            if (drawable == null) {
                LogUtil.d("drawable: null");
                return null;
            }

            if (tint) {
                drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);
                drawable.mutate();
            }

            int size = context.getResources().getDimensionPixelSize(R.dimen.cafebar_icon_size);
            Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);

            return new BitmapDrawable(context.getResources(),
                    Bitmap.createScaledBitmap(bitmap, size, size, true));
        } catch (Exception | OutOfMemoryError e) {
            LogUtil.e(Log.getStackTraceString(e));
            return null;
        }
    }

    static int getNavigationBarHeight(@NonNull Context context) {
        Point appUsableSize = getAppUsableScreenSize(context);
        Point realScreenSize = getRealScreenSize(context);

        if (appUsableSize.x < realScreenSize.x) {
            Point point = new Point(realScreenSize.x - appUsableSize.x, appUsableSize.y);
            LogUtil.d("Navigation Bar height position: Right");
            LogUtil.d("Navigation Bar height: " +point.x);
            return point.x;
        }

        if (appUsableSize.y < realScreenSize.y) {
            Point point = new Point(appUsableSize.x, realScreenSize.y - appUsableSize.y);
            LogUtil.d("Navigation Bar height position: Bottom");
            LogUtil.d("Navigation Bar height: " +point.y);
            return point.y;
        }

        LogUtil.d("Navigation Bar height: 0");
        return 0;
    }

    private static Point getAppUsableScreenSize(@NonNull Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size;
    }

    private static Point getRealScreenSize(@NonNull Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();

        if (Build.VERSION.SDK_INT >= 17) {
            display.getRealSize(size);
        } else if (Build.VERSION.SDK_INT >= 14) {
            try {
                size.x = (Integer)     Display.class.getMethod("getRawWidth").invoke(display);
                size.y = (Integer) Display.class.getMethod("getRawHeight").invoke(display);
            } catch (Exception e) {
                LogUtil.e(Log.getStackTraceString(e));
            }
        }
        return size;
    }

    static boolean isLongAction(@Nullable String action) {
        return action != null && action.length() > 10;
    }

    static int getAccentColor(Context context, int defaultColor) {
        if (context == null) {
            LogUtil.e("getAccentColor() context is null");
            return defaultColor;
        }

        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(R.attr.colorAccent, typedValue, true);
        return typedValue.data;
    }

    static int getTitleTextColor(@ColorInt int color) {
        double darkness = 1-(0.299*Color.red(color) + 0.587*Color.green(color) + 0.114*Color.blue(color))/255;
        return (darkness < 0.35) ? getDarkerColor(color) : Color.WHITE;
    }

    static int getSubTitleTextColor(@ColorInt int color) {
        int titleColor = getTitleTextColor(color);
        int alpha2 = Math.round(Color.alpha(titleColor) * 0.7f);
        int red = Color.red(titleColor);
        int green = Color.green(titleColor);
        int blue = Color.blue(titleColor);
        return Color.argb(alpha2, red, green, blue);
    }

    private static int getDarkerColor(@ColorInt int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.25f;
        return Color.HSVToColor(hsv);
    }

    static int getColor(@NonNull Context context, int color) {
        try {
            return ContextCompat.getColor(context, color);
        } catch (Exception e) {
            LogUtil.e(Log.getStackTraceString(e));
            return color;
        }
    }

    @Nullable
    static Typeface getTypeface(@NonNull Context context, String fontName) {
        try {
            return Typeface.createFromAsset(context.getAssets(), "fonts/" +fontName);
        } catch (Exception e) {
            LogUtil.e(Log.getStackTraceString(e));
        }
        return null;
    }
}
