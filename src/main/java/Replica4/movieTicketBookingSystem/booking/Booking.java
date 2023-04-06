package Replica4.movieTicketBookingSystem.booking;

public class Booking {
    private final String movieName;
    private int capacity;
    private String movieID;

    public Booking(String movieName, String movieID, int capacity) {
        this.movieName = movieName;
        this.movieID = movieID;
        this.capacity = capacity;
    }

    public String getMovieName() {
        return movieName;
    }

    @Override
    public String toString() {
        return "Booking{" +
                "movieName='" + movieName + '\'' +
                ", capacity=" + capacity +
                ", movieID='" + movieID + '\'' +
                '}';
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public String getMovieID() {
        return movieID;
    }

    public void setMovieID(String movieID) {
        this.movieID = movieID;
    }
}
