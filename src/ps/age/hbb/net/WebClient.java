package ps.age.hbb.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import ps.age.hbb.R;
import ps.age.hbb.core.RecordItem;

import android.content.Context;
import android.util.Log;

public class WebClient {

	private static HttpPost mPost;
	private static HttpClient mClient;
	/*
	 * Server URL
	 */
	private static String uploadURL = "http://helpingbabybreath.appspot.com/data";

	/*
	 * URL variables
	 */
	public static final String USER_NAME = "user";
	public static final String AUTH_KEY = "auth";
	public static final String FIRST_MARK = "first_mark";
	public static final String SECOND_MARK = "second_mark";
	public static final String THIRD_MARK = "third_mark";
	public static final String FOURTH_MARK = "fourth_mark";
	public static final String TIME_CREATED = "time_created";
	public static final String TOTAL_LENGTH = "length";
	public static final String EXTRA = "extra";

	private static String mResponse;
	private static String errorMessage;
	private static final String tag = WebClient.class.getSimpleName();
	private static Context mContext;
	private static LogListener mListener;

	public interface LogListener {
		public abstract void log(String txt);
	}

	public static void setContext(Context context) {
		mContext = context;
	}

	public static void setURL(String url) {
		uploadURL = url;
	}

	public static void setLogListener(LogListener listener) {
		mListener = listener;
	}

	public static String getResponse() {
		return mResponse;
	}

	public static String getErrorMessage() {
		Log.e(tag, "getErrorMessage " + errorMessage);
		return errorMessage;
	}

	public static synchronized boolean upload(String userName, String authKey,
			RecordItem item) {

		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
				7);

		nameValuePairs.add(new BasicNameValuePair(USER_NAME, userName));
		nameValuePairs.add(new BasicNameValuePair(TIME_CREATED, String
				.valueOf(item.getTime())));
		// nameValuePairs.add(new
		// BasicNameValuePair(TOTAL_LENGTH,String.valueOf(item.getLength())));
		nameValuePairs.add(new BasicNameValuePair(FIRST_MARK, String
				.valueOf(item.getMark(0))));
		nameValuePairs.add(new BasicNameValuePair(SECOND_MARK, String
				.valueOf(item.getMark(1))));
		nameValuePairs.add(new BasicNameValuePair(THIRD_MARK, String
				.valueOf(item.getMark(2))));
		nameValuePairs.add(new BasicNameValuePair(FOURTH_MARK, String
				.valueOf(item.getMark(3))));
		nameValuePairs.add(new BasicNameValuePair(EXTRA, item.getExtra()));
		return postToServer(uploadURL, nameValuePairs);
	}

	public static boolean synchronizeRecords(String userName, String authKey,
			ArrayList<RecordItem> mList) {
		boolean ok = true;
		for (RecordItem item : mList) {
			ok = upload(userName, authKey, item);
			Log.e(tag, "item");
			if (!ok)
				break;
		}
		return ok;
	}

	private static boolean postToServer(String url,
			ArrayList<NameValuePair> parameters) {

		getHTTPClient();

		mPost = new HttpPost(url);

		for (NameValuePair pair : parameters)
			Log.e(tag, pair.getName() + ":" + pair.getValue());

		try {

			mPost.setEntity(new UrlEncodedFormEntity(parameters));

		} catch (UnsupportedEncodingException e) {

			Log.e(tag, e.toString());
			e.printStackTrace();
			mResponse = "URL not correct !";

			return false;
		}

		try {
			HttpResponse response = mClient.execute(mPost);
			StatusLine status_line = response.getStatusLine();
			int status_code = status_line.getStatusCode();
			/*
			 * Not ok Response
			 */
			if (status_code != 200) {

				errorMessage = mContext.getResources().getString(
						R.string.webResponse_protocol_error);
				log("Error Status code = " + String.valueOf(status_code));

				return false;
			}
			log("Server ok , code = 200");

			BufferedReader reader = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent(), "UTF-8"));
			mResponse = reader.readLine();
			log(mResponse);

		} catch (ClientProtocolException e) {

			e.printStackTrace();
			Log.e(tag, "ClientProtocolException " + e.toString());
			log("ClientProtocolException " + e.toString());

			errorMessage = mContext.getResources().getString(
					R.string.webResponse_protocol_error);
			return false;

		} catch (IOException e) {

			e.printStackTrace();
			Log.e(tag, "IOException " + e.toString());
			log("IOException " + e.toString());

			errorMessage = mContext.getResources().getString(
					R.string.webResponse_protocol_error);
			return false;

		}
		return true;
	}

	public static JSONObject parseResponse() {
		if (mResponse == null)
			return null;
		try {
			return new JSONObject(mResponse);
		} catch (JSONException e1) {
			e1.printStackTrace();
			Log.e(tag, e1.toString());
			log("JSONException " + e1.toString());
			log("Response " + mResponse);

			errorMessage = mContext.getResources().getString(
					R.string.webResponse_parsing_error);
			return null;
		}
	}

	private static void log(String data) {
		if (mListener != null)
			mListener.log(data);
		Log.e(tag, data);
	}

	private static void getHTTPClient() {
		if (mClient == null) {
			HttpParams params = new BasicHttpParams();
			HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
			HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
			HttpProtocolParams.setUseExpectContinue(params, false);
			HttpConnectionParams.setConnectionTimeout(params, 10000);
			HttpConnectionParams.setSoTimeout(params, 10000);
			ConnManagerParams.setMaxTotalConnections(params, 100);
			ConnManagerParams.setTimeout(params, 30000);

			SchemeRegistry registry = new SchemeRegistry();
			registry.register(new Scheme("http", PlainSocketFactory
					.getSocketFactory(), 80));
			registry.register(new Scheme("https", PlainSocketFactory
					.getSocketFactory(), 80));
			ThreadSafeClientConnManager manager = new ThreadSafeClientConnManager(
					params, registry);
			mClient = new DefaultHttpClient(manager, params);
		}
	}
}
