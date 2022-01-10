package test.auctionsniper;

import auctionsniper.AuctionEventListener;
import auctionsniper.AuctionMessageTranslator;
import org.junit.Test;
import static org.mockito.Mockito.*;

public class AuctionMessageTranslatorTest {
    private final AuctionEventListener listener = mock(AuctionEventListener.class);
    private final AuctionMessageTranslator translator = new AuctionMessageTranslator(listener);

    @Test
    public void notifiesAuctionClosedWhenCloseMessageReceived() {
        String message = "SOLVersion: 1.1; Event: CLOSE;";

        translator.translateMessage(message);

        verify(listener, times(1)).auctionClosed();
    }
}
