package ps.age.hbb.core;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class RecordItem implements Serializable {
	public static final String tag = RecordItem.class.getSimpleName();
	/**
	 * 
	 */
	private static final long serialVersionUID = 6858227128259519668L;
	public static final String EXTRA_ID  		  = "extra_id";
	public static final String EXTRA_CRY 		  = "extra_cry";
	public static final String EXTRA_VENTILATION  = "exra_vent";
	public static final String EXTRA_OTHER        = "extra_other";
	public static final String EXTRA_PROBLEM      = "extra_problem";
	public static final String EXTRA_PRIMARY      = "extra_primary";
	public static final String EXTRA_ALIVE        = "extra_alive";
	private static final int   TOTAL_MARKS        = 4;
	/**
	 * 
	 */
	private long id;
	
	long[] marksArray = new long[TOTAL_MARKS];

	private int length;
	private String path;
	transient private JSONObject extra = new JSONObject();
	private long time;
	private long uploadTime = -1;
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getMark(int position){
		if(position<TOTAL_MARKS){
			return marksArray[position];
		}
		else
			return -1;
	}
	public void setMark(int position, long value) {
		this.marksArray[position] = value;
	}

	public int getLength(){
		return length;
	}
	public void setLength(int length){
		this.length = length;
	}
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getExtra() {
		if(extra != null)
			return extra.toString();
		else return null;
	}
	public void setExtra(String extra){
			if(extra == null)
				this.extra = new JSONObject();
			else{	
				try {
					this.extra = new JSONObject(extra);
				} catch (JSONException e) {
				// TODO Auto-generated catch block
			
					this.extra = new JSONObject();
				}
			}
		
	}

	public long getTime() {
		return time;
	}
	public boolean isReviewed(){
		long total =  0 ;
		for( long mark : marksArray){
			total+=mark;
		}
		return (total > 0) ? true : false;
	}
	public void setTime(long time) {
		this.time = time;
	}
	public void setUploadTime(long time){
		uploadTime = time;
	}
	public long getUploadTime(){
		return uploadTime;
	}
	public int getTotalMarks(){
		int total = 0;
		
		for(long mark : marksArray){
			if(mark != 0)
				total++;
		}
		
		
		return total;
	}
	public void putExtraString(String key, String value){
		try {
		if(extra == null)
			extra = new JSONObject();
		
		extra.put(key, value);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public String getExtraString(String key){
		if(extra != null)
		{
			try {	
				return extra.getString(key);
				
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	@SuppressWarnings("unchecked")
	public String[] getExtraKeys(){
		if(extra == null)
			return null;
		Iterator<String> iter = extra.keys();
		ArrayList<String> keys= new ArrayList<String>();
		while(iter.hasNext()){
			keys.add(iter.next());
		}
		return keys.toArray(new String[keys.size()]);	
	}
	  private void writeObject(java.io.ObjectOutputStream out)
		       throws IOException {
		  out.defaultWriteObject();
		  
		  out.writeChars(extra.toString());
		  
	  }
	  private void readObject(java.io.ObjectInputStream in)
		       throws IOException, ClassNotFoundException {
		  in.defaultReadObject();
		  StringBuilder builder = new StringBuilder();
		  while(in.available()>0){
			  	builder.append(in.readChar());
		  }
		  try{
			  extra = new JSONObject(builder.toString());
		  }catch(JSONException e){
			  Log.e(tag, "read object "+e.toString());
		  }
	  }
}
