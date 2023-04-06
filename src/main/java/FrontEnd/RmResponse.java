package FrontEnd;

public class RmResponse {
    /**
     * Sequence id;Response;RM Number; function(addEvent,...);userID; newEventID;newEventType; oldEventID; oldEventType;bookingCapacity
     */
    private int sequenceID = 0;
    private String response = "null";
    private int rmNumber = 0;
    private String function = "null";
    private String userID = "null";
    private String newEventID = "null";
    private String newEventType = "null";
    private String oldEventID = "null";
    private String oldEventType = "null";
    private int bookingCapacity = 0;
    private String udpMessage = "null";
    private boolean isSuccess = false;

    public RmResponse(String udpMessage) {
        setUdpMessage(udpMessage.trim());
        String[] messageParts = getUdpMessage().split(";");
        setSequenceID(Integer.parseInt(messageParts[0]));
        setResponse(messageParts[1].trim());
        setRmNumber(messageParts[2]);
        setFunction(messageParts[3]);
        setUserID(messageParts[4]);
        setNewEventID(messageParts[5]);
        setNewEventType(messageParts[6]);
        setOldEventID(messageParts[7]);
        setOldEventType(messageParts[8]);
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

    public String getNewEventID() {
        return newEventID;
    }

    public void setNewEventID(String newEventID) {
        this.newEventID = newEventID;
    }

    public String getNewEventType() {
        return newEventType;
    }

    public void setNewEventType(String newEventType) {
        this.newEventType = newEventType;
    }

    public String getOldEventID() {
        return oldEventID;
    }

    public void setOldEventID(String oldEventID) {
        this.oldEventID = oldEventID;
    }

    public String getOldEventType() {
        return oldEventType;
    }

    public void setOldEventType(String oldEventType) {
        this.oldEventType = oldEventType;
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
            if (obj instanceof RmResponse) {
                RmResponse obj1 = (RmResponse) obj;
                return obj1.getFunction().equalsIgnoreCase(this.getFunction())
                        && obj1.getSequenceID() == this.getSequenceID()
                        && obj1.getUserID().equalsIgnoreCase(this.getUserID())
                        && obj1.isSuccess() == this.isSuccess();
//                        && obj1.getResponse().equalsIgnoreCase(this.getResponse());
            }
        }
        return false;
    }
}
