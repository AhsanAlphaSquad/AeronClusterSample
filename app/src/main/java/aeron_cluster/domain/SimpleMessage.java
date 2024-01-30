package aeron_cluster.domain;

import org.agrona.DirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;

import com.aeroncookbook.sbe.SimpleMessageEncoder;
import com.aeroncookbook.sbe.MessageHeaderDecoder;
import com.aeroncookbook.sbe.MessageHeaderEncoder;
import com.aeroncookbook.sbe.SimpleMessageDecoder;

public record SimpleMessage(
        String sessionId,
        String message) {

    public interface SimpleMessageEncodeIntoCallback {
        void run(SimpleMessage message, SimpleMessageEncoder encoder);
    }

    @Override
    public final String toString() {
        return sessionId + ": " + message;
    }

    public static void encodeInto(String sessionId, String message, UnsafeBuffer buffer,
            SimpleMessageEncodeIntoCallback callback) {
        SimpleMessage m = new SimpleMessage(sessionId, message);

        SimpleMessageEncoder encoder = new SimpleMessageEncoder()
                .wrapAndApplyHeader(buffer, 0, new MessageHeaderEncoder())
                .sessionId(sessionId)
                .message(message);

        callback.run(m, encoder);
    }

    public static SimpleMessage decodeOutof(DirectBuffer buffer, int offset) {
        SimpleMessageDecoder decoder = new SimpleMessageDecoder();
        decoder.wrapAndApplyHeader(buffer, offset, new MessageHeaderDecoder());
        return new SimpleMessage(decoder.sessionId(), decoder.message());
    }
}
