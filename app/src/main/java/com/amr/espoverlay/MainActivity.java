
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
    private final int PANEL = Color.rgb(17, 27, 43);
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
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(18));
        if (border != 0) d.setStroke(dp(2), border);
        return d;
    }

    private TextView makeLabel(String text, int size, int color) {
        TextView t = new TextView(this);
        t.setText(text);
        t.setTextSize(size);
        t.setTextColor(color);
        t.setGravity(Gravity.CENTER);
        t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        t.setPadding(dp(10), dp(10), dp(10), dp(10));
        return t;
    }

    private Button makeButton(String text) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextSize(15);
        b.setTextColor(WHITE);
        b.setAllCaps(false);
        b.setBackground(makeBackground(PANEL, BLUE));
        return b;
    }

    private LinearLayout.LayoutParams itemParams() {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        p.setMargins(0, dp(5), 0, dp(5));
        return p;
    }

    private void buildMainScreen() {
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(BG);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18), dp(22), dp(18), dp(22));
        scroll.addView(root);

        root.addView(makeLabel("ESP OVERLAY PRO", 27, BLUE));
        root.addView(makeLabel("NEON BLUE CONTROL PANEL", 13, GRAY));

        mainStatus = makeLabel("● الحالة: متوقفة", 18, GRAY);
        root.addView(mainStatus);

        Button start = makeButton("▶ تشغيل الواجهة التجريبية");
        root.addView(start, itemParams());
        start.setOnClickListener(v -> {
            running = true;
            updateStatus();
            showToast("تم تشغيل الواجهة");
        });

        Button stop = makeButton("■ إيقاف الواجهة");
        root.addView(stop, itemParams());
        stop.setOnClickListener(v -> {
            running = false;
            updateStatus();
            showToast("تم إيقاف الواجهة");
        });

        Button show = makeButton("◉ إظهار زر ESP العائم");
        root.addView(show, itemParams());
        show.setOnClickListener(v -> showBubble());

        Button hide = makeButton("✕ إخفاء الزر والقائمة");
        root.addView(hide, itemParams());
        hide.setOnClickListener(v -> {
            removeMenu();
            removeBubble();
        });

        root.addView(makeLabel(
                "واجهة تجريبية فقط.", 13, GRAY));

        setContentView(scroll);
    }

    private void updateStatus() {
        if (mainStatus != null) {
            mainStatus.setText(
                    running ? "● الحالة: تعمل" : "● الحالة: متوقفة");
            mainStatus.setTextColor(running ? GREEN : GRAY);
        }
    }

    private int overlayType() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }
        return WindowManager.LayoutParams.TYPE_PHONE;
    }

    private boolean hasOverlayPermission() {
        return Build.VERSION.SDK_INT < 23
                || Settings.canDrawOverlays(this);
    }

    private void showBubble() {
        if (!hasOverlayPermission()) {
            Intent i = new Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivity(i);
            showToast("فعّل صلاحية الظهور فوق التطبيقات ثم ارجع");
            return;
        }

        if (bubbleView != null) {
            showToast("زر ESP ظاهر بالفعل");
            return;
        }

        TextView bubble = makeLabel("ESP", 17, WHITE);
        bubble.setBackground(makeBackground(PANEL, BLUE));
        bubble.setElevation(dp(10));

        bubbleParams = new WindowManager.LayoutParams(
                dp(76), dp(58), overlayType(),
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);

        bubbleParams.gravity = Gravity.TOP | Gravity.START;
        bubbleParams.x = dp(20);
        bubbleParams.y = dp(220);

        bubble.setOnTouchListener(new View.OnTouchListener() {
            private float downX, downY;
            private int startX, startY;
            private boolean moved;

            @Override
            public boolean onTouch(View v, MotionEvent e) {
                switch (e.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        downX = e.getRawX();
                        downY = e.getRawY();
                        startX = bubbleParams.x;
                        startY = bubbleParams.y;
                        moved = false;
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        float dx = e.getRawX() - downX;
                        float dy = e.getRawY() - downY;

                        if (Math.abs(dx) > dp(6)
                                || Math.abs(dy) > dp(6)) {
                            moved = true;
                        }

                        if (moved && bubbleView != null) {
                            bubbleParams.x = startX + (int) dx;
                            bubbleParams.y = startY + (int) dy;
                            try {
                                wm.updateViewLayout(
                                        bubbleView, bubbleParams);
                            } catch (Exception ignored) { }
                        }
                        return true;

                    case MotionEvent.ACTION_UP:
                        if (!moved) showMenu();
                        return true;
                }
                return true;
            }
        });

        try {
            wm.addView(bubble, bubbleParams);
            bubbleView = bubble;
            showToast("اضغط ESP لفتح القائمة");
        } catch (Exception e) {
            bubbleView = null;
            showToast("تعذر عرض الزر: " +
                    e.getClass().getSimpleName());
        }
    }

    private void showMenu() {
        if (menuView != null) {
            removeMenu();
            return;
        }

        if (!hasOverlayPermission()) {
            showToast("فعّل صلاحية الظهور فوق التطبيقات");
            return;
        }

        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(16), dp(16), dp(16), dp(16));
        panel.setBackground(makeBackground(BG, BLUE));
        panel.setElevation(dp(16));

        panel.addView(
                makeLabel("ESP OVERLAY PRO", 23, BLUE),
                itemParams());

        panel.addView(
                makeLabel("NEON CONTROL PANEL", 13, GRAY),
                itemParams());

        TextView state = makeLabel(
                running ? "● RUNNING" : "● STOPPED",
                17, running ? GREEN : GRAY);
        panel.addView(state, itemParams());

        Button toggle = makeButton(
                running ? "■ إيقاف الواجهة" : "▶ تشغيل الواجهة");
        panel.addView(toggle, itemParams());

        toggle.setOnClickListener(v -> {
            running = !running;
            updateStatus();
            state.setText(running ? "● RUNNING" : "● STOPPED");
            state.setTextColor(running ? GREEN : GRAY);
            toggle.setText(running
                    ? "■ إيقاف الواجهة"
                    : "▶ تشغيل الواجهة");
        });

        Button close = makeButton("✕ إغلاق القائمة");
        panel.addView(close, itemParams());
        close.setOnClickListener(v -> removeMenu());

        int screenWidth = getResources()
                .getDisplayMetrics().widthPixels;
        int menuWidth = Math.min(screenWidth - dp(32), dp(320));

        WindowManager.LayoutParams p =
                new WindowManager.LayoutParams(
                        menuWidth,
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        overlayType(),
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                        PixelFormat.TRANSLUCENT);

        p.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        p.y = dp(100);

        try {
            // سجل النافذة قبل عرضها حتى يسهل إغلاقها لاحقاً.
            menuView = panel;
            wm.addView(panel, p);
            panel.bringToFront();
            panel.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            menuView = null;
            showToast("فشل فتح القائمة: " +
                    e.getClass().getSimpleName());
            android.util.Log.e("ESP_OVERLAY", "showMenu failed", e);
        }
    }

    private void removeMenu() {
        if (menuView != null) {
            try {
                wm.removeView(menuView);
            } catch (Exception ignored) { }
            menuView = null;
        }
    }

    private void removeBubble() {
        if (bubbleView != null) {
            try {
                wm.removeView(bubbleView);
            } catch (Exception ignored) { }
            bubbleView = null;
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        removeMenu();
        removeBubble();
        super.onDestroy();
    }
}
