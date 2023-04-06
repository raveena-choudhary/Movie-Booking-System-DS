package utils;

import java.time.LocalDate;

public class Utils {

    public static LocalDate getLocalDateObFromMovieID(
            String movieID) {
        String movieDateTime1 = movieID.substring(4);


        int month1 = Integer.parseInt(movieDateTime1.substring(2, 4));
        int day1 = Integer.parseInt(movieDateTime1.substring(0, 2));
        int year1 = Integer.parseInt("20" + movieDateTime1.substring(4, 6));

        LocalDate movieTime = LocalDate.of(year1, month1, day1);
        return movieTime;
    }

    public static String getMovieTheatreFromID(String movieID) {
        return movieID.substring(0, 3);
    }

}
