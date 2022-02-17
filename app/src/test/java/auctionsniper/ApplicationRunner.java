package auctionsniper;

import auctionsniper.ui.MainWindow;

import static auctionsniper.ui.MainWindow.SnipersTableModel.textFor;

import static auctionsniper.CustomMatchers.getMainFrameByName;
import static auctionsniper.FakeAuctionServer.XMPP_HOSTNAME;

import static auctionsniper.Main.AUCTION_RESOURCE;
import static auctionsniper.ui.MainWindow.SNIPERS_TABLE_NAME;

import org.assertj.swing.core.BasicRobot;
import org.assertj.swing.core.Robot;
import org.assertj.swing.finder.WindowFinder;
import org.assertj.swing.launcher.ApplicationLauncher;
import org.assertj.swing.fixture.FrameFixture;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.swing.timing.Pause.pause;

import org.assertj.swing.timing.Condition;

import java.util.Arrays;


public class ApplicationRunner {
    public static final String SNIPER_ID = "sniper";
    public static final String SNIPER_PASSWORD = "sniper";
    public static final String SNIPER_XMPP_ID = SNIPER_ID + "@localhost/" + AUCTION_RESOURCE;

    private FrameFixture window;

    public void startBiddingIn(final FakeAuctionServer ...auctions) {
        ApplicationLauncher.application(Main.class).withArgs(arguments(auctions)).start();

        Robot robot = BasicRobot.robotWithCurrentAwtHierarchy();
        window = WindowFinder.findFrame(getMainFrameByName(MainWindow.MAIN_WINDOW_NAME)).using(robot);
        window.focus();

        window.requireTitle(MainWindow.APPLICATION_TITLE);
        hasColumnTitles();
        showsSniperStatus("", 0, 0, textFor(SniperState.JOINING));
    }

    protected static String[] arguments(FakeAuctionServer ...auctions) {
        String[] arguments = new String[auctions.length + 3];

        arguments[0] = XMPP_HOSTNAME;
        arguments[1] = SNIPER_ID;
        arguments[2] = SNIPER_PASSWORD;
        for (int i = 0; i < auctions.length; i++)
            arguments[i+3] = auctions[i].getItemId();

        return arguments;
    }

    private void hasColumnTitles() {
        for (String columnName : new String[] {"Item", "Last Price", "Last Bid", "State"})
            assertThat(window.table(SNIPERS_TABLE_NAME).columnIndexFor(columnName));
    }

    public void showsSniperHasLostAuction(FakeAuctionServer auction, int lastPrice, int lastBid) {
        showsSniperStatus(auction.getItemId(), lastPrice, lastBid, textFor(SniperState.LOST));
    }

    public void hasShownSniperIsBidding(FakeAuctionServer auction, int lastPrice, int lastBid) {
        showsSniperStatus(auction.getItemId(), lastPrice, lastBid, textFor(SniperState.BIDDING));
    }

    public void hasShownSniperIsWinning(FakeAuctionServer auction, int winningBid) {
        showsSniperStatus(auction.getItemId(), winningBid, winningBid, textFor(SniperState.WINNING));
    }

    public void showsSniperHasWonAuction(FakeAuctionServer auction, int lastPrice) {
        showsSniperStatus(auction.getItemId(), lastPrice, lastPrice, textFor(SniperState.WON));
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
