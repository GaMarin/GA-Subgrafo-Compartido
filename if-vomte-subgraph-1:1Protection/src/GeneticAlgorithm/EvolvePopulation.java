package GeneticAlgorithm;

import java.io.IOException;

public class EvolvePopulation {
	
	/* GA parameters */
    private static double crossoverRate = 0.5;
    private static double mutationRate = 0.015;
    private static final int tournamentSize = 5;
    private static final boolean elitism = true;

    /* Public methods */
    public EvolvePopulation(double crossoverRate, double mutationRate) {
    	
    	this.crossoverRate = crossoverRate;
    	this.mutationRate = mutationRate;
    	
    }
    
    // Evolución de la población 
    public Population evolvePopulation(Population pop) throws IOException {
        Population newPopulation = new Population(pop.size());

        // Mantener el mejor individuo
        if (elitism) {
            newPopulation.saveIndividual(0, pop.getFittest());
        }

        // Cruce de población
        int elitismOffset;
        if (elitism) {
            elitismOffset = 1;
        } else {
            elitismOffset = 0;
        }
        
        //Se crean nuevos individuos a través del cruzamiento
        for (int i = elitismOffset; i < pop.size(); i++) {                     
        	Individual indiv1 = tournamentSelection(pop);
        	Individual indiv2 = tournamentSelection(pop);
        	Individual newIndiv = crossover(indiv1, indiv2);
        	newPopulation.saveIndividual(i, newIndiv);
        }

        // Mutación de población
        for (int i = elitismOffset; i < newPopulation.size(); i++) {
            mutate(newPopulation.getIndividual(i));
        }

        return newPopulation;
    }

    //Cruza de individuos - Metodo Order-Based Crossover
    private static Individual crossover(Individual indiv1, Individual indiv2) {
        
    	Individual newSol = new Individual(indiv1.size(), mutationRate);
    	Individual aux2 = new Individual(indiv1.size(), mutationRate);
    	Individual aux3 = new Individual(indiv1.size(), mutationRate);
    	
    	
    	//Si es menor a uniformRate se realiza el cruzamiento en base al primer individuo
    	if(Math.random()<=crossoverRate) {
    		
    		aux2 = indiv1;
    		aux3 = indiv2;
    	
    		//Si no, en base al segundo individuo
    	} else {
    		aux2 = indiv2;
    		aux3 = indiv1;
    	}
    	
    	int range = indiv1.size() - 1;
        
        //Se obtiene aleatoriamente entre 0 y N para el inicio del substring
        int subStringStart = (int)(Math.random() * (range));
        //Se obtiene aleatoriamente subStringStart y N para el fin del substring
        int subStringEnd = (int)(Math.random() * (range - subStringStart + 1) + subStringStart); 
        
        //Se guarda el substring
        int[] subString = new int[subStringEnd - subStringStart + 1];
        
        int aux = 0;
        //Se copia la subcadena del primer padre en el hijo en las mismas posiciones
        for (int i = subStringStart; i <subStringEnd + 1 ; i++) {
        	
			subString[aux] = aux2.getGene(i);
			
			aux++;
		} 
        
        //Se copia la subcadena del primer padre en el hijo en las mismas posiciones
        for (int i = subStringStart; i <subStringEnd + 1 ; i++) {
        	
			newSol.setGene(i, aux2.getGene(i));
		}
        
        int[] P2 = new int[aux3.size()];
        
        for (int i = 0; i < aux3.size(); i++) {
        	
        	P2[i] = aux3.getGene(i);
			
		}
        
        //Eliminar de P2 los valores que ya se encuentren en el substring
        for (int i = 0; i < subString.length; i++) {
        	
        	
        	for (int j = 0; j < aux3.size(); j++) {
				
        		if(subString[i] == P2[j]) {
        			P2[j]=0;
        		}
			}
			
		}
        
        //Se copia el resto de los valores de P2 al hijo
        for (int i = 0; i < newSol.size(); i++) {
        	
        	if(newSol.getGene(i) == 0) {
        		
        		for (int j = 0; j < P2.length; j++) {
				
        			if(P2[j] != 0) {
        			
        				newSol.setGene(i, P2[j]);
        				P2[j]=0;
        				j= P2.length;
        			}
        		}
        	}
        	
			
		}

        return newSol;
    }

    // Mutación de individuo por intercambio recíproco
    public static void mutate(Individual indiv) {
        //Recorrer los genes
        for (int i = 0; i < indiv.size(); i++) {
            if (Math.random() <= mutationRate) {
            	
                //Elegir posición aleatoria para el intercambio
                int genePosition = (int) (Math.random() * indiv.size());
                
                //Se obtienen los valores de las dos posiciones para el intercambio
                int gene1 = indiv.getGene(genePosition);
                int gene2 = indiv.getGene(i);
                
                //Se intercambian los valores de las posiciones i y gene             
                indiv.setGene(i, gene1);
                indiv.setGene(genePosition, gene2);
            }
        }
    }

    // Selección de individuo para la cruza
    private static Individual tournamentSelection(Population pop) throws IOException {
        // Crear población para torneo
        Population tournament = new Population(tournamentSize);
        
        // Para cada lugar en el torneo obtener un individuo aleatorio        
        for (int i = 0; i < tournamentSize; i++) {
            int randomId = (int) (Math.random() * pop.size());
            tournament.saveIndividual(i, pop.getIndividual(randomId));
        }
        // Se obtiene el mejor de la población
        Individual fittest = tournament.getFittest();
        
        return fittest;
    }

}
