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

    // Outstanding request and critical section details
    private boolean isOutstandingRequest = false;
    private int clockOutstandingRequest;
    private boolean inCriticalSection = false;

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
                this.keysHolding += 1;
            }
        }
        this.ID_TO_CHANNEL_MAP = idToChannelMap;
    }

    public int csEnter(){
        System.out.println("Called csEnter() method.");

        this.isOutstandingRequest = true;
        int val = this.clock;
        this.clockOutstandingRequest = val;

        int messageComplexity = 2 * (this.NUMBER_OF_NODES - this.keysHolding);

        synchronized (this.keys) {
            for (int i=0; i<this.NUMBER_OF_NODES; i++){ // Send REQUESTs to unpossesed keys
                if (! this.keys[i]){
                    sendRequestOrReply(i, MessageType.REQUEST);
                }
            }
        }

        while (this.keysHolding < this.NUMBER_OF_NODES / 2){
            Utils.sleep(10  );
        }

        System.out.println("Exiting csEnter() method.");

        this.inCriticalSection = true;
        return messageComplexity;
    }

    public void csLeave(){
        System.out.println("Entering csLeave() method.");

        this.inCriticalSection = false;
        this.isOutstandingRequest = false;

        this.clock += 1;

        synchronized (this.queue){
            while (!this.queue.isEmpty()){

                int[] element = this.queue.poll();
                int requestedNodeId = element[1];

                sendRequestOrReply(requestedNodeId, MessageType.REPLY);
            }
        }
        
        System.out.println("Exiting csLeave() method.");
    }

    public void receiveReply(Message msg){
        System.out.println("-------------------");

        synchronized(this.queue){displayState(msg.MESSAGE_ID);};

        Utils.displayMessageDetails(msg, this.NODE_ID, this.clock);

        // this.clock = Math.max(msg.SENDER_CLOCK, this.clock) + 1;
        synchronized (this.keys){
            this.keys[msg.SENDER_ID] = true;
            this.keysHolding ++;
        }
        
        synchronized(this.queue){displayState(msg.MESSAGE_ID);};

        System.out.println("-------------------");
    }

    public void receiveRequest(Message msg){
        System.out.println("-------------------");
        synchronized(this.queue){displayState(msg.MESSAGE_ID);};

        Utils.displayMessageDetails(msg, this.NODE_ID, this.clock);

        System.out.println(String.format("OutstandingRequest=%b - OutstandingRequestClock=%d - inCriticalSection=%b", 
        this.isOutstandingRequest, this.clockOutstandingRequest, this.inCriticalSection
        ));

        // this.clock = Math.max(msg.SENDER_CLOCK, this.clock) + 1;

        if (this.inCriticalSection){
            synchronized (this.queue){
                this.queue.add(new int[]{msg.SENDER_CLOCK, msg.SENDER_ID});
            }
        } else if (! this.isOutstandingRequest){
            sendRequestOrReply(msg.SENDER_ID, MessageType.REPLY);
        } else {
            if (this.clockOutstandingRequest <= msg.SENDER_CLOCK){

                if (this.clockOutstandingRequest < msg.SENDER_CLOCK){
                    synchronized (this.queue){
                        this.queue.add(new int[]{msg.SENDER_CLOCK, msg.SENDER_ID});
                    }
                } else if (this.NODE_ID < msg.SENDER_ID) {
                    synchronized (this.queue){
                        this.queue.add(new int[]{msg.SENDER_CLOCK, msg.SENDER_ID});
                    }
                } else {
                    sendRequestOrReply(msg.SENDER_ID, MessageType.REPLY);
                    sendRequestOrReply(msg.SENDER_ID, MessageType.REQUEST);
                }
            } else {
                sendRequestOrReply(msg.SENDER_ID, MessageType.REPLY);
                sendRequestOrReply(msg.SENDER_ID, MessageType.REQUEST);
            }
        }

        synchronized(this.queue){displayState(msg.MESSAGE_ID);};
        System.out.println("-------------------");
    }

    public void sendRequestOrReply(int destination, MessageType messageType){
        // this.clock ++; // Update the clock before a send event

        Message msg; 

        if (messageType == MessageType.REQUEST){
            msg = new Message(this.NODE_ID, destination, this.clockOutstandingRequest);
        } else {
            msg = new Message(this.NODE_ID, destination, this.clock);
        }

        msg.messageType = messageType;

        synchronized(this.queue){displayState(msg.MESSAGE_ID);};
        Utils.displayMessageDetails(msg, this.NODE_ID, this.clock);

        synchronized (this.keys) {
            if (messageType == MessageType.REPLY){ // For REPLY message, delete the key
                this.keys[destination] = false;
                this.keysHolding -= 1;
                Utils.handleKeysCountError(this.keysHolding);
            }
        }

        MessageInfo messageInfo = MessageInfo.createOutgoing(null, 0);
        SctpChannel channel = this.ID_TO_CHANNEL_MAP.get(destination);
        try {
            byte[] messageBytes = msg.toMessageBytes();

            channel.send(ByteBuffer.wrap(messageBytes), messageInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }

        synchronized(this.queue){displayState(msg.MESSAGE_ID);};
    }

    public void displayState(int messageId){
        String display = "";
        display += messageId + " - State: ("+this.keysHolding+" keys) ";
        
        for (int i=0; i < this.keys.length; i++){
            display += this.keys[i] + ", ";
        }
        display += " | Queue: ";

        for (int[] element : this.queue){
            display += String.format("[%d,%d],", element[0], element[1]);
        }
        System.out.println(display);
    }
}
