package com.bsu.promevideo;

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.view.Menu;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

public class VideoActivity extends Activity {
	private VideoView vv;
	private MediaController mc;
	private TextView tv;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_video);

		Intent intent = this.getIntent();
		String content = intent.getStringExtra("content");
		String vpath = intent.getStringExtra("vpath");
		Toast.makeText(this, content, Toast.LENGTH_SHORT).show();
		
		vv = (VideoView) findViewById(R.id.vv);
		mc = new MediaController(this);
		tv = (TextView) findViewById(R.id.tv_videotitle);
		tv.setText(content);
		
		vv.setMediaController(mc);
//		vv.setVideoPath(vpath);
		System.out.println(vpath);
		vv.setVideoURI(Uri.parse(vpath));
		vv.requestFocus();
		vv.start();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		// getMenuInflater().inflate(R.menu.video, menu);
		return true;
	}

}
