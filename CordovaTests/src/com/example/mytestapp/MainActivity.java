package com.example.mytestapp;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.cordova.Config;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CordovaWebViewClient;
import org.apache.cordova.DroidGap;
import org.apache.cordova.api.CordovaInterface;
import org.apache.cordova.api.CordovaPlugin;
import org.apache.cordova.api.LOG;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;

/**
 * 
 * Test app to investigate cordova communication between the js and java layer. Idea is that we are loading a remote web
 * page inside a CordovaWebView that accesses locally the cordova.android.js file deployed with the application. This is
 * easier said than done as for SDK < 11 it looks like there is no way to modify the webview response. So the code below
 * only works for SDK=11 and up.
 * 
 * The cordova-js is about 70K minified (the whole set) which is too much to send over a 3G connection. Thus deploying
 * with the application allows us to have the best of both worlds:
 * 
 * 1. Access from js to native features like connection , acelerometer or camera 2. Small payloads on the webview side
 * as we do not need to download the whole cordova framework on JS.
 * 
 * 
 */
public class MainActivity extends DroidGap implements CordovaInterface {

	private final ExecutorService threadPool = Executors.newCachedThreadPool();

	CordovaWebView cwv;
	CordovaWebView appView;
	AssetManager mngr ;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mngr = getAssets();

		Config.init(this);
		setContentView(R.layout.activity_main);
		cwv = (CordovaWebView) findViewById(R.id.mainView);
		super.appView = cwv;
		cwv.setWebViewClient(new TestWebViewClient(this,cwv));

		cwv.loadUrl("http://web2.nruiz.tuenti.local/page1.html");

	}


	@Override
	public void setActivityResultCallback(CordovaPlugin plugin) {
		this.activityResultCallback = plugin;
	}

	/**
	 *  Launch an activity for which you would like a result when it finished. When this activity exits, your
	 * onActivityResult() method will be called.  
	 * 
	 * @param command   The command object  
	 * @param intent    The intent to start  
	 * @param requestCode   The request code that is passed to callback to identify the activity  
	 */
	public void startActivityForResult(CordovaPlugin command, Intent intent, int requestCode) {
		this.activityResultCallback = command;
		this.activityResultKeepRunning = this.keepRunning;

		// If multitasking turned on, then disable it for activities that return results
		if (command != null) {
			this.keepRunning = false;
		}
		// Start activity
		super.startActivityForResult(intent, requestCode);
	}

	@Override
	/**
	 * Called when an activity you launched exits, giving you the requestCode you started it with,
	 * the resultCode it returned, and any additional data from it.
	 *
	 * @param requestCode       The request code originally supplied to startActivityForResult(),
	 *                          allowing you to identify who this result came from.
	 * @param resultCode        The integer result code returned by the child activity through its setResult().
	 * @param data              An Intent, which can return result data to the caller (various data can be attached to Intent "extras").
	 */
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);

		CordovaPlugin callback = this.activityResultCallback;
		if (callback != null) {
			callback.onActivityResult(requestCode, resultCode, intent);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public Activity getActivity() {
		return this;
	}

	@Override
	public ExecutorService getThreadPool() {
		return threadPool;
	}

	class TestWebViewClient extends CordovaWebViewClient {

		public TestWebViewClient(CordovaInterface cordova, CordovaWebView view) {
			super(cordova,view);
			// TODO Auto-generated constructor stub
		}

		public TestWebViewClient(CordovaInterface cordova) {
			super(cordova);
			// TODO Auto-generated constructor stub
		}
		@Override
		public void onPageStarted(WebView view, String url, Bitmap bitmap) {
			super.onPageStarted(view, url, bitmap);
			Log.i("TEST", "onPageStarted: " + url);
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			super.onPageFinished(view, url);
			Log.i("TEST", "onPageFinished: " + url);
		}


		public WebResourceResponse shouldInterceptRequest (WebView view, String url){
			LOG.d(" requested",url);

			if (url.endsWith("android.js")){

				try {
					Log.d("js",url);

					InputStream stream = MainActivity.this.mngr.open("cordova.android.js");
					WebResourceResponse response = new WebResourceResponse("text/javascript", "UTF-8", stream);
					return response;


				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return null;

		}
	}
}
