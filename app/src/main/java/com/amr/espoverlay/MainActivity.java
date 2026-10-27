package com.amr.espoverlay;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.PixelFormat;
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

    private final int BG = Color.rgb(7, 9, 16);
    private final int PANEL = Color.rgb(17, 23, 38);
    private final int BLUE = Color.rgb(0, 210, 255);
    private final int WHITE = Color.WHITE;
    private final int MUTED = Color.rgb(165, 180, 200);

    private TextView status;
    private boolean enabled = false;
    private boolean animationsEnabled = true;
    private boolean floatingEnabled = false;

    private WindowManager windowManager;
    private View floatingView;

    private GradientDrawable shape(int color, int stroke) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(24);
        d.setStroke(2, stroke);
        return d;
    }

    private TextView label(String value, int size, int color) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);
        t.setGravity(Gravity.CENTER);
        t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        t.setPadding(12, 12, 12, 12);
        return t;
    }

    private Button makeButton(String value) {
        Button b = new Button(this);
        b.setText(value);
        b.setTextColor(WHITE);
        b.setTextSize(16);
        b.setAllCaps(false);
        b.setGravity(Gravity.CENTER);
        b.setMinHeight(64);
        b.setMinimumHeight(64);
        b.setPadding(12, 8, 12, 8);
        b.setBackground(shape(PANEL, BLUE));

        if (Build.VERSION.SDK_INT >= 21) {
            b.setStateListAnimator(null);
        }

        if (animationsEnabled) {
            b.setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.animate().scaleX(0.97f).scaleY(0.97f)
                        .setDuration(80).start();
                } else if (event.getAction() == MotionEvent.ACTION_UP
                        || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    v.animate().scaleX(1f).scaleY(1f)
                        .setDuration(100).start();
                }
                return false;
            });
        }

        return b;
    }

    private void addButton(LinearLayout parent, Button b) {
        LinearLayout.LayoutParams p =
            new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            );
        p.setMargins(0, 8, 0, 8);
        parent.addView(b, p);
    }

    private void updateStatus() {
        if (status == null) return;

        if (enabled) {
            status.setText("● النظام يعمل");
            status.setTextColor(BLUE);
        } else {
            status.setText("● النظام متوقف");
            status.setTextColor(MUTED);
        }
    }

    private void showMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        windowManager = (WindowManager) getSystemService(
            Context.WINDOW_SERVICE
        );
        buildInterface();
    }

    private void buildInterface() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(BG);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(22, 28, 22, 24);
        root.setBackgroundColor(BG);

        scroll.addView(root);

        TextView title = label("ESP OVERLAY PRO", 28, BLUE);
        root.addView(title, new LinearLayout.LayoutParams(-1, -2));

        TextView subtitle = label(
            "لوحة التحكم الليونية",
            17,
            WHITE
        );
        root.addView(subtitle, new LinearLayout.LayoutParams(-1, -2));

        View line = new View(this);
        line.setBackgroundColor(BLUE);
        LinearLayout.LayoutParams lineParams =
            new LinearLayout.LayoutParams(-1, 3);
        lineParams.setMargins(0, 16, 0, 18);
        root.addView(line, lineParams);

        status = label("● النظام متوقف", 21, MUTED);
        status.setBackground(shape(PANEL, BLUE));
        root.addView(status, new LinearLayout.LayoutParams(-1, 64));

        Button on = makeButton("تشغيل النظام");
        addButton(root, on);

        Button off = makeButton("إيقاف النظام");
        addButton(root, off);

        on.setOnClickListener(v -> {
            enabled = true;
            updateStatus();
            showMessage("تم تشغيل واجهة التطبيق");
        });

        off.setOnClickListener(v -> {
            enabled = false;
            updateStatus();
            showMessage("تم إيقاف واجهة التطبيق");
        });

        TextView section = label("المؤشرات", 21, BLUE);
        LinearLayout.LayoutParams sp =
            new LinearLayout.LayoutParams(-1, 60);
        sp.setMargins(0, 14, 0, 0);
        root.addView(section, sp);

        TextView indicator = label(
            "حالة الواجهة: جاهزة\nالمؤثرات: متاحة\nنوع اللوحة: مستقلة",
            15,
            MUTED
        );
        indicator.setBackground(shape(PANEL, Color.rgb(45, 65, 90)));
        root.addView(indicator, new LinearLayout.LayoutParams(-1, 100));

        Button floating = makeButton("إظهار الزر العائم");
        addButton(root, floating);
        floating.setOnClickListener(v -> requestFloating());

        Button settings = makeButton("الإعدادات");
        addButton(root, settings);
        settings.setOnClickListener(v -> showSettings());

        Button close = makeButton("إغلاق الزر العائم");
        addButton(root, close);
        close.setOnClickListener(v -> removeFloating());

        TextView footer = label(
            "واجهة تجريبية مستقلة\nلا تتحكم باللعبة ولا تكشف مواقع اللاعبين",
            13,
            MUTED
        );
        LinearLayout.LayoutParams fp =
            new LinearLayout.LayoutParams(-1, -2);
        fp.setMargins(0, 18, 0, 0);
        root.addView(footer, fp);

        setContentView(scroll);
    }

    private void showSettings() {
        String[] options = {
            "تفعيل المؤثرات",
            "إيقاف المؤثرات",
            "معلومات التطبيق"
        };

        new AlertDialog.Builder(this)
            .setTitle("إعدادات ESP OVERLAY PRO")
            .setItems(options, (dialog, which) -> {
                if (which == 0) {
                    animationsEnabled = true;
                    showMessage("تم تفعيل المؤثرات");
                    buildInterface();
                } else if (which == 1) {
                    animationsEnabled = false;
                    showMessage("تم إيقاف المؤثرات");
                    buildInterface();
                } else {
                    new AlertDialog.Builder(this)
                        .setTitle("معلومات التطبيق")
                        .setMessage(
                            "ESP OVERLAY PRO\n" +
                            "واجهة مستقلة تجريبية\n" +
                            "أزرار التشغيل تتحكم بالواجهة فقط."
                        )
                        .setPositiveButton("حسناً", null)
                        .show();
                }
            })
            .setNegativeButton("إلغاء", null)
            .show();
    }

    private void requestFloating() {
        if (Build.VERSION.SDK_INT >= 23
                && !Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName())
            );
            startActivity(intent);
            showMessage("فعّل إذن الظهور فوق التطبيقات ثم ارجع");
            return;
        }

        showFloating();
    }

    private void showFloating() {
        if (floatingView != null) {
            showMessage("الزر العائم ظاهر بالفعل");
            return;
        }

        TextView bubble = label("ESP", 16, WHITE);
        bubble.setBackground(shape(PANEL, BLUE));
        bubble.setElevation(12);

        int type;
        if (Build.VERSION.SDK_INT >= 26) {
            type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            type = WindowManager.LayoutParams.TYPE_PHONE;
        }

        WindowManager.LayoutParams params =
            new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                type,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            );

        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 30;
        params.y = 250;

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
                        params.x = startX + (int)(event.getRawX() - touchX);
                        params.y = startY + (int)(event.getRawY() - touchY);
                        windowManager.updateViewLayout(bubble, params);
                        return true;

                    case MotionEvent.ACTION_UP:
                        float dx = Math.abs(event.getRawX() - touchX);
                        float dy = Math.abs(event.getRawY() - touchY);

                        if (dx < 10 && dy < 10) {
                            new AlertDialog.Builder(MainActivity.this)
                                .setTitle("ESP OVERLAY PRO")
                                .setMessage(
                                    enabled
                                    ? "حالة الواجهة: تعمل"
                                    : "حالة الواجهة: متوقفة"
                                )
                                .setPositiveButton("تشغيل", (d, w) -> {
                                    enabled = true;
                                    updateStatus();
                                })
                                .setNegativeButton("إيقاف", (d, w) -> {
                                    enabled = false;
                                    updateStatus();
                                })
                                .setNeutralButton("إغلاق", (d, w) -> removeFloating())
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
            showMessage("تعذر إظهار الزر العائم");
}
}

@Override
protected void onDestroy() {
    removeFloating();
    super.onDestroy();
}

}