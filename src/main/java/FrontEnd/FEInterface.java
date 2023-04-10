package FrontEnd;

public interface FEInterface {
    void informSoftwareFailureIn(int RmNumber);

    void InformReplicaDown(int RmNumber);

    int sendRequestToSequencer(Request request);

    void retryRequest(Request request);
}
