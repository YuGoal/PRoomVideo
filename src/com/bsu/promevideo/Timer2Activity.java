package com.bsu.promevideo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import com.bsu.promevideo.TimerActivity.TimerHandler;
import com.bsu.promevideo.tools.NFCActivityHelper;
import com.bsu.promevideo.tools.OnNFCReadListener;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
/**
 * 另一个计时界面，此处视频不随时间消耗增加，通过扫描来增加视频
 * @author fengchong
 *
 */
public class Timer2Activity extends Activity {
	public long c_nTime;		//要显示的时间
	public long init_timer;		//开始计时后开始的时间
	public long remain_time;	//剩余时间
	public TextView tv_timer = null;
	
	private TimerHandler timerHandler;
	private Timer timer;
	
	private ListView lv_message;
	private List<Map<String,Object>> list;
	private SimpleAdapter sa;
	
	private NFCActivityHelper nfch ;
	
	
	
	private NfcAdapter adapter;
	private PendingIntent pintent;
	private IntentFilter[] filters;
	private String[][] techLists;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_timer2);

		initTimer();			//初始化倒计时计时器
		initMessage();			//初始化消息部分
		
		//初始化设备
		adapter = NfcAdapter.getDefaultAdapter(this);
		//截获Intent,使用当前的Activity
		pintent = PendingIntent.getActivity(this, 0, new Intent(this,getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
		//初始化nfc数据帮助类
		nfch = new NFCActivityHelper(this,adapter,pintent);
		nfch.onCreate();
		//当读取到nfc数据时的操作
		nfch.setOnNFCReadListener(new OnNFCReadListener(){
			@Override
			public void read(String data) {
				if(data.equals("bk42-lr002")){
					Map map = new HashMap<String,Object>();
					map.put("id", data);
					map.put("content", "视频2");
					map.put("image", R.drawable.msg);
					list.add(map);
				}else if(data.equals("bk42-lr003")){
					Map map = new HashMap<String,Object>();
					map.put("id", data);
					map.put("content", "视频3");
					map.put("image", R.drawable.msg);
					list.add(map);
				}else if(data.equals("bk42-lr004")){
					Map map = new HashMap<String,Object>();
					map.put("id", data);
					map.put("content", "视频4");
					map.put("image", R.drawable.msg);
					list.add(map);
				}
			}});
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		nfch.onPause();
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		nfch.onResume();
	}
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		nfch.onNewIntent(intent);
	}
	
	/**
	 * Timer的Handler
	 * @author fengchong
	 */
	public class TimerHandler extends Handler {
		private Timer2Activity me;

		public TimerHandler(Timer2Activity m) {
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

	private int sendid = 2;				//发送视频的id								
	
	private void initTimer(){
		remain_time = 1000 * 60 * 60; 	//1小时的毫秒数

		tv_timer = (TextView) this.findViewById(R.id.tv_timer);

		timerHandler = new TimerHandler(this);

		timer = new Timer(true);
		timer.schedule(new TimerTask() {
			{
				long datetime = System.currentTimeMillis(); //获得当前时间的毫秒数
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
				
//				if(sendid== 2 && c_nTime<=3590000 && c_nTime>3580000){
//					Map map = new HashMap<String,Object>();
//					map.put("content", "视频2");
//					map.put("image", R.drawable.msg);
//					list.add(map);
//					sendid++;
//				}else if(sendid==3 && c_nTime<=3580000 && c_nTime>3570000){
//					Map map = new HashMap<String,Object>();
//					map.put("content", "视频3");
//					map.put("image", R.drawable.msg);
//					list.add(map);
//					sendid++;
//				}else if(sendid==4 && c_nTime<=3570000 && c_nTime>3560000){
//					Map map = new HashMap<String,Object>();
//					map.put("content", "视频4");
//					map.put("image", R.drawable.msg);
//					list.add(map);
//					sendid++;
//				}
//				
				Timer2Activity.this.timerHandler.sendEmptyMessage(0);
				
			}
		}, 0, 10);
	}
	
	
	private String vpath;
	private void initMessage(){
		lv_message = (ListView) findViewById(R.id.lv_message);

		list = new ArrayList<Map<String, Object>>();
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("id", "bk42-lr001");
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
				Intent intent = new Intent(Timer2Activity.this,VideoActivity.class);
				intent.putExtra("content",list.get(position).get("content").toString());
				//获得每个选项的视频id，通过视频id来对应视频
				String vid = list.get(position).get("id").toString();
				if(vid.equals("bk42-lr001"))
					intent.putExtra("vpath", vpath+R.raw.v001);
				else if(vid.equals("bk42-lr002"))
					intent.putExtra("vpath", vpath+R.raw.v002);
				else if(vid.equals("bk42-lr003"))
					intent.putExtra("vpath", vpath+R.raw.v003);
				else if(vid.equals("bk42-lr004"))
					intent.putExtra("vpath", vpath+R.raw.v004);
				Timer2Activity.this.startActivity(intent);
			}});
	}
	
}
