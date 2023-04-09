package util;

import java.rmi.RemoteException;

public class RMServersDownException extends Throwable {
    private final RemoteException re;
    private final int sequenceId;

    public RMServersDownException(RemoteException re, int sequenceId) {
        this.re = re;
        this.sequenceId = sequenceId;

    }

    public RemoteException getRe() {
        return re;
    }

    public int getSequenceId() {
        return sequenceId;
    }
}
