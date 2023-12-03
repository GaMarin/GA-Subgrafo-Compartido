package Protection;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import DFSearch.DepthFirstSearch;
import ShortestPath.DijkstrasAlgorithm;

public class SubgraphBasedProtection {
	
	public static final Integer PATH_PROTECTION_INCLUDED = -1;
	
	public SubgraphBasedProtection() {
		
	}
	
	public Map<String, ArrayList<Integer>> getSubgraph(Map<String, ArrayList<Integer>> allPaths, int root, int[][] SON, 
			int[][] VOR, int linkSizeSON) {
		
		ArrayList<Integer> disconnectNodes = new ArrayList<>();
		ArrayList<Integer> connectNodes = new ArrayList<>();
		
		//Contendrá el enlace de protección para cada enlace fisico
		Map<String, ArrayList<Integer>> linkProtection = new LinkedHashMap<>();
		
		//Contendrá todos los enlaces fisicos de la solicitud
		ArrayList<String> allLinksFromRequest = new ArrayList<>();
		
		//Copìa de la lista de enlaces
		ArrayList<String> allLinksFromRequestAux = new ArrayList<>();
		
		//Contendrá la lista de nodos fisicos utilizados por la solicitud
		ArrayList<Integer> sustrateNodes = new ArrayList<>();
		
		//Se agregan al mapa todos los enlaces fisico utilizados por la solicitud 
		for(Map.Entry<String, ArrayList<Integer>> paths: allPaths.entrySet()) {
			
			ArrayList<Integer> path = paths.getValue();
			
			for (int i = 0; i < path.size()-1; i++) {
				
				int node1 = path.get(i);
				int node2 = path.get(i+1);
				String link = "";
				
				//Se intercambian los nodos de los enlaces para que coincidan con los enlaces guardados
				//Por ejemplo, el enlaces 2-1, se cambia por 1-2
				if(node1>node2) {
					
					//Se obtiene el enlace
					link = Integer.toString(node2) + '-' + Integer.toString(node1);
				}else {
				
					//Se obtiene el enlace
					link = Integer.toString(node1) + '-' + Integer.toString(node2);	
				}

				if(!allLinksFromRequest.contains(link)) {
					allLinksFromRequest.add(link);
				}
			}

		}
		
		//Se inicializa el mapa de protección con null
		for (int i = 0; i < allLinksFromRequest.size(); i++) {
			
			String link = allLinksFromRequest.get(i);
			linkProtection.put(link, null);	
		}
		
		//Se agregan los nodos fisicos a la lista
		for (int i = 0; i < allLinksFromRequest.size(); i++) {
			
			String aux3 = allLinksFromRequest.get(i);
			String [] aux = aux3.split("-");
			String aux1 = aux[0];
			String aux2 = aux[1];
			int nodeInit = Integer.parseInt(aux1);
			int nodeEnd = Integer.parseInt(aux2);
			
			if(!sustrateNodes.contains(nodeInit)) {
				sustrateNodes.add(nodeInit);
			}
			if(!sustrateNodes.contains(nodeEnd)) {
				sustrateNodes.add(nodeEnd);
			}
		}
		
		/**
		 * Inicio de la protección 		
		 */
		//Se recorre cada enlace para obtener su protección correspondiente
		for (int i = 0; i < allLinksFromRequest.size(); i++) {
			
			//Contendrá el subgrafo
			int[][] subgraph = new int[SON.length][SON.length];
			
			//Se copia la lista de enlaces
			for (int k = 0; k < allLinksFromRequest.size(); k++) {
				
				allLinksFromRequestAux.add(allLinksFromRequest.get(k));
			}
			
			String link = allLinksFromRequest.get(i);
			
			//Se elimina el enlace actual del auxiliar de caminos y de la red fisica
			allLinksFromRequestAux.remove(link);
			
			//Se elimina el enlace actual de la red fisica
			String [] aux = allLinksFromRequest.get(i).split("-");
			String aux1 = aux[0];
			String aux2 = aux[1];
			int nodeInit = Integer.parseInt(aux1);
			int nodeEnd = Integer.parseInt(aux2);
			SON[nodeInit-1][nodeEnd-1]=0;
			SON[nodeEnd-1][nodeInit-1]=0;
						
			//Se crea la matriz que representa el arbol virtual con los nodos intermedios
			for (int j = 0; j < allLinksFromRequestAux.size(); j++) {
					
					aux = allLinksFromRequestAux.get(j).split("-");
					aux1 = aux[0];
					aux2 = aux[1];
					int nodeInit1 = Integer.parseInt(aux1);
					int nodeEnd1 = Integer.parseInt(aux2);
					subgraph[nodeInit1-1][nodeEnd1-1]=1;
					subgraph[nodeEnd1-1][nodeInit1-1]=1;
			}
						
			//Se obtienen los nodos conectados al origen
			DepthFirstSearch dfs = new DepthFirstSearch();
			ArrayList<Integer> nodes = dfs.depthFirstSearch(subgraph, root);
						
			//Se recorre la lista de nodos para agregar a la lista de nodos conectados o desconectados
			for (int k = 0; k < sustrateNodes.size(); k++) {
				
				if(nodes.contains(sustrateNodes.get(k))) {
					
					connectNodes.add(sustrateNodes.get(k));
				}else {
					disconnectNodes.add(sustrateNodes.get(k));
				
				}
			}
			
			Map<Integer, ArrayList<Integer>> shortespath = new LinkedHashMap<>();
			ArrayList<Integer> path = new ArrayList<>();
			ArrayList<Integer> bestPath = new ArrayList<>();

			int shortDistance = linkSizeSON;
			int distance = 0;
			int bestshortDistance = linkSizeSON;
			
			if(disconnectNodes.size() == 0) {

				bestPath.add(PATH_PROTECTION_INCLUDED);				
			
			}
			
			for (int j = 0; j < disconnectNodes.size(); j++) {
				
				for (int j2 = 0; j2 < connectNodes.size(); j2++) {
					
					ArrayList<Integer> P = new ArrayList<>();

					shortespath = DijkstrasAlgorithm.dijkstra(SON, disconnectNodes.get(j), connectNodes.get(j2));
					
					if(shortespath!=null) {

						for (Map.Entry<Integer, ArrayList<Integer>> pathDistance : shortespath.entrySet()) {
							
							distance = pathDistance.getKey();
							P = pathDistance.getValue();
						}
					}
						
					if(shortDistance > distance) {
						
						shortDistance = distance;
						path = P;
					}
					
				}
				
				if(bestshortDistance > shortDistance) {
					
					bestshortDistance = shortDistance;
					bestPath = path;
				}
			}
				
			linkProtection.put(link, bestPath);
			connectNodes.clear();
			disconnectNodes.clear();
			allLinksFromRequestAux.clear();
			nodes.clear();
			SON[nodeInit-1][nodeEnd-1]=1;
			SON[nodeEnd-1][nodeInit-1]=1;
	
		}
		
		return linkProtection;

	}
}
