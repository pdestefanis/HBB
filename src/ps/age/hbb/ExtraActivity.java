package ps.age.hbb;

import android.app.Activity;
import android.content.res.Resources;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;;

public class ExtraActivity extends Activity {



	Button mSave;
	EditText mProblem;
	EditText mOther;
	RadioGroup cryGroup;
	RadioGroup ventilationGroup;
	Spinner mPrimary;
	RecordItem mItem;
	DBWraper mWraper;
	String selectedPrimary;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.extra);
        
        mSave 			 = (Button) findViewById(R.id.button_save);
        cryGroup 		 = (RadioGroup) findViewById(R.id.radioCry);
        ventilationGroup = (RadioGroup) findViewById(R.id.radioVentilation);
        mPrimary 		 = (Spinner) findViewById(R.id.primary_spinner);
        mProblem	     = (EditText) findViewById(R.id.problem_editText);
        mOther		     = (EditText) findViewById(R.id.other_editText);
        
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
               R.array.primary_array , android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mPrimary.setAdapter(adapter);
        mPrimary.setOnItemSelectedListener(new OnItemSelectedListener(){

			@Override
			  public void onItemSelected(AdapterView<?> parent, View view, 
			            int pos, long id) {
				selectedPrimary = ((TextView) view).getText().toString();
				Log.e("tag", selectedPrimary);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				selectedPrimary = null;
			}
        	
        });
        mItem = (RecordItem) getIntent().getSerializableExtra("item");
        mWraper = new DBWraper(this);
        mSave.setOnClickListener(listener);
    }
    private OnClickListener listener = new OnClickListener(){

		@Override
		public void onClick(View v) {
			if(mItem != null){
		        Resources res = getResources();
		        StringBuilder builder = new StringBuilder();
		        builder.append(res.getString(R.string.cry_lable)).append("\n");
		        RadioButton btn = (RadioButton) findViewById(cryGroup.getCheckedRadioButtonId());
		        builder.append(btn.getText()).append("\n");
		        
		        builder.append(res.getString(R.string.ventilation_lable)).append("\n");
		        btn = (RadioButton) findViewById(ventilationGroup.getCheckedRadioButtonId());
		        builder.append(btn.getText()).append("\n");		   
		        if(selectedPrimary != null){
			        builder.append(res.getString(R.string.primary_lable)).append("\n");		   
			        builder.append(selectedPrimary+"\n");
		        }
		        if(mOther.getText().toString().trim().length() > 0){
			        builder.append(res.getString(R.string.other_lable)).append("\n");		   
			        builder.append(mOther.getText().toString().trim()).append("\n");
		        }
		        if(mProblem.getText().toString().trim().length() > 0){
			        builder.append(res.getString(R.string.problem_lable)).append("\n");		   
			        builder.append(mProblem.getText().toString().trim()).append("\n");
		        }
			    mItem.setNote(builder.toString());
				Log.e("tag", builder.toString());
				mWraper.insertRecord(mItem);
				mWraper.close();
			}
			finish();
		}
    	
    };
}
