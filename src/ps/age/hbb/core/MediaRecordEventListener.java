package ps.age.hbb.core;

public interface MediaRecordEventListener {
	/*
	 * called when an exception event happnes ex: storage unmounted or phone
	 * call received
	 */
	public void onError();

	/*
	 * Exception doesn't exist anymore and we are free to resume our work
	 */
	public void onReady();

}
