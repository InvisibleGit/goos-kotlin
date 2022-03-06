package auctionsniper;

import auctionsniper.ui.MainWindow;
import auctionsniper.ui.MainWindow.SnipersTableModel;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.FromMatchesFilter;
import org.jivesoftware.smack.filter.StanzaFilter;
import org.jivesoftware.smack.filter.StanzaTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jxmpp.jid.EntityBareJid;
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

    public static final String AUCTION_RESOURCE = "Auction";
    public static final String ITEM_ID_AS_LOGIN = "auction-%s";
    public static final String AUCTION_ID_FORMAT = ITEM_ID_AS_LOGIN + "@%s/" + AUCTION_RESOURCE;

    public static final String JOIN_COMMAND_FORMAT = "SOLVersion: 1.1; Command: JOIN;";
    public static final String BID_COMMAND_FORMAT = "SOLVersion: 1.1; Command: BID; Price: %d;";

    private final SnipersTableModel snipers = new SnipersTableModel();
    private MainWindow ui;

    public Main() throws Exception {
        startUserInterface();
    }

    public static void main(String... args) throws Exception {
        Main main = new Main();
        XMPPTCPConnection connection = connection(args[ARG_HOSTNAME], args[ARG_USERNAME], args[ARG_PASSWORD]);
        main.disconnectWhenUICloses(connection);
        main.addUserRequestListenerFor(connection);
    }

    private void addUserRequestListenerFor(final XMPPTCPConnection connection) {
        ui.addUserRequestListener(itemId -> {
            snipers.addSniper(SniperSnapshot.joining(itemId));

            try {
                Auction auction = new XMPPAuction(connection, itemId);
                AuctionMessageTranslator translator = new AuctionMessageTranslator(
                    connection.getUser().toString(),
                    new AuctionSniper(itemId, auction, new SwingThreadSniperListener(snipers))
                );

                StanzaFilter filter = new AndFilter(StanzaTypeFilter.MESSAGE, FromMatchesFilter.create(auctionId(itemId, connection)));
                connection.addAsyncStanzaListener(stanza -> {
                    String messageBody = ((Message) stanza).getBody();
                    translator.translateMessage(messageBody);
                }, filter);

                auction.join();
            } catch (Exception e) {
                throw new RuntimeException("Couldn't join auction: " + itemId, e);
            }
        });
    }

    private void disconnectWhenUICloses(XMPPTCPConnection connection) {
        ui.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                connection.disconnect();
            }
        });
    }

    private static XMPPTCPConnection connection(String hostname, String username, String password) throws SmackException, IOException, XMPPException, InterruptedException {
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

    private static EntityBareJid auctionId(String itemId, XMPPTCPConnection connection) throws XmppStringprepException {
        return JidCreate.entityBareFrom(
            String.format(AUCTION_ID_FORMAT, itemId, connection.getXMPPServiceDomain())
        );
    }

    private void startUserInterface() throws Exception {
        SwingUtilities.invokeAndWait(() -> ui = new MainWindow(snipers));
    }

    public class XMPPAuction implements Auction {
        private final XMPPTCPConnection connection;
        private final String itemId;

        public XMPPAuction(XMPPTCPConnection connection, String itemId) {
            this.connection = connection;
            this.itemId = itemId;
        }

        @Override
        public void join() throws Exception {
            sendMessage(JOIN_COMMAND_FORMAT);
        }

        @Override
        public void bid(int amount) {
            String message = String.format(BID_COMMAND_FORMAT, amount);
            try {
                sendMessage(message);
            } catch (Exception e) {
                System.out.println("Could not send a bid: " + message);
            }
        }

        private void sendMessage(final String message) throws Exception {
            Message stanza = connection.getStanzaFactory()
                    .buildMessageStanza()
                    .to(auctionId(itemId, connection))
                    .setBody(message)
                    .build();
            connection.sendStanza(stanza);
        }
    }

    public class SwingThreadSniperListener implements SniperListener {
        private final SniperListener delegate;

        public SwingThreadSniperListener(SniperListener delegate) {
            this.delegate = delegate;
        }

        @Override
        public void sniperStateChanged(final SniperSnapshot snapshot) {
            SwingUtilities.invokeLater(() -> delegate.sniperStateChanged(snapshot));
        }

        @Override
        public void addSniper(SniperSnapshot snapshot) {}
    }
}
