package actors;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;

import play.Play;
import play.api.libs.json.JsArray;
import play.libs.Json;
import play.mvc.WebSocket;
import akka.actor.UntypedActor;
import ch.epfl.bigdata.btc.types.Transfer.Points;
import ch.epfl.bigdata.btc.types.Transfer.Tweet;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import scala.collection.JavaConverters.*;

/**
 * The broker between the WebSocket and the StockActor(s). The UserActor holds
 * the connection and sends serialized JSON data to the client.
 */

public class UserActor extends UntypedActor {

	private final WebSocket.Out<JsonNode> out;

	public UserActor(WebSocket.Out<JsonNode> out) {
		// get outputstream to websocket
		this.out = out;

		// watch the default stocks
		// get the list of stock names from config file
		List<String> defaultStocks = Play.application().configuration()
				.getStringList("default.stocks");

		// send message to StocksActor to create StockActor(s)
		for (String stockSymbol : defaultStocks) {
			StocksActor.stocksActor().tell(new WatchStock(stockSymbol),
					getSelf());
		}

	}

	public void onReceive(Object message) {
		if (message instanceof StockUpdate) {

			// push the stock to the client
			StockUpdate stockUpdate = (StockUpdate) message;
			ObjectNode stockUpdateMessage = Json.newObject();
			stockUpdateMessage.put("type", "stockupdate");
			stockUpdateMessage.put("symbol", stockUpdate.symbol());
			stockUpdateMessage.put("price", stockUpdate.price().doubleValue());
			// time field is number of seconds since 1.1.1970
			stockUpdateMessage.put("time", stockUpdate.time());

			out.write(stockUpdateMessage);
		} else if (message instanceof StockHistory) {
			// push the history to the client
			StockHistory stockHistory = (StockHistory) message;

			ObjectNode stockUpdateMessage = Json.newObject();
			stockUpdateMessage.put("type", "stockhistory");
			stockUpdateMessage.put("symbol", stockHistory.symbol());

			ArrayNode historyJson = stockUpdateMessage.putArray("history");
			for (Object price : stockHistory.history()) {
				historyJson.add(((Number) price).doubleValue());
			}

			out.write(stockUpdateMessage);
		} else if (message instanceof Tweet) {
			Tweet tweet = (Tweet) message;
			ObjectNode messageForGUI = Json.newObject();
			messageForGUI.put("type", "tweet");
			messageForGUI.put("content", tweet.content());
			messageForGUI.put("author", tweet.author());
			messageForGUI.put("imagesrc", tweet.imagesrc());
			messageForGUI.put("sentiment", tweet.sentiment());
			messageForGUI
					.put("hour", tweet.date().hourOfDay().getAsShortText());
			messageForGUI.put("minutes", tweet.date().minuteOfHour()
					.getAsShortText());
			out.write(messageForGUI);

		} else if (message instanceof EMAupdate) {
			// type: "EMA"
			// array: "values", dedans: array: [timestamp,valeur]
			out.write(((EMAupdate) message).json());

		} else if (message instanceof SMAupdate) {
			// type: "SMA"
			// array: "values", dedans: array: [timestamp,valeur]
			out.write(((SMAupdate) message).json());
		}

	}
}
