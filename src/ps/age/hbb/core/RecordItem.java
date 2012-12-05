package ps.age.hbb.core;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class RecordItem implements Serializable {
	public static final String tag = RecordItem.class.getSimpleName();
	/**
	 * 
	 */
	private static final long serialVersionUID = 6858227128259519668L;
	public static final String EXTRA_ID = "extra_id";
	public static final String EXTRA_CRY = "extra_cry";
	public static final String EXTRA_VENTILATION = "exra_vent";
	public static final String EXTRA_OTHER = "extra_other";
	public static final String EXTRA_PROBLEM = "extra_problem";
	public static final String EXTRA_PRIMARY = "extra_primary";
	public static final String EXTRA_ALIVE = "extra_alive";
	private static final int TOTAL_MARKS = 4;

	private static final String ID = "id";
	private static final String VALUE = "value";
	private static final int ID_IDENT = 1;
	private static final int ID_PRIMARY = 2;
	private static final int ID_CRY = 3;
	private static final int ID_VENTILATION = 4;
	private static final int ID_OTHER = 5;
	private static final int ID_PROBLEM = 6;
	private static final int ID_ALIVE = 7;
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

	public long getMark(int position) {
		if (position < TOTAL_MARKS) {
			return marksArray[position];
		} else
			return -1;
	}

	public void setMark(int position, long value) {
		//fix for old HBB db where value is init to -1
		marksArray[position] = value > 0 ? value : 0;
		
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getExtra() {
		if (extra != null)
			return extra.toString();
		else
			return null;
	}

	public void setExtra(String extra) {
		if (extra == null)
			this.extra = new JSONObject();
		else {
			Log.e("setExtra", extra);

			try {
				// hack to check if the extra is a JSON array "old extra format"
				if (extra.startsWith("[")) {

					JSONArray mForm = new JSONArray(extra);
					for (int i = 0; i < mForm.length(); i++) {
						JSONObject obj = mForm.getJSONObject(i);
						String value = obj.getString(VALUE);
						int id = obj.getInt(ID);
						Log.e(tag, value);
						if (value == null)
							continue;

						switch (id) {
						case ID_IDENT:
							this.putExtraString(RecordItem.EXTRA_ID, value);
							break;
						case ID_CRY:
							this.putExtraString(RecordItem.EXTRA_CRY, value);
							break;
						case ID_VENTILATION:
							this.putExtraString(RecordItem.EXTRA_VENTILATION,
									value);
							break;
						case ID_OTHER:
							this.putExtraString(RecordItem.EXTRA_OTHER, value);
							break;
						case ID_PROBLEM:
							this.putExtraString(RecordItem.EXTRA_PROBLEM, value);
							break;
						case ID_PRIMARY:
							this.putExtraString(RecordItem.EXTRA_PRIMARY, value);
							break;
						case ID_ALIVE:
							this.putExtraString(RecordItem.EXTRA_ALIVE, value);
						}

					}
				} else {

					this.extra = new JSONObject(extra);

				}
			} catch (JSONException e) {
				// Forget about it create an empty extra object instead
				Log.e(tag, e.toString());
				this.extra = new JSONObject();
			}
		}

	}

	public long getTime() {
		return time;
	}

	public boolean isReviewed() {
		long total = 0;
		for (long mark : marksArray) {
			total += mark;
		}
		return (total > 0) ? true : false;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public void setUploadTime(long time) {
		uploadTime = time;
	}

	public long getUploadTime() {
		return uploadTime;
	}

	public int getTotalMarks() {
		int total = 0;

		for (long mark : marksArray) {
			if (mark != 0)
				total++;
		}

		return total;
	}

	public void putExtraString(String key, String value) {
		try {
			if (extra == null)
				extra = new JSONObject();

			extra.put(key, value);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getExtraString(String key) {
		if (extra != null) {
			try {
				extra.has(key);
					return extra.getString(key);

			} catch (JSONException e) {
				Log.i(tag,e.toString());
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public String[] getExtraKeys() {
		if (extra == null)
			return null;
		Iterator<String> iter = extra.keys();
		ArrayList<String> keys = new ArrayList<String>();
		while (iter.hasNext()) {
			keys.add(iter.next());
		}
		return keys.toArray(new String[keys.size()]);
	}

	private void writeObject(java.io.ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();

		out.writeChars(extra.toString());

	}

	private void readObject(java.io.ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		in.defaultReadObject();
		StringBuilder builder = new StringBuilder();
		while (in.available() > 0) {
			builder.append(in.readChar());
		}
		try {
			extra = new JSONObject(builder.toString());
		} catch (JSONException e) {
			Log.e(tag, "read object " + e.toString());
		}
	}
}
