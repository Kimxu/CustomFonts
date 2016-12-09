package me.kimxu.texttype;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public class MainActivity extends AppCompatActivity {

    private WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mWebView = (WebView) findViewById(R.id.webView);
        mWebView.getSettings().setJavaScriptEnabled(true);
        //访问外网html
        mWebView.loadUrl("http://www.baidu.com");
        //访问assets中html
//        mWebView.loadUrl("file:///android_asset/test.html");


        WebSettings webSettings = mWebView.getSettings();
        webSettings.setDefaultTextEncodingName("utf-8");
        //获取系统中的字体
        InputStream systemFont = getSystemFont();
        final InputStream finalSystemFont = systemFont;
        mWebView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view,url);
                Log.w("loading","onPageFinished "+url);
                view.loadUrl("javascript:!function(){" +
                        "s=document.createElement('style');s.innerHTML="
                        + "\"@font-face{font-family:kimxuFont;src:url('**injection**/Oswald-Stencbab.ttf');}*{font-family:kimxuFont !important;}\";"
                        + "document.getElementsByTagName('head')[0].appendChild(s);" +
                        "document.getElementsByTagName('body')[0].style.fontFamily = \"kimxuFont\";}()");
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                WebResourceResponse response =  super.shouldInterceptRequest(view, url);
                Log.w("loading","shouldInterceptRequest "+url);
                if (url != null && url.contains("**injection**/")) {
                    String assertPath = url.substring(url.indexOf("**injection**/") + "**injection**/".length(), url.length());

                    try {
                        //指定使用assets中的字体
                        response = new WebResourceResponse("application/x-font-ttf",
                                "UTF8", getAssets().open("fonts/"+assertPath));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
//                    //使用系统字体
//                    response = new WebResourceResponse("application/x-font-ttf",
//                            "UTF8", finalSystemFont);
                }
                return response;
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.w("loading","shouldOverrideUrlLoading"+url);
                view.loadUrl(url);
                return true;
            }


        });

    }

    @Nullable
    private InputStream getSystemFont() {
        String path = "/system/fonts";
        File systemFonts = new File(path);
        File fonts[] = systemFonts.listFiles();
        File useFont =null;
        for (File font : fonts) {
            Log.i("File", font.getName());
            if (TextUtils.equals("DroidSansEthiopic-Regular.ttf",font.getName())){
                useFont=font;
                break;
            }
        }
        InputStream fontInput;
        try {
            fontInput = new FileInputStream(useFont);
        } catch (FileNotFoundException e) {
            Log.i("Error",e.getLocalizedMessage());
            return null;
        }
        return fontInput;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
