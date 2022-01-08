package auctionsniper;

import auctionsniper.ui.MainWindow;

import static auctionsniper.CustomMatchers.getMainFrameByName;
import static auctionsniper.FakeAuctionServer.XMPP_HOSTNAME;

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

    private FrameFixture window;

    public void startBiddingIn(final FakeAuctionServer auction) {
        ApplicationLauncher.application(Main.class).withArgs(XMPP_HOSTNAME, SNIPER_ID, SNIPER_PASSWORD, auction.getItemId()).start();

        Robot robot = BasicRobot.robotWithCurrentAwtHierarchy();
        window = WindowFinder.findFrame(getMainFrameByName(MainWindow.MAIN_WINDOW_NAME)).using(robot);
        window.focus();

        window.label(SNIPER_STATUS_NAME).requireText(MainWindow.STATUS_JOINING);
    }

    public void showSniperHasLostAuction() {
        String expectedValue = MainWindow.STATUS_LOST;

        pause(new Condition(String.format("sniper status text to change to: \"%s\"", expectedValue)) {
            String foundValue = EMPTY_TEXT;

            @Override
            public boolean test() {
                foundValue = window.label(SNIPER_STATUS_NAME).text();

                if (foundValue.equals(expectedValue))
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
