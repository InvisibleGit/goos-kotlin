package test.auctionsniper;

import auctionsniper.AuctionSniper;
import auctionsniper.SniperListener;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class AuctionSniperTest {
    private final SniperListener sniperListener = mock(SniperListener.class);
    private final AuctionSniper sniper = new AuctionSniper(sniperListener);

    @Test
    public void reportsLostWhenAuctionCloses() {
        sniper.auctionClosed();

        verify(sniperListener, times(1)).sniperLost();
    }
}
