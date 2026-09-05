package com.example.kehadiran;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

public class MainActivity extends Activity {

    TextView debugText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WebView webView = new WebView(this);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);

        webView.setWebViewClient(new WebViewClient());

        debugText = new TextView(this);
        debugText.setText("Memulakan aplikasi...");
        debugText.setTextColor(Color.RED);
        debugText.setTextSize(12);
        debugText.setBackgroundColor(Color.WHITE);
        debugText.setPadding(12, 8, 12, 8);

        android.widget.LinearLayout layout =
                new android.widget.LinearLayout(this);

        layout.setOrientation(
                android.widget.LinearLayout.VERTICAL
        );

        layout.addView(debugText,
                new android.widget.LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                ));

        layout.addView(webView,
                new android.widget.LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        0,
                        1
                ));

        setContentView(layout);

        webView.setWebChromeClient(new WebChromeClient() {

            @Override
            public boolean onConsoleMessage(ConsoleMessage cm) {

                String msg =
                        cm.message()
                        + "\nBaris: "
                        + cm.lineNumber();

                debugText.setText(
                        "JAVASCRIPT ERROR / LOG:\n" + msg
                );

                return true;
            }
        });

        webView.loadUrl(
                "file:///android_asset/index.html"
        );
    }
}
