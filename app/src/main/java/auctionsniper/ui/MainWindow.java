package auctionsniper.ui;

import auctionsniper.SniperSnapshot;
import auctionsniper.SniperState;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;


public class MainWindow extends JFrame {
    public static final String MAIN_WINDOW_NAME = "Auction Sniper Main";
    public static final String SNIPERS_TABLE_NAME = "Snipers Table";

    public static final String STATUS_JOINING = "Joining";
    public static final String STATUS_LOST = "Lost";
    public static final String STATUS_BIDDING = "Bidding";
    public static final String STATUS_WINNING = "Winning";
    public static final String STATUS_WON = "Won";

    private final SnipersTableModel snipers = new SnipersTableModel();

    public MainWindow() {
        super("Auction Sniper");
        setName(MAIN_WINDOW_NAME);
        fillContentPane(makeSnipersTable());
        pack();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    private void fillContentPane(JTable snipersTable) {
        final Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        contentPane.add(new JScrollPane(snipersTable), BorderLayout.CENTER);
    }

    private JTable makeSnipersTable() {
        final JTable snipersTable = new JTable(snipers);
        snipersTable.setName(SNIPERS_TABLE_NAME);
        return snipersTable;
    }

    public void showStatus(final String statusText) {
        snipers.setStatus(statusText);
    }

    public void sniperStateChanged(SniperSnapshot snapshot) {
        snipers.sniperStateChanged(snapshot);
    }

    public static class SnipersTableModel extends AbstractTableModel {
        private final static SniperSnapshot STARTING_UP = new SniperSnapshot("", 0, 0, SniperState.JOINING);
        private String state = STATUS_TEXT[SniperState.JOINING.ordinal()];
        private SniperSnapshot snapshot = STARTING_UP;
        private static String[] STATUS_TEXT = { MainWindow.STATUS_JOINING,
                                                MainWindow.STATUS_BIDDING,
                                                MainWindow.STATUS_WINNING };

        @Override
        public int getRowCount() {
            return 1;
        }

        @Override
        public int getColumnCount() { return Column.values().length; }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            switch (Column.at(columnIndex)) {
                case ITEM_IDENTIFIER: return snapshot.itemId;
                case LAST_PRICE: return snapshot.lastPrice;
                case LAST_BID: return snapshot.lastBid;
                case SNIPER_STATE: return state;
                default:
                    throw new IllegalArgumentException("No column at " + columnIndex);
            }
        }

        public void setStatus(String newStatusText) {
            this.state = newStatusText;
            fireTableRowsUpdated(0, 0);
        }

        public void sniperStateChanged(SniperSnapshot newSnapshot) {
            this.snapshot = newSnapshot;
            this.state = STATUS_TEXT[newSnapshot.state.ordinal()];
            fireTableRowsUpdated(0, 0);
        }
    }
}
