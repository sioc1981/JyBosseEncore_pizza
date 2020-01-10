package fr.sioc1981.hashcode.y2020.pizzapuzzle;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;

public class Launcher {

	private static int maxSlices;
	private static ArrayList<Long> pizzas;
	private static long max;

	public static void main(String[] args) throws Exception {
		String fileName = "";
//		fileName = "a_example";
//		fileName = "b_small";
//		fileName = "c_medium";
		fileName = "d_quite_big";
//		fileName = "e_also_big";
		loadInput(new File("in", fileName+".in"));
		TreeMap<Long, ArrayList<Integer>> combinations = new TreeMap<>();
		ArrayList<Integer> pizzasToOrder = process(pizzas,combinations);
		writeOutput(pizzasToOrder, fileName);
	}

	private static ArrayList<Integer> process(ArrayList<Long> allPizzas, TreeMap<Long, ArrayList<Integer>> combinations) {
		Collections.reverse(allPizzas);
		int nbPizzas = allPizzas.size();
		max = 0;
		for (int i = 0 ; i< nbPizzas; i++) {
			Long pizza = allPizzas.get(i);
//			final int index = i;
			final int index = nbPizzas - i - 1;
			ConcurrentHashMap<Long, ArrayList<Integer>> combinationsToAdd = new ConcurrentHashMap<Long, ArrayList<Integer>>(); 
			combinations.entrySet().parallelStream().forEach(e -> {
				long newScore = e.getKey() + pizza;
				if (newScore <= maxSlices) {
					if (!combinations.containsKey(newScore) && !combinationsToAdd.containsKey(newScore)) {
						max = Math.max(max, newScore);
						ArrayList<Integer> newPizzasToOrder = new ArrayList<Integer>(e.getValue().size()+1);
//						newPizzasToOrder.addAll(e.getValue());
//						newPizzasToOrder.add(index);
						newPizzasToOrder.add(index);
						newPizzasToOrder.addAll(e.getValue());
						combinationsToAdd.put(newScore, newPizzasToOrder);
					}
				}
			});
			combinations.putAll(combinationsToAdd);
			
			ArrayList<Integer> pizzasToOrder = new ArrayList<Integer>();
			long newScore = pizza;
			if (!combinations.containsKey(newScore)) {
				max = Math.max(max, newScore);
				ArrayList<Integer> newPizzasToOrder = new ArrayList<Integer>(pizzasToOrder);
				newPizzasToOrder.add(index);
				combinations.put(newScore, newPizzasToOrder);
			}
			System.out.println("index: " + index + " combinations:" + combinations.size() + " max: " + max);
		}
		Entry<Long, ArrayList<Integer>> best = combinations.pollLastEntry();
		System.out.println("score: " + best.getKey());
		return best.getValue();
	}

	private static void loadInput(File file) throws FileNotFoundException {
		try (final Scanner scanner = new Scanner(file)) {
			maxSlices = scanner.nextInt();
			int nbPizzas = scanner.nextInt();
			pizzas = new ArrayList<>(nbPizzas);
			scanner.forEachRemaining(s -> pizzas.add(Long.parseLong(s)));
			System.out.println(pizzas);
			final LongAdder longAdder = new LongAdder();
			pizzas.forEach(longAdder::add);
			System.out.println(maxSlices);
			System.out.println(longAdder.sum());
		}
	}

	private static void writeOutput(ArrayList<Integer> pizzas, String fileName) throws Exception {
		System.out.println(pizzas);
		FileWriter fwriter = new FileWriter(new File("out", fileName + ".out"));
		try (BufferedWriter bwriter = new BufferedWriter(fwriter)) {
			bwriter.write(Integer.toString(pizzas.size()));
			bwriter.write('\n');
			bwriter.write(pizzas.stream().map(i -> Integer.toString(i)).collect(Collectors.joining(" ")));
		}
	}
	
}
