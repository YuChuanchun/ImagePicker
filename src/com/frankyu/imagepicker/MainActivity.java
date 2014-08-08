package com.frankyu.imagepicker;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	private static final int FILE_SELECT_CODE = 1;
	private static final int REQUEST_CODE_WEB = 2;
	private String mFilePath = "";
	private TextView mFilePathTxt;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mFilePathTxt = (TextView) findViewById(R.id.file_name);
		String path = Utils.getFILE_PATH(this);
		if (!TextUtils.isEmpty(path)) {
			mFilePathTxt.setText(path);
			mFilePath = path;
			UrlManager.getInstance(MainActivity.this).loadData(mFilePath);
		}
	}
	
	public void onViewClick(View v) {
		switch(v.getId()) {
		case R.id.btn:
			if (Utils.checkSDCard(this))
				showFileChooser();
			break;
		case R.id.start:
			if (!Utils.checkSDCard(this)) return;
			if(TextUtils.isEmpty(mFilePath)) {
				Toast.makeText(this, "请先按‘选择’按钮，选择文件", Toast.LENGTH_LONG).show();
				break;
			}
			if (!Utils.checkNetworkAailable(this)) return;
			Intent intent = new Intent(MainActivity.this, WebActivity.class);
			startActivityForResult(intent, REQUEST_CODE_WEB);
			break;
		}
	}
	
	/** 调用文件选择软件来选择文件 **/
	private void showFileChooser() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("*/*.txt");
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		try {
			startActivityForResult(Intent.createChooser(intent, "请选择一个要上传的文件"),
					FILE_SELECT_CODE);
		} catch (android.content.ActivityNotFoundException ex) {
			// Potentially direct the user to the Market with a Dialog
			Toast.makeText(this, "请安装文件管理器", Toast.LENGTH_SHORT).show();
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			if (requestCode == FILE_SELECT_CODE) {
				Uri uri = data.getData();
				mFilePath = uri.getPath();
				if (!TextUtils.isEmpty(mFilePath)) {
					mFilePathTxt.setText(mFilePath);
					Utils.setFILE_PATH(this, mFilePath);
				}
				UrlManager.getInstance(MainActivity.this).loadData(mFilePath);
			} else if (requestCode == REQUEST_CODE_WEB) {
				
			}
		}
	}

}
