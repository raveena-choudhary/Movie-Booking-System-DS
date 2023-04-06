package Replica3.util.booking;

public class Booking {

	private Movie movie;
	private int numberOfTickets;
	
	public Booking(Movie movie, int numberOfTickets) {
		this.movie=movie;
		this.numberOfTickets = numberOfTickets;
	}
	public Movie getMovie() {
		return movie;
	}
	public void setMovie(Movie movie) {
		this.movie = movie;
	}
	public int getNumberOfTickets() {
		return numberOfTickets;
	}
	public void setNumberOfTickets(int numberOfTickets) {
		this.numberOfTickets = numberOfTickets;
	}
	@Override
	public String toString() {
		return "Booking [movie=" + movie + ", numberOfTickets=" + numberOfTickets + "]";
	}	
	
}
