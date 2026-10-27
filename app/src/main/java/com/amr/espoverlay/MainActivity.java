package com.amr.espoverlay;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private final int BG = Color.rgb(8, 10, 18);
    private final int BLUE = Color.rgb(0, 210, 255);
    private final int WHITE = Color.WHITE;
    private boolean enabled = false;
    private TextView status;

    private GradientDrawable shape(int color, int stroke) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(24);
        d.setStroke(2, stroke);
        return d;
    }

    private TextView text(String value, int size) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(WHITE);
        t.setGravity(Gravity.CENTER);
        t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return t;
    }

    private Button button(String value) {
        Button b = new Button(this);
        b.setText(value);
        b.setTextColor(WHITE);
        b.setTextSize(16);
        b.setAllCaps(false);
        b.setBackground(shape(Color.rgb(18, 25, 40), BLUE));
        return b;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(24, 35, 24, 24);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setBackgroundColor(BG);

        TextView title = text("ESP OVERLAY", 28);
        title.setTextColor(BLUE);
        root.addView(title, new LinearLayout.LayoutParams(
            -1, 75));

        TextView subtitle = text("NEON CONTROL PANEL", 14);
        subtitle.setTextColor(Color.LTGRAY);
        root.addView(subtitle, new LinearLayout.LayoutParams(
            -1, 40));

        View line = new View(this);
        line.setBackgroundColor(BLUE);
        LinearLayout.LayoutParams lp =
            new LinearLayout.LayoutParams(-1, 2);
        lp.setMargins(10, 12, 10, 24);
        root.addView(line, lp);

        status = text("● SYSTEM OFF", 19);
        status.setTextColor(Color.LTGRAY);
        status.setBackground(shape(
            Color.rgb(18, 25, 40), BLUE));
        root.addView(status, new LinearLayout.LayoutParams(
            -1, 65));

        Button on = button("تشغيل  ON");
        LinearLayout.LayoutParams bp =
            new LinearLayout.LayoutParams(-1, 65);
        bp.setMargins(0, 28, 0, 12);
        root.addView(on, bp);

        Button off = button("إيقاف  OFF");
        root.addView(off, new LinearLayout.LayoutParams(
            -1, 65));

        TextView info = text(
            "واجهة تجريبية\nالتحكم هنا بواجهة التطبيق فقط",
            14);
        info.setTextColor(Color.LTGRAY);
        LinearLayout.LayoutParams ip =
            new LinearLayout.LayoutParams(-1, 100);
        ip.setMargins(0, 25, 0, 0);
        root.addView(info, ip);

        on.setOnClickListener(v -> {
            enabled = true;
            status.setText("● SYSTEM ON");
            status.setTextColor(BLUE);
            Toast.makeText(this, "تم التشغيل",
                Toast.LENGTH_SHORT).show();
        });

        off.setOnClickListener(v -> {
            enabled = false;
            status.setText("● SYSTEM OFF");
            status.setTextColor(Color.LTGRAY);
            Toast.makeText(this, "تم الإيقاف",
                Toast.LENGTH_SHORT).show();
        });

        setContentView(root);
    }
}