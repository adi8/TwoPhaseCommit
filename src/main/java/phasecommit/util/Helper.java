package phasecommit.util;

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

}
