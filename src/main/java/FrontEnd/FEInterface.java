package FrontEnd;

public interface FEInterface {
    void informRmHasBug(int RmNumber);

    void informRmIsDown(int RmNumber);

    int sendRequestToSequencer(MyRequest myRequest);

    void retryRequest(MyRequest myRequest);
}
