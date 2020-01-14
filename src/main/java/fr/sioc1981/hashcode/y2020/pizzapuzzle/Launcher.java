package fr.sioc1981.hashcode.y2020.pizzapuzzle;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Launcher {

	private static int maxSlices;
	private static ArrayList<Long> pizzas;
	private static long max;
	private static long allScore;
	private static Long allSlices;

	public static void main(String[] args) throws Exception {
//		String fileName = "";
//		fileName = "a_example";
//		fileName = "b_small";
//		fileName = "c_medium";
//		fileName = "d_quite_big";
//		fileName = "e_also_big";

		processFile("b_small");
//		Stream.of("a_example", "b_small", "c_medium", "d_quite_big", "e_also_big").forEach(Launcher::processFile);
		
		System.out.println("All Scores: " + allScore);
	}
	
	private static void processFile(String fileName){
		try {
			loadInput(new File("in", fileName + ".in"));
			TreeMap<Long, ArrayList<Integer>> combinations = new TreeMap<>();
			List<Integer> pizzasToOrder = process(pizzas, combinations);
			writeOutput(pizzasToOrder, fileName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
 
	private static List<Integer> process(ArrayList<Long> allPizzas,
			TreeMap<Long, ArrayList<Integer>> combinations) {
		Collections.reverse(allPizzas);
		int nbPizzas = allPizzas.size();
		max = 0;
		List<Integer> pizzasToOrder = new ArrayList<Integer>();
		final ArrayList<Integer> pizzasToRemove = new ArrayList<Integer>();
		long score = 0;
		long maxSliceToRemove = allSlices - maxSlices;
		long best = allSlices;
		int startIndex = 0;
		
		for (int i = startIndex; i < nbPizzas; i++) {
			Long pizza = allPizzas.get(i);
			long current = pizza - maxSliceToRemove;
			if (current == 0 ) {
				pizzasToRemove.clear();
				pizzasToRemove.add(i);
				score = allSlices - pizza;
				break;
			} else if (current > 0 && current < best) {
				best = current;
				pizzasToRemove.clear();
				pizzasToRemove.add(i);
				score = allSlices - pizza;
			}
		}
		System.out.println("score: " + score + " / " + maxSlices + " / " + allSlices);
		allScore += score;
		pizzasToOrder = IntStream.range(0, allPizzas.size()).filter(i -> !pizzasToRemove.contains(i)).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
		return pizzasToOrder;
	}

	private static void loadInput(File file) throws FileNotFoundException {
		try (final Scanner scanner = new Scanner(file)) {
			maxSlices = scanner.nextInt();
			int nbPizzas = scanner.nextInt();
			pizzas = new ArrayList<>(nbPizzas);
			scanner.forEachRemaining(s -> pizzas.add(Long.parseLong(s)));
//			System.out.println(pizzas);
			allSlices = pizzas.parallelStream().reduce((a,b) -> a+b).get();
//			System.out.println(maxSlices);
//			System.out.println(longAdder.sum());
		}
	}

	private static void writeOutput(List<Integer> pizzas, String fileName) throws Exception {
		System.out.println(pizzas);
		FileWriter fwriter = new FileWriter(new File("out", fileName + ".out"));
		try (BufferedWriter bwriter = new BufferedWriter(fwriter)) {
			bwriter.write(Integer.toString(pizzas.size()));
			bwriter.write('\n');
			bwriter.write(pizzas.stream().map(i -> Integer.toString(i)).collect(Collectors.joining(" ")));
		}
	}

}
