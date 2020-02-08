package fr.sioc1981.hashcode.y2020.pizzapuzzle;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Launcher {

	private static int maxSlices;
	private static ArrayList<Long> pizzas;
	private static long allScore;
	private static Long allSlices;
	private static ForkJoinPool pool;
	
	private static boolean maxSlicesFound = false;

	public static void main(String[] args) throws Exception {
//		String fileName = "";
//		fileName = "a_example";
//		fileName = "b_small";
//		fileName = "c_medium";
//		fileName = "d_quite_big";
//		fileName = "e_also_big";

		Stream.of(
				"a_example"
				, 
				"b_small"
				, 
				"c_medium"
				, 
				"d_quite_big"
				,
				"e_also_big"
				).forEach(fileName -> {
			try {
				maxSlicesFound = false;
				loadInput(new File("in", fileName + ".in"));
				ArrayList<Integer> pizzasToOrder = process(pizzas);
				writeOutput(pizzasToOrder, fileName);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		
		System.out.println("All Scores: " + allScore);
	}

	private static ArrayList<Integer> process(ArrayList<Long> allPizzas) {
		int nbPizzas = allPizzas.size();
		ArrayList<Integer> pizzasToOrder = null;
		
	    pool = new ForkJoinPool();
		NodeTask app = new NodeTask(pizzas.size() - 1, 0, new ArrayList<Integer>());
		long best = pool.invoke(app);
		System.out.println("score: " + best + " / " + maxSlices + " / " + allSlices);
		allScore += best;
		pizzasToOrder = app.currentPizzasToOrder;
		// revert as we start from the end
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
			allSlices = pizzas.parallelStream().reduce((a,b) -> a+b).get();
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

	
	public static class NodeTask extends RecursiveTask<Long> {
		/**
		 * 
		 */
		private static final long serialVersionUID = 104262222884216920L;
		
		final int index;
		
		
		final long score;
		
		ArrayList<Integer> currentPizzasToOrder;
		
		NodeTask(int index, long score, ArrayList<Integer> currentPizzasToOrder) {
			this.index = index;
			this.score = score;
			this.currentPizzasToOrder = currentPizzasToOrder;
		}

		public ArrayList<Integer> getCurrentPizzasToOrder() {
			return currentPizzasToOrder;
		}

		protected Long compute() {
			if (index < 0 || maxSlicesFound) {
				return score;
			}
			 
			long slicePizza = pizzas.get(index);
			
			long currentScore = score;
			long includeScore = score + slicePizza;
			
			if ( includeScore == maxSlices) {
				maxSlicesFound = true;
				currentPizzasToOrder = (ArrayList<Integer>) currentPizzasToOrder.clone();
				currentPizzasToOrder.add(index);
				return (long) maxSlices;
			}
			
			NodeTask nt1 = new NodeTask(index - 1, currentScore, currentPizzasToOrder);
			nt1.fork();
			
			
			NodeTask nt2 = null;
			NodeTask nt = null;
			if (includeScore < maxSlices) {
				currentPizzasToOrder = (ArrayList<Integer>) currentPizzasToOrder.clone();
				currentPizzasToOrder.add(index);
				currentScore = includeScore;
				nt2  = new NodeTask(index - 1, currentScore, currentPizzasToOrder);
				nt2.setRawResult(nt2.compute());
				nt  = nt2.getRawResult() > nt1.join() ? nt2 : nt1;
			} else {
				nt1.join();
				nt = nt1;
			}

			this.currentPizzasToOrder = nt.getCurrentPizzasToOrder();
			return nt.getRawResult();
		}
	}

}
