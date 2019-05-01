package phasecommit.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ReplicaInterface extends Remote {
    int voteRequest(int transactionID, String op, int key, String val) throws RemoteException;

    int globalCommit(int transactionID) throws RemoteException;

    int globalAbort(int transactionID) throws RemoteException;

}
