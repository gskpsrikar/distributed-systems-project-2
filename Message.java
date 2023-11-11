import java.io.Serializable;
import java.nio.ByteBuffer;

import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;


public class Message implements Serializable {

    public MessageType messageType; // REQUEST, REPLY
    public int SENDER_ID;
    public int DESTINATION_ID;
	public int SENDER_CLOCK;
	public int MESSAGE_ID;
	
	public Message(int senderId, int destinationID, int clock)
	{
		this.SENDER_ID = senderId;
		this.DESTINATION_ID = destinationID;
		this.SENDER_CLOCK = clock;

		this.MESSAGE_ID = Utils.generateMessageId();
	}


	// Convert current instance of Message to ByteBuffer in order to send message over SCTP
	public byte[] toMessageBytes() throws Exception
	{
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
		objectOutputStream.writeObject(this);
		byte[] messageBytes = byteArrayOutputStream.toByteArray();

		return messageBytes;
	}

	// Retrieve Message from ByteBuffer received from SCTP
	public static Message fromByteBuffer(ByteBuffer buf) throws Exception
	{
		buf.flip();
		byte[] data = new byte[buf.limit()];
		buf.get(data);
		buf.clear();

		ByteArrayInputStream bis = new ByteArrayInputStream(data);
		ObjectInputStream ois = new ObjectInputStream(bis);
		Message msg = (Message) ois.readObject();

		bis.close();
		ois.close();

		return msg;
	};
}