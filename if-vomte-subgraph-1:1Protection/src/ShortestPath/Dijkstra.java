package ShortestPath;
import java.util.PriorityQueue;

class Data implements Comparable<Data> {
	
	public final int index;
	public final int priority;

	public Data(int index, int priority) {
		this.index = index;
		this.priority = priority;
	}

	@Override
	public int compareTo(Data other) {
		return Integer.valueOf(priority).compareTo(other.priority);
	}
	
	public boolean equals(Data other) {
		return priority == other.priority;
	}

	// also implement equals() and hashCode()
}

public class Dijkstra{
	/* dijkstra(G,n,i,j)
		Given a weighted adjacency matrix for graph G, return the length
		of the shortest (i,j)-path.
		
		For full marks, the implementation must run in O(m*log(n)) time in the worst
		case on a graph with n vertices and m edges.
		
		If G[i][j] == 0, there is no edge between vertex i and vertex j
		If G[i][j] > 1, there is an edge between i and j and the value of G[i][j] is its weight.
		No entry of G will be negative.
	*/
			
	static int MAX_VERTS = 50000;
	public static int dijkstra(int[][] G, int m, int n){
		//Get the number of vertices in G
		//int n = G.length;
		
		int i = m - 1;
		int j = n - 1;
		
		/* ... Your code here ... */
		int[] distance = new int[G.length];
		PriorityQueue<Data> PQ = new PriorityQueue<Data>();
		boolean[] inTree = new boolean[G.length];
		
		for (int index = 0; index < G.length; index++) {
			if (index == i) {
				distance[index] = 0;
			}
			else {
				distance[index] = Integer.MAX_VALUE;
				PQ.add(new Data(index,distance[index]));
				inTree[index] = true; 
			}
		}
		
		for (int index = 0; index < G.length; index++) { // for each edge (v,z) do
			if (G[i][index] != 0) { // There is an edge
				if (distance[i] + G[i][index] < distance[index]) { // if D[v] + w((v,z)) < D[z] then 
					int oldIndex = distance[index];
					distance[index] = distance[i] + G[i][index]; // D[z] ← D[v] + w((v,z))  
					PQ.remove(new Data(index, oldIndex));
					PQ.add(new Data(index, distance[index])); // update PQ wrt D[z] 
				}
			}
		}
		
			
		while (PQ.peek() != null) { // If PQ isn't empty
			Data vertex = PQ.poll(); // RemoveMin
			for (int index = 0; index < G.length; index++) { // for each edge (u,z) with z ∈ PQ do
				if (G[vertex.index][index] != 0 && inTree[index] == true) { // z ∈ PQ
					if (distance[vertex.index] + G[vertex.index][index] < distance[index]) { // if D[v] + w((v,z)) < D[z] then 
						int oldIndex = distance[index];
						distance[index] = distance[vertex.index] + G[vertex.index][index]; // D[z] ← D[v] + w((v,z)) 
						PQ.remove(new Data(index, oldIndex));
						PQ.add(new Data(index, distance[index])); // update PQ wrt D[z] 
					}
				}
			
			}
		}
		if (distance[j] == Integer.MAX_VALUE || distance[j] < 0) {
			return -1;
		}
		else {
			return distance[j];
		}

	}
}
