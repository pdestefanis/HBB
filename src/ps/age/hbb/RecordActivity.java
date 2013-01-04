package ps.age.hbb;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import ps.age.hbb.core.MediaErrorBroadcastReceiver;
import ps.age.hbb.core.MediaRecordEventListener;
import ps.age.hbb.core.RecordItem;
import ps.age.hbb.core.DBWraper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("HandlerLeak")
public class RecordActivity extends Activity implements
		MediaRecordEventListener {
	public static final String tag = RecordActivity.class.getSimpleName();

	private AudioManager  audioManager;
	private Handler       mHandler;
	private MediaRecorder mRecorder;
	private MediaErrorBroadcastReceiver mReceiver;
	private Button     mRecord;
	private ImageView  mBack;
	private TextView   systemTime;
	private TextView   elapsedTime;
	private TextView   remainingTime;
	private RecordItem mItem;
	private DateFormat fmt = DateFormat.getDateTimeInstance();
	private SimpleDateFormat timerFormat = new SimpleDateFormat("HH:mm:ss");

	private String tmpPath;
	private long startTime;
	private boolean isRecording;
	private boolean useBluetooth;
	private static final long updateSleep = 100;
	private static final String OUT_PATH = Environment
			.getExternalStorageDirectory().getAbsolutePath() + "/HBB/";

	private static final int DIALOG_STOP = 22222;
	private static final int DIALOG_NOTE = 44444;

	// sdcard size when record started
	private int startSize;
	private long totalTime;
	private float secSizeInBlocks;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.record);
		Log.w(tag, "onCreate");
		
		audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
		registerReceiver(bluetoothReceiver, new IntentFilter(AudioManager.ACTION_SCO_AUDIO_STATE_CHANGED));
		audioManager.startBluetoothSco();
		mRecord 	  = (Button)    findViewById(R.id.recordButton);
		mBack		  = (ImageView) findViewById(R.id.back);
		systemTime	  = (TextView)  findViewById(R.id.systemTime_Value);
		elapsedTime	  = (TextView)  findViewById(R.id.elapsedTime_Value);
		remainingTime = (TextView)  findViewById(R.id.remainingTime_Value);

		mRecord.setOnClickListener(listener);
		mBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Log.i(tag, "back key pressed");
				finish();
			}

		});
		mHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case DIALOG_STOP:
					if (msg.arg1 == RESULT_OK) {
						stopRecording();
						HBBDialog dialog = new HBBDialog(RecordActivity.this);
						dialog.setCancelable(false);
						msg = obtainMessage();
						msg.what = DIALOG_NOTE;
						dialog.setDismissMessage(msg);
						dialog.setTitle(getResources().getString(
								R.string.noteDialog_title));
						dialog.setExtra(getResources().getString(
								R.string.noteDialog_extra));
						dialog.setButtonsText(
								getResources().getString(
										R.string.noteDialog_positive),
								getResources().getString(
										R.string.noteDialog_negative));
						dialog.show();
					} else {
						mRecord.setClickable(true);
					}
					break;
				case DIALOG_NOTE:
					if (msg.arg1 == RESULT_OK) {
						Intent intent = new Intent(RecordActivity.this,
								ExtraActivity.class);
						intent.putExtra(ExtraActivity.ITEM, mItem);
						startActivityForResult(intent,
								ExtraActivity.REQUEST_CODE);
					} else {
						saveItem();
						finish();
					}
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
	public void onStop() {
		super.onStop();
		Log.w(tag, "onStop, isFinishing: " + String.valueOf(isFinishing()));
		if (isFinishing()) {

			if (isRecording) {
				stopRecording();
				new File(tmpPath).delete();
			}
			audioManager.stopBluetoothSco();
			if(!useBluetooth)
                unregisterReceiver(bluetoothReceiver);
			mHandler.removeCallbacks(timeRunnable);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.w(tag, "onDestroy");
		unregisterReceiver(mReceiver);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.w(tag, "onActivity Result");
		if ((requestCode == ExtraActivity.REQUEST_CODE) && (data != null)) {
			mItem = (RecordItem) data.getSerializableExtra(ExtraActivity.ITEM);
			saveItem();
		}
		finish();

	}

	private void saveItem() {
		Log.w(tag, "saving recoed Item");
		if(mItem.getExtra() != null)
			Log.w(tag, "extra: "+mItem.getExtra());
		DBWraper wraper = new DBWraper(RecordActivity.this);
		wraper.insertRecord(mItem);
		wraper.close();
	}

	private void startRecording() {
		Log.w(tag, "startRecording , use Bluetooth? "+useBluetooth);
		mRecorder = new MediaRecorder();
		mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
		if(useBluetooth){
			//	for bluetooth devices
			mRecorder.setAudioChannels(1);
			mRecorder.setAudioSamplingRate(8000);
		}
		tmpPath = OUT_PATH + String.valueOf(System.currentTimeMillis())
				+ ".3gp";

		if (!(new File(OUT_PATH)).exists()) {
			(new File(OUT_PATH)).mkdirs();
		}
		mRecorder.setOutputFile(tmpPath);
		mRecorder.setOnInfoListener(infoListener);
		try {
			mRecorder.prepare();
		} catch (IOException e) {
			e.printStackTrace();
			Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
			return;
		}
		StatFs stat = new StatFs(Environment.getExternalStorageDirectory()
				.getPath());
		startSize = stat.getAvailableBlocks();
		secSizeInBlocks = (stat.getBlockSize() * 2.2f / 1024);
		mRecorder.start();
		startTime = System.currentTimeMillis();
		totalTime = 0;
		mItem = new RecordItem();
		mItem.setPath(tmpPath);
		mItem.setTime(startTime);
		isRecording = true;
		mRecord.setText(R.string.button_stop);
		/*
		 * Add delay to ensure that the recoding already started
		 */
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				mRecord.setClickable(true);
			}
		}, 500);
	}

	private void stopRecording() {
		Log.w(tag, "stop Recording");
		if (mRecorder != null) {
			mRecorder.stop();
			mItem.setLength((int) totalTime);
			isRecording = false;
			mRecord.setText(R.string.button_record);
		}
	}

	private final OnClickListener listener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			mRecord.setClickable(false);
			String txt = null;
			if (isRecording) {
				HBBDialog dialog = new HBBDialog(RecordActivity.this);
				dialog.setCancelable(false);
				Message msg = mHandler.obtainMessage();
				msg.what = DIALOG_STOP;
				dialog.setDismissMessage(msg);
				dialog.setTitle(getResources().getString(
						R.string.saveDialog_title));
				dialog.setExtra(getResources().getString(
						R.string.saveDialog_extra));
				dialog.setButtonsText(
						getResources().getString(R.string.saveDialog_positive),
						getResources().getString(R.string.saveDialog_negative));
				dialog.show();
				txt = "displaying options";
			} else {
				startRecording();
				txt = "start recording";

			}
			Log.w(tag, "button click "+txt );

		}

	};
	Runnable timeRunnable = new Runnable() {
		Date date = new Date();

		int prevSec = -1;

		@Override
		public void run() {

			date.setTime(System.currentTimeMillis());
			// Check if the seconds changed
			if (date.getSeconds() != prevSec) {

				prevSec = date.getSeconds();
				systemTime.setText(fmt.format(date));

				if (isRecording) {
					totalTime = System.currentTimeMillis() - startTime;
					date.setTime(totalTime);
					elapsedTime.setText(timerFormat.format(date));
					int remaning = (int) (startSize - (secSizeInBlocks
							* totalTime / 1000));
					long howmuch = (long) (remaning * 1000 / secSizeInBlocks);
					date.setTime(howmuch);
					remainingTime.setText(timerFormat.format(date));
				}
			}
			// check if activity is finished
			if (isFinishing())
				return;
			else {
				mHandler.postDelayed(timeRunnable, updateSleep);
			}
		}
	};
	private MediaRecorder.OnInfoListener infoListener = new MediaRecorder.OnInfoListener() {

		@Override
		public void onInfo(MediaRecorder mr, int what, int extra) {
		}
	};

	@Override
	public void onError() {
		if (isRecording)
			stopRecording();

	}

	@Override
	public void onReady() {
		// TODO Auto-generated method stub

	}
	BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            int state = intent.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, -1);
            Log.d(tag, "Audio SCO state: " + state);

            if (AudioManager.SCO_AUDIO_STATE_CONNECTED == state) { 
                /* 
                 * Now the connection has been established to the bluetooth device. 
                 * Record audio or whatever (on another thread).With AudioRecord you can record with an object created like this:
                 * new AudioRecord(MediaRecorder.AudioSource.MIC, 8000, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                 * AudioFormat.ENCODING_PCM_16BIT, audioBufferSize);
                 *
                 * After finishing, don't forget to unregister this receiver and
                 * to stop the bluetooth connection with am.stopBluetoothSco();
                 */
            	useBluetooth = true;
                unregisterReceiver(this);
            }

        }
    };

}