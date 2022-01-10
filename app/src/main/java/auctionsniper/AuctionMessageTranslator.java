package auctionsniper;

public class AuctionMessageTranslator {
    AuctionEventListener listener;

    public AuctionMessageTranslator(AuctionEventListener listener) {
        this.listener = listener;
    }

    public void translateMessage(String message) {
        listener.auctionClosed();
    }
}
