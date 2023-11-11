enum MessageType {REQUEST, REPLY};

public class Main {
    public static void main (String[] args){
        Node node = new Node();

        MutualExclusionService mutex = new MutualExclusionService(
            node.NODE_ID, node.NUMBER_OF_NODES, node.ID_TO_CHANNEL_MAP
        );

        node.mutex = mutex;

        initiateServerThread(mutex, node);
        
        try {
            System.out.println("Sleeping for 10 seconds to allow other nodes wake up...");
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        node.buildChannels();
        mutex.ID_TO_CHANNEL_MAP = node.ID_TO_CHANNEL_MAP;

        try {
            System.out.println("Sleeping for 10 seconds to allow other nodes build channels...");
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        node.displayNodeDetails();

        node.runApplication();
    }

    private static void initiateServerThread(MutualExclusionService mutex, Node node) {
        // System.out.println("Intiating listener(server) thread...");
        Thread listener = new Thread() {
            public void run() {
                Server listenerObject = new Server(mutex, node);
                try {
                    listenerObject.listen();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        listener.start();
        // System.out.println("Listener(server) initiated");
    }
}
