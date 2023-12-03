package ShortestPath;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class DijkstrasAlgorithm {
	
	private static final int NO_PARENT = -1; 
	  
    // Function that implements Dijkstra's 
    // single source shortest path 
    // algorithm for a graph represented  
    // using adjacency matrix 
    // representation 
    public static Map<Integer, ArrayList<Integer>> dijkstra(int[][] adjacencyMatrix, int sustrateNode, int sustrateMapped) { 
        
    	Map<Integer, ArrayList<Integer>> distancePath = new LinkedHashMap<>();
        
        int startVertex = sustrateNode - 1; 
        int endVertex = sustrateMapped - 1;
    	
    	int nVertices = adjacencyMatrix[0].length; 
  
        // shortestDistances[i] will hold the 
        // shortest distance from src to i 
        int[] shortestDistances = new int[nVertices]; 
  
        // added[i] will true if vertex i is 
        // included / in shortest path tree 
        // or shortest distance from src to  
        // i is finalized 
        boolean[] added = new boolean[nVertices]; 
  
        // Initialize all distances as  
        // INFINITE and added[] as false 
        for (int vertexIndex = 0; vertexIndex < nVertices; vertexIndex++) { 
            
        	shortestDistances[vertexIndex] = Integer.MAX_VALUE; 
            added[vertexIndex] = false; 
        } 
          
        // Distance of source vertex from 
        // itself is always 0 
        shortestDistances[startVertex] = 0; 
  
        // Parent array to store shortest 
        // path tree 
        int[] parents = new int[nVertices]; 
  
        // The starting vertex does not  
        // have a parent 
        parents[startVertex] = NO_PARENT; 
  
        // Find shortest path for all  
        // vertices 
        
        boolean flag = true;

        for (int i = 1; i < nVertices; i++) { 
  
            // Pick the minimum distance vertex 
            // from the set of vertices not yet 
            // processed. nearestVertex is  
            // always equal to startNode in  
            // first iteration. 
            int nearestVertex = -1; 
            int shortestDistance = Integer.MAX_VALUE; 
            for (int vertexIndex = 0; vertexIndex < nVertices; vertexIndex++) { 
                if (!added[vertexIndex] && shortestDistances[vertexIndex] < shortestDistance)  { 
                    nearestVertex = vertexIndex; 
                    shortestDistance = shortestDistances[vertexIndex]; 
                } 
            } 
            
            
            if(nearestVertex==-1) {
            	flag= false;
            	break;
            }
  
            // Mark the picked vertex as 
            // processed 
            added[nearestVertex] = true; 
  
            // Update dist value of the 
            // adjacent vertices of the 
            // picked vertex. 
            for (int vertexIndex = 0; vertexIndex < nVertices;  vertexIndex++) { 
                int edgeDistance = adjacencyMatrix[nearestVertex][vertexIndex]; 
                  
                if (edgeDistance > 0 && ((shortestDistance + edgeDistance) < shortestDistances[vertexIndex])) { 
                    parents[vertexIndex] = nearestVertex; 
                    shortestDistances[vertexIndex] = shortestDistance +  
                                                       edgeDistance; 
                } 
            } 
        } 
        
        for (int i = 0; i < shortestDistances.length; i++) {
			if(shortestDistances[i]== Integer.MAX_VALUE) {
				flag=false;
				break;
			}
		}
        
        if(flag==true) {
  
        	distancePath = printSolution(startVertex, shortestDistances, parents, endVertex); 
        
        	return distancePath;
        }
        else {
        	return null;
        }
    } 
  
    // A utility function to print  
    // the constructed distances 
    // array and shortest paths 
private static Map<Integer, ArrayList<Integer>> printSolution(int startVertex, int[] distances, int[] parents, int endVertex) { 
    	
        Map<Integer, ArrayList<Integer>> distancePath = new LinkedHashMap<>();
        ArrayList<Integer> pathAux = new ArrayList<>();

          
        if (endVertex != startVertex) { 
            printPath(endVertex, parents, pathAux); 
            
            distancePath.put(distances[endVertex], pathAux);
            
        } 
            
        return distancePath;
    } 
  
    // Function to print shortest path 
    // from source to currentVertex 
    // using parents array 
    private static void printPath(int currentVertex, int[] parents, ArrayList<Integer> path) { 
        
        // Base case : Source node has 
        // been processed 
        if (currentVertex == NO_PARENT) { 
            return; 
        } 
        printPath(parents[currentVertex], parents, path); 
        path.add(currentVertex + 1); 
    } 
    /*private static ArrayList<Integer> printPath(int currentVertex, int[] parents) { 
        
    	ArrayList<Integer> path = new ArrayList<>();
    	ArrayList<Integer> pathAux = new ArrayList<>();
    	
    	while(currentVertex!=NO_PARENT) {
    		
    		pathAux.add(currentVertex + 1); 
            currentVertex = parents[currentVertex];
    	}
    	
    	for (int i = pathAux.size()-1; i >=0; i--) {
			
    		path.add(pathAux.get(i));
		}
    	
    	return path;
    } */
  
       
} 
