package actors;

import java.util.List;

import play.Play;
import play.libs.Json;
import play.mvc.WebSocket;
import akka.actor.UntypedActor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The broker between the WebSocket and the StockActor(s).  The UserActor holds the connection and sends serialized
 * JSON data to the client.
 */

public class UserActor extends UntypedActor {

    private final WebSocket.Out<JsonNode> out;
    
    public UserActor(WebSocket.Out<JsonNode> out) {
    	// get outputstream to websocket
        this.out = out;
        
        // watch the default stocks
        // get the list of stock names from config file
        List<String> defaultStocks = Play.application().configuration().getStringList("default.stocks");

        // send message to StocksActor to create StockActor(s)
        for (String stockSymbol : defaultStocks) {
            StocksActor.stocksActor().tell(new WatchStock(stockSymbol), getSelf());
        }
        
    }
    
    public void onReceive(Object message) {
        if (message instanceof StockUpdate) {
        	
        	System.out.println("user actor - stock update");
            // push the stock to the client
            StockUpdate stockUpdate = (StockUpdate)message;
            ObjectNode stockUpdateMessage = Json.newObject();
            stockUpdateMessage.put("type", "stockupdate");
            stockUpdateMessage.put("symbol", stockUpdate.symbol());
            stockUpdateMessage.put("price", stockUpdate.price().doubleValue());
            out.write(stockUpdateMessage);
        }
        else if (message instanceof StockHistory) {
            // push the history to the client
            StockHistory stockHistory = (StockHistory)message;
            System.out.println("user actor - history fetched");

            ObjectNode stockUpdateMessage = Json.newObject();
            stockUpdateMessage.put("type", "stockhistory");
            stockUpdateMessage.put("symbol", stockHistory.symbol());

            ArrayNode historyJson = stockUpdateMessage.putArray("history");
            for (Object price : stockHistory.history()) {
                historyJson.add(((Number)price).doubleValue());
            }
            
            out.write(stockUpdateMessage);
        } else if (message instanceof java.lang.Double) {
        	ObjectNode stockUpdateMessage = Json.newObject();
            stockUpdateMessage.put("type", "stockupdate");
            stockUpdateMessage.put("symbol", "BTC");
            stockUpdateMessage.put("price", (double) message);
//            stockUpdateMessage.put("price", message);
            out.write(stockUpdateMessage);
        }
        
    }
}
