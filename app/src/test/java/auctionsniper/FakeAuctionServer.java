package auctionsniper;

import static org.junit.Assert.fail;

public class FakeAuctionServer {
    public FakeAuctionServer(final String auctionIdentifier) {}

    public void startSellingItem() {
        fail("not implemented");
    }

    public void hasReceivedJoinRequestFromSniper() {
        fail("not implemented");
    }

    public void announceClosed() {
        fail("not implemented");
    }

    public void stop() {
        fail("not implemented");
    }
}
