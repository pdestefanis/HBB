package ps.age.hbb;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ps.age.hbb.core.RecordItem;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

public class ExtraActivity extends Activity {
	protected static final String TAG = ExtraActivity.class.getSimpleName();
	public static final String ITEM   = "item";
	public static final int REQUEST_CODE = 620976104;
	
	ImageView		mBack;
	Button			mSave;
	EditText		mProblem;
	EditText		mOther;
	EditText		mID;
	RadioGroup 		cryGroup;
	RadioGroup 		ventilationGroup;
	Spinner			mPrimary;
	RecordItem		mItem;
	ToggleButton	mAlive;
	
	String selectedPrimary;
	JSONArray mForm;
	 ArrayAdapter<CharSequence> mAdapter;
	/*
	 * JSON String headers
	 */
	protected static final String ID 			= "id";
	protected static final String VALUE			= "value";
	protected static final int ID_IDENT			= 1;
	protected static final int ID_PRIMARY 		= 2;
	protected static final int ID_CRY     		= 3;
	protected static final int ID_VENTILATION	= 4;
	protected static final int ID_OTHER 		= 5;
	protected static final int ID_PROBLEM 		= 6;	
	protected static final int ID_ALIVE         = 7;
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
        mID				 = (EditText) findViewById(R.id.id_editText);
        mBack			 = (ImageView) findViewById(R.id.back);
        mAlive			 = (ToggleButton) findViewById(R.id.alive_toggle);
        mAdapter = ArrayAdapter.createFromResource(this,
               R.array.primary_array , android.R.layout.simple_spinner_item);
        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mPrimary.setAdapter(mAdapter);
        mPrimary.setOnItemSelectedListener(new OnItemSelectedListener(){

			@Override
			  public void onItemSelected(AdapterView<?> parent, View view, 
			            int pos, long id) {
				selectedPrimary = ((TextView) view).getText().toString();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				selectedPrimary = null;
			}
        	
        });
        mBack.setOnClickListener(new OnClickListener(){
        	@Override
        	public void onClick(View view){
        		finish();
        	}
        });
        mAlive.setOnCheckedChangeListener(new OnCheckedChangeListener(){

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if(isChecked){
    				mPrimary.setEnabled(false);
    				mOther.setEnabled(false);
				}else{
    				mPrimary.setEnabled(true);
    				mOther.setEnabled(true);
				}
			}
        	
        });
        mItem = (RecordItem) getIntent().getSerializableExtra(ITEM);
        mSave.setOnClickListener(listener);
        if(mItem.getExtra()!=null){
        	try {
				loadForm();
			} catch (JSONException e) {
				e.printStackTrace();				
			}
        }
    }
    private OnClickListener listener = new OnClickListener(){

		@Override
		public void onClick(View v) {
			if(mItem != null){
				saveForm();
				Intent intent = getIntent();
				intent.putExtra(ITEM, mItem);
				setResult(RESULT_OK, intent);
			}
			finish();
		}
    	
    };
    private void loadForm() throws JSONException{
    	mForm = new JSONArray(mItem.getExtra());
    	for(int i=0;i<mForm.length();i++){
    		JSONObject obj = mForm.getJSONObject(i);
    		String value = obj.getString(VALUE);
    		int id  = obj.getInt(ID);
    		
    		if (value == null)
    			continue;
    		
    		switch(id){
    		case ID_IDENT: 
    			mID.setText(value);
    			break;
    		case ID_CRY:
    			((RadioButton)cryGroup.findViewWithTag(value)).setChecked(true);
    			break;
    		case ID_VENTILATION:
    			((RadioButton)ventilationGroup.findViewWithTag(value)).setChecked(true);
    			break;  			
    		case ID_OTHER:
				mOther.setText(value);
				break;
    		case ID_PROBLEM:
    			mProblem.setText(value);
    			break;
    		case ID_PRIMARY:
    			mPrimary.setSelection(mAdapter.getPosition(value), false);
    			break;
    		case ID_ALIVE:
    			
    			if(value.equals(getResources().getString(R.string.alive))){
    				mAlive.setChecked(true);
    				mPrimary.setEnabled(false);
    				mOther.setEnabled(false);
    			}
    			else{
        			if(value.equals(getResources().getString(R.string.dead))){
        				mAlive.setChecked(false);
        				
        			}
    			}
    		}
    		
    	}
    }
    private void saveForm(){
    	mForm = new JSONArray();
        if(mID.getText().toString().length() != 0 ){
        	appendToArray(ID_IDENT,	mID.getText().toString());
        }
        RadioButton btn = (RadioButton) findViewById(cryGroup.getCheckedRadioButtonId());
    	appendToArray(ID_CRY,	btn.getText().toString());
        btn = (RadioButton) findViewById(ventilationGroup.getCheckedRadioButtonId());
    	appendToArray(ID_VENTILATION,	btn.getText().toString());
    	if(mAlive.isChecked()){
        	appendToArray(ID_ALIVE,	mAlive.getTextOn().toString());
    	}
    	else{
        	appendToArray(ID_ALIVE,	mAlive.getTextOff().toString());
  		
    		/*
    		 * Check if the user selected a value in the primary spinner
    		 */
    		if(selectedPrimary != null){		        	
    			appendToArray(ID_PRIMARY,selectedPrimary);
    		}
    		/*
    		 * Check if their is data in the editText field
    		 */
    		if(mOther.getText().toString().trim().length() > 0){
    			appendToArray(ID_OTHER,mOther.getText().toString().trim());
    		}
    	}
        if(mProblem.getText().toString().trim().length() > 0){;
	        appendToArray(ID_PROBLEM,mProblem.getText().toString().trim());
        }		        
	    mItem.setExtra(mForm.toString());

    }
    private void appendToArray(int id,String value){
    	if(mForm == null)
    		mForm = new JSONArray();
    	JSONObject object = new JSONObject();
    	try {
			object.put(ID, id);
	    	object.put(VALUE, value);
		} catch (JSONException e) {

			e.printStackTrace();
		}
    	mForm.put(object);
    }
}
