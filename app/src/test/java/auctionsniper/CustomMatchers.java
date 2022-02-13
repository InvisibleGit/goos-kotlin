package auctionsniper;

import org.assertj.swing.core.GenericTypeMatcher;
import org.mockito.ArgumentMatcher;

import javax.swing.*;


public class CustomMatchers {
    public static GenericTypeMatcher<JFrame> getMainFrameByName(final String frameName) {
        return new GenericTypeMatcher<>(JFrame.class) {
            @Override
            protected boolean isMatching(JFrame frame) {
                boolean matchesName = frameName
                    .replace(" ", "")
                    .equals(frame.getName().replace(" ", ""));

                return frame.isActive() && matchesName;
            }

            @Override
            public String toString() {
                return "getMainFrameByName: " + frameName;
            }
        };
    }

    public static SniperStateMatcher isStateOf(SniperState wantedState) {
        return new SniperStateMatcher(wantedState);
    }

    static class SniperStateMatcher implements ArgumentMatcher<SniperSnapshot> {
        private final SniperState wantedState;
        private SniperState foundState;

        SniperStateMatcher(SniperState wantedState) {
            this.wantedState = wantedState;
        }

        public boolean matches(SniperSnapshot snapshot) {
            foundState = snapshot.state;
            return snapshot.state.equals(wantedState);
        }
        public String toString() {
            return "wanted snapshot with state: " + wantedState + ", but was: " + foundState;
        }
    }
}
