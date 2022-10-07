package com.utility.finmartcontact.webview;



import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.MimeTypeMap;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.utility.finmartcontact.BaseActivity;
import com.utility.finmartcontact.R;
import com.utility.finmartcontact.core.controller.facade.ApplicationPersistance;
import com.utility.finmartcontact.core.response.LoginResponseEntity;
import com.utility.finmartcontact.home.HomeActivity;
import com.utility.finmartcontact.utility.Constant;


import java.io.File;

public class CommonWebViewActivity extends AppCompatActivity {

    WebView webView;
    String url = "";
    String name = "";
    String title = "";
    String dashBoardtype = "";
    ProgressDialog dialog;

    CountDownTimer countDownTimer;
    public static boolean isActive = false;
    Toolbar toolbar;

    ApplicationPersistance applicationPersistance;
    //endregion



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common_web_view);
        webView = (WebView) findViewById(R.id.webView1);



        url = getIntent().getStringExtra("URL");
        name = getIntent().getStringExtra("NAME");
        title = getIntent().getStringExtra("TITLE");
        applicationPersistance = new ApplicationPersistance(CommonWebViewActivity.this);


        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(title);

      //  webView.loadUrl(url);

        Log.d(Constant.TAG,"FBAID is "+ applicationPersistance.getFBAID() +"and ssid "+applicationPersistance.getSSID() +
                "Parent ID is "+applicationPersistance.getParentID());



        if (isNetworkConnected()) {
            settingWebview();
            startCountDownTimer();
        } else{
            Toast.makeText(this, "Check your internet connection", Toast.LENGTH_SHORT).show();
        }

    }

    private void navigateToHome(){

        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("MarkTYPE", "FROM_HOME");

        startActivity(intent);
        finish();
    }
    private void startCountDownTimer() {
        countDownTimer = new CountDownTimer(30000, 1000) {

            public void onTick(long millisUntilFinished) {
                //mTextField.setText("seconds remaining: " + millisUntilFinished / 1000);
                //here you can have your logic to set text to edittext
            }

            public void onFinish() {
                try {
                    cancelDialog();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        };
        countDownTimer.start();
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    private void downloadPdf(String url, String name) {
        Toast.makeText(this, "Download started..", Toast.LENGTH_LONG).show();
        DownloadManager.Request r = new DownloadManager.Request(Uri.parse(url));
        r.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, name + ".pdf");
        r.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        r.setMimeType(MimeTypeMap.getFileExtensionFromUrl(url));
        DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        dm.enqueue(r);
    }

    private void settingWebview() {

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);

        settings.setBuiltInZoomControls(true);
        settings.setUseWideViewPort(false);
        settings.setSupportMultipleWindows(false);

        settings.setLoadsImagesAutomatically(true);
        settings.setLightTouchEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setLoadWithOverviewMode(true);


      /*  MyWebViewClient webViewClient = new MyWebViewClient(this);
        webView.setWebViewClient(webViewClient);*/
        webView.setWebViewClient(new WebViewClient() {


            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                // TODO show you progress image
                if (isActive)
                    showDialog("");
                // new ProgressAsync().execute();
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                // TODO hide your progress image
                cancelDialog();
                super.onPageFinished(view, url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                //whatsapp plugin call.. via WEB
//                if (url != null && url.startsWith("whatsapp://")) {
//                    view.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
//                    return true;
//                } else
                if (url.endsWith(".pdf")) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.parse(url), "application/pdf");
                    try {
                        startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        //user does not have a pdf viewer installed
                        String googleDocs = "https://docs.google.com/viewer?url=";
                        webView.loadUrl(googleDocs + url);
                    }
                }

                /*qacamp@gmail.com/01011980
                download policy QA user
                878769 crn
                */
                return false;
            }
        });
        webView.getSettings().setBuiltInZoomControls(true);
        webView.addJavascriptInterface(new MyJavaScriptInterface(), "Android");
        webView.addJavascriptInterface(new PaymentInterface(), "PaymentInterface");
        // webView.setWebChromeClient(new WebChromeClient();
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                //Required functionality here
                return super.onJsAlert(view, url, message, result);
            }
        });
        // webView.setWebChromeClient(new WebChromeClient());
        Log.d("URL", url);

        if (url.endsWith(".pdf")) {

            webView.loadUrl("https://docs.google.com/viewer?url=" + url);
            //webView.loadUrl("http://drive.google.com/viewerng/viewer?embedded=true&url=" + url);
        } else {
            webView.loadUrl(url);
        }
        //webView.loadUrl(url);
    }




    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:

                    Log.i("BACK", "Back Triggered");
                    if (webView.canGoBack()) {
                        webView.goBack();
                    } else {

                            this.finish();



                    }
                    return true;
            }

        }
        return super.onKeyDown(keyCode, event);
    }


    class PaymentInterface {
        @JavascriptInterface
        public void success(String data) {
        }

        @JavascriptInterface
        public void error(String data) {
        }
    }

    class MyJavaScriptInterface {

        @JavascriptInterface
        public void crossselltitle(String dynamicTitle) {

            getSupportActionBar().setTitle(dynamicTitle);

        }

        // region Raise Ticket Note :Below Method  Upload_doc and Upload_doc_view Called For Activity Not For Dialog
        // For Dialog We have Used "Base Activity" JavaScript (All Insurance Popup Coming from there because it will already open from CommonWebView)
        // In Menu Raise Tickets Activity :Upload_doc and Upload_doc_view comming From Below code since its Activity Page.


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:


                    finish();


                return true;

            case R.id.action_home:

                navigateToHome();
                return true;


            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

            getMenuInflater().inflate(R.menu.home_menu, menu);


        return true;
   }

    void showDialog(String msg) {
        if (dialog == null)
            dialog = ProgressDialog.show(CommonWebViewActivity.this, "", msg, true);
        else {
            if (!dialog.isShowing())
                dialog = ProgressDialog.show(CommonWebViewActivity.this, "", msg, true);

        }
    }
    protected void cancelDialog() {
        if (dialog != null) {
            dialog.dismiss();
        }
    }


}
