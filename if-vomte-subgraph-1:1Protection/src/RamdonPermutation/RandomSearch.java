package RamdonPermutation;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import IfVomte.IfVomte;

public class RandomSearch {
	
	private static int N;
	private static int ITER;
	
	public static void main(String[] args) throws IOException {
		
		N = 4;
		
		int i,fact=1;  
		int number=N;  
		
		for(i=1;i<=number;i++){    
		      fact=fact*i;    
		}    
		
		ITER = 100;
		
		Map<int[], Integer> result = randomSearch();
		System.out.println();
		
		for (Map.Entry<int [], Integer> out : result.entrySet()) {
			
			int [] array = out.getKey();
			
			System.out.print("La mejor permutación es: ");
			for (int j = 0; j < array.length; j++) {
				
				System.out.print(array[j]);
				
			}
			System.out.println();
			System.out.print("El mejor costo es: " + out.getValue());			
		}
	} 
	
	public RandomSearch(int n, int iter) {
		this.N = n;
		this.ITER = iter;
	}
	
	public static Map<int[], Integer> randomSearch() throws IOException{
		
		int [] best = new int [N];
		int [] iden = new int [N];
		Map<int[], Integer> bestPermutation = new LinkedHashMap<int[], Integer>();
		ArrayList<Integer> ifVomteResult = new ArrayList<Integer>();
				
		//Permutación identidad
		System.out.print("Permutación identidad: ");
		for (int i = 0; i<best.length; i++) {
			
			best[i] = i+1; 
			iden[i] = i+1;
			System.out.print(best[i]);

		}
		System.out.println();

		int costBest = 0;
		
		ifVomteResult = IfVomte.ifVomte(best);
		costBest = ifVomteResult.get(0);
		System.out.println("El costo es: " + costBest);

		
		for (int i = 0; i < ITER; i++) {
			
			int [] candidate = KnuthShuffle.initialize_and_permute(iden);
			
			System.out.print("Permutación " + i + ":");
			for (int j = 0; j < candidate.length; j++) {
				System.out.print(candidate[j]);
			}
			
			System.out.println();
			
			ifVomteResult = IfVomte.ifVomte(candidate);
			int cost = ifVomteResult.get(0);
			
			System.out.println("El costo es: " + cost);
			
			if(cost<costBest) {
				costBest = cost;
				best = candidate;
			}
					
		}
		
		bestPermutation.put(best, costBest);
		
		return bestPermutation;

	}
	
	
	
	

}
