package phasecommit.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

public class Helper {
    /**
     * Returns the IP address of current node.
     *
     * @return String
     */
    public static String getAddress() {
        String ipAddress = "";
        try {
            for (final Enumeration<NetworkInterface> interfaces
                 = NetworkInterface.getNetworkInterfaces();
                 interfaces.hasMoreElements();)
            {
                final NetworkInterface cur = interfaces.nextElement();

                if ( cur.isLoopback() )
                    continue;

                if (!(cur.getDisplayName().startsWith("w") || cur.getDisplayName().startsWith("e")))
                    continue;

                for (final InterfaceAddress addr : cur.getInterfaceAddresses()) {
                    final InetAddress inetAddr = addr.getAddress();

                    if (!(inetAddr instanceof Inet4Address))
                        continue;

                    ipAddress += inetAddr.getHostAddress() + " ";
                }

            }
        }
        catch (Exception e) {
            System.out.println("Failed: " + e.getMessage());
            e.printStackTrace();
        }

        return ipAddress;
    }

    /**
     * Writes to log file details of a given transaction.
     *
     * @param recType - Record type
     * @param transactionID - Transaction ID
     * @param status - Status of transaction
     * @param op - Operation associated with transaction
     * @param key - Key to be stored/deleted/retrieved
     * @param val - Value corresponding to key (for store operation)
     */
    public static void writeToLog(String logName, int recType, int transactionID,
                                  String status, String op, int key, String val)
    {
        try (FileWriter fw = new FileWriter(logName, true);
             BufferedWriter bw = new BufferedWriter(fw);) {
            String entry = String.format("%d:%06d:%s:%s:%d:%s\n",
                    recType,
                    transactionID,
                    status,
                    op,
                    key,
                    val
            );
            bw.write(entry);
            bw.flush();
        }
        catch (IOException e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }


}
