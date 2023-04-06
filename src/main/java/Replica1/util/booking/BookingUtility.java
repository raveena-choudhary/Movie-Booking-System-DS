/** @Author: Raveena Choudhary, 40232370 **/
package Replica1.util.booking;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class BookingUtility {

    //get MovieIds for a whole week : Monday to Sunday with all slot(M,A,E)
    DateTimeFormatter dateTimeFormatter;
    public List<String> getMovieIdsForWeek(String movieID){
        dateTimeFormatter= DateTimeFormatter.ofPattern("ddMMyy");
        List<String> movieIds = new ArrayList<>();
        LocalDate today = LocalDate.now();
        LocalDate monday = today;

        //Go backward to get Monday
        while (monday.getDayOfWeek() != DayOfWeek.MONDAY) {
            monday = monday.minusDays(1);
        }

//        // Go forward to get Sunday
//        LocalDate sunday = today;
//        while (sunday.getDayOfWeek() != DayOfWeek.SUNDAY) {
//            sunday = sunday.plusDays(1);
//        }

        Movie movie = null;
        for(int i=0;i<=7;i++){
            movie = new Movie(movieID);
            movie.setDate(monday.plusDays(i).format(dateTimeFormatter));
            movieIds.add(movie.getMovieId());
        }
        return movieIds;
    }

    //method : validates todays date with movie date to disable booking of tickets previous than todays date.
    public boolean validateMovieDateForPastWeek(String movieID)
    {
        dateTimeFormatter= DateTimeFormatter.ofPattern("ddMMyy");
        Movie movie = new Movie(movieID);
        LocalDate movieDate  = LocalDate.parse(movie.getDate(),dateTimeFormatter);
        LocalDate today = LocalDate.now();

        return today.isAfter(movieDate); // true, if movieDate is of previous date compared to today
    }

    //method : validates todays date with movie date to disable booking of tickets previous than todays date.
    public boolean validateMovieDateForNextWeek(String movieID)
    {
        dateTimeFormatter= DateTimeFormatter.ofPattern("ddMMyy");
        Movie movie = new Movie(movieID);
        LocalDate movieDate  = LocalDate.parse(movie.getDate(),dateTimeFormatter);
        LocalDate today = LocalDate.now();
        LocalDate nextWeek = today.plusDays(7);

        return movieDate.isAfter(nextWeek); // true, if movieDate is after next week from current date
    }

}
