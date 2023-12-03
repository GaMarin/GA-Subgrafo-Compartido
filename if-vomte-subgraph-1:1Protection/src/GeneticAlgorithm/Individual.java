package GeneticAlgorithm;

import java.io.IOException;
import java.util.ArrayList;

public class Individual {
	
	private static int defaultGeneLength = 6;
    private int[] genes = new int[defaultGeneLength];
    private double mutationRate = 0.25;
    

    // Cache
    private ArrayList<Integer> fitness = new ArrayList<Integer>();

    
    public Individual(int defaultGeneLength, double mutationRate) {
		this.defaultGeneLength = defaultGeneLength;
		this.mutationRate = mutationRate;
		this.genes = new int[defaultGeneLength];
	}
    

    //Crear primer individuo base
    public void generateFirtsIndividual() {
        for(int i=0; i<defaultGeneLength; i++) {
        	genes[i]=i+1;
        }
    }
    
   //Crear individuos a partir del individuo base
    public void generateIndividual() {
    	
    	generateFirtsIndividual();
    	//Recorrer los genes
        for (int i = 0; i < genes.length; i++) {
        	
        	double random = Math.random();
            if ( random<= mutationRate) {
            	
                //Elegir posición aleatoria para el intercambio
                int genePosition = (int) (random * genes.length);
                
                //Se obtienen los valores de las dos posiciones para el intercambio
                int gene1 = genes[genePosition];
                int gene2 = genes[i];
                
                //Se intercambian los valores de las posiciones i y gene             
                genes[i] = gene1;
                genes[genePosition] = gene2;
            }
        }
    }

    /* Getters and setters */
    // Use this if you want to create individuals with different gene lengths
    public static void setDefaultGeneLength(int length) {
        defaultGeneLength = length;
    }
    
    public int getGene(int index) {
        return genes[index];
    }

    public void setGene(int index, int value) {
        genes[index] = value;
    }
    
    public int[] getGenes() {
    	return genes;
    }
    
    public void setGenes(int[] genes) {
    	this.genes = genes;
  
    }

    /* Public methods */
    public int size() {
        return genes.length;
    }

    public ArrayList<Integer> getFitness() throws IOException {
        if (fitness.size()==0) {
            fitness = Fitness.getFitness(this);
        }
        return fitness;
    }

    @Override
    public String toString() {
        String geneString = "";
        for (int i = 0; i < size(); i++) {
            geneString += getGene(i);
        }
        return geneString;
    }

}
