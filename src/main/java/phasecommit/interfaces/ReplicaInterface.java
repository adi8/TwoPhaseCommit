package phasecommit.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ReplicaInterface extends Remote {
    int voteRequest(int transactionID, String transType) throws RemoteException;

    int globalCommit(int transactionID) throws RemoteException;

    int globalAbort(int transactionID) throws RemoteException;

    String get(int key) throws RemoteException;

    int put(int key, String value) throws RemoteException;

    int del(int key) throws RemoteException;
}
