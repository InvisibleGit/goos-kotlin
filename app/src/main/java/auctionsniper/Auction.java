package auctionsniper;

public interface Auction {
    void join() throws Exception;
    void bid(int amount);
}
