package FrontEnd;

public class Response {

  /**
   * Sequence id;Response;RM Number; function(addMovie,...);userID; newMovieID;newMovieName; oldMovieID; oldMovieName;bookingCapacity
   */
  private int sequenceID = 0;
  private String response = "null";
  private int rmNumber = 0;
  private String function = "null";
  private String userID = "null";
  private String newMovieID = "null";
  private String newMovieName = "null";
  private String oldMovieID = "null";
  private String oldMovieName = "null";
  private int bookingCapacity = 0;
  private String udpMessage = "null";
  private boolean isSuccess = false;

  public Response(String udpMessage) {
    setUdpMessage(udpMessage.trim());
    String[] messageParts = getUdpMessage().split(";");
    setSequenceID(Integer.parseInt(messageParts[0]));
    setResponse(messageParts[1].trim());
    setRmNumber(messageParts[2]);
    setFunction(messageParts[3]);
    setUserID(messageParts[4]);
    setNewMovieID(messageParts[5]);
    setNewMovieName(messageParts[6]);
    setOldMovieID(messageParts[7]);
    setOldMovieName(messageParts[8]);
    setBookingCapacity(Integer.parseInt(messageParts[9]));
  }

  public int getSequenceID() {
    return sequenceID;
  }

  public void setSequenceID(int sequenceID) {
    this.sequenceID = sequenceID;
  }

  public String getResponse() {
    return response;
  }

  public void setResponse(String response) {
    isSuccess = response.contains("Success:");
    this.response = response;
  }

  public int getRmNumber() {
    return rmNumber;
  }

  public void setRmNumber(String rmNumber) {
    if (rmNumber.equalsIgnoreCase("RM1")) {
      this.rmNumber = 1;
    } else if (rmNumber.equalsIgnoreCase("RM2")) {
      this.rmNumber = 2;
    } else if (rmNumber.equalsIgnoreCase("RM3")) {
      this.rmNumber = 3;
    } else {
      this.rmNumber = 0;
    }
  }

  public String getFunction() {
    return function;
  }

  public void setFunction(String function) {
    this.function = function;
  }

  public String getUserID() {
    return userID;
  }

  public void setUserID(String userID) {
    this.userID = userID;
  }

  public String getNewMovieID() {
    return newMovieID;
  }

  public void setNewMovieID(String newMovieID) {
    this.newMovieID = newMovieID;
  }

  public String getNewMovieName() {
    return newMovieName;
  }

  public void setNewMovieName(String newMovieName) {
    this.newMovieName = newMovieName;
  }

  public String getOldMovieID() {
    return oldMovieID;
  }

  public void setOldMovieID(String oldMovieID) {
    this.oldMovieID = oldMovieID;
  }

  public String getOldMovieName() {
    return oldMovieName;
  }

  public void setOldMovieName(String oldMovieName) {
    this.oldMovieName = oldMovieName;
  }

  public int getBookingCapacity() {
    return bookingCapacity;
  }

  public void setBookingCapacity(int bookingCapacity) {
    this.bookingCapacity = bookingCapacity;
  }

  public String getUdpMessage() {
    return udpMessage;
  }

  public void setUdpMessage(String udpMessage) {
    this.udpMessage = udpMessage;
  }

  public boolean isSuccess() {
    return isSuccess;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj != null) {
      if (obj instanceof Response) {
        Response obj1 = (Response) obj;
        return (
          obj1.getFunction().equalsIgnoreCase(this.getFunction()) &&
          obj1.getSequenceID() == this.getSequenceID() &&
          obj1.getUserID().equalsIgnoreCase(this.getUserID()) &&
          obj1.isSuccess() == this.isSuccess()
        );
        //                        && obj1.getResponse().equalsIgnoreCase(this.getResponse());
      }
    }
    return false;
  }
}
