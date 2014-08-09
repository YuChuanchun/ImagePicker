package com.frankyu.imagepicker;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Picture;
import android.net.http.SslError;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.webkit.HttpAuthHandler;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

public class WebActivity extends Activity {
	private static final String TAG = WebActivity.class.getSimpleName();
	private WebView mWebView;
	private int mPosition = 0;
	private int mFailedPosition = 0;
	private ArrayList<String> mUrls = new ArrayList<String>();
	private ArrayList<Integer> mTmpFailedUrlPositions = new ArrayList<Integer>();
	private ArrayList<Integer> mFailedUrlPositions = new ArrayList<Integer>();
	private TextView mTxt, mBtn;
	private long mUseTime;
	private long mStartTime;
	private boolean mPause = false, mStop = false;;
	private String mPromptStr;
	private View mSaveView;
	private boolean canClick;
	private long showNotifStartTime = 0;
	private boolean loadUrlFinish = false;
	private boolean loadingFailedUrls = false;
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch(msg.what) {
			case 0:
				Bitmap bm = captureWebViewVisibleSize();
				new SaveImageTask(bm).execute();
				break;
			case 1:
				loadUrl();
				break;
			case 2:
				showCompleteDialog();
				break;
			}
		}
		
	};

	@Override	
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_web);

		//保持屏幕唤醒
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		mWebView = (WebView) findViewById(R.id.webview);
//		mWebView.getSettings().setBuiltInZoomControls(true);
//		mWebView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.getSettings().setLoadWithOverviewMode(true);
		mWebView.getSettings().setUseWideViewPort(true);

		mWebView.setWebChromeClient(new WebChromeClient() {
			public void onProgressChanged(WebView view, int progress) {
				Log.d(TAG, "WebView onProgressChanged: " + progress);
			}
		});
		mWebView.setWebViewClient(new WebViewClient() {
			@Override
			public void onReceivedHttpAuthRequest(WebView view,
					HttpAuthHandler handler, String host, String realm) {
				// TODO Auto-generated method stub
				super.onReceivedHttpAuthRequest(view, handler, host, realm);
				Log.d(TAG, "WebView onReceivedHttpAuthRequest");
			}

			@Override
			public void onReceivedSslError(WebView view,
					SslErrorHandler handler, SslError error) {
				// TODO Auto-generated method stub
				super.onReceivedSslError(view, handler, error);
				Log.d(TAG, "WebView onReceivedSslError");
			}

			public void onReceivedError(WebView view, int errorCode,
					String description, String failingUrl) {
				Log.d(TAG, "WebView onReceivedError: " + description);
			}
		});

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
				return true;
			}

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				super.onPageStarted(view, url, favicon);
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				if (!url.equals("www.baidu.com")) {
					loadUrlFinish = true;
				}
				Log.d(TAG, "onPageFinished: " + url);
			}
			
		});
		
		loadUrl();
		new Thread() {
			public void run() {
				int count = 0;
				while (!mStop) {
					try {
						Thread.sleep(1000);
						if (loadUrlFinish && !mPause) {
							loadUrlFinish = false;
							count = 0;
							mHandler.sendEmptyMessage(0);
						}
						if (!mPause) count++;
						if (count > 20) {
							mTmpFailedUrlPositions.add(mPosition);
							Log.d(TAG, "Add to mTmpFailedUrlPositions: " + mPosition);
							count = 0;
							if (mPosition < mUrls.size() - 1) {
								mPosition = mPosition + 1;
								mHandler.sendEmptyMessage(1);
							} else {
								mHandler.sendEmptyMessage(2);
							}
						}
						Log.d(TAG, "loadFinish: " + loadUrlFinish + "mPause:" + mPause);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}.start();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		Log.d(TAG, "onPause");
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Log.d(TAG, "onResume");
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		mStop = true;
	}

	private void loadFailedUrls() {
		loadingFailedUrls = true;
		mFailedUrlPositions.clear();
		mFailedUrlPositions.addAll(mTmpFailedUrlPositions);
		mTmpFailedUrlPositions.clear();
		mFailedPosition = 0;
		loadUrl();
		setPromptString((mFailedPosition + 1) + "/" + mFailedUrlPositions.size());
		new Thread() {
			public void run() {
				int count = 0;
				mStop = false;
				while (!mStop) {
					try {
						Thread.sleep(1000);
						if (loadUrlFinish && !mPause) {
							loadUrlFinish = false;
							count = 0;
							mHandler.sendEmptyMessage(0);
						}
						if (!mPause) count++;
						if (count > 20) {
							mTmpFailedUrlPositions.add(mFailedUrlPositions.get(mFailedPosition));
							count = 0;
							if (mFailedPosition < mFailedUrlPositions.size() - 1) {
								mFailedPosition = mFailedPosition + 1;
								mHandler.sendEmptyMessage(1);
							} else {
								mHandler.sendEmptyMessage(2);
							}
						}
						Log.d(TAG, "loadFinish: " + loadUrlFinish + "mPause:" + mPause);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}.start();
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
		loadUrlFinish = false;
		mStartTime = System.currentTimeMillis();
		if (!loadingFailedUrls) {
			mWebView.loadUrl(mUrls.get(mPosition));
			Log.d(TAG, "loading url: " + mUrls.get(mPosition));
		} else {
			mWebView.loadUrl(mUrls.get(mFailedUrlPositions.get(mFailedPosition)));
			Log.d(TAG, "loading fialed url: " + mUrls.get(mFailedUrlPositions.get(mFailedPosition)));
		}
	}
	
	private Bitmap takeScreenShot() {
		View view = this.getWindow().getDecorView();
		view.setDrawingCacheEnabled(true);
		view.buildDrawingCache();
		return view.getDrawingCache();
	}

	private void setPromptString(String txt) {
		String time = "";
		if (!loadingFailedUrls)
			time = (mUseTime * (mUrls.size() - mPosition) / (mPosition + 1)) / 1000 / 60 + "分钟";
		else
			time = (mUseTime * (mFailedUrlPositions.size() - mFailedPosition) / (mFailedPosition + 1)) / 1000 / 60 + "分钟";
		mPromptStr = txt +  "  预计还有" + time + "完成";
		mTxt.setText(mPromptStr);
		long curTime = System.currentTimeMillis();
		if (showNotifStartTime == 0 || curTime - showNotifStartTime > Const.NOTIFICATION_INTERVAL) {
			showNotifStartTime = curTime;
			Utils.showNotification(this, mPromptStr);
		}
	}

	/**
	 * 截取webView快照(webView加载的整个内容的大小)
	 * @param webView
	 * @return
	 */
	private Bitmap captureWebView(){
		Picture snapShot = mWebView.capturePicture();
		Bitmap bmp = Bitmap.createBitmap(snapShot.getWidth(), snapShot.getHeight(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bmp);
		snapShot.draw(canvas);
		int h = snapShot.getWidth() * mWebView.getHeight() / mWebView.getWidth();
		return Bitmap.createBitmap(bmp, 0, 0, snapShot.getWidth(), h);
	}
	
    /**
     * 截取webView可视区域的截图
     * @param webView 前提：WebView要设置webView.setDrawingCacheEnabled(true);
     * @return
     */
	private Bitmap captureWebViewVisibleSize(){
//		mWebView.setDrawingCacheEnabled(true);
//		mWebView.buildDrawingCache();
//		Bitmap bmp = mWebView.getDrawingCache();

		Bitmap bmp = Bitmap.createBitmap(mWebView.getWidth(), mWebView.getHeight(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bmp);
		mWebView.draw(canvas);
		return bmp;
	}
	
	public void saveBitmap(Bitmap bm) throws IOException {
		File folder = new File(Const.SAVE_PATH);
		if (!folder.exists()) folder.mkdir();
		int position = loadingFailedUrls ? mFailedUrlPositions.get(mFailedPosition) : mPosition;
        File f = new File(Const.SAVE_PATH + (position + 1) + Const.IMAGE_SUFFIX);

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

	private void showCompleteDialog() {
		mStop = true;
		if (mTmpFailedUrlPositions.size() <= 0) {
			loadingFailedUrls = false;
			new AlertDialog.Builder(this)
				.setTitle("任务全部完成")
				.setMessage("请在sd卡下imagepicker文件夹中查看结果。")
				.setCancelable(false)
				.setPositiveButton("确定", new OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO Auto-generated method stub
						finish();
					}
				})
				.create()
				.show();
		} else {
			new AlertDialog.Builder(this)
			.setTitle("任务未全部完成")
			.setMessage("您有" + mTmpFailedUrlPositions.size() + "条数据加载失败或者超时，是否重新加载失败项？")
			.setCancelable(false)
			.setPositiveButton("重新加载失败项", new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					// TODO Auto-generated method stub
					loadFailedUrls();
				}
			})
			.setNegativeButton("退出", new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					// TODO Auto-generated method stub
					finish();
				}
			})
			.create()
			.show();
		}
	}

	private class SaveImageTask extends AsyncTask<Void, Void, Bitmap> {
//		private ProgressDialog dialog;
		Bitmap bm;
		public SaveImageTask(Bitmap bm) {
			this.bm = bm;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
//			dialog = new ProgressDialog(WebActivity.this);
//			dialog.setMessage("正在保存图片...");
//			dialog.show();
//			mSaveView.setVisibility(View.VISIBLE);
		}
		
		@Override
		protected Bitmap doInBackground(Void... arg0) {
			// TODO Auto-generated method stub
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
				mUseTime += System.currentTimeMillis() - mStartTime;
				if (!loadingFailedUrls) {
					setPromptString((mPosition + 1) + "/" + mUrls.size());
					if (mPosition < mUrls.size() - 1) {
						mPosition = mPosition + 1;
						loadUrl();
					}
					if (mPosition == mUrls.size() - 1) {
						showCompleteDialog();
					}
				} else {
					setPromptString((mFailedPosition + 1) + "/" + mFailedUrlPositions.size());
					if (mFailedPosition < mFailedUrlPositions.size() - 1) {
						mFailedPosition = mFailedPosition + 1;
						loadUrl();
					}
					if (mFailedPosition == mFailedUrlPositions.size() - 1) {
						showCompleteDialog();
					}
				}
			}
		}
	}
}