package actors;

import play.libs.Json;
import play.mvc.WebSocket;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.JsonNode;

import akka.actor.UntypedActor;

public class TwitterActor extends UntypedActor {

	private final WebSocket.Out<JsonNode> out;

	public TwitterActor(WebSocket.Out<JsonNode> out) {
		// TODO Auto-generated constructor stub
		this.out = out;
		
		System.out.println("twitter actor is online weep weep");
		
        ObjectNode tweet = Json.newObject();
        tweet.put("type", "tweet");
        tweet.put("symbol", "ca marche, yeeeeehaaaaaa *PAN* *PAN* *PAN*");
        out.write(tweet);
	}

	@Override
	public void onReceive(Object arg0) throws Exception {
		// TODO Auto-generated method stub

	}

}
