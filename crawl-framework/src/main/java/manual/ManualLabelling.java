package manual;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class ManualLabelling {

	private static int countLines(File file) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(file));
		int count = 0;
		while(br.readLine() != null) {count++;}
		br.close();
		return count;
	}
	
	private static void printLabel(String path, String tweet) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(path), true));
		bw.write(tweet+System.lineSeparator());
		bw.close();
	}
	
	public static void main(String[] args) {
		File toClassify = new File("neutral_tweets_to_split.txt");
		Scanner scanner = new Scanner(System.in);
		try {
			BufferedReader br = new BufferedReader(new FileReader(toClassify));
			String line = "";
			int count = 0;
			int total = countLines(toClassify);
			while((line = br.readLine()) != null) {
				boolean cont = false;
				while(!cont) {
					cont = true;
					System.out.println("#### Tweet " + count++ + " out of " + total + ". ####");
					System.out.println(line);
					System.out.println("POSITIVE (1) ? NEUTRAL/IRRELEVANT (2) ? NEGATIVE (3) ?");
					int choice = scanner.nextInt();
					scanner.nextLine();
					switch (choice) {
					case 1:
						printLabel("positive_tweets.txt", line);
						break;
					case 2:
						printLabel("neutral_tweets.txt", line);
						break;
					case 3:
						printLabel("negative_tweets.txt", line);
						break;
					default:
						cont = false;
						System.err.println("Press 1, 2 or 3 !");
					}
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
