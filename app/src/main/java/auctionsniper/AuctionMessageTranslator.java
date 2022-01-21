package auctionsniper;

import java.util.HashMap;

import auctionsniper.AuctionEventListener.PriceSource;
import static auctionsniper.AuctionEventListener.PriceSource.FromOtherBidder;
import static auctionsniper.AuctionEventListener.PriceSource.FromSniper;


public class AuctionMessageTranslator {
    private final String sniperId;
    AuctionEventListener listener;

    public AuctionMessageTranslator(String sniperId, AuctionEventListener listener) {
        this.sniperId = sniperId;
        this.listener = listener;
    }

    public void translateMessage(String message) {
        AuctionEvent event = AuctionEvent.from(message);

        String eventType = event.type();
        if (eventType.equals("CLOSE")) {
            listener.auctionClosed();
        } else if (eventType.equals("PRICE")) {
            listener.currentPrice(
                event.currentPrice(),
                event.increment(),
                event.isFrom(sniperId)
            );
        }
    }

    private static class AuctionEvent {
        private HashMap<String, String> fields = new HashMap<>();

        public static AuctionEvent from(String message) {
            AuctionEvent event = new AuctionEvent();

            for (String field : fieldsIn(message))
                event.addField(field);

            return event;
        }

        static String[] fieldsIn(String messageBody) {
            return messageBody.split(";");
        }

        private void addField(String field) {
            String[] pair = field.split(":");
            fields.put(pair[0].trim(), pair[1].trim());
        }

        public String type() {
            return get("Event");
        }

        public int currentPrice() {
            return getInt("CurrentPrice");
        }

        public int increment() {
            return getInt("Increment");
        }

        public PriceSource isFrom(String sniperId) {
            return sniperId.equals(bidder()) ? FromSniper : FromOtherBidder;
        }

        private String bidder() {
            return get("Bidder");
        }

        private String get(String fieldName) {
            return fields.get(fieldName);
        }

        private int getInt(String fieldName) {
            return Integer.parseInt(get(fieldName));
        }
    }
}
