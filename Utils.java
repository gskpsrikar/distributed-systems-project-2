import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.sun.nio.sctp.*;

enum MessageType {REQUEST, REPLY};

public class Utils {

    static String LOCAL_CONFIG_PATH = "config.txt";
    static String CLUSTER_CONFIG_PATH = "distributed-systems-project-2/config.txt";
    static String MY_LAPTOP_NAME = "srikargskp";

    public static void main(String[] args) {
    }

    public static int generateExponentialRandomVariable(int expectedValue){
        // Given the expected value (1/lambda) of an exponential random distribution
        Random r = new Random();
        double u = r.nextDouble();
        double rvDouble = (-expectedValue)*(Math.log(1-u));
        int rvInt = (int)rvDouble;
        return rvInt;
    }

    public static void sleep(int duration){
        // Sleep for the specified duration
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static String currentTime(){
        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDateTime = currentDateTime.format(formatter);
        return formattedDateTime;
    }

    public static Map<Integer, SctpChannel> buildChannels(
        int hostId, 
        Map<Integer, Integer> idToPortMap, 
        Map<Integer, String> idToHostMap
    ){
        
        Map<Integer, SctpChannel> idToChannelMap = new HashMap<>();

        for (Map.Entry<Integer, Integer> entry: idToPortMap.entrySet()) {
            
            int neighborId = entry.getKey();
            int port = entry.getValue();
            String neighborName = idToHostMap.get(neighborId);

            if (hostId == neighborId){
                continue;
            }
            try {
                SctpChannel channel = SctpChannel.open();
                channel.connect(new InetSocketAddress(neighborName, port));
                idToChannelMap.put(neighborId, channel);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return idToChannelMap;
    }

    public static String getConfigFilePath() {
        // Get the location of config.txt file path based on where the script is running.
        String hostName = getHostName();
        if (hostName.equals(MY_LAPTOP_NAME)){
            return LOCAL_CONFIG_PATH;
        } else {
            return CLUSTER_CONFIG_PATH;
        }
    }

    public static String getHostName() {
        // Get the host name Eg: srikargskp, dc01, etc.
        String localhost = "Unknown Host";
        try {
            localhost = InetAddress.getLocalHost().getHostName();
            return localhost;
        } catch (UnknownHostException e){
            System.out.println("Error: Unknown Host Exception in Utils.getHostName();");
        }
        return localhost;
    };

    public static void displayMessageDetails(Message msg, int hostId){
        // Display the message details on screen for debugging purposes
        String sendingOrReceiving = "";
        if (msg.SENDER_ID == hostId){
            sendingOrReceiving = "Sending";
        } else {
            sendingOrReceiving = "Receiving";
        }

        System.out.println(
            String.format(
                "[%s] (%s) FROM:%d TO:%d TYPE:%s", 
                currentTime(), sendingOrReceiving, msg.SENDER_ID, msg.DESTINATION_ID, msg.messageType
            )
        );
    }

    public static void sendMessage(Message msg, SctpChannel channel){
        
    }
}
