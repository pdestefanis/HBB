package ps.age.hbb;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class NoteActivity extends Activity {
	Button mSave;
	EditText mNote;
	RecordItem mItem;
	DBWraper mWraper;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note);
        
        mSave = (Button) findViewById(R.id.button_save);
        mNote = (EditText) findViewById(R.id.note);
        mItem = (RecordItem) getIntent().getSerializableExtra("item");
        mWraper = new DBWraper(this);
        mSave.setOnClickListener(listener);
    }
    private OnClickListener listener = new OnClickListener(){

		@Override
		public void onClick(View v) {
			if(mItem != null){
				String note = mNote.getText().toString();
				if( note != null){
					mItem.setNote(note);
				}
				mWraper.insertRecord(mItem);
				mWraper.close();
			}
			finish();
		}
    	
    };
}
