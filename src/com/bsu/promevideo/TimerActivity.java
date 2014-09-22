package com.bsu.promevideo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class TimerActivity extends Activity {
	public long c_nTime; // 要显示的时间
	public long init_timer; // 开始计时后开始的时间
	public long remain_time; // 剩余的时间
	public TextView tv_timer = null;

	private TimerHandler timerHandler;
	private Timer timer;

	private ListView lv_message;
	private List<Map<String, Object>> list;
	private SimpleAdapter sa;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_timer);
		initTimer(); 
		initMessage(); 
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		// getMenuInflater().inflate(R.menu.timer, menu);
		return true;
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * Timer��Handler
	 * 
	 * @author fengchong
	 * 
	 */
	public class TimerHandler extends Handler {
		private TimerActivity me;

		public TimerHandler(TimerActivity m) {
			me = m;
		}

		@Override
		public void handleMessage(Message msg) {

			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
					"00:mm:ss:SSS");

			String updateCurrentTime = simpleDateFormat.format(new Date(
					me.c_nTime));

			me.tv_timer.setText(updateCurrentTime);
		}
	}

	private int sendid = 2;								//���͵���Ϣ��id
	
	/**
	 * 初始化倒计时
	 */
	private void initTimer() {
		remain_time = 1000 * 60 * 60; // ����Ĭ�ϳ�ʼʱ��Ϊ1��Сʱ

		tv_timer = (TextView) this.findViewById(R.id.tv_timer);

		timerHandler = new TimerHandler(this);

		timer = new Timer(true);
		timer.schedule(new TimerTask() {
			{
				long datetime = System.currentTimeMillis(); // ��õ�ǰʱ��Ϊ��ʼ����ʱ��
				init_timer = datetime;
			};

			@Override
			public void run() {
				long current_timer = System.currentTimeMillis();
				c_nTime = remain_time - (current_timer - init_timer);

				if(c_nTime<=0){
					c_nTime=0;
					return;
				}
				
				if(sendid== 2 && c_nTime<=3590000 && c_nTime>3580000){
					Map map = new HashMap<String,Object>();
					map.put("content", "视频2");
					map.put("image", R.drawable.msg);
					list.add(map);
					sendid++;
				}else if(sendid==3 && c_nTime<=3580000 && c_nTime>3570000){
					Map map = new HashMap<String,Object>();
					map.put("content", "视频3");
					map.put("image", R.drawable.msg);
					list.add(map);
					sendid++;
				}else if(sendid==4 && c_nTime<=3570000 && c_nTime>3560000){
					Map map = new HashMap<String,Object>();
					map.put("content", "视频4");
					map.put("image", R.drawable.msg);
					list.add(map);
					sendid++;
				}
				
				TimerActivity.this.timerHandler.sendEmptyMessage(0);
				
			}
		}, 0, 10);
	}

//	private void updateReadMessage(){
//		int size = list.size();
//		for(int i=0;i<size;i++){
//			View v = (View) lv_message.getChildAt(i);
//			TextView tv = (TextView) v.findViewById(R.id.item_content);
//			if(tv==null)
//				continue;
//			if((Boolean) list.get(i).get("read"))
//				tv.setTextColor(Color.RED);
//			else
//				tv.setTextColor(Color.BLACK);
//		}
//	}
	private String vpath;
	/**
	 * 初始化消息列表
	 */
	private void initMessage() {
		lv_message = (ListView) findViewById(R.id.lv_message);

		list = new ArrayList<Map<String, Object>>();
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("content", "视频1");
		map.put("image", R.drawable.msg);
		list.add(map);
		
//		map = new HashMap<String,Object>();
//		map.put("content", "视频2");
//		map.put("image", R.drawable.ic_launcher);
//		list.add(map);
//
//		map = new HashMap<String,Object>();
//		map.put("content", "视频3");
//		map.put("image", R.drawable.ic_launcher);
//		list.add(map);
//		
//		map = new HashMap<String,Object>();
//		map.put("content", "视频4");
//		map.put("image", R.drawable.ic_launcher);
//		list.add(map);
		
		sa = new SimpleAdapter(this,list,R.layout.listitem
				,new String[]{"content","image"}
				,new int[]{R.id.item_content,R.id.item_icon});
		
		lv_message.setAdapter(sa);
		vpath = "android.resource://com.bsu.promevideo/";
		
		lv_message.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> l, View v, int position,long id) {
				Intent intent = new Intent(TimerActivity.this,VideoActivity.class);
				intent.putExtra("content",list.get(position).get("content").toString());
				
				switch(position){
				case 0:
					intent.putExtra("vpath", vpath+R.raw.v001);
					break;
				case 1:
					intent.putExtra("vpath", vpath+R.raw.v002);
					break;
				case 2:
					intent.putExtra("vpath", vpath+R.raw.v002);
					break;
				case 3:
					intent.putExtra("vpath", vpath+R.raw.v002);
					break;
				default:
					
					break;
				}
				TimerActivity.this.startActivity(intent);
			}});
		
	}
}
