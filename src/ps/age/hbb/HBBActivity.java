package ps.age.hbb;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class HBBActivity extends Activity implements OnSeekBarChangeListener, OnCompletionListener, OnPreparedListener {
	MediaPlayer mPlayer;
	Handler mHandler;
	RecordItem mItem;
	
	ImageView play;
	ImageView first;
	ImageView second;
	ImageView third;
	ImageView fourth;
	SeekBar seekBar;
	TextView firstText;
	TextView secondText;
	TextView thirdText;
	TextView fourthText;
	TextView currentTime;
	TextView endTime;
	
	SimpleDateFormat fmt = new SimpleDateFormat("HH:mm:ss");
	Date date = new Date();
	
	long firstTime;
	long secondTime;
	long thirdTime;
	long fourthTime;
	boolean isPlaying;
	boolean isReady = false;
	DBWraper mWraper;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hbb);
        mWraper = new DBWraper(this);
        mItem = (RecordItem) getIntent().getSerializableExtra("item");
        
        play     	= (ImageView) findViewById(R.id.play);
        first 		= (ImageView) findViewById(R.id.mark1);
        second		= (ImageView) findViewById(R.id.mark2);
        third 		= (ImageView) findViewById(R.id.mark3);
        fourth 		= (ImageView) findViewById(R.id.mark4);
        
        seekBar 	= (SeekBar) findViewById(R.id.seekBar_player);
        
        firstText   = (TextView) findViewById(R.id.mark1_time);
        secondText  = (TextView) findViewById(R.id.mark2_time);
        thirdText   = (TextView) findViewById(R.id.mark3_time);
        fourthText  = (TextView) findViewById(R.id.mark4_time);
        currentTime = (TextView) findViewById(R.id.time_current);
        endTime     = (TextView) findViewById(R.id.time_end);
        
    	fmt.setTimeZone(TimeZone.getTimeZone("UTC"));
    	
    	play.setOnClickListener(listener);
    	first.setOnClickListener(listener);
    	second.setOnClickListener(listener);
    	third.setOnClickListener(listener);
    	fourth.setOnClickListener(listener);
    	mHandler = new Handler();
    	seekBar.setEnabled(false);
    	seekBar.setOnSeekBarChangeListener(this);
    	updateUI();
    	PrepairPlayer();
        
    }
    @Override
    public void onPause(){
    	super.onPause();
    	if(isFinishing()){
    		if(mPlayer!= null){
    			
    			if(mPlayer.isPlaying()){
    				mPlayer.stop();
    				isPlaying = false;
    			}
    			mPlayer.release();
    			mPlayer = null;
    			
    		}
    		
    		new Thread(){
    			@Override
    			public void run(){
    				mWraper.updateRecord(mItem);
    				mWraper.close();
    			}
    		}.start();
    	}
    }
    private void updateUI(){
        if(mItem.getFirstMark() != -1){
        	date.setTime(mItem.getFirstMark());
        	firstText.setText(fmt.format(date));
        }
        if(mItem.getSecondMark() != -1){
        	date.setTime(mItem.getSecondMark());
        	secondText.setText(fmt.format(date));
        }
        if(mItem.getThirdMark() != -1){
        	date.setTime(mItem.getThirdMark());
        	thirdText.setText(fmt.format(date));
        }
        if(mItem.getFourthMark() != -1){
        	date.setTime(mItem.getFourthMark());
        	fourthText.setText(fmt.format(date));
        }
    }
    public void PrepairPlayer(){
    	mPlayer = new MediaPlayer();
    	try {
			mPlayer.setDataSource(mItem.getPath());
			mPlayer.setOnCompletionListener(this);
			mPlayer.setOnPreparedListener(this);
			mPlayer.prepareAsync();

		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Toast.makeText(HBBActivity.this, R.string.error_sdcard, Toast.LENGTH_LONG).show();

		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Toast.makeText(HBBActivity.this, R.string.error_sdcard, Toast.LENGTH_LONG).show();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Toast.makeText(HBBActivity.this, R.string.error_sdcard, Toast.LENGTH_LONG).show();
		}
    	
    }
    private OnClickListener listener = new OnClickListener(){

		@Override
		public void onClick(View view) {
			switch(view.getId()){
			case R.id.play:
				if(isPlaying && mPlayer.isPlaying()){
					mPlayer.pause();
					play.setImageResource(android.R.drawable.ic_media_play);
				}
				else{
					if(isReady){
						mPlayer.start();
						isPlaying = true;
						mHandler.postDelayed(run, 100);
						play.setImageResource(android.R.drawable.ic_media_pause);
					}

				}
				return;
				
			case R.id.mark1:
				if(isReady){
					mItem.setFirstMark(mPlayer.getCurrentPosition());
				}
				break;
			case R.id.mark2:
				if(isReady){
					mItem.setSecondMark(mPlayer.getCurrentPosition());
				}
				break;
			case R.id.mark3:
				if(isReady){
					mItem.setThirdMark(mPlayer.getCurrentPosition());
				}
				break;
			case R.id.mark4:
				if(isReady){
					mItem.setFourthMark(mPlayer.getCurrentPosition());
				}
				break;
			}
			updateUI();
		}
    	
    };

	@Override
	public void onProgressChanged(SeekBar view, int position, boolean fromUser) {
		
		if(isReady){
			
			if(fromUser){
				mPlayer.seekTo(position);
			}
			
			date.setTime(mPlayer.getCurrentPosition());
			currentTime.setText(fmt.format(date));
		}
		
	}
	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onCompletion(MediaPlayer arg0) {
			isPlaying = false;
			seekBar.setProgress(0);
			play.setImageResource(android.R.drawable.ic_media_play);
				
	}
	@Override
	public void onPrepared(MediaPlayer arg0) {
		
		isReady = true;
		seekBar.setEnabled(true);
		seekBar.setMax(mPlayer.getDuration());	
		date.setTime(mPlayer.getDuration());
		endTime.setText(fmt.format(date));
		
	}
	private Runnable run = new Runnable(){

		@Override
		public void run() {
			Log.e("tag", "palying progress");
			if(isPlaying && mPlayer.isPlaying()){
				seekBar.setProgress(mPlayer.getCurrentPosition());
				mHandler.postDelayed(this, 100);
			}
		}
		
	};
}