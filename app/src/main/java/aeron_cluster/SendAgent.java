package aeron_cluster;

import java.nio.ByteBuffer;

import org.agrona.concurrent.Agent;
import org.agrona.concurrent.UnsafeBuffer;

import com.aeroncookbook.sbe.MessageHeaderEncoder;
import com.aeroncookbook.sbe.SimpleMessageEncoder;

import io.aeron.Publication;

class SendAgent implements Agent {
    private static final int MAX_MESSAGE_SIZE = 1024;

    private final Publication publication;
    private UnsafeBuffer sendBuffer = new UnsafeBuffer(ByteBuffer.allocate(MAX_MESSAGE_SIZE));
    private boolean hasMessage = false;

    public SendAgent(Publication publication) {
        this.publication = publication;
    }

    @Override
    public int doWork() throws Exception {
        if (hasMessage)
        {
            if (publication.isConnected())
            {
                publication.offer(sendBuffer);
                hasMessage = false;
                return 0;
            }
            return 1;
        }
        else 
        {
            return 0;
        }
    }

    @Override
    public String roleName() {
        return "Sender";
    }

    public void setMessage(String sessionId, String message) {
        if ((sessionId.length() + message.length() + Integer.BYTES * 2) > MAX_MESSAGE_SIZE) {
            return;
        }

        new SimpleMessageEncoder()
            .wrapAndApplyHeader(sendBuffer, 0, new MessageHeaderEncoder())
            .sessionId(sessionId)
            .message(message);

        hasMessage = true;
    }
}