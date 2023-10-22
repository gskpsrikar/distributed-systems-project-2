import java.net.InetAddress;
import java.net.UnknownHostException; 

public class Utils {

    static String LOCAL_CONFIG_PATH = "config.txt";
    static String CLUSTER_CONFIG_PATH = "distributed-systems-project-2/config.txt";

    public static void main(String[] args) {
        System.out.println(getConfigFilePath());
    }

    public static String getConfigFilePath() {
        String hostName = getHostName();
        if (hostName.equals("srikargskp")){
            return LOCAL_CONFIG_PATH;
        } else {
            return CLUSTER_CONFIG_PATH;
        }
    }

    public static String getHostName() {
        String localhost = "Unknown Host";
        try {
            localhost = InetAddress.getLocalHost().getHostName();
            return localhost;
        } catch (UnknownHostException e){
            System.out.println("Error: Unknown Host Exception in Utils.getHostName();");
        }
        return localhost;
    };
}
