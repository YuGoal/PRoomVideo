package com.bsu.promevideo;

import android.os.Bundle;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;

public class MainActivity extends Activity {

	// 接收home键的系统监听广播
	// private final BroadcastReceiver homePressReceiver = new
	// BroadcastReceiver() {
	// final String SYSTEM_DIALOG_REASON_KEY = "reason";
	// final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";
	//
	// @Override
	// public void onReceive(Context context, Intent intent) {
	// String action = intent.getAction();
	// if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
	// String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
	// if (reason != null
	// && reason.equals(SYSTEM_DIALOG_REASON_HOME_KEY)) {
	// // 自己随意控制程序，关闭...
	// System.out.println("catch home");
	// }
	// }
	// }
	// };

	// @Override
	// public boolean onKeyDown(int keyCode, KeyEvent event){
	// if(KeyEvent.KEYCODE_HOME==keyCode)
	// android.os.Process.killProcess(android.os.Process.myPid());
	// return super.onKeyDown(keyCode, event);
	// }

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// 注册处理home键的广播
		// final IntentFilter homeFilter = new IntentFilter(
		// Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
		// registerReceiver(homePressReceiver, homeFilter);

		Button bt_start = (Button) this.findViewById(R.id.bt_start);
		bt_start.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this,
						TimerActivity.class);
				MainActivity.this.startActivity(intent);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			return true;
		}

		if (KeyEvent.KEYCODE_HOME == keyCode)
			return true;
//			android.os.Process.killProcess(android.os.Process.myPid());

		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onAttachedToWindow() {
//		super.onAttachedToWindow();
		this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		// 屏蔽home键反注册
		// if (homePressReceiver != null) {
		// try {
		// unregisterReceiver(homePressReceiver);
		// } catch (Exception e) {
		// Log.e("","unregisterReceiver homePressReceiver failure :"+
		// e.getCause());
		// }
		// }
	}

}
