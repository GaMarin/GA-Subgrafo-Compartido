package FilesMain;

import java.io.IOException;
import java.util.ArrayList;

import IfVomte.IfVomte;

public class Main2 {
	
	public static void main(String[] args) throws IOException {
		
		final int N=2;

		long starTime, endTime, totalTime; //Variables para determinar el tiempo de ejecución
		starTime = System.currentTimeMillis(); //Tomamos la hora en que inicio el algoritmo y la almacenamos en la variable inicio

		int [] best = new int [N];
		int [] iden = new int [N];
		ArrayList<Integer> ifVomteResult = new ArrayList<Integer>();
				
		//Permutación identidad
		System.out.print("Permutación identidad: ");
		for (int i = 0; i<best.length; i++) {
			
			best[i] = i+1; 
			iden[i] = i+1;
			System.out.print(best[i]);

		}
		System.out.println();
		
		ifVomteResult = IfVomte.ifVomte(best);
        
        endTime = System.currentTimeMillis(); //Tomamos la hora en que finalizó el algoritmo y la almacenamos en la variable T
        totalTime = endTime - starTime; //Calculamos los milisegundos de diferencia
		System.out.println("Tiempo de ejecución en milisegundos: " + totalTime); //Mostramos en pantalla 

    }


}
