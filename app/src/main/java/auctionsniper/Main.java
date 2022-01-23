package auctionsniper;

import auctionsniper.ui.MainWindow;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jxmpp.jid.FullJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Resourcepart;
import org.jxmpp.stringprep.XmppStringprepException;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;


public class Main {
    public static final int ARG_HOSTNAME = 0;
    public static final int ARG_USERNAME = 1;
    public static final int ARG_PASSWORD = 2;
    public static final int ARG_ITEM_ID  = 3;

    public static final String AUCTION_RESOURCE = "Auction";
    public static final String ITEM_ID_AS_LOGIN = "auction-%s";
    public static final String AUCTION_ID_FORMAT = ITEM_ID_AS_LOGIN + "@%s/" + AUCTION_RESOURCE;

    public static final String JOIN_COMMAND_FORMAT = "SOLVersion: 1.1; Command: JOIN;";
    public static final String BID_COMMAND_FORMAT = "SOLVersion: 1.1; Command: BID; Price: %d;";

    private AuctionMessageTranslator translator;

    private MainWindow ui;

    private Chat notToBeGC;

    public Main() throws Exception {
        startUserInterface();
    }

    public static void main(String... args) throws Exception {
        Main main = new Main();
        main.joinAuction(
            connect(args[ARG_HOSTNAME], args[ARG_USERNAME], args[ARG_PASSWORD]),
            args[ARG_ITEM_ID]
        );
    }

    private void joinAuction(final XMPPTCPConnection connection, final String itemId) throws Exception {
        disconnectWhenUICloses(connection);

        Auction auction = new XMPPAuction(connection, itemId);
        translator = new AuctionMessageTranslator(
            connection.getUser().toString(),
            new AuctionSniper(itemId, auction, new SniperStateDisplayer())
        );
        auction.join();
    }

    private void disconnectWhenUICloses(XMPPTCPConnection connection) {
        ui.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                connection.disconnect();
            }
        });
    }

    private static XMPPTCPConnection connect(String hostname, String username, String password) throws SmackException, IOException, XMPPException, InterruptedException {
        XMPPTCPConnection connection = new XMPPTCPConnection(
            XMPPTCPConnectionConfiguration.builder()
                .setXmppDomain(hostname)
                .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                .build()
        );
        connection.connect();
        connection.login(username, password, Resourcepart.from(AUCTION_RESOURCE));

        return connection;
    }

    private static FullJid auctionId(String itemId, XMPPTCPConnection connection) throws XmppStringprepException {
        return JidCreate.fullFrom(
            String.format(AUCTION_ID_FORMAT, itemId, connection.getXMPPServiceDomain())
        );
    }

    private void startUserInterface() throws Exception {
        SwingUtilities.invokeAndWait(() -> ui = new MainWindow());
    }

    public class XMPPAuction implements Auction {
        private final XMPPTCPConnection connection;
        private final String itemId;

        public XMPPAuction(XMPPTCPConnection connection, String itemId) {
            this.connection = connection;
            this.itemId = itemId;

            ChatManager.getInstanceFor(connection).addIncomingListener((from, message, chat) -> {
                notToBeGC = chat;

                translator.translateMessage(message.getBody());
            });
        }

        @Override
        public void join() throws Exception {
            sendMessage(JOIN_COMMAND_FORMAT);
        }

        private void sendMessage(final String message) throws Exception {
            Message stanza = connection.getStanzaFactory()
                .buildMessageStanza()
                .to(auctionId(itemId, connection))
                .setBody(message)
                .build();
            connection.sendStanza(stanza);
        }

        @Override
        public void bid(int amount) {
            try {
                Message message = connection.getStanzaFactory()
                    .buildMessageStanza()
                    .to(auctionId(itemId, connection))
                    .setBody(String.format(BID_COMMAND_FORMAT, amount))
                    .build();
                connection.sendStanza(message);
            } catch (XmppStringprepException|SmackException.NotConnectedException|InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public class SniperStateDisplayer implements SniperListener {
        @Override
        public void sniperLost() {
            showStatus(MainWindow.STATUS_LOST);
        }

        @Override
        public void sniperBidding(final SniperState state) {
            SwingUtilities.invokeLater(() -> ui.sniperStatusChanged(state, MainWindow.STATUS_BIDDING));
        }

        @Override
        public void sniperWinning() {
            showStatus(MainWindow.STATUS_WINNING);
        }

        @Override
        public void sniperWon() {
            showStatus(MainWindow.STATUS_WON);
        }

        private void showStatus(final String status) {
            SwingUtilities.invokeLater(() -> ui.showStatus(status));
        }
    }
}
