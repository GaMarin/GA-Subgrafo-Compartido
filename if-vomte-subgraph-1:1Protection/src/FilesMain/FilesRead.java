package FilesMain;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Clase que lee dos archivos: red fisica y red virtual; 
 * para luego cargarlo en las estructuras.
 * 
 * SON: red fisica
 * VOR: red virtual
 *  
 * @author gmarin
 *
 */
public class FilesRead {
	
	//Matriz para la red fisica SON
	private static int [][] SON;
	
	//Vector que contiene los enlaces virtuales asignados y los VOR correspondientes
	//private int [] v_linkAssignmentToSON;
	
	//Vector que especifica la capacidad cpu del SON disponible
	private static Map<Integer, Integer> cpuAvailableInSON;
	
	//Vector que contiene los nodos virtuales asignados, el VOR correspondiente 
	//y el cpu utilizado por el nodo virtual.
	private int [] v_nodeAssignmentToSON;
	
	//Vector que contiene la cantidad de cpu requerida por cada nodo virtual
	private static Map<Integer,int[]> cpuRequestForVOR;
	
	//Cantidad de subportadoras solicitadas por el VOR
	private static int bandwidthRequestForVOR;
	
	//Cantidad de nodos físicos
	private static int s_nodeSize;
	
	//Cantidad de nodos virtuales
	private static int v_nodeSize;
	
	//Cantidad de enlaces físicos
	private static int s_linkSize;
		
	//Cantidad de enlaces virtuales
	private static int v_linkSize;
	
	private static int bandwidthSizeInSON;
	
	//Archivo VOR
	private static String VORFILE;

	//Archivo SON
	private static String SONFILE;
	
	//Archivo que contiene la url de los archivos VOR y SON
	private static final String FILES = "/home/gmarin/if-vomte-protection/readFiles.txt";
	
	private static ArrayList<String> partsSON = new ArrayList<String>();
	
	private static ArrayList<String> partsVOR = new ArrayList<String>();

	public FilesRead() {
		
		ArrayList<String> files = new ArrayList<>();
		
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
                files.add(strLinea);
            }

            entrada.close();
						
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		SONFILE = files.get(0);
		VORFILE = files.get(1);
		
		setSONFile(SONFILE);
		setVORFile(VORFILE);
				
	}
	
	/**
	 * Retorna el archivo que contiene la red física y añade linea por linea al arraylist 
	 * 
	 * @param SONFILE
	 * @return
	 */
	public ArrayList<String> setSONFile(String SONFILE) {
		
		try {
			
			
			//Se abre el archivo con la ruta especificada.
            FileInputStream fstream = new FileInputStream(new File(SONFILE));
            
            //Se crea el objeto de entrada
            DataInputStream entrada = new DataInputStream(fstream);
            
            //Se crea el Buffer de Lectura
            BufferedReader buffer = new BufferedReader(new InputStreamReader(entrada));
            String strLinea;
            
            //Leer el archivo linea por linea
            while ((strLinea = buffer.readLine()) != null)   {
                //Se agrega la línea al vector
            	partsSON.add(strLinea);
            }

            entrada.close();
						
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return partsSON;
		
	}
	
	/**
	 * Retorna la cantidad de nodos de la red física
	 * 
	 * @return
	 */
	public int getNodeSizeInSON() {
		
		String aux = null;
		String [] aux2 = null;
		String aux3 = null;
		
		//Se obtiene del archivo el número de nodos y se guarda en la variable
		aux = partsSON.get(0);
		aux2 = aux.split(":");
		aux3 = aux2[1];
		s_nodeSize = Integer.parseInt(aux3);
		
		return s_nodeSize;
		
	}
	
	/**
	 * Retorna la cantidad de enlaces de la red física
	 * 
	 * @return
	 */
	public int getLinkSizeInSON() {
		
		String aux = null;
		String [] aux2 = null;
		String aux3 = null;
		
		//Se obtiene del archivo el número de enlaces y se guarda en la variable
		aux = partsSON.get(1);
		aux2 = aux.split(":");
		aux3 = aux2[1];
		s_linkSize = Integer.parseInt(aux3);
		
		return s_linkSize;
	}
	
	/**
	 * Retorna la cantidad de subportadoras que contiene cada enlace de la red física
	 * 
	 * @return
	 */
	public int getBandwithSizeInSON() {
		
		String aux = null;
		String [] aux2 = null;
		String aux3 = null;
		
		//Se obtiene del archivo el número de subportadoras y se guarda en la variable
		aux = partsSON.get(2);
		aux2 = aux.split(":");
		aux3 = aux2[1];
		bandwidthSizeInSON = Integer.parseInt(aux3);
		
		return bandwidthSizeInSON;
	}
	
	/**
	 * Retorna la lista que contiene la cantidad de cpu disponible en cada nodo de la red física
	 * 
	 * @param s_nodeSize
	 * @return
	 */
	public Map<Integer, Integer> getCpuAvailableInSON(int s_nodeSize){
		
		String aux = null;
		String [] aux2 = null;
		String aux3 = null;
		
		//Se obtiene del archivo la cantidad de CPU y se guarda en la variable
		aux = partsSON.get(3);
		aux2 = aux.split(":");
		aux3 = aux2[1];
		
		Map<Integer, Integer> cpuAvailableInSON = new LinkedHashMap<>();
						
		//Se agrega las capacidades cpu de cada nodo fisico al vector 
		for (int i = 0; i < s_nodeSize; i++) {	
			
			cpuAvailableInSON.put(i+1, Integer.parseInt(aux3));
			
		}
		
		return cpuAvailableInSON;
		
	}
	
	/**
	 * Retorna la lista de nodos de la red física
	 * 
	 * @param s_nodeSize
	 * @return
	 */
	public int [] getNodeSONList(int s_nodeSize) {
		
		int [] nodeSONList = new int[s_nodeSize];
		
		for (int i = 0; i < s_nodeSize; i++) {
			
			nodeSONList[i] = i +1;
			
		}
		
		return nodeSONList;
		
	}
	
	/**
	 * Crea la matriz de la red física y lo retorna 
	 *  
	 * @param s_nodeSize
	 * @return
	 */
	public int[][] getSONArray(int s_nodeSize){
		
		String aux = null;
		String [] aux2 = null;
		String [] aux3 = null;
		String aux5 = null;
		
		int [][] SON = null;
		
		//Se inicializa la matriz SON con el tamaño de los nodos
		SON = new int [s_nodeSize][s_nodeSize];
				
		//Se pone a cero todos los elementos de la matriz
		for (int i = 0; i < s_nodeSize; i++) {
			for (int j = 0; j < s_nodeSize; j++) {
				
				SON[i][j]= 0;
				
			}
			
		}
		
		//Se pone a uno los nodos que tienen enlaces
		for(int i=4; i<partsSON.size();i++) {
			
			aux = partsSON.get(i);
			aux2 = aux.split(":");
			aux5 = aux2[0];
			aux3 = aux2[1].split(",");
			
			for (int j = 0; j < aux3.length; j++) {
				
				SON[Integer.parseInt(aux5)-1][Integer.parseInt(aux3[j])-1]= 1;

			}
	
		}		
		
		return SON;
	}
	
	/**
	 * Crea el arreglo de enlaces con las subportadoras disponibles
	 * @param s_nodeSize
	 * @param s_linkSize
	 * @return
	 */
	public Map<String, int []> createVLinkAssignmentToSONLink(int s_nodeSize, int bandwithSize){
		
		Map<String, int []> v_linkAssignmentToSONList = new LinkedHashMap<String, int[]>();
		
		String aux = null;
		
		for(int i=4; i<partsSON.size();i++) {
			
			aux = partsSON.get(i);
			
			int [] v_linkAssignmentToSON = new int [bandwithSize];
			
			for (int j = 0; j < v_linkAssignmentToSON.length; j++) {
				
				v_linkAssignmentToSON[j] = 0;
				
			}
			
			aux = partsSON.get(i);
			String [] aux2 = aux.split(":");
			String aux5 = aux2[0];
			String [] aux3 = aux2[1].split(",");
			
			for (int j = 0; j < aux3.length; j++) {
				
				if(Integer.parseInt(aux5) < Integer.parseInt(aux3[j])) {
					String aux6 = aux5 + "-" + aux3[j];
					v_linkAssignmentToSONList.put(aux6, v_linkAssignmentToSON);
				}
			}
			
		}
		
		return v_linkAssignmentToSONList;
		
	}
	
	/**
	 * Crea la estructura que permite mantener información de los nodos virtuales mapeados a nodos fisicos
	 * 
	 * @param s_nodeList
	 * @return
	 */
	/*public Map<Integer, ArrayList<String[]>> createVNodeAssignmentToSONnode(int[] s_nodeList){
		
		Map<Integer, ArrayList<String[]>> virtualNodeAssignment = new LinkedHashMap<>();
		
		for (int i = 0; i < s_nodeList.length; i++) {
			
			virtualNodeAssignment.put(s_nodeList[i], null);
		}
		
		return virtualNodeAssignment;

	}
	*/
	

	
	/**
	 * Retorna el archivo que contiene las redes virtuales y añade linea por linea al arraylist
	 * 
	 * @param VORFILE
	 */
	public static void setVORFile(String VORFILE) {
				
		try {
			
			//Se abre el archivo con la ruta especificada.
            FileInputStream fstream = new FileInputStream(new File(VORFILE));
            
            //Se crea el objeto de entrada
            DataInputStream entrada = new DataInputStream(fstream);
            
            //Se crea el Buffer de Lectura
            BufferedReader buffer = new BufferedReader(new InputStreamReader(entrada));
            String strLinea;
            
            //Leer el archivo linea por linea
            while ((strLinea = buffer.readLine()) != null)   {
                //Se agrega la línea al vector
                partsVOR.add(strLinea);
            }

            entrada.close();
						
		} catch (IOException e) {
			e.printStackTrace();
		}
	
	}
	
	/**
	 * Retorna la cantidad de solicitudes VOR
	 * 
	 * @return
	 */
	public int getVORrequestSize() {
		
		String aux = null;
		String [] aux2 = null;
		String aux3 = null;
		
		int VORrequestSize;
	
		//Se obtiene la cantidad de solicitudes y se guarda en la variable
		aux = partsVOR.get(0);
		aux2 = aux.split(":");
		aux3 = aux2[1];
		VORrequestSize = Integer.parseInt(aux3);
		
		return VORrequestSize;
	}
	
	/**
	 * Retorna la estructura que contiene la cantidad de nodos de cada solicitud VOR
	 * 
	 * @param VORrequestSize
	 * @return
	 */
	public Map<Integer, Integer> getVNodeSizeList(int VORrequestSize){
		
		String aux = null;
		String [] aux2 = null;
		String aux3 = null;
	
		Map<Integer, Integer> v_nodeSizeList = new LinkedHashMap<Integer, Integer>();
	
		int p=0;
		
		for (int i = 0; i < VORrequestSize; i++) {
																
			//Se obtiene del archivo el número de nodos y se guarda en la variable
			aux = partsVOR.get(p+1);
			aux2 = aux.split(":");
			aux3 = aux2[1];
			v_nodeSize = Integer.parseInt(aux3);
			
			v_nodeSizeList.put(i+1,v_nodeSize);
			
			p=p+8;

		}
		
		return v_nodeSizeList;
	}
	
	/**
	 * Retorna la estructura que contiene la cantidad de enlaces de cada solicitud VOR
	 * 
	 * @param VORrequestSize
	 * @return
	 */
	public Map<Integer, Integer> getVLinkSizeList(int VORrequestSize){
		
		String aux = null;
		String [] aux2 = null;
		String aux3 = null;
	
		Map<Integer, Integer> v_linkSizeList = new LinkedHashMap<Integer, Integer>();
	
		int p=0;
		
		for (int i = 0; i < VORrequestSize; i++) {
																
			//Se obtiene del archivo el número de enlaces y se guarda en la variable
			aux = partsVOR.get(p+2);
			aux2 = aux.split(":");
			aux3 = aux2[1];
			v_linkSize = Integer.parseInt(aux3);
			v_linkSizeList.put(i+1, v_linkSize);
			
			p=p+8;

		}
		
		return v_linkSizeList;
	}
	
	/**
	 * Retorna la estructura que contiene la cantidad de subportadoras requeridas de cada solicitud VOR
	 * 
	 * @param VORrequestSize
	 * @return
	 */
	public Map<Integer, Integer> getBandwidthRequestForVORList(int VORrequestSize){
		
		String aux = null;
		String [] aux2 = null;
		String aux3 = null;
	
		Map<Integer, Integer> bandwidthRequestForVORList = new LinkedHashMap<Integer, Integer>();
	
		int p=0;
		
		for (int i = 0; i < VORrequestSize; i++) {
																
			//Se obtiene del archivo el número de subportadoras solicitadas y se guarda en la variable
			aux = partsVOR.get(p+3);
			aux2 = aux.split(":");
			aux3 = aux2[1];
			bandwidthRequestForVOR = Integer.parseInt(aux3);
			bandwidthRequestForVORList.put(i+1, bandwidthRequestForVOR);
			
			p=p+8;

		}
		
		return bandwidthRequestForVORList;
	}
	
	/**
	 * Retorna la estructura que contiene la cantidad de cpu requerida en cada nodo de cada solicitud VOR
	 * 
	 * @param v_nodeSizeList
	 * @return
	 */
	public Map<Integer, Map<Integer,Integer>> getCpuRequestForVORList(int VORrequestSize){
		
		String aux = null;
		String [] aux2 = null;
		String [] aux5 = null;
		String aux3 = null;
		String aux4;
	
		Map<Integer, Map<Integer,Integer>> cpuRequestForVORList = new LinkedHashMap<Integer, Map<Integer,Integer>>();

	
		int p=0;
		
		for (int i = 0; i < VORrequestSize; i++) {
			
			Map<Integer,Integer> cpuRequestForVOR = new LinkedHashMap<Integer,Integer>();
			
			//Se agrega las solicitudes de cpu de cada nodo virtual al vector 
			aux = partsVOR.get(p+4);
			aux2 = aux.split(",");	
		
			for (int k = 0; k < aux2.length; k++) {
				aux5= aux2[k].split(":");
				aux3= aux5[0];
				aux4 = aux5[1];
				
				cpuRequestForVOR.put(Integer.parseInt(aux3), Integer.parseInt(aux4));
				
			}	
			
			cpuRequestForVORList.put(i+1, cpuRequestForVOR);
			
			p=p+8;

		}
		
		return cpuRequestForVORList;
	}
	
	/**
	 * Retorna la lista de nodos de cada solicitud
	 * 
	 * @param v_nodeSizeList
	 * @return
	 */
	public Map<Integer, int[]> getNodeVORList(Map<Integer, Integer> v_nodeSizeList, int VORrequestSize) {
		
		Map<Integer, int[]> nodeVORList = new LinkedHashMap<Integer, int[]>();
		
		for (int i = 0; i < VORrequestSize; i++) {
			
			int nodeSize = v_nodeSizeList.get(i+1);
			int [] nodeVOR = new int [nodeSize];
			
			for (int j = 0; j < nodeSize; j++) {
		
				nodeVOR[j] = j + 1;
			}
			
			nodeVORList.put(i+1, nodeVOR);
		}
		
		return nodeVORList;
		
	}
	
	/**
	 * Retorna las matrices de las redes virtuales
	 * 
	 * @param VORrequestSize
	 * @param v_nodeSizeList
	 * @return
	 */
	public Map<Integer, int[][]> getVORArrayList(int VORrequestSize, Map<Integer, Integer> v_nodeSizeList){
		
		String aux = null;
		String [] aux2 = null;
		String [] aux5 = null;
		String aux3 = null;
		String aux4 = null;
		
		Map<Integer, int[][]> v_networkArrayList = new LinkedHashMap<Integer, int[][]>();
		
		int p=0;
		for (int i = 0; i < VORrequestSize; i++) {
														
			//Se crea y se inicializa la matriz SON con el tamaño de los nodos
			int [][] VOR = new int [v_nodeSizeList.get(i+1)][v_nodeSizeList.get(i+1)];
			
			//Se pone a cero todos los elementos de la matriz
			for (int k = 0; k < v_nodeSizeList.get(i+1); k++) {
				for (int l = 0; l < v_nodeSizeList.get(i+1); l++) {
					
					VOR[k][l]= 0;
					
				}
				
			}
			
			//Se pone a uno los nodos que tienen enlaces
			aux = partsVOR.get(p+7);
			aux2 = aux.split(",");
			
			for (int j = 0; j < aux2.length; j++) {
				
				aux5= aux2[j].split("-");
				aux3 = aux5[0];
				aux4 = aux5[1];
				
				VOR[Integer.parseInt(aux3)-1][Integer.parseInt(aux4)-1]= 1;
				
			}
			
			v_networkArrayList.put(i+1,VOR);
			
			p=p+8;

		}
		
		return v_networkArrayList;
	}
	
    public int[] getRoot(int VORrequestSize) {
		
		//Lista de nodos origen
		int[] roots = new int[VORrequestSize];
		
		String aux = null;
		String[] aux2 = null;
		String aux3 = null;
		
		int p=0;
		
		for (int i = 0; i < VORrequestSize; i++) {
						
			//Se agrega las solicitudes de cpu de cada nodo virtual al vector 
			aux = partsVOR.get(p+5);
			aux2 = aux.split(":");	
			aux3 = aux2[1];
			
			roots[i] = Integer.parseInt(aux3);
			
			p=p+8;

		}
		
		return roots;
	}
	
	public static void clear() {
		
		partsSON.clear();
		partsVOR.clear();
	}


}
