package com.example.kehadiran;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends Activity {

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

        webView.setWebChromeClient(new WebChromeClient() {

            @Override
            public boolean onJsAlert(
                    WebView view,
                    String url,
                    String message,
                    final JsResult result) {

                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Kehadiran Sekolah Minggu")
                        .setMessage(message)
                        .setPositiveButton("OK",
                                (dialog, which) -> result.confirm())
                        .setOnCancelListener(
                                dialog -> result.cancel())
                        .show();

                return true;
            }


            @Override
            public boolean onJsConfirm(
                    WebView view,
                    String url,
                    String message,
                    final JsResult result) {

                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Kehadiran Sekolah Minggu")
                        .setMessage(message)
                        .setNegativeButton("BATAL",
                                (dialog, which) ->
                                        result.cancel())
                        .setPositiveButton("PADAM",
                                (dialog, which) ->
                                        result.confirm())
                        .setOnCancelListener(
                                dialog -> result.cancel())
                        .show();

                return true;
            }
        });

        setContentView(webView);

        webView.loadUrl(
                "file:///android_asset/index.html"
        );
    }
}
