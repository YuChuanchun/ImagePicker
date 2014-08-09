package com.frankyu.imagepicker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.widget.Toast;

public class UrlManager {
	private static UrlManager INSTANCE;
	private ArrayList<String> mUrls = new ArrayList<String>();
	private String mFilePath;
	private Context mContext;

	private UrlManager(Context context) {
		mContext = context;
	}

	public static UrlManager getInstance(Context context) {
		if (INSTANCE == null) {
			INSTANCE = new UrlManager(context);
		}
		return INSTANCE;
	}

	public ArrayList<String> getUrls() {
		return mUrls;
	}

	public void loadData(String path) {
		mFilePath = path;
		mUrls.clear();
		new GetUrlTask().execute();
	}

	private class GetUrlTask extends AsyncTask<Void, Void, Boolean> {
		private ProgressDialog dialog;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialog = new ProgressDialog(mContext);
			dialog.setMessage("请稍等...");
			dialog.show();
		}

		@Override
		protected Boolean doInBackground(Void... arg0) {
			// TODO Auto-generated method stub
			if (TextUtils.isEmpty(mFilePath)) {
				Toast.makeText(mContext, "文件路径是空的", Toast.LENGTH_SHORT).show();
				return false;
			}

			try {
				String encoding = "GBK";
				File file = new File(mFilePath);
				if (file.isFile() && file.exists()) { // 判断文件是否存在
					InputStreamReader read = new InputStreamReader(
							new FileInputStream(file), encoding);// 考虑到编码格式
					BufferedReader bufferedReader = new BufferedReader(read);
					String lineTxt = null;
					while ((lineTxt = bufferedReader.readLine()) != null) {
						mUrls.add(lineTxt);
					}
					read.close();
				} else {
					Toast.makeText(mContext, "找不到指定的文件", Toast.LENGTH_SHORT).show();
				}
			} catch (Exception e) {
				Toast.makeText(mContext, "读取文件内容出错", Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			}
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			dialog.dismiss();
		}
	}
}