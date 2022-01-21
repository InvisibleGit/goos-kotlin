package test.auctionsniper;

import auctionsniper.Auction;
import auctionsniper.AuctionEventListener.PriceSource;
import auctionsniper.AuctionSniper;
import auctionsniper.SniperListener;
import org.junit.Test;

import static org.mockito.Mockito.*;


public class AuctionSniperTest {
    private final SniperListener sniperListener = mock(SniperListener.class);
    private final Auction auction = mock(Auction.class);
    private final AuctionSniper sniper = new AuctionSniper(auction, sniperListener);


    @Test
    public void reportsLostWhenAuctionCloses() {
        sniper.auctionClosed();

        verify(sniperListener, times(1)).sniperLost();
    }

    @Test
    public void bidsHigherAndReportsBiddingWhenNewPriceArrives() {
        final int price = 1001;
        final int increment = 25;

        sniper.currentPrice(price, increment, PriceSource.FromOtherBidder);

        verify(auction, times(1)).bid(price + increment);
        verify(sniperListener).sniperBidding();
    }
}
