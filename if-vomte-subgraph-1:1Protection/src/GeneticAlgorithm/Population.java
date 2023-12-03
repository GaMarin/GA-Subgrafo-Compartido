package GeneticAlgorithm;

import java.io.IOException;
import java.util.ArrayList;

public class Population {
	
	Individual[] individuals;

    /*
     * Constructores
     */
	
	//Crear población vacía
	public Population(int populationSize) {
		
        individuals = new Individual[populationSize];
	}
	
    // Crear población
    public Population(int populationSize, int individualLength, double mutationRate) {
        individuals = new Individual[populationSize];
        	
    	//Crear primer individuo base
    	Individual individualBase = new Individual(individualLength, mutationRate);
    	individualBase.generateFirtsIndividual();
    	
    	saveIndividual(0, individualBase);
    	
        // Crear los siguientes N-1 individuos
        for (int i = 1; i < size(); i++) {
            Individual newIndividual = new Individual(individualLength, mutationRate);
            newIndividual.generateIndividual();
            saveIndividual(i, newIndividual);
        }
    }

    /* Getters */
    public Individual getIndividual(int index) {
        return individuals[index];
    }

    //Retornar el mejor individuo
    public Individual getFittest() throws IOException {
        Individual fittest = individuals[0];
        // Loop through individuals to find fittest
        for (int i = 0; i < size(); i++) {
        	
        	ArrayList<Integer> aux1 = fittest.getFitness();
        	ArrayList<Integer> aux2 = getIndividual(i).getFitness();
        	
        	//Se comparan los bloqueos
            if (aux1.get(1) > aux2.get(1)) {
                fittest = getIndividual(i);
            }
            
            //Se comparan los bloqueos de protección
            else if(aux1.get(1) == aux2.get(1) && aux1.get(2) > aux2.get(2)) {
            	fittest = getIndividual(i);
            }
            //Se comparan los costos en caso de que el número de bloqueos sean iguales
            else if (aux1.get(2) == aux2.get(2) && aux1.get(1) == aux2.get(1) && aux1.get(0) > aux2.get(0)) {
            	fittest = getIndividual(i);
            }
        }
        return fittest;
    }

    /* Public methods */
    // Get population size
    public int size() {
        return individuals.length;
    }

    // Save individual
    public void saveIndividual(int index, Individual indiv) {
        individuals[index] = indiv;
    }
}
