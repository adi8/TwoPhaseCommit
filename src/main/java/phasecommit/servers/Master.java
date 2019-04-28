package phasecommit.servers;

import phasecommit.interfaces.MasterInterface;
import phasecommit.interfaces.ReplicaInterface;
import phasecommit.util.Helper;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class Master implements MasterInterface {
    /**
     * Port on which servers listens.
     */
    public static final int MASTER_PORT = 4001;

    /**
     * Map of replica and their stub.
     */
    private Map<Integer, ReplicaInterface> replicaServerStubs;

    private static AtomicInteger transactionIDGen;

    /**
     * Default Constructor
     */
    public Master() {
        replicaServerStubs = new HashMap<>();
        transactionIDGen = new AtomicInteger();
    }

    /**
     * Connects with the given replica and returns a unique
     * ID for the same.
     *
     * @param replicaStub - Stub of replica to be connected
     *                      with
     * @return int - Replica ID
     */
    public int connectReplica(ReplicaInterface replicaStub) {
        int replicaID = transactionIDGen.incrementAndGet();
        replicaServerStubs.put(replicaID, replicaStub);
        return replicaID;
    }

    /**
     * Returns value corresponding to given key form a
     * randomly chosen replica.
     *
     * @param key - Key for which corresponding value is
     *              to be returned
     * @return String - Value corresponding to given key
     */
    public String get(int key) {
        ReplicaInterface replicaServerStub = null;
        int idx = (int) (Math.random() * replicaServerStubs.values().size());
        for (ReplicaInterface rep : replicaServerStubs.values()) {
            if (--idx < 0)
                replicaServerStub = rep;
        }
        String ret = "";

        try {
            ret = replicaServerStub.get(key);
        }
        catch (RemoteException e) {
            System.out.println("ERROR: " + e.getMessage());
        }

        return ret;
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
     * Start Server.
     */
    public static void start() {
        Master master = new Master();
        MasterInterface masterStub = null;
        Registry reg = null;
        try {
            masterStub = (MasterInterface) UnicastRemoteObject.exportObject(master, Master.MASTER_PORT);

            reg = LocateRegistry.createRegistry(Master.MASTER_PORT);
            reg.rebind("phasecommit-rmi://master", masterStub);

            System.out.println("Master Initialized ......");
            System.out.println("IP Address: " + Helper.getAddress());
        }
        catch (RemoteException e) {
            System.out.println("ERROR: " + e.getMessage());
            System.exit(1);
        }
    }

}
