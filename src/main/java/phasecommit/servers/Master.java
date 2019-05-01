package phasecommit.servers;

import phasecommit.interfaces.MasterInterface;
import phasecommit.interfaces.ReplicaInterface;
import phasecommit.util.Helper;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Master implements MasterInterface {
    /**
     * Port on which servers listens.
     */
    public static final int MASTER_PORT = 4001;

    /**
     * Map of replica and their stub.
     */
    private Map<String, ReplicaInterface> replicaServerStubs;

    /**
     * Transaction ID Generator
     */
    private static AtomicInteger transactionIDGen;

    /**
     * Log file for the master.
     */
    private static final String LOG_NAME = "log/master.log";

    /**
     * Checkpoing flag. Indicates whether checkpoint process is
     * about to start.
     */
    private boolean checkpointFlag;

    /**
     * Transacts in system.
     */
    private Map<Integer, String> transactions;

    /**
     * Default Constructor
     */
    public Master() {
        replicaServerStubs = new HashMap<>();
        transactionIDGen = new AtomicInteger();
        checkpointFlag = false;
        transactions = new HashMap<>();
    }

    /**
     * Stores the given replicas remote stub.
     *
     * @param replicaStub - Stub of replica to be connected
     *                      with
     * @param replicaIP - IP address of replica
     */
    public void connect(ReplicaInterface replicaStub, String replicaIP) {
        // Check if replica stub already tracked
        if (!replicaServerStubs.containsValue(replicaStub)) {
            replicaServerStubs.put(replicaIP, replicaStub);
        }
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
        // Forcing a failure as checkpoint is to start/started.
        if (checkpointFlag) {
            return null;
        }

        ReplicaInterface replicaServerStub = null;
        int idx = (int) (Math.random() * replicaServerStubs.values().size());
        for (ReplicaInterface rep : replicaServerStubs.values()) {
            if (--idx < 0)
                replicaServerStub = rep;
        }

        String ret = "";
        if (replicaServerStub != null) {
            try {
                ret = replicaServerStub.get(key);
            } catch (RemoteException e) {
                System.out.println("ERROR: " + e.getMessage());
            }
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
        // Forcing a failure as checkpoint is to start/started.
        if (checkpointFlag) {
            return 1;
        }

        int retval = 0;

        // Extract a unique transaction ID generator.
        int transactionID = transactionIDGen.incrementAndGet();

        // Write to log
        Helper.writeToLog(LOG_NAME, 0, transactionID, "request", "put", key, val);

        synchronized (transactions) {
            transactions.put(transactionID, String.format("%s:%d:%s", "put", key, val));
        }

        List<Integer> votes = new ArrayList<>();
        for (ReplicaInterface rep : replicaServerStubs.values()) {
            int ret = -1;
            try {
                ret = rep.voteRequest(transactionID, "put", key, val);
            }
            catch (RemoteException e) {
                System.out.println("ERROR: " + e.getMessage());
                ret = 0;
            }
            votes.add(ret);
        }

        // Calculate sum of all votes
        int voteSum = 0;
        for(int vote : votes) {
            voteSum += vote;
        }

        // Check if all votes are for commit
        if (voteSum == votes.size()) {
            Helper.writeToLog(LOG_NAME, 0, transactionID, "commit", "put", key, val);

            for (ReplicaInterface rep : replicaServerStubs.values()) {
                try {
                    rep.globalCommit(transactionID);
                }
                catch (RemoteException e) {
                    System.out.println("ERROR: " + e.getMessage());
                }
            }

            // TODO: If any replica has crashed we need to wait for its recovery

            Helper.writeToLog(LOG_NAME, 0, transactionID, "committed", "put", key, val);
        }
        else {
            retval = 1;

            Helper.writeToLog(LOG_NAME, 0, transactionID, "abort", "put", key, val);

            for (ReplicaInterface rep : replicaServerStubs.values()) {
                try {
                    rep.globalAbort(transactionID);
                }
                catch (RemoteException e) {
                    System.out.println("ERROR: " + e.getMessage());
                }
            }

            // TODO: If any replica has crashed we need to wait for its recovery

            Helper.writeToLog(LOG_NAME, 0, transactionID, "aborted", "put", key, val);
        }

        synchronized (transactions) {
            transactions.remove(transactionID);
        }

        return retval;
    }

    /**
     * Deletes a key/value corresponding to given key.
     *
     * @param key - Key to be deleted
     * @return int - 0: Success
     *               1: Failure
     */
    public int del(int key) {
        // Forcing a failure as checkpoint is to start/started.
        if (checkpointFlag) {
            return 1;
        }

        int retval = 0;

        // Extract a unique transaction ID
        int transactionID = transactionIDGen.incrementAndGet();

        // Write to log
        Helper.writeToLog(LOG_NAME, 0, transactionID, "request", "del", key, "");

        synchronized (transactions) {
            transactions.put(transactionID, String.format("%s:%d:%s", "del", key, ""));
        }

        List<Integer> votes = new ArrayList<>();

        for (ReplicaInterface rep : replicaServerStubs.values()) {
            int ret = -1;
            try {
                ret = rep.voteRequest(transactionID, "del", key, "");
            }
            catch (RemoteException e) {
                System.out.println("ERROR: " + e.getMessage());
            }
            votes.add(ret);
        }

        // Calculate sum of all votes
        int voteSum = 0;
        for (int vote : votes) {
            voteSum += vote;
        }

        // Check if all votes are for commit
        if (voteSum == votes.size()) {
            for (ReplicaInterface rep : replicaServerStubs.values()) {
                try {
                    rep.globalCommit(transactionID);
                }
                catch (RemoteException e) {
                    System.out.println("ERROR: " + e.getMessage());
                }
            }

            Helper.writeToLog(LOG_NAME, 0, transactionID, "committed", "del", key, "");
        }
        else {
            retval = 1;

            for (ReplicaInterface rep : replicaServerStubs.values()) {
                try {
                    rep.globalAbort(transactionID);
                }
                catch (RemoteException e) {
                    System.out.println("ERROR: " + e.getMessage());
                }
            }

            Helper.writeToLog(LOG_NAME, 0, transactionID, "aborted", "del", key, "");
        }

        synchronized (transactions) {
            transactions.remove(transactionID);
        }

        return retval;
    }

    /**
     * Co-ordinates with all replicas to log local checkpoints.
     */
    public void startCheckpointProcess() {
        synchronized (this) {
            checkpointFlag = true;
        }

        // Block until all current transactions have been processed
        while (transactions.size() != 0) {}

        boolean checkPointSuccess = true;
        for (ReplicaInterface replica : replicaServerStubs.values()) {
            try {
                replica.checkpointRequest();
            }
            catch (RemoteException e) {
                checkPointSuccess = false;
            }
        }

        if (checkPointSuccess) {
            Helper.writeToLog(LOG_NAME, 1, -1, "checkpoint", "", -1, "");
        }

        synchronized (this) {
            checkpointFlag = false;
        }
    }

    /**
     * Tries to connect to replica servers in case they are already up and running.
     * For eg. in case of a crash.
     *
     * @param replicaServers - String of replica server IP addresses
     * @param master - Master object
     */
    public static void getReplicaServerStubs(String replicaServers, Master master) {
        String[] replicaIPs = replicaServers.split(" ");
        for (String replicaIP : replicaIPs) {
            try {
                Registry reg = LocateRegistry.getRegistry(replicaIP, Replica.REPLICA_PORT);
                ReplicaInterface replicaStub = (ReplicaInterface) reg.lookup("phasecommit-rmi://replica");

                master.connect(replicaStub, replicaIP);
            }
            catch (RemoteException | NotBoundException e) { }
        }
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

        // Read a properties file and check if we can
        // connect to any of the replicas.
        String replicaServers = "";

        try (InputStream inp = new FileInputStream("config/master.properties")) {
            Properties prop = new Properties();

            prop.load(inp);
            replicaServers = prop.getProperty("replica.servers");
        }
        catch (IOException e) {
            System.out.println("ERROR: " + e.getMessage());
            System.exit(1);
        }

        // Get replica server stubs incase they are already up.
        getReplicaServerStubs(replicaServers, master);

        // Send check point request every 30 seconds
//        Timer timer = new Timer();
//        timer.scheduleAtFixedRate(new TimerTask() {
//            @Override
//            public void run() {
//                master.startCheckpointProcess();
//            }
//        }, 0, 30*1000);

        // TODO: Replay log if there are any transactions that require this.

    }

}
