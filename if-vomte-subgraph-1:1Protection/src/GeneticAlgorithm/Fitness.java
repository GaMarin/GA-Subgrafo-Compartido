package GeneticAlgorithm;

import java.io.IOException;
import java.util.ArrayList;

import IfVomte.IfVomte;

public class Fitness {
	
	//static int[] solution = new int[64];

    /* Public methods 
    // Set a candidate solution as a byte array
    public static void setSolution(int[] newSolution) {
        solution = newSolution;
    }

    // To make it easier we can use this method to set our candidate solution 
    // with string of 0s and 1s
    static void setSolution(String newSolution) {
        solution = new int[newSolution.length()];
        // Loop through each character of our string and save it in our byte 
        // array
        for (int i = 0; i < newSolution.length(); i++) {
            String character = newSolution.substring(i, i + 1);
            if (character.contains("0") || character.contains("1")) {
                solution[i] = Byte.parseByte(character);
            } else {
                solution[i] = 0;
            }
        }
    }
*/
    // Calculate inidividuals fittness by comparing it to our candidate solution
    static ArrayList<Integer> getFitness(Individual individual) throws IOException {
        
        ArrayList<Integer> fitness = new ArrayList<>();
        fitness = IfVomte.ifVomte(individual.getGenes());
        
        return fitness;
    }
    
    // Get optimum fitness
    /*static int getMaxFitness() {
        int maxFitness = solution.length;
        return maxFitness;
    }
*/
}
