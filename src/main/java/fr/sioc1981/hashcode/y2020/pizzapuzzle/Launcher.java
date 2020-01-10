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
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Launcher {

	private static int maxSlices;
	private static ArrayList<Long> pizzas;
	private static long max;

	public static void main(String[] args) throws Exception {
//		String fileName = "";
//		fileName = "a_example";
//		fileName = "b_small";
//		fileName = "c_medium";
//		fileName = "d_quite_big";
//		fileName = "e_also_big";

		Stream.of("a_example", "b_small", "c_medium", "d_quite_big", "e_also_big").forEach(fileName -> {
			try {
				loadInput(new File("in", fileName + ".in"));
				TreeMap<Long, ArrayList<Integer>> combinations = new TreeMap<>();
				ArrayList<Integer> pizzasToOrder = process(pizzas, combinations);
				writeOutput(pizzasToOrder, fileName);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	private static ArrayList<Integer> process(ArrayList<Long> allPizzas,
			TreeMap<Long, ArrayList<Integer>> combinations) {
		Collections.reverse(allPizzas);
		int nbPizzas = allPizzas.size();
		max = 0;
		ArrayList<Integer> pizzasToOrder = new ArrayList<Integer>();
		long score = 0;
		for (int i = 0; i < nbPizzas; i++) {
			Long pizza = allPizzas.get(i);
			final int index = nbPizzas - i - 1;
			if (score + pizza < maxSlices) {
				pizzasToOrder.add(index);
				score += pizza;
			}
		}
		System.out.println("score: " + score);
		Collections.reverse(pizzasToOrder);
		return pizzasToOrder;
	}

	private static void loadInput(File file) throws FileNotFoundException {
		try (final Scanner scanner = new Scanner(file)) {
			maxSlices = scanner.nextInt();
			int nbPizzas = scanner.nextInt();
			pizzas = new ArrayList<>(nbPizzas);
			scanner.forEachRemaining(s -> pizzas.add(Long.parseLong(s)));
//			System.out.println(pizzas);
			final LongAdder longAdder = new LongAdder();
			pizzas.forEach(longAdder::add);
//			System.out.println(maxSlices);
//			System.out.println(longAdder.sum());
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
