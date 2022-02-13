package auctionsniper;

import auctionsniper.ui.Column;
import auctionsniper.ui.MainWindow;

import static auctionsniper.CustomMatchers.getMainFrameByName;
import static auctionsniper.FakeAuctionServer.XMPP_HOSTNAME;

import static auctionsniper.Main.AUCTION_RESOURCE;
import static auctionsniper.ui.MainWindow.SNIPERS_TABLE_NAME;

import org.assertj.swing.core.BasicRobot;
import org.assertj.swing.core.Robot;
import org.assertj.swing.data.TableCell;
import org.assertj.swing.finder.WindowFinder;
import org.assertj.swing.launcher.ApplicationLauncher;
import org.assertj.swing.fixture.FrameFixture;

import static org.assertj.swing.timing.Pause.pause;
import static org.junit.Assert.fail;

import org.assertj.swing.timing.Condition;

import java.util.Arrays;


public class ApplicationRunner {
    public static final String SNIPER_ID = "sniper";
    public static final String SNIPER_PASSWORD = "sniper";
    public static final String SNIPER_XMPP_ID = SNIPER_ID + "@localhost/" + AUCTION_RESOURCE;

    private FrameFixture window;

    private String itemId;

    public void startBiddingIn(final FakeAuctionServer auction) {
        itemId = auction.getItemId();

        ApplicationLauncher.application(Main.class).withArgs(XMPP_HOSTNAME, SNIPER_ID, SNIPER_PASSWORD, itemId).start();

        Robot robot = BasicRobot.robotWithCurrentAwtHierarchy();
        window = WindowFinder.findFrame(getMainFrameByName(MainWindow.MAIN_WINDOW_NAME)).using(robot);
        window.focus();

        window.table(SNIPERS_TABLE_NAME).cell(TableCell.row(0).column(Column.SNIPER_STATE.ordinal())).requireValue(MainWindow.STATUS_JOINING);
    }

    public void showsSniperHasLostAuction() {
        fail("fix me"); // was: showsSniperStatus(MainWindow.STATUS_LOST);
    }

    public void hasShownSniperIsBidding(int lastPrice, int lastBid) {
        showsSniperStatus(itemId, lastPrice, lastBid, MainWindow.STATUS_BIDDING);
    }

    public void hasShownSniperIsWinning(int winningBid) {
        showsSniperStatus(itemId, winningBid, winningBid, MainWindow.STATUS_WINNING);
    }

    public void showsSniperHasWonAuction(int lastPrice) {
        showsSniperStatus(itemId, lastPrice, lastPrice, MainWindow.STATUS_WON);
    }

    private void showsSniperStatus(String itemId, int lastPrice, int lastBid, String statusText) {
        pause(new Condition(String.format("sniper table row text to change to: [%s, %d, %d, %s]", itemId, lastPrice, lastBid, statusText)) {
            String foundValue = EMPTY_TEXT;

            @Override
            public boolean test() {
                String[][] tableContents = window.table(SNIPERS_TABLE_NAME).contents();

                if (Arrays.deepEquals(tableContents, new String[][] {
                    { itemId, Integer.toString(lastPrice), Integer.toString(lastBid), statusText }
                })) return true;

                foundValue = String.format("%s", Arrays.toString(tableContents[0]));
                return false;
            }

            @Override
            public String descriptionAddendum() {
                return ", found: \"" + foundValue + "\""; // adds more descriptive error by appending found value
            }
        }, 1000);
    }

    public void stop() {
        if (window != null)
            window.cleanUp();
    }
}
