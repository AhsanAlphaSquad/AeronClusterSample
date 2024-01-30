package aeron_cluster;

import org.agrona.concurrent.ShutdownSignalBarrier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.aeron.cluster.ClusteredMediaDriver;
import io.aeron.cluster.service.ClusteredServiceContainer;
import io.aeron.samples.cluster.ClusterConfig;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.io.File;
import java.util.ArrayList;

public class App {

    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);

    private static final int NODE_ID = 0;
    private static final int PORT_BASE = 9000;

    public static void main(String[] args) {
        final ShutdownSignalBarrier barrier = new ShutdownSignalBarrier();

        final List<String> hosts = new ArrayList<>();
        hosts.add("127.0.0.1");

        final ClusterConfig clusterConfig = ClusterConfig.create(NODE_ID, hosts, PORT_BASE, new ServiceContainer());

        clusterConfig.consensusModuleContext().ingressChannel("aeron:udp");
        clusterConfig.baseDir(getBaseDir(NODE_ID));

        clusterConfig.consensusModuleContext().leaderHeartbeatTimeoutNs(TimeUnit.SECONDS.toNanos(3));

        try (
            ClusteredMediaDriver clusteredMediaDriver = ClusteredMediaDriver.launch(
                    clusterConfig.mediaDriverContext(), 
                    clusterConfig.archiveContext(), 
                    clusterConfig.consensusModuleContext());
            ClusteredServiceContainer clusteredServiceContainer = ClusteredServiceContainer.launch( 
                    clusterConfig.clusteredServiceContext()))
        {
            LOGGER.info("Clusterd Service started");
            barrier.await();
            LOGGER.info("Clusterd Service terminated");
        }
    }

    private static File getBaseDir(final int nodeId) {
        final String baseDir = System.getenv("BASE_DIR");
        if (null == baseDir || baseDir.isEmpty()) {
            return new File(System.getProperty("user.dir"), "node" + nodeId);
        }

        return new File(baseDir);
    }
}
