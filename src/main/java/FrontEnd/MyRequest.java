package FrontEnd;


public class MyRequest {
    private String methodCalled = "null";
    private String clientID = "null";
    private String movieName = "null";
    private String oldMovieName = "null";
    private String movieId = "null";
    private String oldMovieId = "null";

    private String FeIpAddress = FE.FE_IP_Address;
    private int bookingCapacity = 0;

    private int numberOfTickets =0;
    private int sequenceNumber = 0;
    private String MessageType = "00";
    private int retryCount = 1;

    public MyRequest(String function, String clientID) {
        setMethodCalled(function);
        setClientID(clientID);
    }

    public MyRequest(int rmNumber, String bugType) {
        setMessageType(bugType + rmNumber);
    }

    public String getMethodCalled() {
        return methodCalled;
    }

    public void setMethodCalled(String methodCalled) {
        this.methodCalled = methodCalled;
    }

    public String getClientID() {
        return clientID;
    }

    public void setClientID(String clientID) {
        this.clientID = clientID;
    }

    public String getMovieName() {
        return movieName;
    }

    public void setMovieName(String movieName) {
        this.movieName = movieName;
    }

    public String getOldMovieName() {
        return oldMovieName;
    }

    public void setOldMovieName(String oldMovieName) {
        this.oldMovieName = oldMovieName;
    }

    public String getMovieId() {
        return movieId;
    }

    public void setMovieId(String movieId) {
        this.movieId = movieId;
    }

    public String getOldMovieId() {
        return oldMovieId;
    }

    public void setOldMovieId(String oldMovieId) {
        this.oldMovieId = oldMovieId;
    }

    public int getNumberOfTickets() {
        return numberOfTickets;
    }

    public void setNumberOfTickets(int numberOfTickets) {
        this.numberOfTickets = numberOfTickets;
    }

    public int getBookingCapacity() {
        return bookingCapacity;
    }

    public void setBookingCapacity(int bookingCapacity) {
        this.bookingCapacity = bookingCapacity;
    }

    public String noRequestSendError() {
        return "request: " + getMethodCalled() + " from " + getClientID() + " not sent";
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public String getFeIpAddress() {
        return FeIpAddress;
    }

    public void setFeIpAddress(String feIpAddress) {
        FeIpAddress = feIpAddress;
    }

    public String getMessageType() {
        return MessageType;
    }

    public void setMessageType(String messageType) {
        MessageType = messageType;
    }

    public boolean haveRetries() {
        return retryCount > 0;
    }

    public void countRetry() {
        retryCount--;
    }

    //Message Format: Sequence_id;FrontIpAddress;Message_Type;function(addEvent,...);userID; newEventID;newEventType; oldEventID; oldEventType;bookingCapacity
    @Override
    public String toString() {
        return getSequenceNumber() + ";" +
                getFeIpAddress().toUpperCase() + ";" +
                getMessageType().toUpperCase() + ";" +
                getMethodCalled().toUpperCase() + ";" +
                getClientID() + ";" +
                getMovieId() + ";" +
                getMovieName() + ";" +
                getOldMovieId() + ";" +
                getOldMovieName() + ";" +
                getBookingCapacity() +";"+
                getNumberOfTickets();
    }
}
