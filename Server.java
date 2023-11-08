import com.sun.nio.sctp.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;


public class Server {
    private int PORT;
    private int MAX_MSG_SIZE = 4096;
    private Node node;

    public Server(Node node) {
        this.PORT = node.ID_TO_PORT_MAP.get(node.NODE_ID);
        this.node = node;
    }

    public void listen() throws Exception {
        InetSocketAddress address = new InetSocketAddress(this.PORT);
        SctpServerChannel ssc = SctpServerChannel.open();

        ssc.bind(address);

        while(true) { // ssc.isOpen() is equivalent to 'true' in this case
            SctpChannel sc = ssc.accept();
            Thread listener = new Thread() {
                public void run(){
                    ByteBuffer buf = ByteBuffer.allocateDirect(MAX_MSG_SIZE);
                    while (sc.isOpen()){
                        try {
                            sc.receive(buf, null, null);
                            Message msg = Message.fromByteBuffer(buf);

                            handleMessage(msg);

                        } catch (Exception e) {
                            e.printStackTrace();
                            break;
                        };
                    }
                }
            };
            listener.start();
        }
    }

    public void handleMessage(Message msg) throws Exception{
        
    }
}