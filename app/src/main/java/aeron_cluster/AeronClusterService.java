package aeron_cluster;

import org.agrona.concurrent.AgentRunner;

import org.agrona.concurrent.SleepingIdleStrategy;

import io.aeron.Aeron;
import io.aeron.Publication;
import io.aeron.Subscription;
import io.aeron.driver.MediaDriver;
import io.aeron.driver.ThreadingMode;

public class AeronClusterService {

    static AeronClusterService instance = null;

    Publication publication = null;
    Subscription subscription = null;
    private static final int PUB_STREAM_ID = 11;
    private static final int SUB_STREAM_ID = 10;
    SleepingIdleStrategy senderIdleStrategy = new SleepingIdleStrategy();
    SleepingIdleStrategy receiverIdleStrategy = new SleepingIdleStrategy();

    SendAgent sender;
    ReceiveAgent receiver;

    private AeronClusterService() {
        final MediaDriver.Context mediaDriverCtx = new MediaDriver.Context()
            .dirDeleteOnStart(true)
            .threadingMode(ThreadingMode.DEDICATED)
            .sharedIdleStrategy(new SleepingIdleStrategy())
            .dirDeleteOnShutdown(true);

        final MediaDriver mediaDriver = MediaDriver.launchEmbedded(mediaDriverCtx);
        final Aeron.Context aeronCtx = new Aeron.Context()
            .aeronDirectoryName(mediaDriver.aeronDirectoryName());

        final Aeron aeron = Aeron.connect(aeronCtx);
        publication = aeron.addPublication("aeron:udp?endpoint=localhost:5556", PUB_STREAM_ID);
        subscription = aeron.addSubscription("aeron:udp?endpoint=localhost:5555", SUB_STREAM_ID);

        sender = new SendAgent(publication);
        AgentRunner sendRunner = new AgentRunner(senderIdleStrategy, Throwable::printStackTrace, null, sender);
        AgentRunner.startOnThread(sendRunner);

        receiver = new ReceiveAgent(subscription);
        AgentRunner receiveRunner = new AgentRunner(receiverIdleStrategy, Throwable::printStackTrace, null, receiver);
        AgentRunner.startOnThread(receiveRunner);
    }

    public static AeronClusterService getInstance() {
        if (instance == null) {
            instance = new AeronClusterService();
        }

        return instance;
    }

    public void send(String sessionId, String message) {
        sender.setMessage(sessionId, message);
    }

}
