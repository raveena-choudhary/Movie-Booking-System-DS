package Replica1;

public class Message {
	public void setFrontEndIpAddress(String frontEndIpAddress) {
		FrontEndIpAddress = frontEndIpAddress;
	}

	public void setMethodCalled(String methodCalled) {
		MethodCalled = methodCalled;
	}

	public void setMessageType(String messageType) {
		MessageType = messageType;
	}

	public void setUserID(String userID) {
		this.userID = userID;
	}

	public void setNewMovieId(String newMovieId) {
		this.newMovieId = newMovieId;
	}

	public void setNewMovieName(String newMovieName) {
		this.newMovieName = newMovieName;
	}

	public void setOldMovieId(String oldMovieId) {
		this.oldMovieId = oldMovieId;
	}

	public void setOldMovieName(String oldMovieName) {
		this.oldMovieName = oldMovieName;
	}

	public void setBookingCapacity(int bookingCapacity) {
		this.bookingCapacity = bookingCapacity;
	}

	public void setNumberOfTickets(int numberOfTickets) {
		this.numberOfTickets = numberOfTickets;
	}

	public void setSequenceId(int sequenceId) {
		this.sequenceId = sequenceId;
	}

	public String FrontEndIpAddress,MethodCalled , MessageType, userID, newMovieId, newMovieName, oldMovieId, oldMovieName;
	public int bookingCapacity, numberOfTickets, sequenceId;
		  public Message(){}
	public Message(int sequenceId, String FrontEndIpAddress, String MessageType, String MethodCalled, String userID, String newMovieId,
                   String newMovieName, String oldMovieId, String oldMovieName, int bookingCapacity,int numberOfTickets)
	{ 
		this.sequenceId = sequenceId; 
		this.FrontEndIpAddress = FrontEndIpAddress;
		this.MessageType = MessageType; 
		this.MethodCalled = MethodCalled;
		this.userID = userID; 
		this.newMovieId = newMovieId;
		this.newMovieName = newMovieName;
		this.oldMovieId = oldMovieId;
		this.oldMovieName = oldMovieName;
		this.bookingCapacity = bookingCapacity;
		this.numberOfTickets = numberOfTickets;
	}
    @Override
    public String toString() {
		return sequenceId + ";" + FrontEndIpAddress + ";" +MessageType + ";" +MethodCalled + ";" +userID + ";" +newMovieId +
		";" +newMovieName + ";" +oldMovieId + ";" +oldMovieName + ";" +bookingCapacity +";"+numberOfTickets;
    }
}
