package manual;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


public class JythonTest {

	public static void main(String[] args) {
		String strNeutral = "I don't know what to think about Bitcoin. Maybe it will last, maybe it won't";
		String strNegative = "Bitcoin is falling !!!, everybody should sell now before it's too late, I already have lost 2 millions :(!";
		String strPositive = "So happy for having invest a lot of money in bitcoin, it finally pays off!";
		String sad = "I hate my fucking life, it sucks so much. Mtgox bankruptcy !!!";
		try {
//			Process p = Runtime.getRuntime().exec("python sentiment.py " + "`" + strNeutral + "`");
//			Process p = Runtime.getRuntime().exec("python sentiment.py " + "`" + strNegative + "`");
			String[] commands = {"python","sentiment.py",strNegative};
			Process p = Runtime.getRuntime().exec(commands);
			BufferedReader stdInput = new BufferedReader(new 
					InputStreamReader(p.getInputStream()));

			BufferedReader stdError = new BufferedReader(new 
					InputStreamReader(p.getErrorStream()));

			String res = stdInput.readLine();
			System.out.println(res);
			
             String s = "";
            //Print stderr for debugging purpose
			while ((s = stdError.readLine()) != null) {
				System.err.println(s);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
