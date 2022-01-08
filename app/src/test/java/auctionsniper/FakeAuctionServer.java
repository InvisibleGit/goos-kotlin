package auctionsniper;

import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import static org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;

import org.jxmpp.jid.parts.Resourcepart;

import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.chat2.Chat;

import static org.junit.Assert.fail;

public class FakeAuctionServer {
    public static final String XMPP_HOSTNAME = "localhost";
    public static final String AUCTION_RESOURCE = "Auction";
    public static final String ITEM_ID_AS_LOGIN = "auction-%s";
    public static final String AUCTION_PASSWORD = "auction";

    private final String itemId;
    private final XMPPTCPConnection connection;
    private Chat currentChat;

    public FakeAuctionServer(final String itemId) throws Exception {
        this.itemId = itemId;
        this.connection = new XMPPTCPConnection(
            XMPPTCPConnectionConfiguration.builder()
                .setXmppDomain(XMPP_HOSTNAME)
                .setSecurityMode(SecurityMode.disabled)
                .build()
        );
    }

    public void startSellingItem() throws Exception {
        connection.connect();
        connection.login(String.format(ITEM_ID_AS_LOGIN, itemId), AUCTION_PASSWORD, Resourcepart.from(AUCTION_RESOURCE));
        ChatManager.getInstanceFor(connection).addIncomingListener((from, message, chat) -> currentChat = chat);
    }

    public void hasReceivedJoinRequestFromSniper() {
        fail("not implemented");
    }

    public void announceClosed() {
        fail("not implemented");
    }

    public void stop() {
        connection.disconnect();
    }
}
