
package com.amr.espoverlay;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private final int BG = Color.rgb(7, 10, 18);
    private final int PANEL = Color.rgb(17, 26, 42);
    private final int BLUE = Color.rgb(0, 170, 255);
    private final int WHITE = Color.WHITE;
    private final int GRAY = Color.rgb(165, 185, 205);
    private final int GREEN = Color.rgb(0, 235, 150);

    private WindowManager wm;
    private View bubbleView;
    private View menuView;
    private WindowManager.LayoutParams bubbleParams;

    private TextView mainStatus;
    private boolean running = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

        buildMainScreen();
    }

    private int dp(float value) {
        return (int) (value *
                getResources().getDisplayMetrics().density + 0.5f);
    }

    private GradientDrawable makeBackground(int color, int border) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(18));

        if (border != 0) {
            drawable.setStroke(dp(2), border);
        }

        return drawable;
    }

    private TextView makeLabel(
            String text, int size, int color) {

        TextView label = new TextView(this);
        label.setText(text);
        label.setTextSize(size);
        label.setTextColor(color);
        label.setGravity(Gravity.CENTER);
        label.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        label.setPadding(dp(10), dp(12), dp(10), dp(12));

        return label;
    }

    private Button makeButton(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextSize(15);
        button.setTextColor(WHITE);
        button.setAllCaps(false);
        button.setBackground(makeBackground(PANEL, BLUE));

        return button;
    }

    private LinearLayout.LayoutParams buttonParams() {
        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);

        params.setMargins(0, dp(6), 0, dp(6));
        return params;
    }

    private void buildMainScreen() {
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(BG);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(18), dp(24), dp(18), dp(24));

        scroll.addView(layout);

        layout.addView(
                makeLabel("ESP OVERLAY PRO", 27, BLUE));

        layout.addView(
                makeLabel("NEON BLUE • CONTROL PANEL", 13, GRAY));

        mainStatus = makeLabel("● الحالة: متوقفة", 18, GRAY);
        layout.addView(mainStatus);

        Button start = makeButton("▶ تشغيل الواجهة التجريبية");
        layout.addView(start, buttonParams());

        start.setOnClickListener(v -> {
            running = true;
            updateStatus();
            showToast("تم تشغيل الواجهة");
        });

        Button stop = makeButton("■ إيقاف الواجهة");
        layout.addView(stop, buttonParams());

        stop.setOnClickListener(v -> {
            running = false;
            updateStatus();
            showToast("تم إيقاف الواجهة");
        });

        Button show = makeButton("◉ إظهار زر ESP العائم");
        layout.addView(show, buttonParams());
        show.setOnClickListener(v -> showBubble());

        Button hide = makeButton("✕ إخفاء الزر والقائمة");
        layout.addView(hide, buttonParams());

        hide.setOnClickListener(v -> {
            removeMenu();
            removeBubble();
        });

        layout.addView(makeLabel(
                "واجهة تجريبية للتصميم والتحكم فقط.",
                13, GRAY));

        setContentView(scroll);
    }

    private void updateStatus() {
        if (mainStatus != null) {
            mainStatus.setText(
                    running
                            ? "● الحالة: تعمل"
                            : "● الحالة: متوقفة");

            mainStatus.setTextColor(
                    running ? GREEN : GRAY);
        }
    }

    private int getOverlayType() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }

        return WindowManager.LayoutParams.TYPE_PHONE;
    }

    private void showBubble() {
        if (Build.VERSION.SDK_INT >= 23
                && !Settings.canDrawOverlays(this)) {

            Intent intent = new Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));

            startActivity(intent);

            showToast("فعّل صلاحية الظهور فوق التطبيقات، ثم ارجع للتطبيق");
            return;
        }

        if (bubbleView != null) {
            showToast("زر ESP ظاهر بالفعل");
            return;
        }

        TextView bubble = makeLabel("ESP", 17, WHITE);
        bubble.setBackground(makeBackground(PANEL, BLUE));
        bubble.setElevation(dp(10));

        WindowManager.LayoutParams params =
                new WindowManager.LayoutParams(
                        dp(76),
                        dp(58),
                        getOverlayType(),
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                        PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | Gravity.START;
        params.x = dp(20);
        params.y = dp(220);

        bubbleParams = params;

        bubble.setOnTouchListener(new View.OnTouchListener() {
            private float downX;
            private float downY;
            private int originalX;
            private int originalY;
            private boolean moved;

            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        downX = event.getRawX();
                        downY = event.getRawY();
                        originalX = bubbleParams.x;
                        originalY = bubbleParams.y;
                        moved = false;
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        float dx = event.getRawX() - downX;
                        float dy = event.getRawY() - downY;

                        if (Math.abs(dx) > dp(6)
                                || Math.abs(dy) > dp(6)) {
                            moved = true;
                        }

                        if (moved) {
                            bubbleParams.x =
                                    originalX + (int) dx;
                            bubbleParams.y =
                                    originalY + (int) dy;

                            try {
                                wm.updateViewLayout(
                                        bubbleView, bubbleParams);
                            } catch (Exception ignored) {
                            }
                        }
                        return true;

                    case MotionEvent.ACTION_UP:
                        if (!moved) {
                            showMenu();
                        }
                        return true;
                }

                return true;
            }
        });

        try {
            wm.addView(bubble, params);
            bubbleView = bubble;
            showToast("اضغط على ESP لفتح القائمة");
        } catch (Exception e) {
            showToast("تعذر إظهار الزر: "
                    + e.getClass().getSimpleName());
        }
    }

    private void showMenu() {
        if (menuView != null) {
            removeMenu();
            return;
        }

        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(16), dp(14), dp(16), dp(14));
        panel.setBackground(makeBackground(BG, BLUE));

        panel.addView(
                makeLabel("ESP OVERLAY PRO", 23, BLUE));

        panel.addView(
                makeLabel("NEON CONTROL PANEL", 13, GRAY));

        TextView state = makeLabel(
                running ? "● RUNNING" : "● STOPPED",
                17, running ? GREEN : GRAY);

        panel.addView(state);

        Button toggle = makeButton(
                running ? "■ إيقاف الواجهة" : "▶ تشغيل الواجهة");

        panel.addView(toggle, buttonParams());

        toggle.setOnClickListener(v -> {
            running = !running;
            updateStatus();

            state.setText(
                    running ? "● RUNNING" : "● STOPPED");

            state.setTextColor(running ? GREEN : GRAY);

            toggle.setText(
                    running
                            ? "■ إيقاف الواجهة"
                            : "▶ تشغيل الواجهة");
        });

        Button close = makeButton("✕ إغلاق القائمة");
        panel.addView(close, buttonParams());

        close.setOnClickListener(v -> removeMenu());

        Button hide = makeButton("إخفاء زر ESP");
        panel.addView(hide, buttonParams());

        hide.setOnClickListener(v -> {
            removeMenu();
            removeBubble();
        });

        int screenWidth =
                getResources().getDisplayMetrics().widthPixels;

        int menuWidth = Math.min(
                screenWidth - dp(32), dp(320));

        WindowManager.LayoutParams params =
                new WindowManager.LayoutParams(
                        menuWidth,
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        getOverlayType(),
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                        PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.CENTER;

        try {
            wm.addView(panel, params);
            menuView = panel;
            showToast("تم فتح القائمة");
        } catch (Exception e) {
            menuView = null;
            showToast("خطأ في فتح القائمة: "
                    + e.getClass().getSimpleName());
        }
    }

    private void removeMenu() {
        if (menuView != null) {
            try {
                wm.removeView(menuView);
            } catch (Exception ignored) {
            }

            menuView = null;
        }
    }

    private void removeBubble() {
        if (bubbleView != null) {
            try {
                wm.removeView(bubbleView);
            } catch (Exception ignored) {
            }

            bubbleView = null;
        }
    }

    private void showToast(String message) {
        Toast.makeText(
                this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        removeMenu();
        removeBubble();
        super.onDestroy();
    }
}
