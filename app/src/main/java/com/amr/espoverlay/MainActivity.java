
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

    private final int BG = Color.rgb(8, 12, 20);
    private final int PANEL = Color.rgb(18, 27, 42);
    private final int BLUE = Color.rgb(0, 170, 255);
    private final int WHITE = Color.WHITE;
    private final int GRAY = Color.rgb(170, 185, 205);
    private final int GREEN = Color.rgb(0, 230, 150);

    private LinearLayout root;
    private TextView status;
    private boolean enabled = false;

    private WindowManager windowManager;
    private View floatingView;
    private View floatingMenu;
    private WindowManager.LayoutParams bubbleParams;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        windowManager = (WindowManager)
                getSystemService(Context.WINDOW_SERVICE);
        buildInterface();
    }

    private int dp(float value) {
        return (int) (value * getResources()
                .getDisplayMetrics().density + 0.5f);
    }

    private GradientDrawable shape(int color, int stroke) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(18));
        if (stroke != 0) d.setStroke(dp(2), stroke);
        return d;
    }

    private TextView label(String text, int size, int color) {
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
        b.setTextColor(WHITE);
        b.setTextSize(15);
        b.setAllCaps(false);
        b.setBackground(shape(PANEL, BLUE));
        return b;
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    private void buildInterface() {
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(BG);

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18), dp(22), dp(18), dp(22));
        scroll.addView(root);

        root.addView(label("ESP OVERLAY PRO", 27, BLUE),
                matchWrap());
        root.addView(label(
                "واجهة تجريبية • أسود وأزرق نيوني", 14, GRAY),
                matchWrap());

        status = label("● الحالة: متوقفة", 18, GRAY);
        root.addView(status, matchWrap());

        Button start = makeButton("▶ تشغيل الواجهة");
        root.addView(start, matchWrap());
        start.setOnClickListener(v -> {
            enabled = true;
            updateStatus();
            showMessage("تم تشغيل الواجهة التجريبية");
        });

        Button stop = makeButton("■ إيقاف الواجهة");
        root.addView(stop, matchWrap());
        stop.setOnClickListener(v -> {
            enabled = false;
            updateStatus();
            showMessage("تم إيقاف الواجهة");
        });

        Button show = makeButton("◉ إظهار الزر العائم");
        root.addView(show, matchWrap());
        show.setOnClickListener(v -> showFloating());

        Button hide = makeButton("إخفاء الزر والقائمة");
        root.addView(hide, matchWrap());
        hide.setOnClickListener(v -> {
            removeFloatingMenu();
            removeFloating();
        });

        root.addView(label(
                "واجهة تجريبية فقط، لا تكشف مواقع اللاعبين داخل PUBG.",
                13, GRAY), matchWrap());

        setContentView(scroll);
    }

    private void updateStatus() {
        if (status != null) {
            status.setText(enabled
                    ? "● الحالة: تعمل"
                    : "● الحالة: متوقفة");
            status.setTextColor(enabled ? GREEN : GRAY);
        }
    }

    private int overlayType() {
        if (Build.VERSION.SDK_INT >= 26) {
            return WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }
        return WindowManager.LayoutParams.TYPE_PHONE;
    }

    private void showFloating() {
        if (Build.VERSION.SDK_INT >= 23
                && !Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivity(intent);
            showMessage("فعّل صلاحية الظهور فوق التطبيقات");
            return;
        }

        if (floatingView != null) {
            showMessage("الزر العائم ظاهر بالفعل");
            return;
        }

        TextView bubble = label("ESP", 16, WHITE);
        bubble.setBackground(shape(PANEL, BLUE));
        bubble.setElevation(dp(12));

        bubbleParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                overlayType(),
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        bubbleParams.gravity = Gravity.TOP | Gravity.START;
        bubbleParams.x = dp(24);
        bubbleParams.y = dp(220);

        bubble.setOnTouchListener(new View.OnTouchListener() {
            private int startX, startY;
            private float touchX, touchY;
            private boolean moved;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = bubbleParams.x;
                        startY = bubbleParams.y;
                        touchX = event.getRawX();
                        touchY = event.getRawY();
                        moved = false;
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        float dx = event.getRawX() - touchX;
                        float dy = event.getRawY() - touchY;

                        if (Math.abs(dx) > dp(6)
                                || Math.abs(dy) > dp(6)) {
                            moved = true;
                        }

                        if (moved) {
                            bubbleParams.x = startX + (int) dx;
                            bubbleParams.y = startY + (int) dy;
                            try {
                                windowManager.updateViewLayout(
                                        floatingView, bubbleParams);
                            } catch (Exception ignored) {
                            }
                        }
                        return true;

                    case MotionEvent.ACTION_UP:
                        if (!moved) showFloatingMenu();
                        return true;
                }
                return false;
            }
        });

        try {
            windowManager.addView(bubble, bubbleParams);
            floatingView = bubble;
            showMessage("اضغط ESP لفتح القائمة");
        } catch (Exception e) {
            floatingView = null;
            showMessage("تعذر إظهار الزر العائم");
        }
    }

    private void showFloatingMenu() {
        if (floatingMenu != null) {
            removeFloatingMenu();
            return;
        }

        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(14), dp(12), dp(14), dp(12));
        panel.setBackground(shape(BG, BLUE));
        panel.setElevation(dp(16));

        panel.addView(label("ESP OVERLAY PRO", 20, BLUE),
                matchWrap());
        panel.addView(label("واجهة تجريبية", 13, GRAY),
                matchWrap());

        TextView state = label(
                enabled ? "● الحالة: تعمل" : "● الحالة: متوقفة",
                15, enabled ? GREEN : GRAY);
        panel.addView(state, matchWrap());

        Button toggle = makeButton(
                enabled ? "■ إيقاف الواجهة" : "▶ تشغيل الواجهة");
        panel.addView(toggle, matchWrap());

        toggle.setOnClickListener(v -> {
            enabled = !enabled;
            updateStatus();
            state.setText(enabled
                    ? "● الحالة: تعمل"
                    : "● الحالة: متوقفة");
            state.setTextColor(enabled ? GREEN : GRAY);
            toggle.setText(enabled
                    ? "■ إيقاف الواجهة"
                    : "▶ تشغيل الواجهة");
        });

        Button close = makeButton("إغلاق القائمة");
        panel.addView(close, matchWrap());
        close.setOnClickListener(v -> removeFloatingMenu());

        int width = Math.min(
                getResources().getDisplayMetrics().widthPixels - dp(32),
                dp(300));

        WindowManager.LayoutParams params =
                new WindowManager.LayoutParams(
                        width,
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        overlayType(),
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                        PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.CENTER;

        try {
            
try {
    windowManager.addView(panel, params);
    floatingMenu = panel;
    showMessage("تم فتح القائمة");
} catch (Exception e) {
    floatingMenu = null;
    showMessage("خطأ القائمة: " + e.getClass().getSimpleName());
    android.util.Log.e("ESP_MENU", "Failed to show menu", e);
}

        }
    }

    private void removeFloatingMenu() {
        if (floatingMenu != null) {
            try {
                windowManager.removeView(floatingMenu);
            } catch (Exception ignored) {
            }
            floatingMenu = null;
        }
    }

    private void removeFloating() {
        if (floatingView != null) {
            try {
                windowManager.removeView(floatingView);
            } catch (Exception ignored) {
            }
            floatingView = null;
        }
        showMessage("تم إخفاء الزر العائم");
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        removeFloatingMenu();
        removeFloating();
        super.onDestroy();
    }
}
