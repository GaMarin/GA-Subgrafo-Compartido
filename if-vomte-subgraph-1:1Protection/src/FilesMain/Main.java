package FilesMain;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

import GeneticAlgorithm.EvolvePopulation;
import GeneticAlgorithm.Individual;
import GeneticAlgorithm.Population;
import IfVomte.IfVomte;

public class Main {
	
	public static void main(String[] args) throws IOException {

		for(int size= 91; size<= 116; size++) {
			
	        System.out.println("Iteracion " + size);

			long starTime, endTime, totalTime; //Variables para determinar el tiempo de ejecución
			starTime = System.currentTimeMillis(); //Tomamos la hora en que inicio el algoritmo y la almacenamos en la variable inicio
	
			int stopCriterionCount = 0;
			
			//Archivo que contiene la url de los parametros para el AG
			String FILES = "/home/gmarin/if-vomte-protection/AG-readFiles.txt";
			String urlInputs = null;
			ArrayList<String> inputs = new ArrayList<String>();
	
					
			try {
				
				//Se abre el archivo con la ruta especificada.
	            FileInputStream fstream = new FileInputStream(new File(FILES));
	            
	            //Se crea el objeto de entrada
	            DataInputStream entrada = new DataInputStream(fstream);
	            
	            //Se crea el Buffer de Lectura
	            BufferedReader buffer = new BufferedReader(new InputStreamReader(entrada));
	            String strLinea;
	            
	            //Leer el archivo linea por linea
	            while ((strLinea = buffer.readLine()) != null)   {
	                //Se agrega la línea al vector
	            	urlInputs = strLinea;
	            }
	
	            entrada.close();
							
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			try {
				
				
				//Se abre el archivo con la ruta especificada.
	            FileInputStream fstream = new FileInputStream(new File(urlInputs));
	            
	            //Se crea el objeto de entrada
	            DataInputStream entrada = new DataInputStream(fstream);
	            
	            //Se crea el Buffer de Lectura
	            BufferedReader buffer = new BufferedReader(new InputStreamReader(entrada));
	            String strLinea;
	            
	            //Leer el archivo linea por linea
	            while ((strLinea = buffer.readLine()) != null)   {
	                //Se agrega la línea al vector
	            	inputs.add(strLinea);
	            }
	
	            entrada.close();
							
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			//Se obtienen los valores del archivo
			String aux1 = "";
			String [] aux2 = null;
			String aux3 = "";
	
			aux1 = inputs.get(0);
			aux2 = aux1.split(":");
			aux3 = aux2[1];
			
			//Contendrá el tamaño de la población
			int populationSize = 0;
			populationSize = Integer.parseInt(aux3);
			
			aux1 = "";
			aux2 = null;
			aux3 = "";
			
			aux1 = inputs.get(1);
			aux2 = aux1.split(":");
			aux3 = aux2[1];
			
			//Contendrá la probabilidad de cruza
			double crossoverRate = 0.0;
			crossoverRate = Double.parseDouble(aux3);
			
			aux1 = "";
			aux2 = null;
			aux3 = "";
			
			aux1 = inputs.get(2);
			aux2 = aux1.split(":");
			aux3 = aux2[1];
			
			//Contendrá la probabilidad de mutación
			double mutationRate = 0.0;
			mutationRate = Double.parseDouble(aux3);
			
			aux1 = "";
			aux2 = null;
			aux3 = "";
			
			aux1 = inputs.get(3);
			aux2 = aux1.split(":");
			aux3 = aux2[1];
			
			//Contendrá la cantidad de generaciones que deben permanecer sin cambios
			int stopCriterion = 0;
			stopCriterion = Integer.parseInt(aux3);
			
			aux1 = "";
			aux2 = null;
			aux3 = "";
			
			aux1 = inputs.get(4);
			aux2 = aux1.split(":");
			aux3 = aux2[1];
			
			//Contendrá la longitud del individuo
			int individualLength = 0;
			individualLength = Integer.parseInt(aux3);
	
			//Se guarda en un archivo el mejor individuo de cada población
			String newUrl = "/home/gmarin/if-vomte-protection/AG-Output5.txt";
	        File output = new File(newUrl);
	        FileWriter fw = new FileWriter(output);
	        BufferedWriter bw = new BufferedWriter(fw);
	        
	        //Se guarda en un archivo todos los fitness de cada población
			String newUrl2 = "/home/gmarin/if-vomte-protection/AG-Output-Fitness5.txt";
	        File output2 = new File(newUrl2);
	        FileWriter fw2 = new FileWriter(output2);
	        BufferedWriter bw2 = new BufferedWriter(fw2);
	        
	        //Se guarda en un archivo todos los individuos de cada población
	        String newUrl3 = "/home/gmarin/if-vomte-protection/AG-Output-Individuals5.txt";
	        File output3 = new File(newUrl3);
	        FileWriter fw3 = new FileWriter(output3);
	        BufferedWriter bw3 = new BufferedWriter(fw3);
	
	        // Create an initial population
	        Population myPop = new Population(populationSize, individualLength, mutationRate);
	                
	        // Evolve our population until we reach an optimum solution
	        int generationCount = 0;
	        ArrayList<Integer> bestFitness = new ArrayList<>();
	        ArrayList<Integer> newbestFitness = new ArrayList<>();
	        
	    	bestFitness = myPop.getFittest().getFitness();
	    	Individual bestIndividual = myPop.getFittest();
	                
	        while (stopCriterionCount < stopCriterion) {  
	            
	        	//Guardar en el archivo el mejor individuo de cada población
	        	String outputAux = "Generation: " + generationCount + " Fittest: " + bestFitness.get(0) + " , " 
	        	+ bestFitness.get(1) + " , " + bestFitness.get(2) + ". Genes: " + bestIndividual;
	            System.out.println(outputAux);
	            bw.write(outputAux + "\n");
	            
	            
	            //Guardar en el archivo los fitness de todos los individuos
	            String outputAux2="";
	            outputAux2 += "Generación " + generationCount + "; Fitnes: ";
	            
	            for(int i=0; i<myPop.size(); i++) {
	            	
	            	outputAux2 +=  myPop.getIndividual(i).getFitness().get(0) + "," + myPop.getIndividual(i).getFitness().get(1) + "," + myPop.getIndividual(i).getFitness().get(2) + ";";
	            }
	            
	            outputAux2 += "\n";
	            
	            bw2.write(outputAux2);
	            
	            //Guardar en el archivo los individuos de la población
	            String outputAux3="";
	            outputAux3 += "Generación " + generationCount + "; Individuo: ";
	            
	            for(int i=0; i<myPop.size(); i++) {
	            	
	            	outputAux3 +=  myPop.getIndividual(i) + ";";
	            }
	            
	            outputAux3 += "\n";
	            
	            bw3.write(outputAux3);
	            
	        	generationCount++;
	            
	        	EvolvePopulation evolve = new EvolvePopulation(crossoverRate, mutationRate);
	            myPop = evolve.evolvePopulation(myPop);
	            
	            newbestFitness = myPop.getFittest().getFitness();
	            
	            int aux4 = 0, aux5=0, aux6=0, aux7=0, aux8=0, aux9=0;
	            aux4 = bestFitness.get(0);
	            aux5 = bestFitness.get(1);
	            aux6 = bestFitness.get(2);
	            aux7 = newbestFitness.get(0);
	            aux8 = newbestFitness.get(1);
	            aux9 = newbestFitness.get(2);
	            
	            if(aux4!=aux7 || aux5!=aux8 || aux6!=aux9) {
	            	stopCriterionCount=0;
	            } else {
	            	stopCriterionCount++;
	            }
	            
	            //Se comparan los bloqueos
	            if (aux8 < aux5) {
	            	bestFitness = newbestFitness;
	            	bestIndividual = myPop.getFittest();
	            }
	            
	            //Se comparan los bloqueos de protección
	            else if (aux8 == aux5 && aux9 < aux6) {
	            	bestFitness = newbestFitness;
	            	bestIndividual = myPop.getFittest();
	
	            }
	            
	            //Se comparan los costos en caso de que el número de bloqueos sean iguales
	            else if (aux8 == aux5 && aux9 == aux6 && aux7 < aux4) {
	            	bestFitness = newbestFitness;
	            	bestIndividual = myPop.getFittest();
	
	            }
	                       
	        }
	        System.out.println("Solution found!");
	        System.out.println("Generation: " + generationCount);
	        System.out.println("El mejor costo es: " + bestFitness.get(0));
	        System.out.println("Genes:");
	        System.out.println(bestIndividual);
	        
	        ArrayList<Integer> best = new ArrayList<>();
	        best = IfVomte.ifVomte(bestIndividual.getGenes());
	        
	        bw.close();
	        bw2.close();
	        bw3.close();
	
	        
	        endTime = System.currentTimeMillis(); //Tomamos la hora en que finalizó el algoritmo y la almacenamos en la variable T
	        totalTime = endTime - starTime; //Calculamos los milisegundos de diferencia
			System.out.println("Tiempo de ejecución en milisegundos: " + totalTime); //Mostramos en pantalla 

	        String contenido = "Tiempo de ejecución en milisegundos: " + totalTime;

			String newUrlForTime = "/home/gmarin/if-vomte-protection/time5.txt";
	        File outputTime = new File(newUrlForTime);
	        
	        try {
	            
	            // Si el archivo no existe es creado
	            if (!outputTime.exists()) {
	            	outputTime.createNewFile();
	            }
	            
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	        
	        FileWriter fw4 = new FileWriter(outputTime);
	        BufferedWriter bw4 = new BufferedWriter(fw4);
	
	        bw4.write(contenido);
	
	        bw4.close();
	        
	        
	        String ruta = "/home/gmarin/if-vomte-protection/" + size + "/";
	        
	        
	        Path FROM1 = Paths.get(newUrl);
	        Path TO1 = Paths.get(ruta + "AG-Output5.txt");
	        Path FROM2 = Paths.get(newUrl2);
	        Path TO2 = Paths.get(ruta + "AG-Output-Fitness5.txt");
	        Path FROM3 = Paths.get(newUrl3);
	        Path TO3 = Paths.get(ruta + "AG-Output-Individuals5.txt");
	        Path FROM4 = Paths.get("/home/gmarin/if-vomte-protection/output5.txt");
	        Path TO4 = Paths.get(ruta + "output5.txt");
	        Path FROM5 = Paths.get(newUrlForTime);
	        Path TO5 = Paths.get(ruta + "time5.txt");
	        
	        //sobreescribir el fichero de destino, si existe, y copiar
	        // los atributos, incluyendo los permisos rwx
	        CopyOption[] options = new CopyOption[]{
	          StandardCopyOption.REPLACE_EXISTING,
	        }; 
	        
	        Files.move(FROM1, TO1, options);
	        Files.move(FROM2, TO2, options);
	        Files.move(FROM3, TO3, options);
	        Files.move(FROM4, TO4, options);
	        Files.move(FROM5, TO5, options);
		}
    }


}
