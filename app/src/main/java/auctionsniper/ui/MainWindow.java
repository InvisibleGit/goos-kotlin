package auctionsniper.ui;

import auctionsniper.SniperSnapshot;
import auctionsniper.SniperState;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;


public class MainWindow extends JFrame {
    public static final String MAIN_WINDOW_NAME = "Auction Sniper Main";
    public static final String SNIPERS_TABLE_NAME = "Snipers Table";

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


    public void sniperStateChanged(SniperSnapshot snapshot) {
        snipers.sniperStateChanged(snapshot);
    }

    public static class SnipersTableModel extends AbstractTableModel {
        private final static SniperSnapshot STARTING_UP = new SniperSnapshot("", 0, 0, SniperState.JOINING);
        private SniperSnapshot snapshot = STARTING_UP;
        private final static String[] STATUS_TEXT = { "Joining", "Bidding", "Winning", "Lost", "Won" };

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
                case SNIPER_STATE: return textFor(snapshot.state);
                default:
                    throw new IllegalArgumentException("No column at " + columnIndex);
            }
        }

        public void sniperStateChanged(SniperSnapshot newSnapshot) {
            this.snapshot = newSnapshot;
            fireTableRowsUpdated(0, 0);
        }

        public static String textFor(SniperState state) {
            return STATUS_TEXT[state.ordinal()];
        }
    }
}
