package phasecommit.servers;

import phasecommit.interfaces.MasterInterface;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Client {
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

        InputStreamReader isr = new InputStreamReader(System.in);
        BufferedReader br = new BufferedReader(isr);

        String usage = "Commands: put <int:key> <string:value>\n" +
                       "          get <int:key>\n" +
                       "          del <int:key>\n" +
                       "          exit\n";
        System.out.println("Client Initialized ......");
        System.out.println(usage);

        String command = "";
        String[] commandParts = command.split(" ");
        System.out.print("command > ");
        while (!commandParts[0].equalsIgnoreCase("exit")) {
            try {
                command = br.readLine();
            }
            catch (IOException e) {
                System.out.println("ERROR: " + e.getMessage() + "\nTry Again");
                System.out.print("\ncommand > ");
                continue;
            }

            commandParts = command.split(" ");

            try {
                switch (commandParts[0]) {
                    case "get":
                        if (commandParts.length != 2) {
                            System.out.println(usage);
                        }
                        else {
                            int key = -1;
                            try {
                                key = Integer.parseInt(commandParts[1]);
                            }
                            catch (NumberFormatException e) {
                                System.out.println("ERROR: Key has to be an int.");
                                System.out.println(usage);
                                break;
                            }
                            String value = masterStub.get(key);

                            if (value == null) {
                                System.out.println("No such value exists");
                            }
                            else {
                                System.out.println("Value: " + value);
                            }
                        }
                        break;

                    case "put":
                        if (commandParts.length != 3) {
                            System.out.println(usage);
                        }
                        else {
                            int key = -1;
                            try {
                                key = Integer.parseInt(commandParts[1]);
                            }
                            catch (NumberFormatException e) {
                                System.out.println("ERROR: key has to be an int.");
                                System.out.println(usage);
                                break;
                            }

                            String val = commandParts[2];

                            int retval = masterStub.put(key, val);

                            if (retval > 0)
                                System.out.println("Failed to put key/value " + key + "/" + val + " Try again");
                            else
                                System.out.println("Successfully put key/value " + key + "/" + val);
                        }
                        break;

                    case "del":
                        if (commandParts.length != 2) {
                            System.out.println(usage);
                        }
                        else {
                            int key = -1;
                            try {
                                key = Integer.parseInt(commandParts[1]);
                            }
                            catch (NumberFormatException e) {
                                System.out.println("ERROR: key hsa to be an int.");
                                System.out.println(usage);
                                break;
                            }

                            int retval = masterStub.del(key);

                            if (retval > 0)
                                System.out.println("Failed to delete key " + key + ". Try again");
                            else
                                System.out.println("Successfully deleted key " + key);
                        }
                        break;

                    case "exit":
                        break;

                    default:
                        System.out.println("Command " + commandParts[0] + " not supported.");
                        break;
                }

            }
            catch (RemoteException e) {
                System.out.println("ERROR: " + e.getMessage() + "\nTry again");
            }

            if (!commandParts[0].equalsIgnoreCase("exit")) {
                System.out.print("\ncommand > ");
            }
        }
    }
}
