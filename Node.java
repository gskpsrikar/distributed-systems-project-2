import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.sun.nio.sctp.*;

public class Node {

    int NODE_ID = -1;
    String HOST_NAME = Utils.getHostName();
    
    int NUMBER_OF_NODES;
    int EXPECTED_INTER_REQUEST_DELAY;
    int EXPECTED_CS_EXECUTION_TIME;
    int REQUESTS_PER_NODE;

    Map<Integer, String> ID_TO_HOST_MAP = new HashMap<>();
    Map<String, Integer> HOST_TO_ID_MAP = new HashMap<>();
    Map<Integer, Integer> ID_TO_PORT_MAP = new HashMap<>();
    Map<Integer, SctpChannel> ID_TO_CHANNEL_MAP = new HashMap<>();

    public Node(){
        this.parseConfiguationFile();
        this.displayNodeDetails();
        
    }

    public static void main(String[] args) throws UnknownHostException{
        Node node = new Node();
        node.parseConfiguationFile();
        node.displayNodeDetails();
        node.runApplication();
    }

    public void runApplication(){
        while (this.REQUESTS_PER_NODE > 0){
            if (this.REQUESTS_PER_NODE % 100 == 0){
                System.out.println(
                    String.format(
                        "[%s] Remaining number of requests to be made = %d", 
                        Utils.currentTime(), 
                        this.REQUESTS_PER_NODE
                    )
                );
            }

            int interRequestDelay = Utils.generateExponentialRandomVariable(this.EXPECTED_INTER_REQUEST_DELAY);
            Utils.sleep(interRequestDelay);

            MutualExclusionService.csEnter();

            int executionTime = Utils.generateExponentialRandomVariable(1/this.EXPECTED_CS_EXECUTION_TIME);
            Utils.sleep(executionTime);

            MutualExclusionService.csLeave();

            this.REQUESTS_PER_NODE -= 1;
        }
    }

    // Utility methods
    public void parseConfiguationFile() {
        String CONFIG_FILENAME = Utils.getConfigFilePath();
        Pattern GLOBAL_VARIABLES_REGEX_PATTERN = Pattern.compile("^\\s*(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)");

        try (BufferedReader reader = new BufferedReader(new FileReader(CONFIG_FILENAME))) {
            String line;
            int n = -1;

            int validLineNumber = 0;

            while ((line = reader.readLine()) != null) {
                line = line.split("#")[0].trim(); // Remove everything after '#'
                if (line.isEmpty()) { // Skip empty lines or lines with only comment
                    continue; 
                }

                Matcher globalMatcher = GLOBAL_VARIABLES_REGEX_PATTERN.matcher(line);

                if (globalMatcher.matches()) {// If the line is about global variables, ignore it
                    n = Integer.parseInt(globalMatcher.group(1));

                    this.NUMBER_OF_NODES = n;
                    this.EXPECTED_INTER_REQUEST_DELAY = Integer.parseInt(globalMatcher.group(2));
                    this.EXPECTED_CS_EXECUTION_TIME = Integer.parseInt(globalMatcher.group(3));
                    this.REQUESTS_PER_NODE = Integer.parseInt(globalMatcher.group(4));

                    validLineNumber += 1;

                } else if (validLineNumber <= n) {

                    String[] nodeDetails = line.split(" ");
                    int nodeId = Integer.parseInt(nodeDetails[0]);
                    String hostName = nodeDetails[1];
                    int port = Integer.parseInt(nodeDetails[2]);

                    if (!hostName.equals(Utils.getHostName())){
                        hostName += ".utdallas.edu";
                    }
                    if (this.HOST_NAME.equals(hostName)){
                        this.NODE_ID = nodeId;
                    }
                    
                    this.ID_TO_HOST_MAP.put(nodeId, hostName);
                    this.HOST_TO_ID_MAP.put(hostName, nodeId);
                    this.ID_TO_PORT_MAP.put(nodeId, port);

                    validLineNumber += 1;
                }
            }

            this.ID_TO_CHANNEL_MAP = Utils.buildChannels(
                this.NODE_ID, this.ID_TO_PORT_MAP, this.ID_TO_HOST_MAP
            );

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void displayNodeDetails(){
        System.out.println("--------------------- CONFIGURATION ----------------------------");
        System.out.println(String.format("| NODE_ID : %d", this.NODE_ID));
        System.out.println(String.format("| HOST_NAME: %s", this.HOST_NAME));
        System.out.println(String.format("| NUMBER_OF_NODES: %d", this.NUMBER_OF_NODES));
        System.out.println(String.format("| EXPECTED_INTER_REQUEST_DELAY: %d", this.EXPECTED_INTER_REQUEST_DELAY));
        System.out.println(String.format("| EXPECTED_CS_EXECUTION_TIME: %d", this.EXPECTED_CS_EXECUTION_TIME));
        System.out.println(String.format("| REQUESTS_PER_NODE: %d", this.REQUESTS_PER_NODE));
        System.out.println("| ID_TO_HOST_MAP: "+this.ID_TO_HOST_MAP);
        System.out.println("| ID_TO_PORT_MAP: "+this.ID_TO_PORT_MAP);
        System.out.println("| ID_TO_CHANNEL_MAP: "+this.ID_TO_CHANNEL_MAP);
        System.out.println("----------------------------------------------------------------");
    }
}
