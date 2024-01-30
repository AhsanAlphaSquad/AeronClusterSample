package aeron_cluster;

import org.agrona.DirectBuffer;
import org.agrona.concurrent.Agent;

import com.aeroncookbook.sbe.MessageHeaderDecoder;
import com.aeroncookbook.sbe.SimpleMessageDecoder;

import io.aeron.Subscription;
import io.aeron.logbuffer.Header;

class ReceiveAgent implements Agent {

    final Subscription subscription;
    final MessageHeaderDecoder headerDecoder = new MessageHeaderDecoder();
    final SimpleMessageDecoder decoder = new SimpleMessageDecoder();

    ReceiveAgent(Subscription subscription) {
        this.subscription = subscription;
    }

    @Override
    public int doWork() throws Exception {
        subscription.poll(this::handler, 1);
        return 0;
    }

    private void handler(DirectBuffer buffer, int offset, int length, Header header) {

        headerDecoder.wrap(buffer, offset);        
        decoder.wrapAndApplyHeader(buffer, offset, headerDecoder);

        // NOTE: we need to get get sessionId first because SBE tool is dumb...
        
        String sessionId = decoder.sessionId();
        // THE actual business logic Goes here....
        String reversed = new StringBuilder(decoder.message()).reverse().toString();

        AeronClusterService.getInstance().send(sessionId, reversed);
    }

    @Override
    public String roleName() {
        return "Receiver";
    }
}