package test.auctionsniper;

import auctionsniper.ui.MainWindow;
import auctionsniper.ui.MainWindow.SnipersTableModel;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.junit.Before;
import org.junit.Test;

import static auctionsniper.ui.MainWindow.JOIN_BUTTON_NAME;
import static org.assertj.core.api.Assertions.assertThat;


public class MainWindowTest {
    private final SnipersTableModel tableModel = new SnipersTableModel();
    private MainWindow mainWindow;
    private FrameFixture window;

    @Before
    public void setUp() {
        mainWindow = GuiActionRunner.execute(() -> new MainWindow(tableModel));
        window = new FrameFixture(mainWindow);
        window.show();
    }

    @Test
    public void makesUserRequestWhenJoinButtonClicked() throws InterruptedException {
        final String auctionId = "an item-id";
        final String[] receivedValue = {""}; // using final one-element array trick to make the field final but value editable

        mainWindow.addUserRequestListener(itemId -> receivedValue[0] = itemId);

        window.textBox(MainWindow.NEW_ITEM_ID_NAME).deleteText().enterText(auctionId);
        window.button(JOIN_BUTTON_NAME).click();

        assertThat(receivedValue[0]).isEqualTo(auctionId);
    }
}
