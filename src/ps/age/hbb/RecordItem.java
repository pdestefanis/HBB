package ps.age.hbb;

import java.io.Serializable;

public class RecordItem implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3077557198841929495L;
	private long id;
	private long firstMark = -1;
	private long secondMark = -1;
	private long thirdMark = -1;
	private long fourthMark = -1;
	private int length;
	private String path;
	private String note;
	private long time;

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

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
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

}
