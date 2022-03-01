package auctionsniper;

import org.assertj.swing.core.GenericTypeMatcher;
import org.mockito.ArgumentMatcher;

import javax.swing.*;
import javax.swing.event.TableModelEvent;


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

    public static TableModelEventMatcher isOfType(int eventType) {
        return new TableModelEventMatcher(eventType);
    }

    static class TableModelEventMatcher implements ArgumentMatcher<TableModelEvent> {
        private final int wantedType;
        private int foundType;

        TableModelEventMatcher(int wantedType) {
            this.wantedType = wantedType;
        }

        public boolean matches(TableModelEvent tableModelEvent) {
            foundType = tableModelEvent.getType();
            return tableModelEvent.getType() == wantedType;
        }

        public String toString() {
            return "wanted TableModelEvent of type: " + typeToString(wantedType) + ", but was: " + typeToString(foundType);
        }

        private String typeToString(int type) {
            return (type == 0) ? "UPDATE" : (type == 1) ? "INSERT" : "DELETE";
        }
    }
}
