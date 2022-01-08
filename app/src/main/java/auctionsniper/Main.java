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
        ChatManager.getInstanceFor(connection).addIncomingListener((from, message, chat) -> {
            notToBeGC = chat;

            SwingUtilities.invokeLater(() -> ui.showStatus(MainWindow.STATUS_LOST) );
        });

        Message message = connection.getStanzaFactory()
                .buildMessageStanza()
                .to(auctionId(itemId, connection))
                .setBody("")
                .build();
        connection.sendStanza(message);
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
}
