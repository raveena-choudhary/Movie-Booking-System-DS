package Replica1.util.db;

import Replica1.util.booking.Booking;
import Replica1.util.booking.Movie;
import util.Enums.ServerEnum;
import util.Enums.SlotEnum;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class MovieTicketBookingDB {

    // private static final int BOOKING_CAPACITY = 50;

    //<name,<movieId,Capacity>>
    public Map<String, Map<String, Integer>> movieTicketBookingRecords = new ConcurrentHashMap<>();

    //customer DB <customerId,<movieId,Booking>>
    public Map<String, Map<String, List<Booking>>> customerRecords = new ConcurrentHashMap<>();


    //add movie in Map
    public boolean addMovie(String movieID, String movieName, int bookingCapacity) {
        if (!movieTicketBookingRecords.containsKey(movieName)) //movie record does not exist in map
        {
            Map<String, Integer> movieMap = new HashMap<String, Integer>();
            movieMap.put(movieID, Integer.valueOf(bookingCapacity));

            movieTicketBookingRecords.put(movieName, movieMap);

        } else  // movie record exists then bookingCapacity is updated
        {
            Map<String, Integer> movieMap = movieTicketBookingRecords.get(movieName);
            movieMap.put(movieID, bookingCapacity);

            //movieTicketBookingRecords.put(movieName, movieMap);
        }

        return true;
    }


    //delete movie from Movie DB
    public boolean deleteMovie(String movieID, String movieName) {
        if (movieTicketBookingRecords.containsKey(movieName)) // movie record exists in map
        {

            //if no client has booked any ticket for that slot.
            Map<String, Integer> movieRecord = movieTicketBookingRecords.get(movieName);
            movieRecord.remove(movieID);
            return true;
        }

        return false;
    }

    //delete movie from customer DB
    public boolean deleteMovieForCustomer(String movieID, String movieName, String customerId) {
        boolean isCustomerExist = customerRecords.containsKey(customerId);
        if(!isCustomerExist){
            System.out.println("Customer "+customerId+" does not exist!");
            return false;
        }
       // movie record exists in customer map
            Map<String, List<Booking>> movieRecordInCustomerDB = customerRecords.get(customerId);
//            movieRecordInCustomerDB.remove(movieID);
            List<Booking> bookings = movieRecordInCustomerDB.get(movieID);

            ListIterator<Booking> bookingListIterator = bookings.listIterator();
            while(bookingListIterator.hasNext()){
                Booking booking = bookingListIterator.next();
                if(booking.getMovie().getName().equals(movieName))
                {
                    bookings.remove(booking);
                    break;
                }
            }

        return true;
    }

    public boolean deleteMovieAndBookNextSlot(String movieID, String movieName) {
        //if client booked ticket for deleted slot
        for (Map.Entry<String, Map<String, List<Booking>>> customerWithMovieBooking : customerRecords.entrySet()) {
            String movieIDBookedByCustomer = "";

            if (customerWithMovieBooking != null && customerWithMovieBooking.getValue().get(movieID) != null) {
                // if movieId is present in booking, then book next available movie show for that movieName and delete the movie
                List<Booking> bookings = customerWithMovieBooking.getValue().get(movieID);
                ListIterator<Booking> bookingListIterator = bookings.listIterator();
                while(bookingListIterator.hasNext()){
                    Booking booking = bookingListIterator.next();
                    if(booking.getMovie().getName().equals(movieName))
                    {
                        movieIDBookedByCustomer=booking.getMovie().getMovieId();
                        break;
                    }
                }
                //movieIDBookedByCustomer = customerWithMovieBooking.getValue().get(movieID).getMovie().getMovieId();
            }

            if (movieIDBookedByCustomer != null && movieIDBookedByCustomer.equals(movieID)) {

                // book next movie available show with same MovieName for that client.
                //method for getNextAvailableSlot
                String movieIdForNextSlot = getNextAvailableSlot(movieName,movieID);
                if(movieIdForNextSlot==null || movieIdForNextSlot.isEmpty()){
                    System.out.println("No slot available for movie "+movieName);
                }
                if (movieIdForNextSlot != null) {
                    //lookup movieTicketBookingRecords and verify the capacity to auto book for customer.
                    Map<String, Integer> movieMap = movieTicketBookingRecords.get(movieName);
                    if (movieMap.containsKey(movieIdForNextSlot)) {
                        //check book capacity with customer number of tickets booked.
                        List<Booking> bookings = customerWithMovieBooking.getValue().get(movieID);
                        int numberOfTicketsBooked=0;
                        ListIterator<Booking> bookingListIterator = bookings.listIterator();
                        while(bookingListIterator.hasNext()){
                            Booking booking = bookingListIterator.next();
                            if(booking.getMovie().getName().equals(movieName))
                            {
                                numberOfTicketsBooked=booking.getNumberOfTickets();
                                break;
                            }
                        }

                        //int numberOfTicketsBooked = customerWithMovieBooking.getValue().get(movieID).getNumberOfTickets();
                        int remainingBookingCapacity = movieMap.get(movieIdForNextSlot);
                        if (remainingBookingCapacity - numberOfTicketsBooked >= 0) { //not overbook the remaning capacity
                            //book next available slot
                            Booking bookingNextSlot = createBooking(movieIdForNextSlot, movieName, numberOfTicketsBooked);
                            bookTickets(customerWithMovieBooking.getKey(),movieID, bookingNextSlot,numberOfTicketsBooked);
                        }
                    }
                }
                //remove movie from customer dB
                deleteMovieForCustomer(movieID, movieName,customerWithMovieBooking.getKey());

            }
        }

        return true;
    }


    //get Movie Id for booking
    public String getNextAvailableSlot(String movieName, String movieId) {
        //lookup movieTicketBookingRecords
        Map<String, Integer> movieMap = movieTicketBookingRecords.get(movieName);
        //Map<String, Integer> sortedMapWithMovieId = new HashMap<String, Integer>();

        if (movieMap != null && movieMap.size() > 1) {
            List<String> movieIdsList = new ArrayList<String>();

            //add all movie IDs in a list to sort using comparator
            for (Map.Entry<String, Integer> entry : movieMap.entrySet()) {
                movieIdsList.add(entry.getKey());
            }

            Collections.sort(movieIdsList, new Comparator<String>() {
                @Override
                public int compare(String movieIdObject1, String movieIdObject2) {
                    Movie movie1 = new Movie(movieIdObject1);
                    Movie movie2 = new Movie(movieIdObject2);
                    int dateCompare = movie1.getDate().compareTo(movie2.getDate());
                    int slotCompare = compareWithSlot(movie1.getSlot(), movie2.getSlot());

                    return (dateCompare != 0) ? dateCompare : slotCompare;

                }
            });

            //return next movie from sorted movies list
            return movieIdsList.get(movieIdsList.indexOf(movieId)+1);
        }


        return null;


    }

    public int getNumberOfTicketsForCustomerBooking(String customerId, String movieID,String movieName) {
        Map<String, List<Booking>> bookingMap = customerRecords.get(customerId);
        if (bookingMap != null) {
            if (bookingMap.containsKey(movieID))  //for customer booking for a movie exists, then get number of tickets.
            {
                List<Booking> bookings = bookingMap.get(movieID);
                ListIterator<Booking> bookingListIterator = bookings.listIterator();
                while(bookingListIterator.hasNext()){
                    Booking booking = bookingListIterator.next();
                    if(booking.getMovie().getName().equals(movieName))
                    {
                        return booking.getNumberOfTickets();
                    }
                }
//                if(bookingMap.get(movieID).getMovie().getName().equals(movieName))
//                {
//                    return bookingMap.get(movieID).getNumberOfTickets();
//                }

            }
        }

        return 0;
    }

    public synchronized String getAvailableShowsForMovie(String movieName) {
        String listOfAvailableShowsForMovie = "";
        Map<String, Integer> movieMap = movieTicketBookingRecords.get(movieName);
        if (movieMap != null) {
            listOfAvailableShowsForMovie = movieMap.entrySet().stream().map(m -> m.getKey() + " " + m.getValue()).collect(Collectors.joining(","));
            if (listOfAvailableShowsForMovie != null)
                return listOfAvailableShowsForMovie;
        }

        return listOfAvailableShowsForMovie;
    }

    //<movieID,Booking>
    public synchronized String getAllMoviesBookedByCustomer(String customerID) {

        String bookingDetails = "";

        Map<String, List<Booking>> bookingsByCustomerMap = customerRecords.get(customerID);
        if (bookingsByCustomerMap != null) {
            // for (Map.Entry<String, Booking> booking : bookingsByCustomerMap.entrySet()) {
            for (Map.Entry<String, List<Booking>> booking : bookingsByCustomerMap.entrySet()) {
                List<Booking> bookings = booking.getValue();
                ListIterator<Booking> bookingListIterator = bookings.listIterator();
                while (bookingListIterator.hasNext()) {
                    Booking movieBooking = bookingListIterator.next();
                    bookingDetails = bookingDetails.trim() + movieBooking.getMovie().getName() + ":" + ServerEnum.getEnumNameForValue(movieBooking.getMovie().getAreaCode()) + " " + SlotEnum.getEnumNameForValue(movieBooking.getMovie().getSlot()) + " " + movieBooking.getMovie().getDate() + " " + movieBooking.getNumberOfTickets() + ",";
                }
            }
        }
        return bookingDetails;
    }

    public synchronized int getAvailableCapacity(String movieName, String movieId) {

        int availableCapacity=0;

        Map<String, Integer> movieMap = movieTicketBookingRecords.get(movieName);
        if (movieMap != null && movieMap.get(movieId)!=null) {
            availableCapacity = movieMap.get(movieId);
        }
        return availableCapacity;
    }

    public boolean bookTickets(String customerId, String movieId, Booking booking, int numberOfTickets) {

        Map<String, List<Booking>> bookingMap = customerRecords.get(customerId);
        if (bookingMap == null) {
            bookingMap = new HashMap<String, List<Booking>>();
        }

        if(bookingMap.get(movieId)!=null)
            {
                List<Booking> bookingList = bookingMap.get(movieId);
                ListIterator<Booking> bookingListIterator = bookingList.listIterator();
                while(bookingListIterator.hasNext()){
                    Booking bookingMapI = bookingListIterator.next();

                    Movie movie = new Movie(movieId);
                    String movieName = bookingMapI.getMovie().getName();
                    String slot=bookingMapI.getMovie().getSlot();
                    String date=bookingMapI.getMovie().getDate();
                    /*if(slot.equals(movie.getSlot()) && date.equals(movie.getDate()))  //different theatres, same slot,same date, and same movieName
                    {
                        return false;
                    }*/

                    if(movieName.equals(booking.getMovie().getName()))
                    {
                        bookingMapI.setNumberOfTickets(booking.getNumberOfTickets()); //update number of tickets for existing booking
                        break;
                    }

                    else {
                        bookingList.add(booking); //add new booking to existing list
                    }

                }
            }
            else {

//            for (List<Booking> bookingL : bookingMap.values()) {
//                ListIterator<Booking> bookingListIterator = bookingL.listIterator();
//                while (bookingListIterator.hasNext()) {
//                    Booking bookingMapI = bookingListIterator.next();
//
//                    Movie movie = new Movie(movieId);
//                    String movieName = bookingMapI.getMovie().getName();
//                    String slot = bookingMapI.getMovie().getSlot();
//                    String date = bookingMapI.getMovie().getDate();
//                    if (slot.equals(movie.getSlot()) && date.equals(movie.getDate()))  //different theatres, same slot,same date, and same movieName
//                    {
//                        System.out.println("Movie slot and date matched!!");
//                        return false;
//                    } else {

                        System.out.println("Movie slot and date not matched!!Book tickets");
                        List<Booking> bookingList = new ArrayList<>();
                        bookingList.add(booking);
                        bookingMap.put(booking.getMovie().getMovieId(), bookingList);
//                    }
//                }
            }
//        }

//        bookingMap.put(booking.getMovie().getMovieId(), bookingList);
        customerRecords.put(customerId, bookingMap);
        //updated Db with capacity
        return updateCapacityAfterMovieBooked(booking.getMovie().getMovieId(), booking.getMovie().getName(), numberOfTickets);
    }

    public boolean updateCapacityAfterMovieBooked(String movieID, String movieName, int numberOfTicketsBooked) {
        Map<String, Integer> movieMap = movieTicketBookingRecords.get(movieName);
        if (movieMap != null) {
            if (movieMap.get(movieID) != null) {
                int availableCapacity = movieMap.get(movieID);
                if (numberOfTicketsBooked > 0 && availableCapacity >= numberOfTicketsBooked) {
                    movieMap.put(movieID, availableCapacity - numberOfTicketsBooked);
                    return true;
                }
            }
        }
        return false;
    }

    public synchronized boolean updateCapacityAfterMovieCancelled(String movieID, String movieName, int numberOfTickets) {
        // if (movieTicketBookingRecords.containsKey(movieName)) {
        Map<String, Integer> movieMap = movieTicketBookingRecords.get(movieName);
        if (movieMap != null) {
            int updatedCapacity = movieMap.get(movieID) + numberOfTickets;
            movieMap.put(movieID, updatedCapacity);
            return true;

        }

        // }

        return false;
    }

    public Booking createBooking(String movieId, String movieName, int numberOfTickets) {
        Movie movie = new Movie(movieId);
        movie.setName(movieName);
        Booking booking = new Booking(movie, numberOfTickets);
        return booking;
    }

    public synchronized Map<String, List<Booking>> getAllTicketsBookedByCustomer(String customerID) {
        Map<String, List<Booking>> bookingsByCustomerMap = customerRecords.get(customerID);
        return bookingsByCustomerMap;
    }

    public boolean cancelBookingAndUpdateBookingCapacity(String customerID, String movieID, String movieName, int numberOfTickets) {

        Map<String, List<Booking>> bookingMap = customerRecords.get(customerID);
        String movieBookedByCustomer = "";
        int numberOfTicketsBookedByCustomer =0;

        if (bookingMap != null) {
            if (bookingMap.get(movieID) != null) {
                List<Booking> bookings = bookingMap.get(movieID);
                ListIterator<Booking> bookingListIterator = bookings.listIterator();
                while(bookingListIterator.hasNext()){
                    Booking booking = bookingListIterator.next();
                    if(booking.getMovie().getName().equals(movieName))
                    {
                        movieBookedByCustomer=booking.getMovie().getName(); //get movie name
                        numberOfTicketsBookedByCustomer=booking.getNumberOfTickets(); //get movie tickets
                        break;
                    }
                }
//                String movieBookedByCustomer = bookingMap.get(movieID).getMovie().getName();
//                int numberOfTicketsBookedByCustomer = bookingMap.get(movieID).getNumberOfTickets(); //this is to update booking capacity in movieDb

                //change this method for list<booking>
                if (movieBookedByCustomer.equals(movieName) && numberOfTicketsBookedByCustomer == numberOfTickets) {
                    //movie db updated after cancellation of tickets by customer
                    updateCapacityAfterMovieCancelled(movieID, movieBookedByCustomer, numberOfTickets);

                    //remove movie for customer
                   // bookings = bookingMap.get(movieID);
                    bookingListIterator = bookings.listIterator();
                    while(bookingListIterator.hasNext()){
                        Booking booking = bookingListIterator.next();
                        if(booking.getMovie().getName().equals(movieName))
                        {
                            bookings.remove(booking);
                            break;
                        }
                    }
                    //if customer has no booking left for a particular movie ID, remove the redundant movie id
                    if(bookingMap.get(movieID).size()==0){
                        bookingMap.remove(movieID);
                    }
                   // bookingMap.remove(movieID);

                    return true;
                } else if (movieBookedByCustomer.equals(movieName) && numberOfTickets < numberOfTicketsBookedByCustomer) //if customer wants to cancel less number of tickets
                {
                    int updatedNumberOfTickets = numberOfTicketsBookedByCustomer - numberOfTickets;
                    updateCapacityAfterMovieCancelled(movieID, movieBookedByCustomer, numberOfTickets);

                    //update movie details for customer
                    //Booking booking = bookingMap.get(movieID);

                    //List<Booking> bookingList = new ArrayList<>();

                    bookings = bookingMap.get(movieID);
                    bookingListIterator = bookings.listIterator();
                    while(bookingListIterator.hasNext()){
                        Booking booking = bookingListIterator.next();
                        if(booking.getMovie().getName().equals(movieName))
                        {
                            //booking.getMovie().setName(movieBookedByCustomer);
                            booking.setNumberOfTickets(updatedNumberOfTickets);

                            break;
                        }
                    }
                    return true;


//                    bookingMap.put(movieID, bookings);

//                    booking.setNumberOfTickets(updatedNumberOfTickets);
//                    bookingMap.put(movieID, booking);

                }

            }

        }

        //if customer has no booking left remove customer from customer records
        if(customerRecords.get(customerID).size()==0){
            customerRecords.remove(customerID);
        }

        return false;
    }

    public Map<String, Map<String, Integer>> getMovieDB() {
        return movieTicketBookingRecords; //Collections.unmodifiableMap(movieTicketBookingRecords);
    }

    //for booking
    public boolean isMovieAlreadyBookedByCustomer(String customerID, String movieID, String movieName, String moviesBookedByCustomer) {
        Map<String, List<Booking>> bookingMap = customerRecords.get(customerID);
        Movie movie = new Movie(movieID);

        boolean isMovieBooked = false;

        if(bookingMap !=null){
            String matchedMovieId=bookingMap.keySet().stream().filter(m -> m.equals(movieID)).findFirst().orElseGet(() -> "").toString();
            String movieNameAlreadyBooked = "";
            if (matchedMovieId != null && !matchedMovieId.equals("")) {

                List<Booking> bookings = bookingMap.get(matchedMovieId);
                ListIterator<Booking> bookingListIterator = bookings.listIterator();

                while(bookingListIterator.hasNext()) {
                    Booking booking = bookingListIterator.next();
                    movieNameAlreadyBooked = booking.getMovie().getName();

                    //movie name booked by customer
                    //if movieName not matches
                    if (!movieNameAlreadyBooked.equals(movieName)) {
                        isMovieBooked = true;
                        break;
                    }
                }
               // return isMovieBooked;
            }
        }
        if (moviesBookedByCustomer!=null && !moviesBookedByCustomer.isEmpty() && !moviesBookedByCustomer.equals("")) {

           /* if(bookingMap.get(movieID)!=null)
            {*/
                //slot and date matches
                String slotAndDateForAMovieUserBookingNow = SlotEnum.getEnumNameForValue(movie.getSlot())+" " + movie.getDate();

                List<String> movies = convertStringToList(moviesBookedByCustomer);

                //check all keys to validate any movie booked with above slot and date and if any found , extract first one and check movieName
               // getAllTicketsBookedByCustomer(customerID);
                List<String> matchedMovieIdWithSlotAndDate = movies.stream
                        ().filter(m -> m.contains(slotAndDateForAMovieUserBookingNow)).map(m->extractMovieFromString(m)).collect(Collectors.toList());


                //when movieIds are same
                //can book same movie multiple times if capacity is not full


                if(matchedMovieIdWithSlotAndDate!=null && !matchedMovieIdWithSlotAndDate.isEmpty()) // cannot book the tickets of the same movie for the same show in different theatres.
                {


                    ListIterator<String> bookingListIterator = matchedMovieIdWithSlotAndDate.listIterator();

                    while(bookingListIterator.hasNext()) {
                        String movieIdForBookedMovie = bookingListIterator.next();
                        if (movieIdForBookedMovie.substring(3).equals(movieID.substring(3))) {
                            isMovieBooked = true;
                            break;
                        }

                        //if movieName matches
//                        if (movieNameAlreadyBooked.equals(movieName)) {
//                            isMovieBooked = true;
//                            break;
//                        }
                }

                    //movieNameAlreadyBooked = bookingMap.get(matchedMovieId).getMovie().getName();
//                //if movieName matches
//                if (!movieNameAlreadyBooked.equals(movieName)) {
//                    return true;
//                }
//                return true;

                }
            }

       // }

        return isMovieBooked;
    }

    //done for assignment -2
    public boolean isMovieAlreadyBooked(String customerID, String movieID, String movieName,String moviesBookedByCustomer) {
        Map<String, List<Booking>> bookingMap = customerRecords.get(customerID);
        Movie movie = new Movie(movieID);

        boolean isMovieBooked = false;
        //slot and date matches
//
//        String slotAndDateForAMovieUserBookingNow = SlotEnum.getEnumNameForValue(movie.getSlot())+" " + movie.getDate();
//        List<String> movies = convertStringToList(moviesBookedByCustomer);
//        List<String> matchedMovieIdWithSlotAndDate = movies.stream
//                ().filter(m -> m.contains(slotAndDateForAMovieUserBookingNow)).map(m->extractMovieFromString(m)).collect(Collectors.toList());
//
//        if(matchedMovieIdWithSlotAndDate.size()>0)
//        {
//            isMovieBooked= true;
//        }

        if (bookingMap != null){
            String matchedMovieId=bookingMap.keySet().stream().filter(m -> m.equals(movieID)).findFirst().orElseGet(() -> "").toString();
            String movieNameAlreadyBooked = "";

            //when movieIds are same
            //can book same movie multiple times if capacity is not full

            if (matchedMovieId != null && !matchedMovieId.equals("")) {
//                    isMovieBooked = true;

                List<Booking> bookings = bookingMap.get(matchedMovieId);
                ListIterator<Booking> bookingListIterator = bookings.listIterator();

                while(bookingListIterator.hasNext()) {
                    Booking booking = bookingListIterator.next();
                    movieNameAlreadyBooked = booking.getMovie().getName();

                    //movie name booked by customer
                    //if movieName not matches
                    if (movieNameAlreadyBooked.equals(movieName)) {
                        isMovieBooked = true;
                        break;
                    }
                }
               // return isMovieBooked;
            }
        }

        if (moviesBookedByCustomer!=null && !moviesBookedByCustomer.isEmpty()) {

           /* if(bookingMap.get(movieID)!=null)
            {*/
                //slot and date matches
                String slotAndDateForAMovieUserBookingNow = SlotEnum.getEnumNameForValue(movie.getSlot())+" " + movie.getDate();

                List<String> movies = convertStringToList(moviesBookedByCustomer);

                //check all keys to validate any movie booked with above slot and date and if any found , extract first one and check movieName
                //getAllTicketsBookedByCustomer(customerID);
                List<String> matchedMovieIdWithSlotAndDate = movies.stream
                    ().filter(m -> m.contains(slotAndDateForAMovieUserBookingNow)).map(m->extractMovieFromString(m)).collect(Collectors.toList());

            if(matchedMovieIdWithSlotAndDate!=null && !matchedMovieIdWithSlotAndDate.isEmpty()) // cannot book the tickets of the same movie for the same show in different theatres.
            {


                ListIterator<String> bookingListIterator = matchedMovieIdWithSlotAndDate.listIterator();

                while(bookingListIterator.hasNext()) {
                    String movieIdForBookedMovie = bookingListIterator.next();
                    if (movieIdForBookedMovie.substring(3).equals(movieID.substring(3))) {
                        isMovieBooked = true;
                        break;
                    }

                    //if movieName matches
//                        if (movieNameAlreadyBooked.equals(movieName)) {
//                            isMovieBooked = true;
//                            break;
//                        }
                }

                    //movieNameAlreadyBooked = bookingMap.get(matchedMovieId).getMovie().getName();
//                //if movieName matches
//                if (!movieNameAlreadyBooked.equals(movieName)) {
//                    return true;
//                }
//                return true;

                }
            }

        //}

        return isMovieBooked;
    }


    public boolean isMovieExist(String movieName, String movieID) {
        Map<String, Integer> movieMap = movieTicketBookingRecords.get(movieName);
        if (null == movieMap) {
            return false;
        }
        return movieMap.get(movieID) != null ? true : false;
    }

    private int compareWithSlot(String slot1, String slot2) {
        if ((slot1.equals("M") && slot2.equals("A")) || (slot1.equals("M") && slot2.equals("E")) ||
                (slot1.equals("A") && slot2.equals("E"))) {
            return -1;
        } else if (slot1.equals(slot2)) {
            return 0;
        } else {
            return 1;
        }
    }

    public synchronized int getBookingCapactiyForAMovie(String movieName, String movieID) {
        Map<String, Integer> movieMap = movieTicketBookingRecords.get(movieName);
        if (movieMap != null && movieMap.get(movieID)!=null) {
            return movieMap.get(movieID);
        }

        return 0;
    }

    public synchronized List<String> getAllMovieNames() {
        List<String> moviesList = new ArrayList<String>();

        if (movieTicketBookingRecords.keySet() != null) {
            for (String movieName : movieTicketBookingRecords.keySet()) {
                moviesList.add(movieName);
            }
        }


        return moviesList;
    }

    public synchronized List<String> getAllMovieIdsForMovie(String movieName) {
        List<String> movieIdsList = new ArrayList<String>();

        Map<String, Integer> movies = movieTicketBookingRecords.get(movieName);

        if (movies != null) {
            for (String movieId : movies.keySet()) {
                movieIdsList.add(movieId);
            }
        }


        return movieIdsList;
    }

    public boolean isMovieSlotExist(String movieName,String movieID, int bookingCapacity) {

        Map<String, Integer> movieSlot = movieTicketBookingRecords.get(movieName);
            if (movieSlot != null && movieSlot.get(movieID)!=null && movieSlot.get(movieID)==bookingCapacity) {
                return true;
            }
        return false;
    }

    private List<String> convertStringToList(String str)
    {
        return Arrays.asList(str.split(","));
    }

    private String extractMovieFromString(String movieDetail){

        String[] dataForMovie = movieDetail.split(" ");
        String movieLocFromBookingSchedule = ServerEnum.valueOf(dataForMovie[0].substring(dataForMovie[0].indexOf(":")+1).trim()).value();
        String slotFromBookingSchedule = String.valueOf(SlotEnum.valueOf(dataForMovie[1]).value());
        String dateFromBookingSchedule = dataForMovie[2];
        return movieLocFromBookingSchedule+slotFromBookingSchedule+dateFromBookingSchedule;
    }

}
