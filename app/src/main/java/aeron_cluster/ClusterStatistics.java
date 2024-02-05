package aeron_cluster;

public class ClusterStatistics {
    public static long messagesReceived = 0;
    public static long messagesSent = 0;

    public static void reset() {
        messagesReceived = 0;
        messagesSent = 0;
    }
}