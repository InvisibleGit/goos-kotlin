package auctionsniper.ui;

import auctionsniper.SniperListener;
import auctionsniper.SniperSnapshot;
import auctionsniper.SniperState;
import auctionsniper.UserRequestListener;
import auctionsniper.util.Defect;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.ArrayList;


public class MainWindow extends JFrame {
    public static final String APPLICATION_TITLE = "Auction Sniper";
    public static final String MAIN_WINDOW_NAME = "Auction Sniper Main";
    public static final String SNIPERS_TABLE_NAME = "Snipers Table";
    public static final String NEW_ITEM_ID_NAME = "item id";
    public static final String JOIN_BUTTON_NAME = "join button";

    private final SnipersTableModel snipers;

    private final Announcer<UserRequestListener> userRequests = Announcer.to(UserRequestListener.class);

    public MainWindow(SnipersTableModel snipers) {
        super(APPLICATION_TITLE);

        this.snipers = snipers;

        setName(MAIN_WINDOW_NAME);
        fillContentPane(makeSnipersTable(), makeControls());
        pack();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    private void fillContentPane(JTable snipersTable, JPanel controls) {
        final Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        contentPane.add(controls, BorderLayout.NORTH);
        contentPane.add(new JScrollPane(snipersTable), BorderLayout.CENTER);
    }

    private JTable makeSnipersTable() {
        final JTable snipersTable = new JTable(snipers);
        snipersTable.setName(SNIPERS_TABLE_NAME);
        return snipersTable;
    }

    private JPanel makeControls() {
        JPanel controls = new JPanel(new FlowLayout());
        final JTextField itemIdField = new JTextField();
        itemIdField.setColumns(25);
        itemIdField.setName(NEW_ITEM_ID_NAME);
        controls.add(itemIdField);

        JButton joinAuctionButton = new JButton("Join Auction");
        joinAuctionButton.setName(JOIN_BUTTON_NAME);
        joinAuctionButton.addActionListener(e -> userRequests.announce().joinAuction(itemIdField.getText()));
        controls.add(joinAuctionButton);

        return controls;
    }

    public void addUserRequestListener(UserRequestListener userRequestListener) {
        userRequests.addListener(userRequestListener);
    }

    public static class SnipersTableModel extends AbstractTableModel implements SniperListener {
        private ArrayList<SniperSnapshot> snapshots = new ArrayList<>();
        private final static String[] STATUS_TEXT = { "Joining", "Bidding", "Winning", "Lost", "Won" };

        @Override
        public int getRowCount() {
            return snapshots.size();
        }

        @Override
        public String getColumnName(int column) {
            return Column.at(column).name;
        }

        @Override
        public int getColumnCount() { return Column.values().length; }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Object value = Column.at(columnIndex).valueIn(snapshots.get(rowIndex));
            return value;
        }

        public void sniperStateChanged(SniperSnapshot newSnapshot) {
            int row = rowMatching(newSnapshot);
            snapshots.set(row, newSnapshot);
            fireTableCellUpdated(row, row);
        }

        private int rowMatching(SniperSnapshot snapshot) {
            for (int i = 0; i < snapshots.size(); i++)
                if (snapshot.isForSameItemAs(snapshots.get(i)))
                    return i;

            throw new Defect("Cannot find match for " + snapshot);
        }

        @Override
        public void addSniper(SniperSnapshot snapshot) {
            snapshots.add(snapshot);
            fireTableRowsInserted(0, snapshots.size() - 1);
        }

        public static String textFor(SniperState state) {
            return STATUS_TEXT[state.ordinal()];
        }
    }
}
