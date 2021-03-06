package ps.age.hbb;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import ps.age.hbb.core.BluetoothAudioManager;
import ps.age.hbb.core.MediaErrorBroadcastReceiver;
import ps.age.hbb.core.MediaRecordEventListener;
import ps.age.hbb.core.RecordItem;
import ps.age.hbb.core.RecordItem.State;
import ps.age.hbb.core.SharedObjects;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
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

public class RecordActivity extends Activity implements
		MediaRecordEventListener {
	
	public static final String tag = RecordActivity.class.getSimpleName();
	
	private BluetoothAudioManager bluetoothAudioManager;
	private MediaErrorBroadcastReceiver mReceiver;
	
	private Handler       mHandler;
	private MediaRecorder mRecorder;
	
	private Button     mRecord;
	private ImageView  mBack;
	private TextView   systemTime;
	private TextView   elapsedTime;
	private TextView   remainingTime;
	private TextView   audioSource;
	
	private Drawable   bluetoothDrawable;
	private Drawable   micDrawable;
	
	private RecordItem mItem;
	private DateFormat fmt = DateFormat.getDateTimeInstance();
	private SimpleDateFormat timerFormat = new SimpleDateFormat("HH:mm:ss", Locale.US);

	private String noteTitle;
	private String noteExtra;
	private String notePositive;
	private String noteNegative;
	private String saveTitle;
	private String saveExtra;
	private String savePositive;
	private String saveNegative;
	
	private String sourceTextBluetooth;
	private String sourceTextMic;
	
	private String tmpPath;
	private long startTime;
	private boolean isRecording;
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
	@SuppressLint("HandlerLeak")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.record);
		Log.w(tag, "onCreate");
		
		bluetoothAudioManager = new BluetoothAudioManager(this);
		
		mRecord 	  = (Button)    findViewById(R.id.recordButton);
		mBack		  = (ImageView) findViewById(R.id.back);
		systemTime	  = (TextView)  findViewById(R.id.systemTime_Value);
		elapsedTime	  = (TextView)  findViewById(R.id.elapsedTime_Value);
		remainingTime = (TextView)  findViewById(R.id.remainingTime_Value);
		audioSource   = (TextView)  findViewById(R.id.textView_sourceIndecator);
		
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
						dialog.setTitle(noteTitle);
						dialog.setExtra(noteExtra);
						dialog.setButtonsText(notePositive, noteNegative);
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
						SharedObjects.getDataManager(getApplicationContext()).addItem(mItem);
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
		final Resources res = getResources();
		
		bluetoothDrawable = res.getDrawable(R.drawable.ic_source_bluetooth);
		micDrawable        = res.getDrawable(R.drawable.ic_source_mic);
		
		Rect bounds = new Rect(0, 0, bluetoothDrawable.getIntrinsicWidth(), bluetoothDrawable.getIntrinsicHeight());
		bluetoothDrawable.setBounds(bounds);
		bounds.set(0, 0, micDrawable.getIntrinsicWidth(), micDrawable.getIntrinsicHeight());
		micDrawable.setBounds(bounds);
		
		// preload  text resources
		saveTitle           = res.getString(R.string.saveDialog_title); 
		saveExtra           = res.getString(R.string.saveDialog_extra);			
		savePositive        = res.getString(R.string.saveDialog_positive);
		saveNegative        = res.getString(R.string.saveDialog_negative);
		noteTitle           = res.getString(R.string.noteDialog_title); 
		noteExtra           = res.getString(R.string.noteDialog_extra);			
		notePositive        = res.getString(R.string.noteDialog_positive);
		noteNegative        = res.getString(R.string.noteDialog_negative);
		
		sourceTextBluetooth = res.getString(R.string.source_bluetooth);
		sourceTextMic       = res.getString(R.string.source_mic);
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
			bluetoothAudioManager.finish();
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
			SharedObjects.getDataManager(getApplicationContext()).addItem(mItem);
//			saveItem();
		}
		finish();

	}
/*
	private void saveItem() {

	}
*/
	private void startRecording() {
		boolean useBluetooth = bluetoothAudioManager.isBluetoothAvailable();
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
		mItem.setState(State.NEW);
		isRecording = true;
		mRecord.setText(R.string.button_stop);
		Drawable drawable = micDrawable;
		String txt = sourceTextMic;
		if(useBluetooth){
			drawable = bluetoothDrawable;
			txt = sourceTextBluetooth;
		}
		audioSource.setText(txt);
		audioSource.setCompoundDrawables(null, drawable, null, null);
		audioSource.setVisibility(View.VISIBLE);
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
		audioSource.setVisibility(View.INVISIBLE);

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
				dialog.setTitle(saveTitle);
				dialog.setExtra(saveExtra);
				dialog.setButtonsText(savePositive, saveNegative);
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
		Calendar cal = Calendar.getInstance();

		int prevSec = -1;

		@Override
		public void run() {
			cal.setTimeInMillis(System.currentTimeMillis());
			// Check if the seconds changed
			if (cal.get(Calendar.SECOND) != prevSec) {

				prevSec = cal.get(Calendar.SECOND);
				systemTime.setText(fmt.format(cal.getTime()));

				if (isRecording) {
					totalTime = System.currentTimeMillis() - startTime;
					cal.setTimeInMillis(totalTime);
					elapsedTime.setText(timerFormat.format(cal.getTime()));
					int remaning = (int) (startSize - (secSizeInBlocks
							* totalTime / 1000));
					long howmuch = (long) (remaning * 1000 / secSizeInBlocks);
					cal.setTimeInMillis(howmuch);
					remainingTime.setText(timerFormat.format(cal.getTime()));
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
			Log.i(tag, "MediInfo "+what+":"+extra);
		}
	};

	@Override
	public void onError() {
		Log.e(tag, "MediError");

		if (isRecording)
			stopRecording();

	}

	@Override
	public void onReady() {
		// TODO Auto-generated method stub

	}

	
}