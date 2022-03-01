package auctionsniper;


public interface SniperListener {
    void sniperStateChanged(final SniperSnapshot snapshot);
    void addSniper(SniperSnapshot snapshot);
}
