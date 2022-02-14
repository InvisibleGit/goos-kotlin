package auctionsniper;


public interface SniperListener {
    void sniperStateChanged(final SniperSnapshot snapshot);
    void sniperLost();
    void sniperWon();
}
