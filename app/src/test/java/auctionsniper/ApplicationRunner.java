package auctionsniper;

import auctionsniper.ui.MainWindow;

import static auctionsniper.CustomMatchers.getMainFrameByName;
import static auctionsniper.FakeAuctionServer.XMPP_HOSTNAME;

import static auctionsniper.Main.AUCTION_RESOURCE;
import static auctionsniper.ui.MainWindow.SNIPER_STATUS_NAME;

import org.assertj.swing.core.BasicRobot;
import org.assertj.swing.core.Robot;
import org.assertj.swing.finder.WindowFinder;
import org.assertj.swing.launcher.ApplicationLauncher;
import org.assertj.swing.fixture.FrameFixture;

import static org.assertj.swing.timing.Pause.pause;

import org.assertj.swing.timing.Condition;


public class ApplicationRunner {
    public static final String SNIPER_ID = "sniper";
    public static final String SNIPER_PASSWORD = "sniper";
    public static final String SNIPER_XMPP_ID = SNIPER_ID + "@localhost/" + AUCTION_RESOURCE;

    private FrameFixture window;

    public void startBiddingIn(final FakeAuctionServer auction) {
        ApplicationLauncher.application(Main.class).withArgs(XMPP_HOSTNAME, SNIPER_ID, SNIPER_PASSWORD, auction.getItemId()).start();

        Robot robot = BasicRobot.robotWithCurrentAwtHierarchy();
        window = WindowFinder.findFrame(getMainFrameByName(MainWindow.MAIN_WINDOW_NAME)).using(robot);
        window.focus();

        window.label(SNIPER_STATUS_NAME).requireText(MainWindow.STATUS_JOINING);
    }

    public void showsSniperHasLostAuction() {
        showsSniperStatus(MainWindow.STATUS_LOST);
    }

    public void hasShownSniperIsBidding() {
        showsSniperStatus(MainWindow.STATUS_BIDDING);
    }

    public void hasShownSniperIsWinning() {
        showsSniperStatus(MainWindow.STATUS_WINNING);
    }

    public void showsSniperHasWonAuction() {
        showsSniperStatus(MainWindow.STATUS_WON);
    }

    private void showsSniperStatus(String statusText) {
        pause(new Condition(String.format("sniper status text to change to: \"%s\"", statusText)) {
            String foundValue = EMPTY_TEXT;

            @Override
            public boolean test() {
                foundValue = window.label(SNIPER_STATUS_NAME).text();

                if (foundValue.equals(statusText))
                    return true;

                return false; // will append #descriptionAddendum()
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
