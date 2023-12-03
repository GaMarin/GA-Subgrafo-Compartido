package RamdonPermutation;
import java.util.Random;

public class KnuthShuffle {

	
	public static void main(String[] args) {
		
		int[] aux1 = new int[10];
		
		for (int i = 0; i < aux1.length; i++) {
			
			aux1[i] = i+1;
			
		}
		
		long fact=1;
		
		for(int i=1;i<=aux1.length;i++){    
		      fact=fact*i;    
		}  
		
		
		for (int i = 0; i < 100; i++) {
			
			int [] aux2 = initialize_and_permute(aux1);
			
			for (int j = 0; j < aux2.length; j++) {
				System.out.print(aux2[j]);
			}
			System.out.println();
			
		}
		
	}
	
	public static int [] initialize_and_permute(int[] arrayIden){
		
		Random r = new Random();
		
		//r.setSeed(20);
		
		int [] array = new int [arrayIden.length];
		
		for (int i = 0; i < array.length; i++) {
			array[i] = arrayIden[i];
		}
		
	    for (int i = array.length - 1; i > 0; i--) {
	      int index = r.nextInt(i);
	      // swap 
	      int tmp = array[index];
	      array[index] = array[i];
	      array[i] = tmp;

	    }
	    
	    return array;
	}

}
