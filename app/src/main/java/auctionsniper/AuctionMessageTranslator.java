package auctionsniper;

import java.util.HashMap;

public class AuctionMessageTranslator {
    AuctionEventListener listener;

    public AuctionMessageTranslator(AuctionEventListener listener) {
        this.listener = listener;
    }

    public void translateMessage(String message) {
        HashMap<String, String> event = unpackEventFrom(message);

        String type = event.get("Event");
        if (type.equals("CLOSE")) {
            listener.auctionClosed();
        } else if (type.equals("PRICE")) {
            listener.currentPrice(
                Integer.parseInt(event.get("CurrentPrice")),
                Integer.parseInt(event.get("Increment"))
            );
        }
    }

    private HashMap<String, String> unpackEventFrom(String message) {
        HashMap<String, String> event = new HashMap<>();

        for (String element : message.split(";")) {
            String[] pair = element.split(":");
            event.put(pair[0].trim(), pair[1].trim());
        }

        return event;
    }
}
