package Replica2.util.booking;

import util.Enums.ServerEnum;
import util.Enums.SlotEnum;

public class Movie {
	private String name;
	private String areaCode;
	private String slot;
	private String date;
	
	public Movie() {}
	public Movie(String movieId) {

		this.areaCode = movieId.substring(0,3);
		this.slot = movieId.substring(3,4);
		this.date=movieId.substring(4);
	}

	public Movie(String name, String areaCode, SlotEnum slotEnum, String date) {
		this.name = name;
		this.areaCode = areaCode;
		this.slot = slotEnum.value();
		this.date=date;
	}

	public Movie(String name, ServerEnum server, SlotEnum slotEnum, String date) {

		this.name = name;
		this.areaCode = server.value();
		this.slot = slotEnum.value();
		this.date=date;
	}

	public String getAreaCode() {
		return areaCode;
	}
	public void setAreaCode(String areaCode) {
		this.areaCode = areaCode;
	}
	public String getSlot() {
		return slot;
	}
	public void setSlot(String slot) {
		this.slot = slot;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	
	public String getMovieId() {
		return areaCode+slot+ date;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "Movie [name="+name+", areaCode=" + areaCode + ", slot=" + slot + ", date=" + date + "]";
	}
	
}
