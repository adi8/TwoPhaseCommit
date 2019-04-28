package phasecommit.servers;

public class Server {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("USAGE: ./server <server-type> [server-ip]");
            System.out.println("       server-type: [master|replica|client]");
            System.exit(1);
        }

        switch(args[0].toLowerCase()) {
            case "master":
                Master.start();
                break;

            case "replica":
                if (args.length != 2) {
                    System.out.println("Usage: ./server replica <server-ip>");
                    System.exit(1);
                }
                Replica.start(args[1]);
                break;

            case "client":
                if (args.length != 2) {
                    System.out.println("Usage: ./server client <server-ip>");
                    System.exit(1);
                }
                Client.start(args[1]);
                break;

            default:
                System.out.println("server type not supported");
                break;
        }
    }
}
