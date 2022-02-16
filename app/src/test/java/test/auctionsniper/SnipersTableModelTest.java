package test.auctionsniper;

import auctionsniper.SniperSnapshot;
import auctionsniper.SniperState;
import auctionsniper.ui.Column;
import auctionsniper.ui.MainWindow;
import auctionsniper.ui.MainWindow.SnipersTableModel;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.junit.Before;
import org.junit.Test;

import static auctionsniper.ui.MainWindow.SnipersTableModel.textFor;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
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
    public void setsSniperValuesInColumns() {
        model.sniperStateChanged(new SniperSnapshot("item id", 555, 666, SniperState.BIDDING));

        assertColumnEquals(Column.ITEM_IDENTIFIER, "item id");
        assertColumnEquals(Column.LAST_PRICE, 555);
        assertColumnEquals(Column.LAST_BID, 666);
        assertColumnEquals(Column.SNIPER_STATE, textFor(SniperState.BIDDING));

        verify(listener).tableChanged(any(TableModelEvent.class));
    }

    private void assertColumnEquals(Column column, Object expected) {
        final int rowIndex = 0;
        final int columnIndex = Column.valueOf(column.name()).ordinal();
        assertThat(model.getValueAt(rowIndex, columnIndex)).isEqualTo(expected);
    }
}
