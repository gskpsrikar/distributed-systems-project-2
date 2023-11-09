public class Main {
    public static void main (String[] args){
        Node node = new Node();

        MutualExclusionService mutex = new MutualExclusionService(
            node.NODE_ID, node.NUMBER_OF_NODES, node.ID_TO_CHANNEL_MAP
        );

        node.mutex = mutex;
    }
}
