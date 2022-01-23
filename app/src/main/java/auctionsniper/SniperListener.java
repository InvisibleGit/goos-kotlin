package auctionsniper;


public interface SniperListener {
    void sniperLost();
    void sniperBidding(final SniperState state);
    void sniperWinning();
    void sniperWon();
}
