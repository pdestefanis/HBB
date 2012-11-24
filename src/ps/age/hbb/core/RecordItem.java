package ps.age.hbb.core;

import java.io.IOException;
import java.io.ObjectOutputStream;
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
	/**
	 * 
	 */
	private long id;
	private long firstMark = -1;
	private long secondMark = -1;
	private long thirdMark = -1;
	private long fourthMark = -1;
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

	public long getFirstMark() {
		return firstMark;
	}

	public void setFirstMark(long firstMark) {
		this.firstMark = firstMark;
	}

	public long getSecondMark() {
		return secondMark;
	}

	public void setSecondMark(long secondMark) {
		this.secondMark = secondMark;
	}

	public long getThirdMark() {
		return thirdMark;
	}

	public void setThirdMark(long thirdMark) {
		this.thirdMark = thirdMark;
	}

	public long getFourthMark() {
		return fourthMark;
	}

	public void setFourthMark(long fourthMark) {
		this.fourthMark = fourthMark;
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
		if(firstMark != -1)
			total++;
		if(secondMark != -1)
			total++;
		if(thirdMark != -1)
			total++;
		if(fourthMark != -1)
			total++;
		
		
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
