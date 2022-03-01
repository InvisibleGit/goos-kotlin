package test.auctionsniper;

import auctionsniper.AuctionSniper;
import auctionsniper.SniperSnapshot;
import auctionsniper.SniperState;
import auctionsniper.ui.Column;
import auctionsniper.ui.MainWindow.SnipersTableModel;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import auctionsniper.util.Defect;
import org.junit.Before;
import org.junit.Test;


import static auctionsniper.CustomMatchers.isOfType;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;


public class SnipersTableModelTest {
    private TableModelListener listener = mock(TableModelListener.class);
    private final SnipersTableModel model = new SnipersTableModel();

    @Before
    public void attachModelListener() {
        model.addTableModelListener(listener);
    }

    @Test
    public void hasEnoughColumns() {
        assertThat(model.getColumnCount()).isEqualTo(Column.values().length);
    }

    @Test
    public void setsUpColumnHeadings() {
        for (Column column : Column.values())
            assertThat(column.name).isEqualTo(model.getColumnName(column.ordinal()));
    }

    @Test
    public void setsSniperValuesInColumns() {
        SniperSnapshot joining = SniperSnapshot.joining("item id");
        SniperSnapshot bidding = joining.bidding(555, 666);

        model.addSniper(joining);
        verify(listener).tableChanged(argThat(isOfType(TableModelEvent.INSERT)));

        model.sniperStateChanged(bidding);

        assertRowMatchesSnapshot(0, bidding);

        verify(listener, atLeastOnce()).tableChanged(aChangeInRow(0));
    }

    private TableModelEvent aChangeInRow(int row) {
        return refEq(new TableModelEvent(model, row, row, Column.ITEM_IDENTIFIER.ordinal(), TableModelEvent.UPDATE));
    }

    @Test
    public void notifiesListenerWhenAddingASniper() {
        SniperSnapshot joining = SniperSnapshot.joining("item123");

        assertThat(model.getRowCount()).isEqualTo(0);

        model.addSniper(joining);

        assertThat(model.getRowCount()).isEqualTo(1);
        verify(listener, times(1)).tableChanged(anInsertionAtRow(0));
        assertRowMatchesSnapshot(0, joining);
    }

    private TableModelEvent anInsertionAtRow(final int row) {
        // Note: This will work only on JDK < v17. See README.md: "Commit 3 - Extending the Table Model"
        return refEq(new TableModelEvent(model, row, row, TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT));
    }

    @Test
    public void updatesCorrectRowForSniper() {
        SniperSnapshot joining = SniperSnapshot.joining("item 0");
        SniperSnapshot joining2 = SniperSnapshot.joining("item 1");
        SniperSnapshot bidding2 = joining2.bidding(200, 2);

        model.addSniper(joining);
        model.addSniper(joining2);
        model.sniperStateChanged(bidding2);

        assertRowMatchesSnapshot(0, joining);
        assertRowMatchesSnapshot(1, bidding2);

        verify(listener, times(2)).tableChanged(argThat(isOfType(TableModelEvent.INSERT)));
        verify(listener, times(1)).tableChanged(argThat(isOfType(TableModelEvent.UPDATE)));
    }

    private void assertRowMatchesSnapshot(int row, SniperSnapshot snapshot) {
        assertThat(cellValue(row, Column.ITEM_IDENTIFIER)).isEqualTo(snapshot.itemId);
        assertThat(cellValue(row, Column.LAST_PRICE)).isEqualTo(snapshot.lastPrice);
        assertThat(cellValue(row, Column.LAST_BID)).isEqualTo(snapshot.lastBid);
        assertThat(cellValue(row, Column.SNIPER_STATE)).isEqualTo(SnipersTableModel.textFor(snapshot.state));
    }

    private Object cellValue(int rowIndex, Column column) {
        return model.getValueAt(rowIndex, column.ordinal());
    }

    @Test(expected=Defect.class)
    public void throwsDefectIfNoExistingSniperForAnUpdate() {
        model.sniperStateChanged(new SniperSnapshot("item 1", 123, 234, SniperState.WINNING));
    }
}
