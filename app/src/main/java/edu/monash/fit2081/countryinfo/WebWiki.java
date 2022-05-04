package edu.monash.fit2081.countryinfo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;

public class WebWiki extends AppCompatActivity {
    WebView myWebView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myWebView = new WebView(this);
        setContentView(R.layout.activity_web_wiki);
        Intent intent = getIntent();
        Bundle args = intent.getBundleExtra("BUNDLE");
        String string = args.getString("COUNTRYNAME");
        myWebView.loadUrl("https://en.wikipedia.org/wiki/" + string);
    }
}
