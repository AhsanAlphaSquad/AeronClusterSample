package aeron_cluster;

import java.nio.ByteBuffer;

import org.agrona.DirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;

import io.aeron.ExclusivePublication;
import io.aeron.Image;
import io.aeron.cluster.codecs.CloseReason;
import io.aeron.cluster.service.ClientSession;
import io.aeron.cluster.service.Cluster;
import io.aeron.cluster.service.Cluster.Role;
import io.aeron.cluster.service.ClusteredService;
import io.aeron.logbuffer.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aeroncookbook.sbe.MessageHeaderDecoder;
import com.aeroncookbook.sbe.MessageHeaderEncoder;
import com.aeroncookbook.sbe.SimpleMessageDecoder;
import com.aeroncookbook.sbe.SimpleMessageEncoder;

public class ServiceContainer implements ClusteredService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceContainer.class);

    final MessageHeaderDecoder headerDecoder = new MessageHeaderDecoder();
    final SimpleMessageDecoder decoder = new SimpleMessageDecoder();
    private UnsafeBuffer sendBuffer = new UnsafeBuffer(ByteBuffer.allocate(1024));


    @Override
    public void onStart(Cluster cluster, Image snapshotImage) {
        LOGGER.info("ServiceContainer started");
    }

    @Override
    public void onSessionOpen(ClientSession session, long timestamp) {
        LOGGER.info("ServiceContainer session opened");
    }

    @Override
    public void onSessionClose(ClientSession session, long timestamp, CloseReason closeReason) {
        LOGGER.info("ServiceContainer session closed");
    }

    @Override
    public void onSessionMessage(ClientSession session, long timestamp, DirectBuffer buffer, int offset, int length, Header header) {
        LOGGER.info("ServiceContainer session message received");
        headerDecoder.wrap(buffer, offset);        

        int templateId = headerDecoder.templateId();
        switch (templateId) {
            case SimpleMessageDecoder.TEMPLATE_ID -> {
                decoder.wrapAndApplyHeader(buffer, offset, headerDecoder);
                // NOTE: we need to get get sessionId first because SBE tool is dumb...
                String sessionId = decoder.sessionId();
                // THE actual business logic Goes here....
                String reversed = new StringBuilder(decoder.message()).reverse().toString();

                SimpleMessageEncoder encoder = new SimpleMessageEncoder()
                    .wrapAndApplyHeader(sendBuffer, 0, new MessageHeaderEncoder())
                    .sessionId(sessionId)
                    .message(reversed);

                session.offer(sendBuffer, 0, MessageHeaderEncoder.ENCODED_LENGTH + encoder.encodedLength());
            }        
            default -> LOGGER.warn("Unknown templateId: {}", templateId);
        }
    }

    @Override
    public void onTimerEvent(long correlationId, long timestamp) {
        LOGGER.info("ServiceContainer timer event received");
    }

    @Override
    public void onTakeSnapshot(ExclusivePublication snapshotPublication) {
        LOGGER.info("ServiceContainer snapshot taken");
    }

    @Override
    public void onRoleChange(Role newRole) {
        LOGGER.info("ServiceContainer role changed new role: {}", newRole);
    }

    @Override
    public void onTerminate(Cluster cluster) {
        LOGGER.info("ServiceContainer terminated");
    }

}