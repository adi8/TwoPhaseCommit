package phasecommit.servers;

import phasecommit.interfaces.MasterInterface;
import phasecommit.interfaces.ReplicaInterface;
import phasecommit.sqlitelib.SqliteDB;
import phasecommit.util.Helper;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Replica implements ReplicaInterface {
    /**
     * Port on which replica listens.
     */
    public static final int REPLICA_PORT = 4002;

    /**
     * Data store at this replica.
     */
    public static SqliteDB db;

    /**
     * Database file name.
     */
    public static String dbName;

    /**
     * Send a response to either commit or abort.
     *
     * @param transactionID - ID of transaction to be voted on.
     * @param transType - Type of transaction
     * @return int - 1 - commit
     *               0 - abort
     */
    public int voteRequest(int transactionID, String transType) {
        return 1;
    }

    /**
     * Commit the transaction with given transaction ID.
     *
     * @param transactionID - ID of transaction to be commited.
     * @return int - 0 - Success
     *               1 - Failure
     */
    public int globalCommit(int transactionID) {
        return 0;
    }

    /**
     * Abort the transaction with given transaction ID.
     *
     * @param transactionID - ID of transaction to be aborted.
     * @return int - 0 - Success
     *               1 - Failure
     */
    public int globalAbort(int transactionID) {
        return 0;
    }

    /**
     * Return the value corresponding to given key.
     *
     * @param key - Key for which corresponding value is
     *              to be returned
     * @return String - Value corresponding to given key
     */
    public String get(int key) {
        return db.get(key);
    }

    /**
     * Stores the given key/value pair in a durable store.
     * @param key - Key to be stored
     * @param val - Value to be stored at given key.
     * @return int - 0: Success
     *               1: Failure
     */
    public int put(int key, String val) {
        int retval = 0;
        return retval;
    }

    /**
     * Deletes a key/value corresponding to given key.
     * @param key - Key to be deleted
     * @return int - 0: Success
     *               1: Failure
     */
    public int del(int key) {
        int retval = 0;
        return retval;
    }

    /**
     * Create datastore of this replica.
     *
     * @param replicaID - ID of replica.
     */
    public void setDB(int replicaID) {
        dbName = "data-" + replicaID + ".db";
        db = new SqliteDB(dbName);
    }

    /**
     * Start Server.
     */
    public static void start(String serverIP) {
        Registry reg = null;
        MasterInterface masterStub = null;
        try {
            reg = LocateRegistry.getRegistry(serverIP, Master.MASTER_PORT);
            masterStub = (MasterInterface) reg.lookup("phasecommit-rmi://master");
        }
        catch (RemoteException | NotBoundException e) {
            System.out.println("ERROR: " + e.getMessage());
            System.exit(1);
        }

        ReplicaInterface replicaStub = null;
        Replica replica = new Replica();
        try {
            replicaStub = (ReplicaInterface) UnicastRemoteObject.exportObject(replica, Replica.REPLICA_PORT);
        }
        catch (RemoteException e) {
            System.out.println("ERROR: " + e.getMessage());
            System.exit(1);
        }

        int replicaID = -1;

        try {
            replicaID = masterStub.connectReplica(replicaStub);
        }
        catch (RemoteException e) {
            System.out.println("ERROR: " + e.getMessage());
            System.exit(1);
        }

        replica.setDB(replicaID);

        System.out.println("Replica " + replicaID + " initialized ......");
        System.out.println("IP Address: " + Helper.getAddress());
    }
}
