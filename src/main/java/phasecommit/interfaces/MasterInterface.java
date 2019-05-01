package phasecommit.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface MasterInterface extends Remote {
    void connect(ReplicaInterface replicaStub, String replicaIP) throws RemoteException;

    String get(int key) throws RemoteException;

    int put(int key, String val) throws RemoteException;

    int del(int key) throws RemoteException;
}
