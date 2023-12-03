package IfVomte;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Clase que obtiene la lista de nodos virtuales y sus respectivos candidatos fisicos
 * 
 * @author gmarin
 *
 */
public class Candidate {
	
	public Candidate() {
	
	}
	
	public Map<Integer, ArrayList<Integer>> getCandidates(	int [][] VOR, Map<Integer, Integer> cpuNodeSONList, Map<Integer, Integer> cpuNodeVORList, 
															Map<String, int[]> linkAssignmentToSON, int bandwith, int [][] SON){
		
		int CF = 0;
		int Cs = 0;
		int Bs = 0;
		int cn = 0;
		int cv= 0;
		int flag = 0;
			
		//Calculo preliminar de bloque disponible
		float block = 0;
		
		//Cantidad de vecinos de un nodo virtual
		int v_neighborSize = 0;
		
		//Lista de nodos virtuales con sus respectivos candidatos fisicos
		Map<Integer, ArrayList<Integer>> candidates = new LinkedHashMap<Integer, ArrayList<Integer>>();
		
		//Cantidad de vecinos de cada nodo virtual
		int [] v_neighborSizeList = new int [cpuNodeVORList.size()];
		
		//se recorre la matriz virtual para contar la cantidad de vecinos
		for (int i = 0; i < VOR.length; i++) {
			for (int j = 0; j < VOR.length; j++) {
				if(VOR[i][j]==1) {
					v_neighborSizeList[i] = v_neighborSizeList[i] + 1;
					v_neighborSizeList[j] = v_neighborSizeList[j] + 1;

				}
			}
			
		}

		//Recorrer la lista de nodos virtuales
		for (Map.Entry<Integer, Integer> cpuNodeVOR : cpuNodeVORList.entrySet()) {
			
			//Se obtiene el valor cpu del nodo virtual
			cv = cpuNodeVOR.getValue();
			
			if(cv!=0) {
				//Lista de candidatos fisicos
				ArrayList<Integer> SONcandidates = new ArrayList<>();
				
				//Recorrer la lista de nodos fisicos
				for (Map.Entry<Integer, Integer> cpuNodeSON : cpuNodeSONList.entrySet()) {
					
					Closeness closeness = new Closeness();
					
					//Se obtiene el valor cpu del nodo fisico
					cn = cpuNodeSON.getValue();
					
					if(cn!=0) {
						
						//Calculo del valor normalizado del cpu
						Cs = Math.floorDiv(cn, cv);
						
						//Se obtiene el nodo fisico actual
						int currentSustrateNode = cpuNodeSON.getKey();
						
						ArrayList<Integer> neighborsForSustrateNode = closeness.getNeighbors(currentSustrateNode, SON);
							
						//Calculo preliminar de las subportadoras libres / la cantidad de subportadora solicitada
						for (int i = 0; i < neighborsForSustrateNode.size(); i++) {
							
							String link = "";
							if(currentSustrateNode<neighborsForSustrateNode.get(i)) {
								
								link = Integer.toString(currentSustrateNode) + '-' + Integer.toString(neighborsForSustrateNode.get(i));
							}else {
								
								link = Integer.toString(neighborsForSustrateNode.get(i)) + '-' + Integer.toString(currentSustrateNode);
							}
							
							int[] aux = linkAssignmentToSON.get(link);
							
							int bandwithFreeSize = 0;
							
							for (int k = 0; k < aux.length; k++) {
								
								bandwithFreeSize = 0;
								
								if(aux[k] == 0) {
									bandwithFreeSize = 1;
									
								} 	
								
								block = ((float)bandwithFreeSize / (float)bandwith) + block;
	
							}
								
						
						}
						
						//Se obtiene la cantidad de vecinos del nodo virtual actual
						v_neighborSize = v_neighborSizeList[cpuNodeVOR.getKey()-1];
						
						//Calculo de bloques de subportadoras disponibles
						if(v_neighborSize!=0) {
							Bs = (int) (block/ v_neighborSize);
						}else {
							Bs = 0;
						}
						CF = Cs * Bs;
						
						if (CF>=1) {
							
							//Se agrega el candidato
							SONcandidates.add(cpuNodeSON.getKey());
							
						}
						CF = 0;
						cn = 0;
						Cs = 0;
						block = 0;
						Bs=0;
					}
				}
				
				//Se agrega el nodo virtual y sus candidatos
				candidates.put(cpuNodeVOR.getKey(), SONcandidates);			
			}
		}
		
		return candidates;
		
	}

}
