package com.frankyu.imagepicker;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Picture;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

public class WebActivity extends Activity {
	private WebView mWebView;
	private int mPosition = 0;
	private ArrayList<String> mUrls = new ArrayList<String>();
	private TextView mTxt, mBtn;
	private long mUseTime;
	private long mStartTime;
	private boolean mPause = false;
	private String mPromptStr;
	private View mSaveView;
	private boolean canClick;

	@Override	
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_web);
		mWebView = (WebView) findViewById(R.id.webview);
		mWebView.getSettings().setBuiltInZoomControls(true);
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
		mWebView.getSettings().setLoadWithOverviewMode(true);
		mWebView.getSettings().setUseWideViewPort(true);   
		mWebView.setWebChromeClient(new WebChromeClient());

		mTxt = (TextView) findViewById(R.id.txt);
		mBtn = (TextView) findViewById(R.id.btn);
		mSaveView = findViewById(R.id.save);
		mBtn.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View arg0, MotionEvent event) {
				// TODO Auto-generated method stub
	            int mWidth = mBtn.getWidth();
	            int mHeight = mBtn.getHeight();
		        switch(event.getAction()) {
		        case MotionEvent.ACTION_DOWN:
		            canClick = true;
		            mBtn.setTextColor(Color.parseColor("#4d4d4d"));
		            break;
		        case MotionEvent.ACTION_UP:
		            mBtn.setTextColor(Color.parseColor("#ffffff"));
		            if (canClick) mBtn.performClick();
		            break;
		        case MotionEvent.ACTION_MOVE:
		            if (event.getX() < 0 || event.getX() > mWidth) {
		                canClick = false;
			            mBtn.setTextColor(Color.parseColor("#4d4d4d"));
		            }
		            if (event.getY() < 0 || event.getY() > mHeight) {
		                canClick = false;
			            mBtn.setTextColor(Color.parseColor("#4d4d4d"));
		            }
		            break;
		        case MotionEvent.ACTION_CANCEL:
		        case MotionEvent.ACTION_OUTSIDE:
		            mBtn.setTextColor(Color.parseColor("#4d4d4d"));
		            break;
		        }
		        return true;
			}
		});
		mUrls = UrlManager.getInstance(this).getUrls();

		mWebView.setWebViewClient(new WebViewClient(){

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				if (url.contains("http")) {
					view.loadUrl(url);
				} else {
					if (url.startsWith("tel")) {
						Uri uri = Uri.parse(url);
						Intent intent = new Intent(Intent.ACTION_DIAL, uri);
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						startActivity(intent);
					}
				}
				return true;
			}

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				super.onPageStarted(view, url, favicon);
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				if (!WebActivity.this.isFinishing()) {
					new SaveImageTask().execute();
				}
			}
			
		});
		
		loadUrl();
	}
	
	public void onViewClick(View v) {
		switch(v.getId()) {
		case R.id.btn:
			mPause = !mPause;
			mBtn.setText(mPause ? "继续" : "暂停");
			if (!mPause) loadUrl();
			break;
		}
	}
	
	private void loadUrl() {
		if (mPause) return;
		mStartTime = System.currentTimeMillis();
		mWebView.loadUrl(mUrls.get(mPosition));
	}
	
	private Bitmap takeScreenShot() {
		View view = this.getWindow().getDecorView();
		view.setDrawingCacheEnabled(true);
		view.buildDrawingCache();
		return view.getDrawingCache();
	}

	private void setPromptString(String txt) {
		String time = (mUseTime * (mUrls.size() - mPosition) / (mPosition + 1)) / 1000 / 60 + "分钟";
		mPromptStr = txt +  "  预计还有" + time + "完成";
		mTxt.setText(mPromptStr);
	}

	/**
	 * 截取webView快照(webView加载的整个内容的大小)
	 * @param webView
	 * @return
	 */
	private Bitmap captureWebView(){
		Picture snapShot = mWebView.capturePicture();
		
		Bitmap bmp = Bitmap.createBitmap(snapShot.getWidth(),snapShot.getHeight(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bmp);
		snapShot.draw(canvas);
		return bmp;
	}
	
    /**
     * 截取webView可视区域的截图
     * @param webView 前提：WebView要设置webView.setDrawingCacheEnabled(true);
     * @return
     */
	private Bitmap captureWebViewVisibleSize(){
		mWebView.setDrawingCacheEnabled(true);
		mWebView.buildDrawingCache();
		Bitmap bmp = mWebView.getDrawingCache();
		return bmp;
	}
	
	public void saveBitmap(Bitmap bm) throws IOException {
		File folder = new File(Const.SAVE_PATH);
		if (!folder.exists()) folder.mkdir();
        File f = new File(Const.SAVE_PATH + (mPosition + 1) + Const.IMAGE_SUFFIX);

        FileOutputStream fos = null;
        try {
        	f.createNewFile();
            fos= new FileOutputStream(f);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            if (fos != null) fos.close();
        }
        bm.compress(Bitmap.CompressFormat.PNG, 100, fos);
        try {
        	fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
	
	private class SaveImageTask extends AsyncTask<Void, Void, Bitmap> {
//		private ProgressDialog dialog;
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
//			dialog = new ProgressDialog(WebActivity.this);
//			dialog.setMessage("正在保存图片...");
//			dialog.show();
			mSaveView.setVisibility(View.VISIBLE);
		}
		
		@Override
		protected Bitmap doInBackground(Void... arg0) {
			// TODO Auto-generated method stub
			Bitmap bm = captureWebView();
			try {
				saveBitmap(bm);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Bitmap result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			if (!WebActivity.this.isFinishing()) {
//				dialog.dismiss();
				mSaveView.setVisibility(View.INVISIBLE);
				setPromptString((mPosition + 1) + "/" + mUrls.size());
				mUseTime += System.currentTimeMillis() - mStartTime;
				if (mUrls != null && mPosition < mUrls.size() - 1) {
					mPosition = mPosition + 1;
					loadUrl();
				}
			}
		}
	}
}