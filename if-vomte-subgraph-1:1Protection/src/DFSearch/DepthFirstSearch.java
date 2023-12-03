package DFSearch;

import java.util.ArrayList;
import java.util.Stack;

public class DepthFirstSearch {
	
	public DepthFirstSearch() {};
	
	 // Function to perform depth first search
    public ArrayList<Integer> depthFirstSearch(int[][] matrix, int source){
    	
    	ArrayList<Integer> virtualNodes = new ArrayList<>();

        boolean[] visited = new boolean[matrix.length];
        visited[source-1] = true;
        Stack<Integer> stack = new Stack<>();
        stack.push(source);
        int i,x;
        virtualNodes.add(source);
        while(!stack.isEmpty()){
            x = stack.pop();
            for(i=0; i<matrix.length; i++){
                if(matrix[x-1][i] == 1 && visited[i] == false){
                    stack.push(x);
                    visited[i] = true;
                    virtualNodes.add(i+1);
                    x = i+1;
                    i = -1;
                }
            }
        }
        
        return virtualNodes;
    }
    // Function to read user input
    /*public static void main(String[] args) {
   
        
        int matrix[][] = {
				{0,1,1,0,0,0,0},  // Node 1: 40
				{0,0,0,1,0,0,0},  // Node 2 :10
				{0,1,0,1,1,1,0},  // Node 3: 20
				{0,0,0,0,1,0,0},  // Node 4: 30
				{0,0,0,0,0,0,0},  // Node 5: 60
				{0,0,0,0,0,0,0},  // Node 6: 50
				{0,0,0,0,0,0,0},  // Node 7: 70
		};
        
       
        depthFirstSearch(matrix,1);
    }*/

}
