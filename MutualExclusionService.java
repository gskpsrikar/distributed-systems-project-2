import java.nio.ByteBuffer;
import java.util.Comparator;
import java.util.Map;
import java.util.PriorityQueue;
import com.sun.nio.sctp.*;

public class MutualExclusionService {

    Comparator<int[]> comparator = new Comparator<int[]>() {
        public int compare(int[] element1, int[] element2) {
            if (element1[0] == element2[0]) {
                return Integer.compare(element1[1], element2[1]);
            }
            return Integer.compare(element1[0], element2[0]);
        }
    };

    private boolean OUTSTANDING = false;
    private boolean IN_CRITICAL_SECTION = false;

    private PriorityQueue<int[]> queue = new PriorityQueue<>(comparator);
    private int NODE_ID;
    private int NUMBER_OF_NODES;

    private boolean[] keys;
    private int keysHolding = 0;
    private int clock = 0;
    Map<Integer, SctpChannel> ID_TO_CHANNEL_MAP;

    public MutualExclusionService(int nodeId, int numberOfNodes, Map<Integer, SctpChannel> idToChannelMap){
        this.NODE_ID = nodeId;
        this.NUMBER_OF_NODES = numberOfNodes;

        this.keys = new boolean[this.NUMBER_OF_NODES];
        for (int i=0; i < this.NUMBER_OF_NODES; i++){
            if (i < this.NODE_ID){
                this.keys[i] = false;
            } else {
                this.keys[i] = true;
                this.keysHolding ++;
            }
        }

        this.ID_TO_CHANNEL_MAP = idToChannelMap;
    }

    public int csEnter(){

        // TODO: Add the REQUEST to its queue.

        this.OUTSTANDING = true;

        int messageComplexity = this.NUMBER_OF_NODES - this.keysHolding;

        for (int i=0; i<this.NUMBER_OF_NODES; i++){
            if (! this.keys[i]){
                sendRequestOrReply(i, MessageType.REQUEST);
            }
        }

        while (this.keysHolding < this.NUMBER_OF_NODES){
        }

        this.IN_CRITICAL_SECTION = true;

        this.clock ++;

        return 2*messageComplexity;
    }

    public void csLeave(){
        // TODO: Remove the request which is just satisfied from the queue.
        // Check the correctness of the logic with others.

        this.IN_CRITICAL_SECTION = false;

        this.OUTSTANDING = false;

        while (!this.queue.isEmpty()){

            int[] element = this.queue.peek();
            int requestedNodeId = element[1];

            if (requestedNodeId == this.NODE_ID){
                break;
            }

            sendRequestOrReply(element[1], MessageType.REPLY);

            this.keys[requestedNodeId] = false;
            this.keysHolding --;

            if (this.OUTSTANDING){
                sendRequestOrReply(element[1], MessageType.REQUEST);
            }

        }
    }

    public void receiveReply(int from){
        this.keys[from] = true;
        this.keysHolding ++;
    }

    public void receiveRequest(int from, int clock){
        if (this.IN_CRITICAL_SECTION | this.OUTSTANDING){
            this.queue.add(new int[]{clock, from});
        } else {
            sendRequestOrReply(from,MessageType.REPLY);
        }
    }

    public void sendRequestOrReply(int to, MessageType messageType){
        Message msg = new Message(this.NODE_ID, to, this.clock);
        msg.messageType = messageType;

        if (messageType == MessageType.REPLY){
            this.keys[to] = false;
            this.keysHolding --;
        }

        MessageInfo messageInfo = MessageInfo.createOutgoing(null, 0);
        SctpChannel channel = this.ID_TO_CHANNEL_MAP.get(to);

        System.out.println(to);
        System.out.println(this.ID_TO_CHANNEL_MAP);
        System.out.println(msg);

        try {
            byte[] messageBytes = msg.toMessageBytes();

            channel.send(ByteBuffer.wrap(messageBytes), messageInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
