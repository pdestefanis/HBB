package ps.age.hbb;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {
	Button mRecord;
	Button mList;
	Button mHelp;
	Handler mHandler;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mHandler = new Handler();
        mRecord = (Button) findViewById(R.id.recordButton);
        mList   = (Button) findViewById(R.id.listButton);
        mHelp   = (Button) findViewById(R.id.helpButton);
        
        mRecord.setOnClickListener(listener);
        mList.setOnClickListener(listener);
        mHelp.setOnClickListener(listener);
        
    }
    
  final OnClickListener listener = new OnClickListener(){

	@Override
	public void onClick(View view) {
		
		Intent intent = null;
		
		switch(view.getId()){
		
		case R.id.recordButton:
			//in case sdcard isn't mounted 
			if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
				Toast.makeText(MainActivity.this, R.string.error_sdcard, Toast.LENGTH_LONG).show();
				return;
			}
			intent = new Intent(MainActivity.this,RecordActivity.class);
			break;
			
		case R.id.listButton:
			
			intent = new Intent(MainActivity.this,ListActivity.class);
			break;
			
		case R.id.helpButton:
			intent = new Intent(MainActivity.this,HelpActivity.class);
			break;
			
		default:
			return;
		}
		
		startActivity(intent);
	}
	  
  };
 
}
