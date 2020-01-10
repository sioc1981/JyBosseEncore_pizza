package fr.sioc1981.hashcode.y2020.pizzapuzzle;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Launcher {

	private static int maxSlices;
	private static ArrayList<Long> pizzas;
	private static long allPizzaSlices;
	private static long max;
	private static ForkJoinPool pool = ForkJoinPool.commonPool();
	private static ArrayList<Task> tasks = new ArrayList<Task>();

	public static void main(String[] args) throws Exception {
		String fileName = "";
//		fileName = "a_example";
//		fileName = "b_small";
//		fileName = "c_medium";
		fileName = "d_quite_big";
//		fileName = "e_also_big";
		loadInput(new File("in", fileName+".in"));
		TreeMap<Long, List<Integer>> combinations = new TreeMap<>();
		List<Integer> pizzasToOrder = process(pizzas,combinations);
		writeOutput(pizzasToOrder, fileName);
	}

	private static List<Integer> process(ArrayList<Long> allPizzas, TreeMap<Long, List<Integer>> combinations) {
//		Collections.reverse(allPizzas);
		int nbPizzas = allPizzas.size();
		max = 0;
		int nbPizzaToRemove = 2;
		
		do {
			ArrayList<Integer> indexToRemove = new ArrayList<Integer>();
			int depth = 0;
			processRecursive(allPizzas, combinations, nbPizzaToRemove, indexToRemove, depth);
			tasks.forEach(t -> {
				try {
					t.get();
				} catch (InterruptedException | ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});
			nbPizzaToRemove++;
		} while (combinations.isEmpty()) ;
		Entry<Long, List<Integer>> best = combinations.pollLastEntry();
		System.out.println("score: " + best.getKey());
		return best.getValue();
	}

	private static void processRecursive(ArrayList<Long> allPizzas, TreeMap<Long, List<Integer>> combinations,
			int nbPizzaToRemove, ArrayList<Integer> indexToRemove, int depth) {
//		System.out.println("nbPizzaToRemove: " + nbPizzaToRemove);
		if (depth < nbPizzaToRemove - 1) {
			System.out.println("depth: " + depth);
			for(int i = 0; i < allPizzas.size(); i++) {
				if( !indexToRemove.contains(i)) {
					indexToRemove.add(i);
//					System.out.println("new indexToRemove: " + indexToRemove );
					processRecursive(allPizzas, combinations, nbPizzaToRemove, indexToRemove, depth+1);
					indexToRemove.remove(indexToRemove.size()-1);
//					System.out.println("reset indexToRemove: " + indexToRemove );
//					System.out.println();
//					System.out.println();
				}
			}
		} else {
			Task task = new Task(allPizzas, combinations,nbPizzaToRemove,  new ArrayList<Integer>(indexToRemove), depth);
			pool.submit(task);
			tasks.add(task);
		}
	}
	
	private static class Task extends RecursiveTask<Void> {
		
		/**
		 * 
		 */
		
		private static final long serialVersionUID = -7027901463716846154L;
		ArrayList<Long> allPizzas;
		TreeMap<Long, List<Integer>> combinations;
		int nbPizzaToRemove;
		ArrayList<Integer> indexToRemove;
		int depth;
		
		public Task(ArrayList<Long> allPizzas, TreeMap<Long, List<Integer>> combinations, int nbPizzaToRemove,
				ArrayList<Integer> indexToRemove, int depth) {
			super();
			this.allPizzas = allPizzas;
			this.combinations = combinations;
			this.nbPizzaToRemove = nbPizzaToRemove;
			this.indexToRemove = indexToRemove;
			this.depth = depth;
		}

		@Override
		protected Void compute() {
//			System.out.println("nbPizzaToRemove: " + nbPizzaToRemove);
				System.out.println("depth: " + depth);
				for(int i = 0; i < allPizzas.size(); i++) {
					if( !indexToRemove.contains(i)) {
						indexToRemove.add(i);
//						System.out.println("new indexToRemove: " + indexToRemove );
						long sum = 0;
						for(int j = 0; j < allPizzas.size(); j++) {
							if( !indexToRemove.contains(j)) {
//								System.out.print(allPizzas.get(i)s
								sum += allPizzas.get(j);
							}
						}
						if (sum <= maxSlices) {
							System.out.println("sum: " + sum );
							System.out.println("indexToRemove: " + indexToRemove );
							combinations.put(sum, IntStream.range(0, allPizzas.size()).filter(index -> !indexToRemove.contains(index)).mapToObj(Integer::valueOf).collect(Collectors.toList()));
						}
						indexToRemove.remove(indexToRemove.size()-1);
//						System.out.println("reset indexToRemove: " + indexToRemove );
//						System.out.println();
//						System.out.println();
					}
				}
			return null;
		}
		
	}

	private static ArrayList<Integer> process_prev(List<Long> allPizzas, TreeMap<Long, ArrayList<Integer>> combinations) {
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
			allPizzaSlices = longAdder.sum();
			System.out.println(allPizzaSlices);
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
