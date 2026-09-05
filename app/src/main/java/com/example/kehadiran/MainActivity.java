package com.example.kehadiran;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends Activity {

    private WebView webView;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        webView = new WebView(this);

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
                                (dialog, which) -> result.cancel())
                        .setPositiveButton("PADAM",
                                (dialog, which) -> result.confirm())
                        .setOnCancelListener(
                                dialog -> result.cancel())
                        .show();

                return true;
            }
        });

        // Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // JavaScript Bridge
        webView.addJavascriptInterface(
                new WebAppBridge(),
                "Android"
        );

        setContentView(webView);

        // Load halaman dahulu
webView.loadUrl(
    "file:///android_asset/index.html"
);
    }

    private void signInFirebase() {

        if (auth.getCurrentUser() != null) {
            notifyJavascript(
                    "window.onFirebaseReady && window.onFirebaseReady(true)"
            );
            return;
        }

        auth.signInAnonymously()
                .addOnSuccessListener(authResult -> {

                    notifyJavascript(
                            "window.onFirebaseReady && window.onFirebaseReady(true)"
                    );

                })
                .addOnFailureListener(e -> {

                    String message = e.getMessage();

                    if (message == null) {
                        message = "Tidak dapat menyambung ke Firebase.";
                    }

                    notifyJavascript(
                            "window.onFirebaseReady && window.onFirebaseReady(false," +
                                    JSONObject.quote(message) +
                                    ")"
                    );
                });
    }

    private void notifyJavascript(String javascript) {

        runOnUiThread(() -> {

            if (webView != null) {
                webView.evaluateJavascript(
                        javascript,
                        null
                );
            }
        });
    }

    public class WebAppBridge {

        @JavascriptInterface
        public boolean isFirebaseReady() {

            return auth != null &&
                    auth.getCurrentUser() != null;
        }

        @JavascriptInterface
        public String getUserId() {

            if (auth != null &&
                    auth.getCurrentUser() != null) {

                return auth.getCurrentUser().getUid();
            }

            return "";
        }

        @JavascriptInterface
        public void saveData(String jsonData) {

            if (auth == null ||
                    auth.getCurrentUser() == null) {

                notifyJavascript(
                        "window.onCloudSave && window.onCloudSave(false," +
                                JSONObject.quote(
                                        "Firebase belum bersedia."
                                ) +
                                ")"
                );

                return;
            }

            String uid =
                    auth.getCurrentUser().getUid();

            Map<String, Object> data =
                    new HashMap<>();

            data.put("data", jsonData);
            data.put(
                    "updatedAt",
                    FieldValue.serverTimestamp()
            );

            db.collection("users")
                    .document(uid)
                    .collection("schoolData")
                    .document("main")
                    .set(data)
                    .addOnSuccessListener(unused -> {

                        notifyJavascript(
                                "window.onCloudSave && window.onCloudSave(true," +
                                        JSONObject.quote(
                                                "Data berjaya disimpan ke cloud."
                                        ) +
                                        ")"
                        );

                    })
                    .addOnFailureListener(e -> {

                        String message = e.getMessage();

                        if (message == null) {
                            message = "Gagal menyimpan data.";
                        }

                        notifyJavascript(
                                "window.onCloudSave && window.onCloudSave(false," +
                                        JSONObject.quote(message) +
                                        ")"
                        );
                    });
        }

        @JavascriptInterface
        public void loadData() {

            if (auth == null ||
                    auth.getCurrentUser() == null) {

                notifyJavascript(
                        "window.onCloudLoad && window.onCloudLoad(false," +
                                JSONObject.quote(
                                        "Firebase belum bersedia."
                                ) +
                                ")"
                );

                return;
            }

            String uid =
                    auth.getCurrentUser().getUid();

            db.collection("users")
                    .document(uid)
                    .collection("schoolData")
                    .document("main")
                    .get()
                    .addOnSuccessListener(
                            documentSnapshot -> {

                                if (documentSnapshot.exists()) {

                                    String jsonData =
                                            documentSnapshot.getString("data");

                                    if (jsonData == null) {
                                        jsonData = "";
                                    }

                                    notifyJavascript(
                                            "window.onCloudLoad && window.onCloudLoad(true," +
                                                    JSONObject.quote(jsonData) +
                                                    ")"
                                    );

                                } else {

                                    notifyJavascript(
                                            "window.onCloudLoad && window.onCloudLoad(true," +
                                                    JSONObject.quote("") +
                                                    ")"
                                    );
                                }
                            }
                    )
                    .addOnFailureListener(e -> {

                        String message = e.getMessage();

                        if (message == null) {
                            message = "Gagal mengambil data cloud.";
                        }

                        notifyJavascript(
                                "window.onCloudLoad && window.onCloudLoad(false," +
                                        JSONObject.quote(message) +
                                        ")"
                        );
                    });
        }
    }
}
