package phasecommit.servers;

public class Server {
    public static void main(String[] args) {
        String usage = "Usage: ./server <server-type> [master-ip]\n" +
                       "       server-type: [master|replica|client]";
        if (args.length < 1) {
            System.out.println(usage);
            System.exit(1);
        }

        switch(args[0].toLowerCase()) {
            case "master":
                if (args.length != 1) {
                    System.out.println("Usage: ./server master");
                    System.exit(1);
                }
                Master.start();
                break;

            case "replica":
                if (args.length != 2) {
                    System.out.println("Usage: ./server replica <master-ip>");
                    System.exit(1);
                }
                Replica.start(args[1]);
                break;

            case "client":
                if (args.length != 2) {
                    System.out.println("Usage: ./server client <master-ip>");
                    System.exit(1);
                }
                Client.start(args[1]);
                break;

            default:
                System.out.println("server type not supported");
                System.out.println(usage);
                break;
        }
    }
}
