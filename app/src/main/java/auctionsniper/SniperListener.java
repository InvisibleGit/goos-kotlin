package auctionsniper;


public interface SniperListener {
    void sniperLost();
    void sniperStateChanged(final SniperSnapshot snapshot);
    void sniperWinning();
    void sniperWon();
}
