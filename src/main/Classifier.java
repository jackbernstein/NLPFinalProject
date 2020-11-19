package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Classifier class that creates a multinomial classifier
 *
 * @author Renae Tamura, Linnea Dahmen, Jack Bernstein
 *
 */
public class Classifier {

	public HashMap<String, Double> posCounts, negCounts;
	public HashMap<String, Double> posProbs, negProbs;
	public HashSet<String> vocab;
	public ArrayList<Tweet> tweets = new ArrayList<Tweet>();

	public Double numPos = 0.0, numNeg = 0.0;
	public Double numWordsPos = 0.0, numWordsNeg = 0.0;
	public Double probPos, probNeg;
	public Double lambda = 0.0;
	public boolean positive;

	/**
	 * Classifier constructor that takes training data, testing data, and a lambda value
	 *
	 * @param training
	 * @param testing
	 * @param smooth
	 */
	public Classifier(String training, String testing, String smooth) throws Exception {
		readDataCsv(training);



		this.lambda = Double.parseDouble(smooth);
		posCounts = new HashMap<String, Double>();
		negCounts = new HashMap<String, Double>();
		vocab = new HashSet<String>();

		// sort training data to train the model
		trainModel(training);
		probPos = Math.log10(numPos/(numPos + numNeg));
		probNeg = Math.log10(numNeg/(numPos + numNeg));


		testModel(testing);

		//NOTE: this is commented out so that the text does not get printed for every run
		// if uncommented, this will print out the probabilities and top 10 most predictive features

		calculateProbabilities();
		for(Map.Entry<String, Double> entry : posProbs.entrySet()) {
			System.out.println("p(" + entry.getKey() + "|positive) = " + entry.getValue().toString());
		}
		for(Map.Entry<String, Double> entry : negProbs.entrySet()) {
			System.out.println("p(" + entry.getKey() + "|negative) = " + entry.getValue().toString());
		}
		getTopTen();
	}

	/**
	 * method to train the model
	 * takes a paramenter for the file with the training data
	 *
	 * @param training
	 */
	public void trainModel(String training) {
		getCounts(training);
	}

	public void readDataCsv(String csv) throws Exception {
		List<List<String>> records = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new FileReader(csv))) {
	    String line;
	    while ((line = br.readLine()) != null) {
	        String[] values = line.split(",");
					if(values.length < 3)
						continue;
					Rating rate = getRating(values[0]);
					Airline air = getAirline(values[1]);
					String msg = getTweet(values);
					if(rate == null | air == null)
						continue;
					Tweet tweet = new Tweet(rate, air, msg);
					tweets.add(tweet);
	    }
		}
	}

	/**
	 * method to get the counts of each of the words for both the
	 * positive and negative sentences
	 *
	 * @param training
	 */
	public void getCounts(String training) {
		try {
			//BufferedReader br = new BufferedReader(new FileReader(new File(training)));
			//String line = br.readLine();

			for(Tweet tweet : tweets) {
					String line = tweet.getTweet();
					String[] words = line.split("\\s");
					if(tweet.getRating().equals(Rating.POSITIVE)) {
						numPos++;
					} else {
						numNeg++;
					}

					// add counts for each sentence
					for(int i = 1; i < words.length; i++) {
						String wrd = words[i];
						vocab.add(wrd);
						if(tweet.getRating().equals(Rating.POSITIVE)) {
							posCounts.putIfAbsent(wrd, 0.0);
							Double count = posCounts.get(wrd) + 1;
							posCounts.put(wrd, count);
							numWordsPos++;
						} else {
							negCounts.putIfAbsent(wrd, 0.0);
							Double count = negCounts.get(wrd) + 1;
							negCounts.put(wrd, count);
							numWordsNeg++;
						}
					}
				}
		} catch(Exception e) {
			System.out.println("Error: unable to read training file");
			e.printStackTrace();
		}
	}

	/**
	 * method to test the model with the given data
	 * takes a string for the testing data
	 *
	 * @param testing
	 */
	public void testModel(String testing) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File(testing)));
			String line = br.readLine();

			// classify each line and print out the result
			while(line != null) {
				String classified = classify(line);
				System.out.println(classified + "	" + line);
				line = br.readLine();
			}

			br.close();
		} catch(Exception e) {
			System.out.println("Error: could not read testing file");
			e.printStackTrace();
		}
	}

	/**
	 * classify method that takes a sentence and classifies as either
	 * negative or positive
	 *
	 * @param sentence
	 * @return classification
	 */
	public String classify(String sentence) {
		String[] words = sentence.split("\\s");
		Double pos = getPosProb(words);
		Double neg = getNegProb(words);

		String retString;
		if(neg > pos) {
			retString = "negative	" + neg;
		} else {
			retString = "positive	" + pos;
		}
		return retString;
	}

	/**
	 * get positive probability for the sentence
	 * takes an array of Strings that represent the words
	 *
	 * @param words
	 * @return positive probability
	 */
	public Double getPosProb(String[] words) {
		Double prob = probPos;
		for(String wrd : words) {
			if(vocab.contains(wrd)) {
				prob += calculateLogProb(wrd, true);
			}
		}
		return prob;
	}

	/**
	 * get negative probability for the sentence
	 * takes an array of Strings that represent the words
	 *
	 * @param words
	 * @return negative probability
	 */
	public Double getNegProb(String[] words) {
		Double prob = probNeg;
		for(String wrd : words) {
			if(vocab.contains(wrd)) {
				prob += calculateLogProb(wrd, false);
			}
		}
		return prob;
	}

	/**
	 * calculate log probabilities
	 * takes a word and a boolean and returns the proper log probability
	 *
	 * @param word
	 * @param isPos
	 * @return log probability
	 */
	public Double calculateLogProb(String word, Boolean isPos) {
		Double prob = 0.0;
		if(isPos) {
			Double res = posCounts.get(word);
			if(res != null && res != 0){
				prob = posCounts.get(word)/numWordsPos;
			} else {
				prob = lambda/numWordsPos;
			}
		} else {
			Double res = negCounts.get(word);
			if(res != null && res != 0){
				prob = negCounts.get(word)/numWordsNeg;
			} else {
				prob = lambda/numWordsNeg;
			}
		}
		return Math.log10(prob);
	}

	/**
	 * method to determine if the sentence is classified as positive or negative
	 *
	 * @param rating
	 * @return positive boolean
	 */
	public Boolean isPositive(String rating) {
		return rating.equals("positive");
	}

	/**
	 * calculate probabilities method
	 * used to calculate probabilities to be printed out for the assignment
	 *
	 * NOTE: this is not actually used in our regular calculations
	 */
	public void calculateProbabilities() {
		posProbs = new HashMap<String, Double>();
		negProbs = new HashMap<String, Double>();
		for(Map.Entry<String, Double> entry : posCounts.entrySet()) {
			String wrd = entry.getKey();
			Double val = calculateLogProb(wrd, true);
			posProbs.put(wrd, val);
		}

		for(Map.Entry<String, Double> entry : negCounts.entrySet()) {
			String wrd = entry.getKey();
			Double val = calculateLogProb(wrd, false);
			negProbs.put(wrd, val);
		}
	}

	/**
	 * get top ten method
	 * used to get the top ten most predictive features for positive and negative
	 *
	 * NOTE: this is not actually used in our regular calculations
	 */
	public void getTopTen() {
		ArrayList<Pair> pairs = new ArrayList<Pair>();
		for(String word : vocab) {
			if(posProbs.containsKey(word) && negProbs.containsKey(word)) {
				Double val = posProbs.get(word)/negProbs.get(word);
				Pair pair = new Pair(word, val);
				pairs.add(pair);
			}
		}

		Collections.sort(pairs);

		// prints out the top 10 most predictive features
		System.out.println("Top 10 most predictive: positive");
		for(int i = 0; i < 10; i++) {
			System.out.println(pairs.get(i).toString());
		}
		System.out.println("Top 10 most predictive: negative");
		for(int i = pairs.size() - 1; i > pairs.size() - 11; i--) {
			System.out.println(pairs.get(i).toString());
		}
	}

	public Airline getAirline(String airline) {
		switch (airline) {
			case "Virgin America":
				return Airline.VIRGIN;
			case "United":
				return Airline.UNITED;
			case "Soutwest":
				return Airline.SOUTHWEST;
			case "Delta":
				return Airline.DELTA;
			case "US Airways":
				return Airline.US;
			// case "American":
			// 	return Airline.AMERICAN;
			default:
				return null;
		}
	}

	public Rating getRating(String rating) {
		switch (rating) {
			case "positive":
				return Rating.POSITIVE;
			case "negative":
				return Rating.NEGATIVE;
			case "neutral":
				return Rating.NEUTRAL;
			default:
				return null;
		}
	}

	public String getTweet(String[] csvLine){
		String res = "";
		for(int i = 2; i < csvLine.length; i++){
			res += csvLine[i];
		}
		return res;
	}


	/**
	 * main method that creates a new classifier
	 *
	 * @param args[0] – training data
	 * @param args[1] – testing data
	 * @param args[2] – lambda
	 */
	public static void main(String[] args) throws Exception {
		if(args.length != 3) {
			System.out.println("Error: Incorrect number of parameters");
		} else {
			new Classifier(args[0], args[1], args[2]);
		}
	}

}
