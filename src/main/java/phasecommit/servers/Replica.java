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
import java.util.*;

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
     * Log file name.
     */
    public static String logName;

    /**
     * List of read keys.
     */
    public static List<Integer> readKeys;

    /**
     * List of write keys.
     */
    public static List<Integer> writeKeys;

    /**
     * List of ongoing transactions
     */
    private static Map<Integer, String> transactions;

    /**
     * Default constructor.
     */
    public Replica() {
        readKeys = new ArrayList<>();
        writeKeys = new ArrayList<>();
        transactions = new HashMap<>();

        String replicaSuffix = Helper.getAddress().split(" ")[0].split("\\.")[3];

        logName = "log/replica-" + replicaSuffix + ".log";
        dbName = "replica-" + replicaSuffix + ".db";

        db = new SqliteDB(dbName);
    }

    /**
     * Send a response, either commit or abort.
     *
     * @param transactionID - ID of transaction to be voted on
     * @param op - Type of transaction
     * @param key - Key associated with transaction
     * @param val - Value associated with transaction
     * @return int - 1 - commit
     *               0 - abort
     */
    public int voteRequest(int transactionID, String op, int key, String val) {
        int vote = 0;

        // Write to log
        Helper.writeToLog(logName, 0, transactionID, "request", op, key, val);

        switch(op) {
            case "put":
            case "del":
                // Check if such a value exists
                String interestedVal = get(key);

                // Vote for commit only when no other write transaction
                // is being performed on the given key and if the key is
                // not already occupied.
                if (interestedVal == null && !writeKeys.contains(key)) {
                    vote = 1;
                    writeKeys.add(key);
                }
                break;

            default:
                break;
        }

        // Store transaction in memory
        String transDetails = String.format("%s:%d:%s", op, key, val);
        transactions.put(transactionID, transDetails);

        return vote;
    }

    /**
     * Commit the transaction with given transaction ID.
     *
     * @param transactionID - ID of transaction to be commited.
     * @return int - 0 - Success
     *               -1 - Failure
     */
    public int globalCommit(int transactionID) {
        int retval = 0;

        // Get required transaction
        String[] transDetails = transactions.get(transactionID).split(":");

        // Complete transaction
        int key = Integer.parseInt(transDetails[1]);
        String val = "";
        switch (transDetails[0]) {
            case "put":
                retval = put(key, transDetails[2]);
                val = transDetails[2];
                break;

            case "del":
                retval = del(key);
                break;
        }

        // Write to log
        Helper.writeToLog(logName, 0, transactionID, "committed", transDetails[0],
                          key, val);

        writeKeys.remove(new Integer(key));

        return retval;
    }

    /**
     * Abort the transaction with given transaction ID.
     *
     * @param transactionID - ID of transaction to be aborted.
     * @return int - 0 - Success
     *               1 - Failure
     */
    public int globalAbort(int transactionID) {
        int retval = 0;

        // Get required transaction
        String[] transDetails = transactions.get(transactionID).split(":");

        int key = Integer.parseInt(transDetails[1]);
        String val = "";
        switch (transDetails[0]) {
            case "put":
                retval = put(key, transDetails[2]);
                val = transDetails[2];
                break;

            case "del":
                retval = del(key);
                break;
        }

        // Write to log
        Helper.writeToLog(logName, 0, transactionID, "aborted", transDetails[0],
                key, val);

        writeKeys.remove(new Integer(key));

        return retval;
    }

    /**
     * Return the value corresponding to given key.
     *
     * @param key - Key for which corresponding value is
     *              to be returned
     * @return String - Value corresponding to given key
     */
    public String get(int key) {
        String retval = "";

        if (!writeKeys.contains(key)) {
            retval = db.get(key);
        }

        return retval;
    }

    /**
     * Stores the given key/value pair in a durable store.
     * @param key - Key to be stored
     * @param val - Value to be stored at given key.
     * @return int - 0: Success
     *               1: Failure
     */
    public int put(int key, String val) {
        int retval = db.put(key, val);

        if (retval > 0)
            retval = 0;
        else
            retval = 1;

        return retval;
    }

    /**
     * Deletes a key/value corresponding to given key.
     * @param key - Key to be deleted
     * @return int - 0: Success
     *               1: Failure
     */
    public int del(int key) {
        int retval = db.del(key);

        if (retval > 0)
            retval = 0;
        else
            retval = 1;

        return retval;
    }

    /**
     * Start Server.
     */
    public static void start(String serverIP) {
        Registry masterReg = null;
        MasterInterface masterStub = null;
        try {
            masterReg = LocateRegistry.getRegistry(serverIP, Master.MASTER_PORT);
            masterStub = (MasterInterface) masterReg.lookup("phasecommit-rmi://master");
        }
        catch (RemoteException | NotBoundException e) {
            System.out.println("ERROR: " + e.getMessage());
            System.exit(1);
        }

        Registry ownReg = null;
        ReplicaInterface replicaStub = null;
        Replica replica = new Replica();
        try {
            replicaStub = (ReplicaInterface) UnicastRemoteObject.exportObject(replica, Replica.REPLICA_PORT);

            ownReg = LocateRegistry.createRegistry(Replica.REPLICA_PORT);
            ownReg.rebind("phasecommit-rmi://replica", replicaStub);
        }
        catch (RemoteException e) {
            System.out.println("ERROR: " + e.getMessage());
            System.exit(1);
        }

        try {
            masterStub.connect(replicaStub, Helper.getAddress().split(" ")[0]);
        }
        catch (RemoteException e) {
            System.out.println("ERROR: " + e.getMessage());
            System.exit(1);
        }

        System.out.println("Replica initialized ......");
        System.out.println("IP Address: " + Helper.getAddress());
    }
}
