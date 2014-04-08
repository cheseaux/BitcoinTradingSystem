
import java.io.IOException;
import java.util.List;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.util.ArrayCoreMap;
import edu.stanford.nlp.util.CoreMap;

public class SentimentAnalysis {

	public static void main(String[] args) throws IOException {
		

		StanfordCoreNLP pipeline = new StanfordCoreNLP();
		Annotation annotation;
		annotation = new Annotation("Kosgi Santosh sent an email to Stanford University. He didn't get a reply.");


		pipeline.annotate(annotation);

		List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
		if (sentences != null && sentences.size() > 0) {
			ArrayCoreMap sentence = (ArrayCoreMap) sentences.get(0);

			Tree tree = sentence.get(TreeCoreAnnotations.TreeAnnotation.class);
			
			int sentiment = SentimentAnnotator
            String partText = sentence.toString();

		}
	}

}