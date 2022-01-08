package auctionsniper;

import org.assertj.swing.core.GenericTypeMatcher;
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
}
