package auctionsniper;


public interface SniperListener {
    void sniperLost();
    void sniperBidding(final SniperSnapshot snapshot);
    void sniperWinning();
    void sniperWon();
}
