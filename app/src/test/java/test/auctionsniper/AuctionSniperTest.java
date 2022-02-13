package test.auctionsniper;

import auctionsniper.*;
import auctionsniper.AuctionEventListener.PriceSource;
import org.junit.Test;
import org.mockito.InOrder;

import static org.mockito.Mockito.*;

import static auctionsniper.CustomMatchers.isStateOf;


public class AuctionSniperTest {
    private final String ITEM_ID = "item-54321";

    private final SniperListener sniperListener = mock(SniperListener.class);
    private final Auction auction = mock(Auction.class);
    private final AuctionSniper sniper = new AuctionSniper(ITEM_ID, auction, sniperListener);


    @Test
    public void reportsLostWhenAuctionClosesImmediately() {
        sniper.auctionClosed();

        verify(sniperListener, times(1)).sniperLost();
        verifyNoMoreInteractions(sniperListener);
    }

    @Test
    public void reportsLostIfAuctionClosesWhenBidding() {
        sniper.currentPrice(123, 45, PriceSource.FromOtherBidder);
        sniper.auctionClosed();

        InOrder inOrder = inOrder(sniperListener);
        inOrder.verify(sniperListener).sniperStateChanged(argThat(isStateOf(SniperState.BIDDING)));
        inOrder.verify(sniperListener, never()).sniperWinning();
        verify(sniperListener, times(1)).sniperLost();
    }

    @Test
    public void bidsHigherAndReportsBiddingWhenNewPriceArrives() {
        final int price = 1001;
        final int increment = 25;
        final int bid = price + increment;

        sniper.currentPrice(price, increment, PriceSource.FromOtherBidder);

        verify(auction, times(1)).bid(bid);
        verify(sniperListener).sniperStateChanged(new SniperSnapshot(ITEM_ID, price, bid, SniperState.BIDDING));
    }

    @Test
    public void reportsIsWinningWhenCurrentPriceComesFromSniper() {
        sniper.currentPrice(123, 45, PriceSource.FromSniper);

        verify(sniperListener).sniperWinning();
    }

    @Test
    public void reportsWonIfAuctionClosesWhenWinning() {
        sniper.currentPrice(123, 45, PriceSource.FromSniper);
        sniper.auctionClosed();

        InOrder inOrder = inOrder(sniperListener);
        inOrder.verify(sniperListener).sniperWinning();
        inOrder.verify(sniperListener).sniperWon();
    }
}
