package com.amr.espoverlay;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.content.Intent;
import android.net.Uri;
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

    private final int BG = Color.rgb(12, 15, 22);
    private final int PANEL = Color.rgb(22, 29, 42);
    private final int BLUE = Color.rgb(0, 150, 255);
    private final int WHITE = Color.WHITE;
    private final int GRAY = Color.rgb(170, 185, 205);
    private final int GREEN = Color.rgb(0, 230, 150);

    private LinearLayout root;
    private TextView status;
    private boolean enabled = false;
    private boolean floatingEnabled = false;

    private WindowManager windowManager;
    private View floatingView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        windowManager =
                (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        buildInterface();
    }

    private GradientDrawable shape(int color, int stroke) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(18));
        if (stroke != 0) {
            d.setStroke(dp(2), stroke);
        }
        return d;
    }

    private int dp(float value) {
        return (int) (value * getResources()
                .getDisplayMetrics().density + 0.5f);
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
        b.setPadding(dp(8), dp(6), dp(8), dp(6));
        return b;
    }

    private void buildInterface() {
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(BG);

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18), dp(22), dp(18), dp(22));
        scroll.addView(root);

        TextView title = label("ESP OVERLAY PRO", 27, BLUE);
        root.addView(title, matchWrap());

        TextView subtitle = label(
                "واجهة تجريبية • أسود وأزرق نيوني", 14, GRAY);
        root.addView(subtitle, matchWrap());

        addSpace(12);

        LinearLayout statusCard = new LinearLayout(this);
        statusCard.setOrientation(LinearLayout.VERTICAL);
        statusCard.setPadding(dp(12), dp(10), dp(12), dp(10));
        statusCard.setBackground(shape(PANEL, BLUE));

        status = label("● الحالة: متوقفة", 18, GRAY);
        statusCard.addView(status, matchWrap());
        root.addView(statusCard, matchWrap());

        addSpace(16);

        Button start = makeButton("▶ تشغيل الواجهة");
        root.addView(start, matchWrap());
        start.setOnClickListener(v -> {
            enabled = true;
            updateStatus();
            showMessage("تم تشغيل الواجهة التجريبية");
        });

        addSpace(8);

        Button stop = makeButton("■ إيقاف الواجهة");
        root.addView(stop, matchWrap());
        stop.setOnClickListener(v -> {
            enabled = false;
            updateStatus();
            showMessage("تم إيقاف الواجهة");
        });

        addSpace(8);

        Button settings = makeButton("⚙ الإعدادات");
        root.addView(settings, matchWrap());
        settings.setOnClickListener(v -> showSettings());

        addSpace(8);

        Button floating = makeButton("◉ إظهار الزر العائم");
        root.addView(floating, matchWrap());
        floating.setOnClickListener(v -> showFloating());

        addSpace(8);

        Button hide = makeButton("إخفاء الزر العائم");
        root.addView(hide, matchWrap());
        hide.setOnClickListener(v -> removeFloating());

        addSpace(16);

        TextView note = label(
                "ملاحظة: التطبيق واجهة تجريبية فقط، " +
                "ولا يكشف اللاعبين أو مواقعهم داخل PUBG.",
                14, GRAY);
        root.addView(note, matchWrap());

        setContentView(scroll);
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    private void addSpace(int height) {
        View space = new View(this);
        root.addView(space, new LinearLayout.LayoutParams(
                1, dp(height)));
    }

    private void updateStatus() {
        if (status != null) {
            if (enabled) {
                status.setText("● الحالة: تعمل");
                status.setTextColor(GREEN);
            } else {
                status.setText("● الحالة: متوقفة");
                status.setTextColor(GRAY);
            }
        }
    }

    private void showSettings() {
        String[] options = {
                "معلومات التطبيق",
                "حالة الواجهة",
                "إغلاق"
        };

        new AlertDialog.Builder(this)
                .setTitle("⚙ الإعدادات")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        new AlertDialog.Builder(this)
                                .setTitle("ESP OVERLAY PRO")
                                .setMessage(
                                        "نسخة تجريبية للأندرويد.\n" +
                                        "ألوان: أسود وأزرق نيوني.\n" +
                                        "لا تتصل باللعبة ولا تكشف اللاعبين.")
                                .setPositiveButton("حسناً", null)
                                .show();
                    } else if (which == 1) {
                        showMessage(enabled
                                ? "الواجهة تعمل"
                                : "الواجهة متوقفة");
                    }
                })
                .show();
    }

    private void showFloating() {
        if (floatingEnabled && floatingView != null) {
            showMessage("الزر العائم ظاهر بالفعل");
            return;
        }

        if (Build.VERSION.SDK_INT >= 23
                && !Settings.canDrawOverlays(this)) {
            new AlertDialog.Builder(this)
                    .setTitle("السماح بالزر العائم")
                    .setMessage(
                            "فعّل السماح بالظهور فوق التطبيقات " +
                            "حتى يظهر الزر العائم.")
                    .setPositiveButton("فتح الإعدادات", (d, w) -> {
                        Intent intent = new Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:" + getPackageName()));
                        startActivity(intent);
                    })
                    .setNegativeButton("إلغاء", null)
                    .show();
            return;
        }

        final TextView bubble = label("ESP", 16, WHITE);
        bubble.setBackground(shape(PANEL, BLUE));
        bubble.setElevation(dp(12));

        int type;
        if (Build.VERSION.SDK_INT >= 26) {
            type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            type = WindowManager.LayoutParams.TYPE_PHONE;
        }

        final WindowManager.LayoutParams params =
                new WindowManager.LayoutParams(
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        type,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                        PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | Gravity.START;
        params.x = dp(30);
        params.y = dp(250);

        bubble.setOnTouchListener(new View.OnTouchListener() {
            private int startX, startY;
            private float touchX, touchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = params.x;
                        startY = params.y;
                        touchX = event.getRawX();
                        touchY = event.getRawY();
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        params.x = startX +
                                (int) (event.getRawX() - touchX);
                        params.y = startY +
                                (int) (event.getRawY() - touchY);
                        try {
                            windowManager.updateViewLayout(bubble, params);
                        } catch (Exception ignored) {
                        }
                        return true;

                    case MotionEvent.ACTION_UP:
                        float dx = Math.abs(
                                event.getRawX() - touchX);
                        float dy = Math.abs(
                                event.getRawY() - touchY);

                        if (dx < dp(10) && dy < dp(10)) {
                            new AlertDialog.Builder(MainActivity.this)
                                    .setTitle("ESP OVERLAY PRO")
                                    .setMessage(enabled
                                            ? "حالة الواجهة: تعمل"
                                            : "حالة الواجهة: متوقفة")
                                    .setPositiveButton("تشغيل", (d, w) -> {
                                        enabled = true;
                                        updateStatus();
                                    })
                                    .setNegativeButton("إيقاف", (d, w) -> {
                                        enabled = false;
                                        updateStatus();
                                    })
                                    .setNeutralButton("إغلاق الزر",
                                            (d, w) -> removeFloating())
                                    .show();
                        }
                        return true;
                }
                return false;
            }
        });

        try {
            windowManager.addView(bubble, params);
            floatingView = bubble;
            floatingEnabled = true;
            showMessage("تم إظهار الزر العائم");
        } catch (Exception e) {
            floatingView = null;
            floatingEnabled = false;
            showMessage("تعذر إظهار الزر. تحقق من الإذن.");
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
        floatingEnabled = false;
        showMessage("تم إخفاء الزر العائم");
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        if (floatingView != null) {
            try {
                windowManager.removeView(floatingView);
            } catch (Exception ignored) {
            }
            floatingView = null;
        }
        floatingEnabled = false;
        super.onDestroy();
    }
}