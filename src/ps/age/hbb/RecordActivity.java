package ps.age.hbb;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class RecordActivity extends Activity implements MediaRecordEventListener{
	
	private String tag = RecordActivity.class.getSimpleName();
	
	Handler mHandler;
	MediaRecorder mRecorder;
	MediaErrorBroadcastReceiver mReceiver;
	Button mRecord;
	TextView systemTime;
	TextView elapsedTime;
	TextView remainingTime;
	RecordItem mItem;
	private DateFormat fmt = DateFormat.getDateTimeInstance();
	private SimpleDateFormat timerFormat = new SimpleDateFormat("HH:mm:ss");
	
	private String  tmpPath;
	private long  startTime;
	private boolean isRecording;
	private static final long updateSleep = 100;
	private static final String OUT_PATH  = Environment.getExternalStorageDirectory().getAbsolutePath()+"/HBB/";
	
	private static final int DIALOG_STOP = 22222;
	private static final int DIALOG_NOTE = 44444;
	
	//sdcard size when record started
	private int startSize;
	private long totalTime;
	private float secSizeInBlocks;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.record);
        
        mRecord       = (Button) findViewById(R.id.recordButton);
        systemTime    = (TextView) findViewById(R.id.systemTime_Value);
        elapsedTime   = (TextView) findViewById(R.id.elapsedTime_Value);
        remainingTime = (TextView) findViewById(R.id.remainingTime_Value);
        
        mRecord.setOnClickListener(listener);
        
        mHandler = new Handler(){
        	
        	@Override
        	public void handleMessage(Message msg){
        		switch(msg.what){
        		case DIALOG_STOP:
        			if(msg.arg1 == RESULT_OK){
        				stopRecording();
        				HBBDialog dialog = new HBBDialog(RecordActivity.this);
        				dialog.setCancelable(false);
        				msg = obtainMessage();
        				msg.what = DIALOG_NOTE;
        				dialog.setDismissMessage(msg);
        				dialog.setTitle(getResources().getString(R.string.noteDialog_title));
        				dialog.setExtra(getResources().getString(R.string.noteDialog_extra));
        				dialog.setButtonsText(getResources().getString(R.string.noteDialog_positive)
        								, getResources().getString(R.string.noteDialog_negative));
        				dialog.show();
        			}
        			break;
        		case DIALOG_NOTE:
        			if(msg.arg1 == RESULT_OK){
        				Intent intent = new Intent(RecordActivity.this,ExtraActivity.class);
        				intent.putExtra("item", mItem);
        				startActivity(intent);
        			}
        			else{
        				DBWraper wraper = new DBWraper(RecordActivity.this);
        				wraper.insertRecord(mItem);
        				wraper.close();
        			}
    				finish();
        			break;
        		}
        	}
        };
        mHandler.post(timeRunnable);
        
    	timerFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    	
    	mReceiver = new MediaErrorBroadcastReceiver();
    	mReceiver.setMediaRecordEventListener(this);
    	
    	IntentFilter filter = new IntentFilter();
    	filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
    	filter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
    	filter.addAction(Intent.ACTION_MEDIA_EJECT);
    	filter.addAction(Intent.ACTION_MEDIA_SHARED);
    	filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
    	filter.addAction(MediaErrorBroadcastReceiver.ACTION_PHONE_STATE_CHANGED);
    	registerReceiver(mReceiver, filter);
    }
    
    @Override
    public void onStop(){
    	super.onStop();
    	
    	if(isFinishing()){
    		
    		if(isRecording){
    			stopRecording();
    			new File(tmpPath).delete();
    		}
    		
    		mHandler.removeCallbacks(timeRunnable);
    		unregisterReceiver(mReceiver);
    	}
    }
    
    private final OnClickListener listener = new OnClickListener(){

		@Override
		public void onClick(View v) {
			if(isRecording){
				HBBDialog dialog = new HBBDialog(RecordActivity.this);
				dialog.setCancelable(false);
				Message msg = mHandler.obtainMessage();
				msg.what = DIALOG_STOP;
				dialog.setDismissMessage(msg);
				dialog.setTitle(getResources().getString(R.string.saveDialog_title));
				dialog.setExtra(getResources().getString(R.string.saveDialog_extra));
				dialog.setButtonsText(getResources().getString(R.string.saveDialog_positive)
								, getResources().getString(R.string.saveDialog_negative));
				dialog.show();
			}
			else{
				startRecording();
			}
		}
    	
    };
    private void startRecording(){
    	
    	mRecorder = new MediaRecorder();
	    mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
	    mRecorder.setOutputFormat(MediaRecorder.VideoEncoder.DEFAULT);
	    mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);	    
	    tmpPath = OUT_PATH+String.valueOf(System.currentTimeMillis())+".3gp";
	    
	    if(!(new File(OUT_PATH)).exists()){
	    	(new File(OUT_PATH)).mkdirs();
	    }
	    mRecorder.setOutputFile(tmpPath);
	    mRecorder.setOnInfoListener(infoListener);
	    try{
	    mRecorder.prepare();
	    }catch(IOException e){
	    	Log.e(tag, e.toString());
	    	Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
	    	return;
	    }
	    StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
	    startSize = stat.getAvailableBlocks();
	    secSizeInBlocks = (stat.getBlockSize()*2.2f/1024);
	    mRecorder.start();
	    startTime = System.currentTimeMillis();
	    totalTime = 0;
	    mItem = new RecordItem();
	    mItem.setPath(tmpPath);
	    mItem.setTime(startTime);
	    isRecording = true;
	    mRecord.setText(R.string.button_stop);
    }
    private void stopRecording(){
    	if(mRecorder != null){
    		mRecorder.stop();
    		mItem.setLength((int)totalTime);
    		isRecording = false;
    		mRecord.setText(R.string.button_record);
    	}
    }
    Runnable timeRunnable = new Runnable(){
    	Date date = new Date();

    	int prevSec = -1;
    	@Override
    	public void run(){
    		
    		date.setTime(System.currentTimeMillis());
    		//Check if the seconds changed
    		if(date.getSeconds()!=prevSec){
    			
    			prevSec = date.getSeconds();
    			systemTime.setText(fmt.format(date));
    			
    			if(isRecording){
    				totalTime = System.currentTimeMillis()-startTime;
    				date.setTime(totalTime);
    				elapsedTime.setText(timerFormat.format(date));
    				
    	//		    StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
    	//		    int remaining = stat.getAvailableBlocks();
    			    
    			    int remaning = (int) (startSize - (secSizeInBlocks*totalTime/1000));
    			    
//			    	Log.e(tag, String.valueOf(startSize)+" : "+String.valueOf(remaning)+" size: ");

    			    
    			    long howmuch = (long) (remaning*1000/secSizeInBlocks);
    				date.setTime(howmuch);
    				remainingTime.setText(timerFormat.format(date));
    				
			 //   	Log.e(tag, String.valueOf(remaining)+" : "+String.valueOf(totalTime)+" : "+String.valueOf(currentSize));

    			}
    		}
    		// check if activity is finished
    		if(isFinishing())
    			return;
    		else{
    			mHandler.postDelayed(timeRunnable,updateSleep);
    		}
    	}
    };
    private MediaRecorder.OnInfoListener infoListener = new MediaRecorder.OnInfoListener() {
    	
		
		@Override
		public void onInfo(MediaRecorder mr, int what, int extra) {
			Log.e(tag, "what: "+String.valueOf(what));
		}
	};

	@Override
	public void onError() {
		if(isRecording)
			stopRecording();
		
	}
	@Override
	public void onReady() {
		// TODO Auto-generated method stub
		
	}
}