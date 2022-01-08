package auctionsniper;

import auctionsniper.ui.MainWindow;

import static auctionsniper.CustomMatchers.getMainFrameByName;
import static auctionsniper.FakeAuctionServer.XMPP_HOSTNAME;

import static auctionsniper.ui.MainWindow.SNIPER_STATUS_NAME;

import static org.junit.Assert.fail;

import org.assertj.swing.core.BasicRobot;
import org.assertj.swing.core.Robot;
import org.assertj.swing.finder.WindowFinder;
import org.assertj.swing.launcher.ApplicationLauncher;
import org.assertj.swing.fixture.FrameFixture;


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
        fail("not implemented");
    }

    public void stop() {
        if (window != null)
            window.cleanUp();
    }
}
