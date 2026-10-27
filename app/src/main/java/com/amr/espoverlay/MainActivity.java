
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
    private final int PANEL = Color.rgb(18, 27, 43);
    private final int BLUE = Color.rgb(0, 170, 255);
    private final int WHITE = Color.WHITE;
    private final int GRAY = Color.rgb(165, 185, 205);
    private final int GREEN = Color.rgb(0, 235, 150);

    private WindowManager wm;
    private View bubble;
    private WindowManager.LayoutParams bubbleParams;
    private TextView status;
    private boolean running = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

        if (getIntent().getBooleanExtra("OPEN_MENU", false)) {
            showMenuScreen();
        } else {
            showHomeScreen();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);

        if (intent.getBooleanExtra("OPEN_MENU", false)) {
            showMenuScreen();
        }
    }

    private int dp(float v) {
        return (int) (v * getResources()
                .getDisplayMetrics().density + 0.5f);
    }

    private GradientDrawable bg(int color, int border) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(18));
        if (border != 0) d.setStroke(dp(2), border);
        return d;
    }

    private TextView label(String text, int size, int color) {
        TextView t = new TextView(this);
        t.setText(text);
        t.setTextSize(size);
        t.setTextColor(color);
        t.setGravity(Gravity.CENTER);
        t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        t.setPadding(dp(10), dp(12), dp(10), dp(12));
        return t;
    }

    private Button button(String text) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextColor(WHITE);
        b.setTextSize(15);
        b.setAllCaps(false);
        b.setBackground(bg(PANEL, BLUE));
        return b;
    }

    private LinearLayout.LayoutParams itemParams() {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        p.setMargins(0, dp(6), 0, dp(6));
        return p;
    }

    private LinearLayout makeLayout() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(18), dp(20), dp(18), dp(20));
        layout.setBackgroundColor(BG);
        return layout;
    }

    private void showHomeScreen() {
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(BG);

        LinearLayout root = makeLayout();
        scroll.addView(root);

        root.addView(label("ESP OVERLAY PRO", 27, BLUE));
        root.addView(label("NEON BLUE CONTROL PANEL", 13, GRAY));

        status = label(currentStatus(), 18, running ? GREEN : GRAY);
        root.addView(status);

        Button open = button("☰ فتح القائمة");
        root.addView(open, itemParams());
        open.setOnClickListener(v -> showMenuScreen());

        Button start = button("▶ تشغيل الواجهة التجريبية");
        root.addView(start, itemParams());
        start.setOnClickListener(v -> {
            running = true;
            updateStatus();
            Toast.makeText(this, "تم التشغيل", Toast.LENGTH_SHORT).show();
        });

        Button stop = button("■ إيقاف الواجهة");
        root.addView(stop, itemParams());
        stop.setOnClickListener(v -> {
            running = false;
            updateStatus();
            Toast.makeText(this, "تم الإيقاف", Toast.LENGTH_SHORT).show();
        });

        Button floating = button("◉ إظهار زر ESP العائم");
        root.addView(floating, itemParams());
        floating.setOnClickListener(v -> showBubble());

        Button hide = button("✕ إخفاء زر ESP");
        root.addView(hide, itemParams());
        hide.setOnClickListener(v -> removeBubble());

        root.addView(label("واجهة تجريبية فقط.", 13, GRAY));
        setContentView(scroll);
    }

    private String currentStatus() {
        return running ? "● الحالة: تعمل" : "● الحالة: متوقفة";
    }

    private void updateStatus() {
        if (status != null) {
            status.setText(currentStatus());
            status.setTextColor(running ? GREEN : GRAY);
        }
    }

    private void showMenuScreen() {
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(BG);

        LinearLayout root = makeLayout();
        root.setBackground(bg(BG, BLUE));
        scroll.addView(root);

        root.addView(label("ESP OVERLAY PRO", 27, BLUE));
        root.addView(label("NEON CONTROL MENU", 14, GRAY));

        TextView state = label(
                running ? "● RUNNING" : "● STOPPED",
                19, running ? GREEN : GRAY);
        root.addView(state, itemParams());

        Button toggle = button(
                running ? "■ إيقاف الواجهة" : "▶ تشغيل الواجهة");
        root.addView(toggle, itemParams());

        toggle.setOnClickListener(v -> {
            running = !running;
            state.setText(running ? "● RUNNING" : "● STOPPED");
            state.setTextColor(running ? GREEN : GRAY);
            toggle.setText(running
                    ? "■ إيقاف الواجهة"
                    : "▶ تشغيل الواجهة");
        });

        Button home = button("⌂ الرجوع للرئيسية");
        root.addView(home, itemParams());
        home.setOnClickListener(v -> showHomeScreen());

        Button hide = button("✕ إخفاء زر ESP العائم");
        root.addView(hide, itemParams());
        hide.setOnClickListener(v -> {
            removeBubble();
            showMenuScreen();
        });

        root.addView(label(
                "هذه واجهة تحكم تجريبية فقط.",
                13, GRAY));

        setContentView(scroll);
    }

    private int overlayType() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }
        return WindowManager.LayoutParams.TYPE_PHONE;
    }

    private void showBubble() {
        if (Build.VERSION.SDK_INT >= 23
                && !Settings.canDrawOverlays(this)) {
            Intent i = new Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivity(i);
            Toast.makeText(this,
                    "فعّل صلاحية الظهور فوق التطبيقات",
                    Toast.LENGTH_LONG).show();
            return;
        }

        if (bubble != null) {
            Toast.makeText(this, "زر ESP ظاهر بالفعل",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        TextView b = label("ESP", 17, WHITE);
        b.setBackground(bg(PANEL, BLUE));
        b.setElevation(dp(10));

        bubbleParams = new WindowManager.LayoutParams(
                dp(76), dp(58), overlayType(),
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        bubbleParams.gravity = Gravity.TOP | Gravity.START;
        bubbleParams.x = dp(20);
        bubbleParams.y = dp(220);

        b.setOnTouchListener(new View.OnTouchListener() {
            float downX, downY;
            int startX, startY;
            boolean moved;

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

                        if (moved && bubble != null) {
                            bubbleParams.x = startX + (int) dx;
                            bubbleParams.y = startY + (int) dy;
                            try {
                                wm.updateViewLayout(bubble, bubbleParams);
                            } catch (Exception ignored) { }
                        }
                        return true;

                    case MotionEvent.ACTION_UP:
                        if (!moved) openMenuActivity();
                        return true;
                }
                return true;
            }
        });

        try {
            wm.addView(b, bubbleParams);
            bubble = b;
            Toast.makeText(this, "اضغط ESP لفتح القائمة",
                    Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            bubble = null;
            Toast.makeText(this, "تعذر عرض الزر",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void openMenuActivity() {
        Intent i = new Intent(this, MainActivity.class);
        i.putExtra("OPEN_MENU", true);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(i);
    }

    private void removeBubble() {
        if (bubble != null) {
            try {
                wm.removeView(bubble);
            } catch (Exception ignored) { }
            bubble = null;
        }
    }

    @Override
    protected void onDestroy() {
        removeBubble();
        super.onDestroy();
    }
}
