package IfVomte;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class ImpacFactor {
	
	public ImpacFactor() {
		// TODO Auto-generated constructor stub
	}
	
	public int getVirtualNode(	Map<Integer, Integer> cpuNodeVORAux,	Map<Integer, Integer> cpuNodeVORList, int linkSizeVOR, 
								int bandwithRequest, Map<Integer, ArrayList<Integer>> candidateList, int [][] VOR) {
		
		//Contendrá el valor de factor de impacto de cada nodo virtual
		Map<Integer, Double> IF = new LinkedHashMap<>();
		
		//Contendrá la demanda total de cpu del VOR
		float totalCpuNodeRequest = 0;
		
		float bandwith = (float)bandwithRequest;
		
		//Demanda total de subportadoras del VOR
		float totalBandwithLinkRequest = (float)linkSizeVOR*bandwithRequest;
		
		//Se calcula la demanda total de cpu del VOR
		for (Map.Entry<Integer, Integer> cpuNodeVOR : cpuNodeVORList.entrySet()) {
			
			totalCpuNodeRequest = totalCpuNodeRequest + (float)cpuNodeVOR.getValue();
		}
		
		for (Map.Entry<Integer, Integer> cpuNodeVOR : cpuNodeVORAux.entrySet()) {
			
			if(cpuNodeVOR.getValue()!=0) {
				
				float Cv = 0;
				float Bv = 0;
				float Z = 0;
				float cpuRequest = 0;
				int currentNode = 0;
				Double impactFactor = 0.0;
				Closeness closeness = new Closeness();
				
				int neighborSizeForCurrentVnode = closeness.getNeighbors(cpuNodeVOR.getKey(), VOR).size();
				
				//Demanda de cpu del nodo actual
				cpuRequest = (float)cpuNodeVOR.getValue();
				
				//Nodo actual
				currentNode = cpuNodeVOR.getKey();
				
				//Relación entre la demanda cpu del nodo actual y la demanda total de cpu del VOR
				Cv = cpuRequest / totalCpuNodeRequest;
				
				//Relación entre la demanda de subportadoras del nodo actual y la demanda total de subportadoras del VOR
				Bv = (bandwith*neighborSizeForCurrentVnode) / totalBandwithLinkRequest;
				
				int maxCandidate = getMaxCandidate(candidateList);
				int candidateNumberForCurrentNode = getCandidateNumberForCurrentNode(currentNode, candidateList);
				
				//Relación entre el número máximo de candidatos y el número de candidatos del nodo actual
				if(candidateNumberForCurrentNode!=0) {
				
					Z = (float)maxCandidate / (float)candidateNumberForCurrentNode;
				}else {
					
					Z = 0;
				}

				impactFactor = (double) (Cv * Bv * Z);
				
				IF.put(currentNode, impactFactor);
			}
			
		}
		
		int virtualNodeSelected = selecNode(IF);
		
		return virtualNodeSelected;

	}
	
	/**
	 * Retorna el número máximo de candidatos
	 * 
	 * @param candidateList
	 * @return
	 */
	private int getMaxCandidate(Map<Integer, ArrayList<Integer>> candidateList) {
		
		int maxCandidate = 0;
		
		for(Map.Entry<Integer, ArrayList<Integer>> candidate : candidateList.entrySet()) {
			
			if (maxCandidate<candidate.getValue().size()) {
				maxCandidate = candidate.getValue().size();
			}
		}
		
		return maxCandidate;
		
	}
	
	/**
	 * Retorna la cantidad de candidatos para el nodo actual
	 * 
	 * @param currentNode
	 * @param candidateList
	 * @return
	 */
	private int getCandidateNumberForCurrentNode(int currentNode, Map<Integer, ArrayList<Integer>> candidateList) {
		
		int candidateNumberForCurrentNode = candidateList.get(currentNode).size();
		
		return candidateNumberForCurrentNode;
		
	}
	
	/**
	 * Retorna el nodo virtual con el impactFactor más alto
	 * 
	 * @param IF
	 * @return
	 */
	private int selecNode(Map<Integer, Double> IF) {
		
		Double maxIF = 0.0;
		int nodeSelected = 0;
		
		for(Map.Entry<Integer, Double> candidate : IF.entrySet()) {
			
			if (maxIF < candidate.getValue()) {
				maxIF = candidate.getValue();
				nodeSelected = candidate.getKey();
			}
		}
		
		return nodeSelected;
		
	}
}
