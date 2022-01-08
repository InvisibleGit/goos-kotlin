package auctionsniper;

import static auctionsniper.Main.AUCTION_RESOURCE;
import static auctionsniper.Main.ITEM_ID_AS_LOGIN;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;

import static org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;

import org.jxmpp.jid.parts.Resourcepart;

import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.chat2.Chat;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;


public class FakeAuctionServer {
    public static final String XMPP_HOSTNAME = "localhost";
    public static final String AUCTION_PASSWORD = "auction";

    private final String itemId;
    private final XMPPTCPConnection connection;
    private Chat currentChat;

    private final ArrayBlockingQueue<Message> messages = new ArrayBlockingQueue<>(1);

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
        ChatManager.getInstanceFor(connection).addIncomingListener((from, message, chat) -> {
            currentChat = chat;
            messages.add(message);
        });
    }

    public void hasReceivedJoinRequestFromSniper() throws InterruptedException {
        assertThat(messages.poll(5, TimeUnit.SECONDS)).describedAs("Message").isNotNull();
    }

    public void announceClosed() throws SmackException.NotConnectedException, InterruptedException {
        currentChat.send("");
    }

    public void stop() {
        connection.disconnect();
    }

    public String getItemId() {
        return itemId;
    }

    public void reportPrice(int price, int increment, String bidder) {
        fail("not implemented");
    }

    public void hasReceivedBid(int bid, String sniperId) {
        fail("not implemented");
    }
}
