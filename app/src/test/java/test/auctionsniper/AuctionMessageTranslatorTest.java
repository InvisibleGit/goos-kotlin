package test.auctionsniper;

import auctionsniper.AuctionEventListener;
import auctionsniper.AuctionEventListener.PriceSource;
import auctionsniper.AuctionMessageTranslator;
import org.junit.Test;

import static auctionsniper.ApplicationRunner.SNIPER_ID;
import static org.mockito.Mockito.*;

public class AuctionMessageTranslatorTest {
    private final AuctionEventListener listener = mock(AuctionEventListener.class);
    private final AuctionMessageTranslator translator = new AuctionMessageTranslator(SNIPER_ID, listener);

    @Test
    public void notifiesAuctionClosedWhenCloseMessageReceived() {
        String message = "SOLVersion: 1.1; Event: CLOSE;";

        translator.translateMessage(message);

        verify(listener, times(1)).auctionClosed();
    }

    @Test
    public void notifiesBidDetailsWhenCurrentPriceMessageReceivedFromOtherBidder() {
        String message = "SOLVersion: 1.1; Event: PRICE; CurrentPrice: 192; Increment: 7; Bidder: Someone else;";

        translator.translateMessage(message);

        verify(listener, times(1)).currentPrice(192, 7, PriceSource.FromOtherBidder);
    }

    @Test
    public void notifiesBidDetailsWhenCurrentPriceMessageReceivedFromSniper() {
        String message = "SOLVersion: 1.1; Event: PRICE; CurrentPrice: 234; Increment: 5; Bidder: " + SNIPER_ID + ";";

        translator.translateMessage(message);

        verify(listener, times(1)).currentPrice(234, 5, PriceSource.FromSniper);
    }
}
