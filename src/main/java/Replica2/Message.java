package Replica5;

public class Message {
	public String FrontEndIpAddress,MethodCalled , MessageType, userID, newMovieId, newMovieName, oldMovieId, oldMovieName;
	public int bookingCapacity, numberOfTickets, sequenceId;
		  
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
