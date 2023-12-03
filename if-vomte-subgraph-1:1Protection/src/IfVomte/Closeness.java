package IfVomte;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import ShortestPath.Dijkstra;

public class Closeness {

	public Closeness() {
		// TODO Auto-generated constructor stub
	}
	
	public int getCloseness(Map<Integer, ArrayList<Integer>> candidates, int virtualNode, int [][] VOR, Map<Integer, Integer> cpuNodeSON,
							ArrayList<Integer> v_nodeMapped, ArrayList<Integer> s_nodeMapped, int [][] SON) {
		
		float CC = 0;
		float temp = CC;
		ArrayList<Integer> set0 = new ArrayList<>();
		ArrayList<Integer> set1 = new ArrayList<>();
		ArrayList<Integer> set2 = new ArrayList<>();
		
		//Contendrá el nodo físico seleccionado para el mapeo
		int nodeSelected = 0;

		
		//Se obtiene la lista de candidatos físicos para el nodo virtual actual
		ArrayList<Integer> candidateForCurrentNode = getCandidatesForCurrentNode(virtualNode, candidates);
		
		if(candidateForCurrentNode == null) {
			return 0;
		}
		
		for (int i = 0; i < candidateForCurrentNode.size(); i++) {
			
			//Se obtiene los vecinos virtuales del nodo actual
			ArrayList<Integer> neighbors = getNeighbors(virtualNode, VOR);
			
			//Se obtienen los candidatos físicos de los vecinos virtuales
			for (int j = 0; j < neighbors.size(); j++) {
				
				//Si el vecino virtual no esta mapeado
				if (!v_nodeMapped.contains(neighbors.get(j))) {
					
					ArrayList<Integer> candidatesForNeighborsNode = getCandidatesForCurrentNode(neighbors.get(j), candidates);

					//Se agregan los candidatos
					for (int k = 0; k < candidatesForNeighborsNode.size(); k++) {
					
						set1.add(candidatesForNeighborsNode.get(k));

					}
				
					//Se eliminan los repetidos del conjunto set1
					Set<Integer> linkedHashSet = new LinkedHashSet<Integer>();
					linkedHashSet.addAll(set1);
					set1.clear();
					set1.addAll(linkedHashSet);
				}else {
				
					//Se obtiene la posición del vecino virtual mapeado
					int neighborNodePosition = v_nodeMapped.indexOf(neighbors.get(j));
				
					//Se agrega el nodo fisico al conjunto					
					set2.add(s_nodeMapped.get(neighborNodePosition));
				}
			}
			
			//Se realiza la unión de las dos listas
			set2.removeAll(set1);
			set1.addAll(set2);
			
			set0 = set1;
			
			float f = 0;
			int shortestPathLengthSum = 0;
			int shortestPathLength = 0;
			
			//Se obtiene la suma de los caminos más cortos desde el nodo fisico actual hasta los demas nodos físicos que se encuentran en el conjuntos set0
			for (int j = 0; j < set0.size(); j++) {
								
				shortestPathLength = Dijkstra.dijkstra(SON,candidateForCurrentNode.get(i),set0.get(j));

				shortestPathLengthSum += shortestPathLength;

			}
			
			int sizeOfSet0;
			
			if(set0.contains(candidateForCurrentNode.get(i))) {
				sizeOfSet0 = set0.size() - 1;
			}else {
				sizeOfSet0 = set0.size();
			}
			
			// Se calcula el ratio CC
			
			if(sizeOfSet0== 0) {
				CC = 0;
			}else {
				
				float aux = (float)shortestPathLengthSum / (float)sizeOfSet0;
				CC = (float) Math.pow(aux, -1);
			}
			f = CC;
			
			//Se obtienen las cantidades cpu del nodo fisico actual y del nodo fisico seleccionado para su posterior comparación
			Integer cpuCurrentNode = cpuNodeSON.get(candidateForCurrentNode.get(i));
			Integer cpuNodeSelected = cpuNodeSON.get(nodeSelected);
			
			if (cpuNodeSelected == null) {
				cpuNodeSelected = 0;
			}
			
			//Se comprueba si es que existe un mejor candidato fisico
			if(f>temp) {
				temp=f;
				nodeSelected = candidateForCurrentNode.get(i);
			}
			else if(f==temp && cpuCurrentNode> cpuNodeSelected) {
				
				nodeSelected = candidateForCurrentNode.get(i);

			}
			
		}
		
		return nodeSelected;
		
	}
	
	/**
	 * Retorna los candidatos físicos para el nodo virtual actual
	 * 
	 * @param virtualNode
	 * @param candidates
	 * @return
	 */
	private ArrayList<Integer> getCandidatesForCurrentNode(int virtualNode, Map<Integer, ArrayList<Integer>> candidates){
		
		//Contendrá la lista de candidatos para el nodo actual
		ArrayList<Integer> candidatesForCurrentNode = new ArrayList<>();
		
		//Se obtiene la lista de candidatos
		candidatesForCurrentNode = candidates.get(virtualNode);
		
		return candidatesForCurrentNode;

	}
	
	public ArrayList<Integer> getNeighbors (int node, int [][] network) {
		
		ArrayList<Integer> neighbors = new ArrayList<Integer>();
		
		for (int i = 0; i < network.length; i++) {
				
			if (network[node-1][i]==1 || network[i][node-1]==1) {
				
				neighbors.add(i+1);
			}
		}
		
		return neighbors;
		
	}
}
