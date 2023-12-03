package IfVomte;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import FilesMain.FilesRead;
import Protection.SubgraphBasedProtection;
import ShortestPath.DijkstrasAlgorithm;

public class IfVomte {
	
	//Lista de nodos virtuales mapeados
	private static ArrayList<Integer> v_nodeMapped = new ArrayList<Integer>();
	
	//Lista de nodos fisicos mapeados
	private static ArrayList<Integer> s_nodeMapped = new ArrayList<Integer>();
	
	//Orden de la permutación
	private static int [] requestPermutation;
	
	//
	private static Map<String, String[]> LinkAssignmentToSONLink = new LinkedHashMap<>();
	private static Map<String, String[]> VirtualLinkAssignmentToSONLink = new LinkedHashMap<>();
	private static Map<String, String[]> LinkAssignmentToSONLinkProtection = new LinkedHashMap<>();
	private static Map<Integer, ArrayList<String[]>> VNodeAssignmentToSONnode = new LinkedHashMap<>();

	//Contendrá los arboles que tienen enlaces en común
	private static int[][] contingencyArray;
	
	//Numero total de saltos de cada árbol
	private static Map<Integer, Integer> JumpCount = new LinkedHashMap<>();
 
	//Vector que contiene el costo total de cada enlace fisico 
	private static Map<String, Integer> SONLinkUsed = new LinkedHashMap<>();

	
	public IfVomte() {
		
	}
	
	public static ArrayList<Integer> ifVomte(int [] requestOrder) throws IOException {

		requestPermutation = requestOrder;
		
		contingencyArray = new int[requestOrder.length][requestOrder.length];
		
		//Se inicializa la matriz de contingencia
		for (int i = 0; i < contingencyArray.length; i++) {
			for (int j = 0; j < contingencyArray.length; j++) {
				contingencyArray[i][j]=0;
			}
		}
		
        FilesRead input = new FilesRead();	
        
        String newUrl = "/home/gmarin/if-vomte-protection/output5.txt";
        File output = new File(newUrl);
				
		try {
            
            // Si el archivo no existe es creado
            if (!output.exists()) {
            	output.createNewFile();
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }

		//Contendrá la cantidad de solicitudes virtuales
		int request = 0;
		//request = input.getVORrequestSize();
		request =20;
		
		//Contendrá la cantidad de nodos físicos del SON
		int nodeSizeInSON = 0;
		nodeSizeInSON = input.getNodeSizeInSON();
		
		//Contendrá la cantidad de ranuras en el SON
		int bandwithSizeInSON = 0;
		bandwithSizeInSON = input.getBandwithSizeInSON();

		//Contendrá los origenes de cada solicitud
		int [] roots = new int[request]; 
		roots = input.getRoot(request);
		
		int newCpuAvailable = 0;
		int [] B = new int [request];
		int requestRejectCount = 0;
		int requestProtectionRejectCount = 0;
		
		String contenido = "RESULTADO\n";
        FileWriter fw = new FileWriter(output);
        BufferedWriter bw = new BufferedWriter(fw);
	
        //Se extraen los datos de los archivos SON y VOR
		Map<String, int[]> vLinkAssignment= new LinkedHashMap<String, int[]>();
		vLinkAssignment = input.createVLinkAssignmentToSONLink(nodeSizeInSON, bandwithSizeInSON);
		Map<Integer, Integer> v_nodeSizeList = new LinkedHashMap<Integer, Integer>();
		v_nodeSizeList = input.getVNodeSizeList(request);
		Map<Integer, Map<Integer, Integer>> cpuRequestList = new LinkedHashMap<Integer, Map<Integer, Integer>>();
		cpuRequestList = input.getCpuRequestForVORList(request);
		Map<Integer, Integer> bandwithRequestList = new LinkedHashMap<Integer, Integer>(); 
		bandwithRequestList = input.getBandwidthRequestForVORList(request);
		Map<Integer, Integer> cpuNodeSON = new LinkedHashMap<Integer, Integer>();
		cpuNodeSON = input.getCpuAvailableInSON(nodeSizeInSON);
		Map<Integer, int[][]> VORList = new LinkedHashMap<Integer, int[][]>();
		VORList = input.getVORArrayList(request, v_nodeSizeList);
		Map<Integer, Integer> linkSizeVORList = new LinkedHashMap<Integer, Integer>();
		linkSizeVORList = input.getVLinkSizeList(request);
		
		Map<Integer, Integer> cpuNodeSONAux = new LinkedHashMap<Integer, Integer>();
		Map<String, int[]> LinkAssignmentSONLinkAux= new LinkedHashMap<String, int[]>();
		Map<int [][], Map<String, int[]>> prunetSONWithLinks= new LinkedHashMap<int[][], Map<String, int[]>>();
		Map<Integer, Integer> cpuNodeVORAux = new LinkedHashMap<Integer, Integer>();
		Map<Integer, ArrayList<Integer>> distanceDijkstra = new LinkedHashMap<Integer, ArrayList<Integer>>();

		int [][] VOR;
		Map<Integer, Integer> cpuRequest = new LinkedHashMap<Integer, Integer>();
		int bandwithRequest = 0;
		int v_nodeSize = 0;
		int linkSizeVOR = 0;
		int linkSizeSON = 0;
		linkSizeSON = input.getLinkSizeInSON();
		
		String contenidoAux = "";
	
		Map<String, int[]> LinkAssignmentSONLinkBackup = new LinkedHashMap<String, int[]>();
		Map<String, String[]> LinkAssignmentBackup = new LinkedHashMap<String, String[]>();
		Map<String, String[]> virtualLinkAssignmentBackup = new LinkedHashMap<String, String[]>();
		Map<String, String[]> linkAssignmentProtectionBackup = new LinkedHashMap<String, String[]>();
		Map<Integer, Integer> cpuNodeBackup = new LinkedHashMap<Integer, Integer>();
		Map<String, Integer> SONLinkUsedBackup = new LinkedHashMap<String, Integer>();
		Map<String, Integer> updateSONUsed = new LinkedHashMap<String, Integer>();
		Map<String, Integer> SONUsedForRequest = new LinkedHashMap<String, Integer>();
		Map<Map<String, Integer>, Map<String, Integer>> linksUsedUpdates = new LinkedHashMap<>();
		Map<Integer, ArrayList<String[]>> VNodeAssignmentToSONnodeBackup = new LinkedHashMap<Integer, ArrayList<String[]>>();
		Map<Integer, Integer> JumpCountAux = new LinkedHashMap<>();
		
		Map<Integer, ArrayList<ArrayList<Integer>>> pathsForProtection = new LinkedHashMap<>();
					
		//La cantidad de saltos se inicializa a cero
		for (int i=0; i<request;i++) {
			
			JumpCount.put(i+1, 0);
		}
		
		//El uso de enlaces se inicializa a cero
		for (Map.Entry<String, int[]> linksUsed : vLinkAssignment.entrySet()) {
			
			String link = linksUsed.getKey();
			
			SONLinkUsed.put(link, 0);
		}
		
		//Agrega todos los enlaces al map de asignaciones de solicitudes virtuales a los enlaces, al auxiliar,
		//al de asignaciones de protección y al de asignaciones General
		for (Map.Entry<String, int[]> linksUsed : vLinkAssignment.entrySet()) {
			
			String[] requestUsed = new String [linksUsed.getValue().length];
			String link = linksUsed.getKey();
			int[] aux = linksUsed.getValue();
			int[] aux2 = new int[linksUsed.getValue().length];
			
			for (int i = 0; i < aux.length; i++) {
				
				aux2[i] = aux[i];
			}
			
			LinkAssignmentSONLinkAux.put(link, aux2);
			VirtualLinkAssignmentToSONLink.put(link, requestUsed);
			LinkAssignmentToSONLink.put(link, requestUsed);
			LinkAssignmentToSONLinkProtection.put(link, requestUsed);
		}
			
		//Agrega todos los nodos y sus informaciones al map de asignaciones de nodos
		for (int i = 0; i < nodeSizeInSON; i++) {
			
			String[] aux = new String[3];
			ArrayList<String[]> nodeInformation = new ArrayList<>();
			
			nodeInformation.add(aux);
			
			VNodeAssignmentToSONnode.put(i+1, nodeInformation);
		}
		
		int requestNumber = 0;
					
			
			//Para probar si funciona con el ejemplo del paper
			/*int[] put1 = {1,1,1,1,1,0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
			int[] put2 = {1,0,0,0,0,0,0,0,0,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
			int[] put3 = {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
			int[] put4 = {0,0,0,0,0,0,0,1,1,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
			int[] put5 = {1,1,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
			int[] put6 = {1,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
			int[] put7 = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
			int[] put8 = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
			int[] put9 = {1,1,1,1,1,0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
			int[] put10 = {1,0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
			int[] put11 = {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
			int[] put12 = {0,0,0,0,0,0,0,1,1,1,1,1,1,1,1,1,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
			int[] put13 = {1,1,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
			int[] put14 = {1,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
			int[] put15 = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
			int[] put16 = {0,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
			int[] put17 = {1,1,1,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
			int[] put18 = {1,1,1,0,0,0,0,0,0,0,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
			int[] put19 = {1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
			int[] put20 = {1,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
			int[] put21 = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
			int[] put22 = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
			int[] put23 = {1,1,1,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
			int[] put24 = {1,1,1,0,0,0,0,0,0,0,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
			int[] put25 = {1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
			int[] put26 = {1,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
			int[] put27 = {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
			int[] put28 = {0,0,0,0,0,0,0,1,1,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
			int[] put29 = {1,1,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
			int[] put30 = {1,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
			int[] put31 = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
			int[] put32 = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
			int[] put33 = {1,1,1,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
			int[] put34 = {1,1,1,0,0,0,0,0,0,0,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
			int[] put35 = {1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
			int[] put36 = {1,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};

			LinkAssignmentSONLinkAux.put("1-2", put1);
			LinkAssignmentSONLinkAux.put("3-7", put2);
			LinkAssignmentSONLinkAux.put("3-4", put3);
			LinkAssignmentSONLinkAux.put("8-9", put4);
			LinkAssignmentSONLinkAux.put("12-18", put5);
			LinkAssignmentSONLinkAux.put("14-15", put6);
			LinkAssignmentSONLinkAux.put("19-24", put7);
			LinkAssignmentSONLinkAux.put("21-22", put8);
			LinkAssignmentSONLinkAux.put("1-35", put9);
			LinkAssignmentSONLinkAux.put("24-28", put10);
			LinkAssignmentSONLinkAux.put("11-20", put11);
			LinkAssignmentSONLinkAux.put("22-28", put12);
			LinkAssignmentSONLinkAux.put("5-10", put13);
			LinkAssignmentSONLinkAux.put("29-34", put14);
			LinkAssignmentSONLinkAux.put("5-31", put15);
			LinkAssignmentSONLinkAux.put("17-22", put16);
			LinkAssignmentSONLinkAux.put("33-34", put17);
			LinkAssignmentSONLinkAux.put("7-13", put18);
			LinkAssignmentSONLinkAux.put("15-20", put19);
			LinkAssignmentSONLinkAux.put("26-31", put20);
			LinkAssignmentSONLinkAux.put("20-21", put21);
			LinkAssignmentSONLinkAux.put("6-11", put22);
			LinkAssignmentSONLinkAux.put("8-14", put23);
			LinkAssignmentSONLinkAux.put("28-33", put24);
			LinkAssignmentSONLinkAux.put("10-11", put25);
			LinkAssignmentSONLinkAux.put("31-35", put26);
			LinkAssignmentSONLinkAux.put("9-13", put27);
			LinkAssignmentSONLinkAux.put("14-19", put28);
			LinkAssignmentSONLinkAux.put("16-25", put29);
			LinkAssignmentSONLinkAux.put("21-30", put30);
			LinkAssignmentSONLinkAux.put("9-14", put31);
			LinkAssignmentSONLinkAux.put("2-7", put32);
			LinkAssignmentSONLinkAux.put("16-21", put33);
			LinkAssignmentSONLinkAux.put("4-9", put34);
			LinkAssignmentSONLinkAux.put("17-18", put35);
			LinkAssignmentSONLinkAux.put("21-26", put36);
			*/
		/*int put1[] = {1,0,0,0,0};
		int put2[] = {1,0,0,0,0};
		int put3[] = {1,0,0,0,0};
		int put4[] = {0,0,0,0,1};
		int put5[] = {0,0,0,0,1};
		
		LinkAssignmentSONLinkAux.put("1-2", put1);
		LinkAssignmentSONLinkAux.put("1-3", put2);
		LinkAssignmentSONLinkAux.put("3-4", put3);
		LinkAssignmentSONLinkAux.put("4-5", put4);
		LinkAssignmentSONLinkAux.put("2-5", put5);
		
		cpuNodeSON.put(1, 12);
		cpuNodeSON.put(2, 10);
		cpuNodeSON.put(3, 11);
		cpuNodeSON.put(4, 9);
		cpuNodeSON.put(5, 8);*/
		
			//Hasta acá el ejemplo	
			
		Map<Integer, ArrayList<ArrayList<Integer>>> pathsByRequest = new LinkedHashMap<>();
		
		//Por cada solicitud
		for (int i = 0; i < requestPermutation.length; i++) {
			
			//Se obtiene el número de solicitud del arreglo de permutaciones
			requestNumber = requestPermutation[i];
			
			int root = 0;
			
			root = roots[requestNumber-1];
			VOR = VORList.get(requestNumber);
			cpuRequest = cpuRequestList.get(requestNumber);
			bandwithRequest = bandwithRequestList.get(requestNumber);
			v_nodeSize = v_nodeSizeList.get(requestNumber);
			linkSizeVOR = linkSizeVORList.get(requestNumber);
						
			//Ruta fisica de cada enlace
			Map<String, ArrayList<Integer>> allPaths = new LinkedHashMap<>();
			ArrayList<ArrayList<Integer>> pathsAux = new ArrayList<>();
			
			//El uso de enlaces para el arbol primario se coloca a cero
			for (Map.Entry<String, Integer> linksUsed : SONLinkUsed.entrySet()) {
				
				String link = linksUsed.getKey();
				
				SONUsedForRequest.put(link, 0);
			}
			
			//Se crea una copia del map de asignaciones de nodos
			for (Map.Entry<Integer, ArrayList<String[]>> VNodeAssignmentTo : VNodeAssignmentToSONnode.entrySet()) {
				
				ArrayList<String[]> aux1 = new ArrayList<String[]>();
				ArrayList<String[]> aux2 = new ArrayList<String[]>();
				
				aux2 = VNodeAssignmentTo.getValue();
				
				String [] aux3 = null;
				String [] aux4 = null;
				
				for (int j = 0; j < aux2.size(); j++) {
					
					aux3 = aux2.get(j);
					aux4 = new String[aux3.length];

					for (int k = 0; k < aux3.length; k++) {
						
						aux4[k] = aux3[k];
					}
					
					aux1.add(aux4);
				}
				
				VNodeAssignmentToSONnodeBackup.put(VNodeAssignmentTo.getKey(), aux1);
				
			}
			
			String [] aux3 = null;
			String [] aux4 = null;
			
			//Copia del map de asignaciones de enlaces general
			for (Map.Entry<String, String[]> LinkAssignmentB : LinkAssignmentToSONLink.entrySet()) {
				
				aux3 = new String[LinkAssignmentB.getValue().length];
				aux4 = LinkAssignmentB.getValue();
				
				for (int j = 0; j < aux4.length; j++) {
					
					aux3[j] = aux4[j];
				}
				
				LinkAssignmentBackup.put(LinkAssignmentB.getKey(), aux3);
			}
			
			//Copia del map de asignaciones de enlaces virtuales
			for (Map.Entry<String, String[]> vLinkAssignmentB : VirtualLinkAssignmentToSONLink.entrySet()) {
				
				aux3 = new String[vLinkAssignmentB.getValue().length];
				aux4 = vLinkAssignmentB.getValue();
				
				for (int j = 0; j < aux4.length; j++) {
					
					aux3[j] = aux4[j];
				}
				
				virtualLinkAssignmentBackup.put(vLinkAssignmentB.getKey(), aux3);
			}
			
			//Copia del map de asignaciones de enlaces de proteccion
			for (Map.Entry<String, String[]> linkAssignmentProtectionB : LinkAssignmentToSONLinkProtection.entrySet()) {
				
				aux3 = new String[linkAssignmentProtectionB.getValue().length];
				aux4 = linkAssignmentProtectionB.getValue();
				
				for (int j = 0; j < aux4.length; j++) {
					
					aux3[j] = aux4[j];
				}
				
				linkAssignmentProtectionBackup.put(linkAssignmentProtectionB.getKey(), aux3);
			}
			
			int [] aux5 = null;
			int [] aux6 = null;
			
			//Copia del map de subportadoras 
			for (Map.Entry<String, int[]> vLinkAssignmentSONLink : LinkAssignmentSONLinkAux.entrySet()) {
				
				aux5 = new int[vLinkAssignmentSONLink.getValue().length];
				aux6 = vLinkAssignmentSONLink.getValue();
				
				for (int j = 0; j < aux6.length; j++) {
					
					aux5[j] = aux6[j];
				}
				
				LinkAssignmentSONLinkBackup.put(vLinkAssignmentSONLink.getKey(), aux5);
			}
			
			//Copia del map de uso de enlaces
			for (Map.Entry<String, Integer> SONLink : SONLinkUsed.entrySet()) {
				 
				SONLinkUsedBackup.put(SONLink.getKey(), SONLink.getValue());
			}
			
			//Copia de la cantidad de saltos
			for (Map.Entry<Integer, Integer> Jump : JumpCountAux.entrySet()) {
				 
				JumpCount.put(Jump.getKey(), Jump.getValue());
			}
			
			//Se agrega los nodos fisicos y sus cpu al auxiliar y al backup
			for (Map.Entry<Integer, Integer> cpuNode : cpuNodeSON.entrySet()) {
				
				cpuNodeSONAux.put(cpuNode.getKey(), cpuNode.getValue());
				cpuNodeBackup.put(cpuNode.getKey(), cpuNode.getValue());
			}
			
			cpuNodeVORAux.clear();
			
			//Se agrega los nodos virtuales y sus cpu al auxiliar
			for (Map.Entry<Integer, Integer> cpuVNode : cpuRequest.entrySet()) {
				
				cpuNodeVORAux.put(cpuVNode.getKey(), cpuVNode.getValue());	
			}
						
			//Podar el SON
			prunetSONWithLinks = getPrunetSON(nodeSizeInSON, LinkAssignmentSONLinkBackup, bandwithRequest);
			//Map<String, int[]> prunetLinkAssignmentSONList = new LinkedHashMap<String, int[]>();
			int [][] prunetSON = new int [nodeSizeInSON][nodeSizeInSON];
		
			Candidate candidates = new Candidate();
			ImpacFactor impactFactor = new ImpacFactor();
			Closeness closeness = new Closeness();
			Map<Integer, ArrayList<Integer>> candidateList = new LinkedHashMap<Integer, ArrayList<Integer>>();
			int sustrateNode = 0;
			int flag = 0;
			
			contenidoAux += "Arbol primario\n";

			while(v_nodeMapped.size() < v_nodeSize) {
								
				//Obtiene la matriz y los arrays de enlaces podados
				for (Map.Entry<int [][], Map<String, int[]>> prunet : prunetSONWithLinks.entrySet()) {
					
					int[][] aux1 = prunet.getKey();
					
					for (int j = 0; j < aux1.length; j++) {
						for (int k = 0; k < aux1.length; k++) {
							
							prunetSON[j][k] = aux1[j][k];
						}
					}

					for (Map.Entry<String, int[]> aux : prunet.getValue().entrySet()) {
						
						int[] aux2 = aux.getValue();
						int[] aux7 = new int[aux2.length];
						
						for (int j = 0; j < aux2.length; j++) {
							
							aux7[j] = aux2 [j];
						}
						
						LinkAssignmentSONLinkBackup.put(aux.getKey(), aux7);
					} 
					
				}
				
				//Se obtiene la lista de candidatos para cada nodo virtual
				candidateList = candidates.getCandidates(VOR, cpuNodeSONAux, cpuNodeVORAux, LinkAssignmentSONLinkBackup, bandwithRequest, prunetSON);

				//Se obtiene el nodo virtual a ser mapeado
				int virtualNode = impactFactor.getVirtualNode(cpuNodeVORAux, cpuRequest, linkSizeVOR, bandwithRequest, candidateList, VOR);
				
				//Se obtiene el nodo fisico seleccionado para ser mapeado
				sustrateNode = closeness.getCloseness(candidateList, virtualNode, VOR, cpuNodeSONAux, v_nodeMapped, s_nodeMapped, prunetSON);
				
				//Se agrega los nodos mapeados
				if(sustrateNode != 0) {
					s_nodeMapped.add(sustrateNode);
					v_nodeMapped.add(virtualNode);
				}
				else {
					
					//System.out.println("No se ha podido mapear el nodo virtual " + virtualNode);
					contenido += "Se ha rechazado la solicitud " + request + " por falta de recursos\n";
					
					//Se retorna a los valores iniciales
					for (Map.Entry<Integer, Integer> cpuNode : cpuNodeBackup.entrySet()) {
						
						cpuNodeSON.put(cpuNode.getKey(), cpuNode.getValue());
					}
					
					for (Map.Entry<Integer, ArrayList<String[]>> VNodeAssignmentTo : VNodeAssignmentToSONnode.entrySet()) {
						
						ArrayList<String[]> aux1 = new ArrayList<String[]>();
						ArrayList<String[]> aux2 = new ArrayList<String[]>();
						
						aux2 = VNodeAssignmentTo.getValue();
						
						String [] aux7 = null;
						String [] aux8 = null;
						
						for (int j = 0; j < aux2.size(); j++) {
							
							aux7 = aux2.get(j);
							aux8 = new String[aux7.length];

							for (int k = 0; k < aux7.length; k++) {
								
								aux8[k] = aux7[k];
							}
							
							aux1.add(aux8);
						}
						
						VNodeAssignmentToSONnodeBackup.put(VNodeAssignmentTo.getKey(), aux1);
					}
					
					for (Map.Entry<String, int[]> vLinkAssignmentSONLink : LinkAssignmentSONLinkAux.entrySet()) {
						
						aux5 = new int[vLinkAssignmentSONLink.getValue().length];
						aux6 = vLinkAssignmentSONLink.getValue();
						
						for (int j = 0; j < aux6.length; j++) {
							
							aux5[j] = aux6[j];
						}
						
						LinkAssignmentSONLinkBackup.put(vLinkAssignmentSONLink.getKey(), aux5);
					}
					
					for (Map.Entry<String, Integer> SONLink : SONLinkUsed.entrySet()) {
						 
						SONLinkUsedBackup.put(SONLink.getKey(), SONLink.getValue());
					}
					
					for (Map.Entry<String, String[]> LinkAssignmentB : LinkAssignmentToSONLink.entrySet()) {
						
						aux3 = new String[LinkAssignmentB.getValue().length];
						aux4 = LinkAssignmentB.getValue();
						
						for (int j = 0; j < aux4.length; j++) {
							
							aux3[j] = aux4[j];
						}
						
						LinkAssignmentBackup.put(LinkAssignmentB.getKey(), aux3);
					}
					
					for (Map.Entry<String, String[]> vLinkAssignmentB : VirtualLinkAssignmentToSONLink.entrySet()) {
						
						aux3 = new String[vLinkAssignmentB.getValue().length];
						aux4 = vLinkAssignmentB.getValue();
						
						for (int j = 0; j < aux4.length; j++) {
							
							aux3[j] = aux4[j];
						}
						
						virtualLinkAssignmentBackup.put(vLinkAssignmentB.getKey(), aux3);
					}
					
					for (Map.Entry<Integer, Integer> Jump : JumpCountAux.entrySet()) {
						 
						JumpCount.put(Jump.getKey(), Jump.getValue());
					}
					
					s_nodeMapped.clear();
					v_nodeMapped.clear();
					contenidoAux = "";
					
					pathsByRequest.put(requestNumber, null);
										
					requestRejectCount += 1;
					requestProtectionRejectCount +=1;
					
					break;
				}
				
				//Se pone a cero el recurso para el nodo fisico mapeado
				cpuNodeSONAux.put(sustrateNode, 0);
				
				newCpuAvailable = updateTreeNodeVirtualAssignmentToSustrateNode(sustrateNode, virtualNode, cpuNodeSON.get(sustrateNode), 
						VNodeAssignmentToSONnodeBackup, requestNumber, cpuRequest.get(virtualNode));
				
				//Se actualiza el cpu disponible
				cpuNodeSON.put(sustrateNode, newCpuAvailable);
				
				cpuNodeVORAux.put(virtualNode, 0);
				
				//Se obtienen los vecinos virtuales del nodo virtual actual
				ArrayList<Integer> neighborsOfVirtualNodeSelected = closeness.getNeighbors(virtualNode, VOR);
				
				flag = 0;

				for (int k = 0; k < neighborsOfVirtualNodeSelected.size(); k++) {
													
					//Se verifica que el nodo virtual actual tenga un vecino mapeado para obtener las rutas fisicas
					if(v_nodeMapped.contains(neighborsOfVirtualNodeSelected.get(k))) {
						
						ArrayList<Integer> P = new ArrayList<>();
						
						//Se obtiene el nodo fisico mapeado para el nodo virtual vecino
						int v_nodeMappedPosition = v_nodeMapped.indexOf(neighborsOfVirtualNodeSelected.get(k));
												
						distanceDijkstra = DijkstrasAlgorithm.dijkstra(prunetSON,sustrateNode, s_nodeMapped.get(v_nodeMappedPosition));
	
						//Se obtiene la ruta física y la distancia
						if(distanceDijkstra!=null) {
							for (Map.Entry<Integer, ArrayList<Integer>> pathDistance : distanceDijkstra.entrySet()) {
								
								ArrayList<Integer> aux = pathDistance.getValue();
								
								for (int j = 0; j < aux.size(); j++) {
									
									int aux1 = aux.get(j);
									P.add(aux1);
								}
								
							}
						}
						
						if(P.size()!=0) {
							
							flag = 0;
							
							//Se actualiza el uso de enlaces
							linksUsedUpdates = updateLinkUsed(P, LinkAssignmentBackup, requestNumber,SONLinkUsedBackup, SONUsedForRequest);
							
							for(Map.Entry<Map<String, Integer>, Map<String, Integer>> used: linksUsedUpdates.entrySet()) {
								
								updateSONUsed = used.getKey();
								SONUsedForRequest = used.getValue();
							}
							
							SONLinkUsedBackup = updateSONUsed;
														
							//Se mapean los enlaces
							Map<Map<String, int []>, Map<Map<String, String[]>,Map<String, String[]>>> updatesLinks = updateLinkAssignmentToSONList(P, LinkAssignmentSONLinkBackup, bandwithRequest, requestNumber, virtualNode, 
									neighborsOfVirtualNodeSelected.get(k), LinkAssignmentBackup, virtualLinkAssignmentBackup);
							
							if(updatesLinks != null) {
							
								for(Map.Entry<Map<String, int []>, Map<Map<String, String[]>,Map<String, String[]>>> updates: updatesLinks.entrySet()) {
									
									for(Map.Entry<String, int []> aux: updates.getKey().entrySet()) {
										
										int[] aux1 = aux.getValue();
										int[] aux2 = new int[aux1.length];
										
										for (int j = 0; j < aux1.length; j++) {
											
											aux2[j] = aux1 [j];
										}

										LinkAssignmentSONLinkBackup.put(aux.getKey(), aux2);
									}
									
									for(Map.Entry<Map<String, String[]>,Map<String, String[]>> assignments: updates.getValue().entrySet()){
										
										for(Map.Entry<String, String[]> aux1: assignments.getKey().entrySet()){

											String[] aux2 = aux1.getValue();
											String[] aux7 = new String[aux2.length];
											
											for (int j = 0; j < aux2.length; j++) {
												
												aux7[j] = aux2[j];
											}
											LinkAssignmentBackup.put(aux1.getKey(), aux7);
										}
										
										for(Map.Entry<String, String[]> aux2: assignments.getValue().entrySet()){

											String[] aux7 = aux2.getValue();
											String[] aux8 = new String[aux7.length];
											
											for (int j = 0; j < aux7.length; j++) {
												
												aux8[j] = aux7[j];
											}
											
											virtualLinkAssignmentBackup.put(aux2.getKey(), aux8);
										}
									}
									
								}
								
								//Se poda los enlaces sin B subportadoras consecutivas sin uso
								prunetSONWithLinks = getPrunetSON(nodeSizeInSON, LinkAssignmentSONLinkBackup, bandwithRequest);
								
								contenidoAux += "Desde el nodo físico " + sustrateNode + " al " + s_nodeMapped.get(v_nodeMappedPosition) + " se llega por los nodos físicos" + P + "\n";

								//pathsForPrimaryRequest.put(requestNumber, );
							}else {
								flag=1;
							}
							
							//Agregar la ruta fisica al Map de rutas
							String pathLink = sustrateNode + "-" + s_nodeMapped.get(v_nodeMappedPosition);
							allPaths.put(pathLink, P); 
							pathsAux.add(P);
						}else {
							flag=1;
						}
						
						if(flag==1) {
							
							//System.out.println("No se ha podido mapear el enlace virtual " + virtualNode + " - " + neighborsOfVirtualNodeSelected.get(k) + "\n");
							contenido += "Se ha rechazado la solicitud " + requestNumber + " por falta de recursos\n";
												
							break;
						}
						
					}
					
					
					//pathsByRequest.put(requestNumber, pathsAux);
					
				}
				
				if(flag==1) {
					
					//Se retorna a los valores iniciales
					SONUsedForRequest.clear();
					
					for (Map.Entry<Integer, Integer> cpuNode : cpuNodeBackup.entrySet()) {
						
						cpuNodeSON.put(cpuNode.getKey(), cpuNode.getValue());
					}
					
					for (Map.Entry<Integer, ArrayList<String[]>> VNodeAssignmentTo : VNodeAssignmentToSONnode.entrySet()) {
						
						ArrayList<String[]> aux1 = new ArrayList<String[]>();
						ArrayList<String[]> aux2 = new ArrayList<String[]>();
						
						aux2 = VNodeAssignmentTo.getValue();
						
						String [] aux7 = null;
						String [] aux8 = null;
						
						for (int j = 0; j < aux2.size(); j++) {
							
							aux7 = aux2.get(j);
							aux8 = new String[aux7.length];

							for (int k = 0; k < aux7.length; k++) {
								
								aux8[k] = aux7[k];
							}
							
							aux1.add(aux8);
						}
						
						VNodeAssignmentToSONnodeBackup.put(VNodeAssignmentTo.getKey(), aux1);
					}
					
					for (Map.Entry<String, int[]> vLinkAssignmentSONLink : LinkAssignmentSONLinkAux.entrySet()) {
						
						aux5 = new int[vLinkAssignmentSONLink.getValue().length];
						aux6 = vLinkAssignmentSONLink.getValue();
						
						for (int j = 0; j < aux6.length; j++) {
							
							aux5[j] = aux6[j];
						}
						
						LinkAssignmentSONLinkBackup.put(vLinkAssignmentSONLink.getKey(), aux5);
					}
					
					for (Map.Entry<String, Integer> SONLink : SONLinkUsed.entrySet()) {
						 
						SONLinkUsedBackup.put(SONLink.getKey(), SONLink.getValue());
					}
					
					for (Map.Entry<String, String[]> LinkAssignmentB : LinkAssignmentToSONLink.entrySet()) {
						
						aux3 = new String[LinkAssignmentB.getValue().length];
						aux4 = LinkAssignmentB.getValue();
						
						for (int j = 0; j < aux4.length; j++) {
							
							aux3[j] = aux4[j];
						}
						
						LinkAssignmentBackup.put(LinkAssignmentB.getKey(), aux3);
					}
					
					for (Map.Entry<String, String[]> vLinkAssignmentB : VirtualLinkAssignmentToSONLink.entrySet()) {
						
						aux3 = new String[vLinkAssignmentB.getValue().length];
						aux4 = vLinkAssignmentB.getValue();
						
						for (int j = 0; j < aux4.length; j++) {
							
							aux3[j] = aux4[j];
						}
						
						virtualLinkAssignmentBackup.put(vLinkAssignmentB.getKey(), aux3);
					}
					
					for (Map.Entry<Integer, Integer> Jump : JumpCountAux.entrySet()) {
						 
						JumpCount.put(Jump.getKey(), Jump.getValue());
					}

					s_nodeMapped.clear();
					v_nodeMapped.clear();
					allPaths.clear();
					contenidoAux = "";
					pathsByRequest.put(requestNumber, null);
										
					requestRejectCount += 1;
					requestProtectionRejectCount +=1;
					
					//El uso de enlaces para la protección se coloca a cero
					for (Map.Entry<String, Integer> linksUsed : SONUsedForRequest.entrySet()) {
						
						String link = linksUsed.getKey();
						
						SONUsedForRequest.put(link, 0);
					}
										
					break;
				}

			}
			
			
			int sum = 0;
			
			for (String key : SONUsedForRequest.keySet()) {
				
				sum = sum + (SONUsedForRequest.get(key));
			} 
			
			//El uso de enlaces para la protección se coloca a cero
			for (Map.Entry<String, Integer> linksUsed : SONUsedForRequest.entrySet()) {
				
				String link = linksUsed.getKey();
				
				SONUsedForRequest.put(link, 0);
			}
			
			for (Map.Entry<Integer, Integer> Jump : JumpCount.entrySet()) {
				 
				JumpCountAux.put(Jump.getKey(), Jump.getValue());
			}
			
			for (Map.Entry<String, String[]> LinkAssignmentB : LinkAssignmentBackup.entrySet()) {
				
				aux3 = new String[LinkAssignmentB.getValue().length];
				aux4 = LinkAssignmentB.getValue();
				
				for (int k = 0; k < aux4.length; k++) {
					
					aux3[k] = aux4[k];
				}
				
				LinkAssignmentToSONLink.put(LinkAssignmentB.getKey(), aux3);
			}
			
			for (Map.Entry<Integer, ArrayList<String[]>> VNodeAssignmentTo : VNodeAssignmentToSONnodeBackup.entrySet()) {
				
				ArrayList<String[]> aux1 = new ArrayList<String[]>();
				ArrayList<String[]> aux2 = new ArrayList<String[]>();
				
				aux2 = VNodeAssignmentTo.getValue();
				
				String [] aux7 = null;
				String [] aux8 = null;
				
				for (int j = 0; j < aux2.size(); j++) {
					
					aux7 = aux2.get(j);
					aux8 = new String[aux7.length];

					for (int k = 0; k < aux7.length; k++) {
						
						aux8[k] = aux7[k];
					}
					
					aux1.add(aux8);
				}
				
				VNodeAssignmentToSONnode.put(VNodeAssignmentTo.getKey(), aux1);
			}
			
			for (Map.Entry<String, int[]> vLinkAssignmentSONLink : LinkAssignmentSONLinkBackup.entrySet()) {
				
				aux5 = new int[vLinkAssignmentSONLink.getValue().length];
				aux6 = vLinkAssignmentSONLink.getValue();
				
				for (int j = 0; j < aux6.length; j++) {
					
					aux5[j] = aux6[j];
				}
				
				LinkAssignmentSONLinkAux.put(vLinkAssignmentSONLink.getKey(), aux5);
			}
			
			for (Map.Entry<String, Integer> SONLink : SONLinkUsedBackup.entrySet()) {
				 
				SONLinkUsed.put(SONLink.getKey(), SONLink.getValue());
			}
			
			for (Map.Entry<String, String[]> vLinkAssignmentB : virtualLinkAssignmentBackup.entrySet()) {
				
				aux3 = new String[vLinkAssignmentB.getValue().length];
				aux4 = vLinkAssignmentB.getValue();
				
				for (int j = 0; j < aux4.length; j++) {
					
					aux3[j] = aux4[j];
				}
				
				VirtualLinkAssignmentToSONLink.put(vLinkAssignmentB.getKey(), aux3);
			}
			
			int costPrimary = bandwithRequest*sum;
			
			if(costPrimary!=0) {
				contenidoAux += "El costo B de la solicitud " + requestNumber + " es " + costPrimary + "\n";
			}else {
				contenido +="";
			}
			
			contenido+= contenidoAux;
			contenidoAux= "";

			sum = 0;
			
			/**
			 * 	Protección de cada solicitud
			 */
			
			if(flag==0 && !s_nodeMapped.isEmpty()) {
				
				ArrayList<ArrayList<Integer>> pathsForCurrentProtection = new ArrayList<>();
			
				//El uso de enlaces para la protección se coloca a cero
				for (Map.Entry<String, Integer> linksUsed : SONUsedForRequest.entrySet()) {
					
					String link = linksUsed.getKey();
					
					SONUsedForRequest.put(link, 0);
				}
				
				Map<String, ArrayList<Integer>> subgraph = new LinkedHashMap<>();
	
				//Se obtiene la posición del origen mapeado
				int rootPosition = v_nodeMapped.indexOf(root);
				
				//Se busca donde esta mapeado el origen
				int sustrateRoot = s_nodeMapped.get(rootPosition);
			
				contenido+= "Arbol de protección\n";
				
				SubgraphBasedProtection subgraphProtection = new SubgraphBasedProtection();
				
				subgraph = subgraphProtection.getSubgraph(allPaths, sustrateRoot, prunetSON, VOR, linkSizeSON);
				ArrayList<ArrayList<Integer>> newPath = new ArrayList<>();

				for (Map.Entry<String, ArrayList<Integer>> path: subgraph.entrySet()) {
					
					String key = path.getKey();
					String [] aux2 = key.split("-");
					int nodo1 = Integer.parseInt(aux2[0]);
					int nodo2 = Integer.parseInt(aux2[1]);
					
					ArrayList<Integer> paths = new ArrayList<>();
					paths.add(nodo1);
					paths.add(nodo2);
					
					
					newPath.add(paths);
					
				}
				pathsByRequest.put(requestNumber, newPath);

				
				int pathProtectionPosition = 0;
				
				//Se recorre todos los enlaces de protección para realizar el mapeo
				for (Map.Entry<String, ArrayList<Integer>> linksAux: subgraph.entrySet()) {
						
					String link = linksAux.getKey();
					ArrayList<Integer> P = linksAux.getValue();
					
					int flag1 = 0;
					
					if(P.size()!=0) {
						
						//La protección ya esta incluida en los enlaces primarios
						if(P.contains(-1)) {
							flag1 = 2;
						}else {
							
							flag1 = 0;
												
							//Se actualiza el uso de enlaces
							linksUsedUpdates = updateLinkProtectionUsed(pathProtectionPosition, pathsByRequest, P, pathsForCurrentProtection, SONLinkUsedBackup, SONUsedForRequest, pathsForProtection, requestNumber);

							for(Map.Entry<Map<String, Integer>, Map<String, Integer>> used: linksUsedUpdates.entrySet()) {
								
								updateSONUsed = used.getKey();
								SONUsedForRequest = used.getValue();
							}
							
							SONLinkUsedBackup = updateSONUsed;
												
							//Se mapean los enlaces
							Map<Map<String, int []>, Map<Map<String, String[]>,Map<String, String[]>>> updatesLinks = updateLinkAssignmentToProtection(pathProtectionPosition, pathsByRequest, pathsForProtection, P, LinkAssignmentSONLinkBackup, bandwithRequest, requestNumber, link, LinkAssignmentBackup, linkAssignmentProtectionBackup);
							
							
							if(updatesLinks != null) {
													
								for(Map.Entry<Map<String, int []>, Map<Map<String, String[]>,Map<String, String[]>>> updates: updatesLinks.entrySet()) {
									
									for(Map.Entry<String, int []> aux2: updates.getKey().entrySet()) {
										
										int[] aux7 = aux2.getValue();
										int[] aux8 = new int[aux7.length];
										
										for (int k = 0; k < aux7.length; k++) {
											
											aux8[k] = aux7[k];
										}
	
										LinkAssignmentSONLinkBackup.put(aux2.getKey(), aux8);
									}
									
									for(Map.Entry<Map<String, String[]>,Map<String, String[]>> assignments: updates.getValue().entrySet()){
										
										for(Map.Entry<String, String[]> aux2: assignments.getKey().entrySet()){
	
											String[] aux7 = aux2.getValue();
											String[] aux8 = new String[aux7.length];
											
											for (int k = 0; k < aux7.length; k++) {
												
												aux8[k] = aux7[k];
											}
											LinkAssignmentBackup.put(aux2.getKey(), aux8);
										}
										
										for(Map.Entry<String, String[]> aux2: assignments.getValue().entrySet()){
	
											String[] aux7 = aux2.getValue();
											String[] aux8 = new String[aux7.length];
											
											for (int k = 0; k < aux7.length; k++) {
												
												aux8[k] = aux7[k];
											}
											
											linkAssignmentProtectionBackup.put(aux2.getKey(), aux8);
										}
									}
									
								}
								
								//Se poda los enlaces sin B subportadoras consecutivas sin uso
								prunetSONWithLinks = getPrunetSON(nodeSizeInSON, LinkAssignmentSONLinkBackup, bandwithRequest);
								
								contenidoAux += "El enlace " + link + " esta protegido por la ruta " + P + "\n";
								
								pathsForCurrentProtection.add(P);
							
							}else {
								flag1=1;
							}
						}
					}else{
						
						flag1=1;
					}
					
					if(flag1==1) {
						
						//System.out.println("No se ha podido mapear el enlace virtual " + link + "\n");
						contenido += "Se ha rechazado la solicitud de protección " + requestNumber + " por falta de recursos\n";
						
						//Se retorna a los valores iniciales
						for (Map.Entry<String, Integer> SONLink : SONLinkUsed.entrySet()) {
							 
							SONLinkUsedBackup.put(SONLink.getKey(), SONLink.getValue());
						}
						
						
						
						for (Map.Entry<String, String[]> LinkAssignmentB : LinkAssignmentToSONLink.entrySet()) {
							
							aux3 = new String[LinkAssignmentB.getValue().length];
							aux4 = LinkAssignmentB.getValue();
							
							for (int k = 0; k < aux4.length; k++) {
								
								aux3[k] = aux4[k];
							}
							
							LinkAssignmentBackup.put(LinkAssignmentB.getKey(), aux3);
						}
						
						for (Map.Entry<String, String[]> LinkAssignmentB : LinkAssignmentToSONLinkProtection.entrySet()) {
							
							aux3 = new String[LinkAssignmentB.getValue().length];
							aux4 = LinkAssignmentB.getValue();
							
							for (int k = 0; k < aux4.length; k++) {
								
								aux3[k] = aux4[k];
							}
							
							linkAssignmentProtectionBackup.put(LinkAssignmentB.getKey(), aux3);
						}
						
						for (Map.Entry<String, int[]> vLinkAssignmentSONLink : LinkAssignmentSONLinkAux.entrySet()) {
							
							aux5 = new int[vLinkAssignmentSONLink.getValue().length];
							aux6 = vLinkAssignmentSONLink.getValue();
							
							for (int k = 0; k < aux6.length; k++) {
								
								aux5[k] = aux6[k];
							}
							
							LinkAssignmentSONLinkBackup.put(vLinkAssignmentSONLink.getKey(), aux5);
						}
						
						for (Map.Entry<Integer, Integer> Jump : JumpCountAux.entrySet()) {
							 
							JumpCount.put(Jump.getKey(), Jump.getValue());
						}
						
						contenidoAux = "";
											
						requestProtectionRejectCount += 1;
						
						//El uso de enlaces para la protección se coloca a cero
						for (Map.Entry<String, Integer> linksUsed : SONUsedForRequest.entrySet()) {
							
							String link1 = linksUsed.getKey();
							
							SONUsedForRequest.put(link1, 0);
						}
											
						break;
											
					}	
					
					pathProtectionPosition++;
										
				}
				pathsForProtection.put(requestNumber, pathsForCurrentProtection);

			}
			
			/**
			 * Fin de la protección
			 */
			
			contenido+= contenidoAux;
			contenidoAux= "";
			
			for (Map.Entry<Integer, Integer> cpuNode : cpuNodeSON.entrySet()) {
				
				cpuNodeBackup.put(cpuNode.getKey(), cpuNode.getValue());
			}
			
			for (Map.Entry<Integer, ArrayList<String[]>> VNodeAssignmentTo : VNodeAssignmentToSONnodeBackup.entrySet()) {
				
				ArrayList<String[]> aux1 = new ArrayList<String[]>();
				ArrayList<String[]> aux2 = new ArrayList<String[]>();
				
				aux2 = VNodeAssignmentTo.getValue();
				
				String [] aux7 = null;
				String [] aux8 = null;
				
				for (int j = 0; j < aux2.size(); j++) {
					
					aux7 = aux2.get(j);
					aux8 = new String[aux7.length];

					for (int k = 0; k < aux7.length; k++) {
						
						aux8[k] = aux7[k];
					}
					
					aux1.add(aux8);
				}
				
				VNodeAssignmentToSONnode.put(VNodeAssignmentTo.getKey(), aux1);
			}
			
			for (Map.Entry<String, int[]> vLinkAssignmentSONLink : LinkAssignmentSONLinkBackup.entrySet()) {
				
				aux5 = new int[vLinkAssignmentSONLink.getValue().length];
				aux6 = vLinkAssignmentSONLink.getValue();
				
				for (int j = 0; j < aux6.length; j++) {
					
					aux5[j] = aux6[j];
				}
				
				LinkAssignmentSONLinkAux.put(vLinkAssignmentSONLink.getKey(), aux5);
			}
			
			for (Map.Entry<String, Integer> SONLink : SONLinkUsedBackup.entrySet()) {
				 
				SONLinkUsed.put(SONLink.getKey(), SONLink.getValue());
			}
			
			for (Map.Entry<String, String[]> LinkAssignmentB : LinkAssignmentBackup.entrySet()) {
				
				aux3 = new String[LinkAssignmentB.getValue().length];
				aux4 = LinkAssignmentB.getValue();
				
				for (int j = 0; j < aux4.length; j++) {
					
					aux3[j] = aux4[j];
				}
				
				LinkAssignmentToSONLink.put(LinkAssignmentB.getKey(), aux3);
			}
			
			for (Map.Entry<String, String[]> LinkAssignmentB : linkAssignmentProtectionBackup.entrySet()) {
				
				aux3 = new String[LinkAssignmentB.getValue().length];
				aux4 = LinkAssignmentB.getValue();
				
				for (int j = 0; j < aux4.length; j++) {
					
					aux3[j] = aux4[j];
				}
				
				LinkAssignmentToSONLinkProtection.put(LinkAssignmentB.getKey(), aux3);
			}
			
			for (Map.Entry<String, String[]> vLinkAssignmentB : virtualLinkAssignmentBackup.entrySet()) {
				
				aux3 = new String[vLinkAssignmentB.getValue().length];
				aux4 = vLinkAssignmentB.getValue();
				
				for (int j = 0; j < aux4.length; j++) {
					
					aux3[j] = aux4[j];
				}
				
				VirtualLinkAssignmentToSONLink.put(vLinkAssignmentB.getKey(), aux3);
			}
			
			for (Map.Entry<Integer, Integer> Jump : JumpCount.entrySet()) {
				 
				JumpCountAux.put(Jump.getKey(), Jump.getValue());
			}
			
			for (String key : SONUsedForRequest.keySet()) {
				
				
				sum = sum + (SONUsedForRequest.get(key));
				
			}
			
			int costProtection = bandwithRequest*sum;
			
			B[i] = costPrimary + costProtection;
			
			if(costProtection!=0) {
				contenido += "El costo B del arbol de protección es " + costProtection + "\n";
			}else {
				contenido +="";
			}
			
			contenido += "El costo B del arbol primario y de protección de la solicitud " + requestNumber + " es " + B[i] + "\n";


			for (int k = 0; k < v_nodeMapped.size(); k++) {
				
				contenido += "El nodo virtual " + v_nodeMapped.get(k) + " esta mapeado en el nodo fisico " + s_nodeMapped.get(k) + "\n";
				
			}
			
			contenido += "\n";			

			v_nodeMapped.clear();
			s_nodeMapped.clear(); 
			SONUsedForRequest.clear();

			
		}
		
		for (Map.Entry<String, Integer> linksUsed : SONLinkUsed.entrySet()) {
			
			contenido += "El link " + linksUsed.getKey() + " es usado " + linksUsed.getValue() + " veces\n";
			
		}
		contenido += "\n";

		
		for (Map.Entry<String, String[]> tree : LinkAssignmentToSONLink.entrySet()) {
			
			contenido += "El link de sustrato " + tree.getKey() + " es usado por: \n";
			
			String[] aux = tree.getValue();
			
			for (int j = 0; j < aux.length; j++) {
				
					String aux2 = aux[j];
					
					if(aux2==null) {
					
						contenido += "Posición" + j + ": null\n";
					}else{
						
						String[] aux3 = aux2.split(",");
					
						contenido +=  "Posición" + j + ": Solicitud " + aux3[0] + ", enlace virtual " + aux3[1] + "\n";
					}
				
			}
			contenido += "\n";
			
		}
		
		contenido += "\n";
		
		for (Map.Entry<Integer, ArrayList<String[]>> vNode : VNodeAssignmentToSONnode.entrySet()) {
			
			contenido += "El nodo fisico " + vNode.getKey() + " es usado por: \n";
			
			ArrayList<String[]> aux = vNode.getValue();
			
			for (int j = 0; j < aux.size(); j++) {
				
				String[] aux2 = aux.get(j);
				
				if(aux2[0]!=null) {
				
					contenido += "- La solicitud " + aux2[0] + "\n";
					contenido += "- El nodo virtual " + aux2[1] + "\n";
					contenido += "- El uso de cpu para el nodo virtual es " + aux2[2] + "\n";
					contenido += "\n";
				}
				
			}					
							
		}
		
		int totalJump = 0;
		
		for(Map.Entry<Integer, Integer> jump: JumpCount.entrySet()) {
			
			contenido += "La cantidad de saltos de la solicitud " + jump.getKey() + " es " + jump.getValue();
			contenido += "\n";
			
			totalJump = totalJump + jump.getValue();

		}
		
		contenido += "\n";
		contenido += "La cantidad total de saltos es " + totalJump;
		contenido += "\n";
		contenido += "\n";

		int sumTotal = 0;
		
		for (int l = 0; l < B.length; l++) {
			
			sumTotal += B[l];
		}
		
		contenido += "EL COSTO TOTAL DE MAPEO ES " + sumTotal + "\n";
		contenido += "\n";
		contenido += "SE HAN RECHAZADO " + requestRejectCount + " SOLICITUDES\n";
		contenido += "SE HAN RECHAZADO " + requestProtectionRejectCount + " SOLICITUDES DE PROTECCIÓN \n";

		bw.write(contenido);

        bw.close();

        ArrayList<Integer> totalCostAndRequestRejects = new ArrayList<>();
		
        totalCostAndRequestRejects.add(sumTotal);
        totalCostAndRequestRejects.add(requestRejectCount);
        totalCostAndRequestRejects.add(requestProtectionRejectCount);
        
        LinkAssignmentToSONLink.clear();
        VNodeAssignmentToSONnode.clear();
        LinkAssignmentToSONLinkProtection.clear();
        VirtualLinkAssignmentToSONLink.clear();
        SONLinkUsed.clear();
        JumpCount.clear();
        vLinkAssignment.clear();
        LinkAssignmentBackup.clear();
        linkAssignmentProtectionBackup.clear();
        virtualLinkAssignmentBackup.clear();
        VNodeAssignmentToSONnodeBackup.clear();
        SONLinkUsedBackup.clear();
        LinkAssignmentSONLinkBackup.clear();
        LinkAssignmentSONLinkAux.clear();
        JumpCountAux.clear();
        
        FilesRead.clear();
        
		return totalCostAndRequestRejects;

	}
	
	/**
	 * Poda el SON eliminando enlaces con insuficientes subportadoras  consecutivas sin uso
	 * 
	 * @param s_nodeSize
	 * @param VLinkAssignmentToSONList
	 * @param bandwithRequest
	 * @return
	 */
	public static Map<int [][], Map<String, int[]>> getPrunetSON(int s_nodeSize, Map<String, int[]> LinkAssignmentToSONList, int bandwithRequest){
		
		//Matriz auxiliar que contiene el SON podado
		Map<int [][], Map<String, int[]>> prunetSON = new LinkedHashMap<int [][], Map<String, int[]>>();
		
		int[][] SONArray = new int [s_nodeSize][s_nodeSize]; 
		
		Map<String, int[]> prunetVLinkAssignmentToSONList= new LinkedHashMap<String, int[]>();
		
		int bandwithFreeSize = 0;
		int flag = 0;
		String [] aux2 = null;
		String aux3 = null;
		String aux4 = null;
		
		for (String key : LinkAssignmentToSONList.keySet()) {
				
			int[] aux = LinkAssignmentToSONList.get(key);
			bandwithFreeSize = 0;
			flag = 0;
			
			for (int i = 0; i < aux.length; i++) {
				
				if(aux[i] == 0) {
					bandwithFreeSize = bandwithFreeSize + 1;
					if (bandwithFreeSize >= bandwithRequest) {
						
						flag=1;
					}else {
						flag=0;
					}
					
				} else {
					 bandwithFreeSize = 0;
					 flag= 0;
				}
				
				if(flag==1) {
					i=aux.length;
				}
				
			}
			
			if(flag==1) {
				
				prunetVLinkAssignmentToSONList.put(key, aux);
			}
			
		}
				
		//Se pone a cero todos los elementos de la matriz
		for (int i = 0; i < s_nodeSize; i++) {
			for (int j = 0; j < s_nodeSize; j++) {
				
				SONArray[i][j]= 0;
				
			}
			
		}
		
		//Se pone a uno los nodos que tienen enlaces
		for(String key : prunetVLinkAssignmentToSONList.keySet()) {
			
			aux2 = key.split("-");
			
			aux3 = aux2[0];
			aux4 = aux2[1];
			
			SONArray[Integer.parseInt(aux3)-1][Integer.parseInt(aux4)-1]= 1;
			SONArray[Integer.parseInt(aux4)-1][Integer.parseInt(aux3)-1]= 1;
		}
		
		prunetSON.put(SONArray, prunetVLinkAssignmentToSONList);
		
		return prunetSON;
		
	}
	
	/**
	 * Actualiza el uso de enlaces
	 * 
	 * @param path
	 * @param linkAssignment
	 * @return
	 */
	public static Map<Map<String, Integer>, Map<String, Integer>> updateLinkUsed(ArrayList<Integer> path, Map<String, String[]> LinkAssignmentBackup, int requestNumber, 
				Map<String, Integer> SONLinkUsedAux, Map<String, Integer> linkUsedForRequest){
		
		int node1;
		int node2;
		String link = "";
		
		Map<Map<String, Integer>, Map<String, Integer>> linksUsedUpdates = new LinkedHashMap<>();
		
		//Se recorre la ruta fisica
		for (int i = 0; i < path.size()-1; i++) {
			 
			node1 = path.get(i);
			node2 = path.get(i+1);
			
			//Se intercambian los nodos de los enlaces para que coincidan con los enlaces guardados
			//Por ejemplo, el enlaces 2-1, se cambia por 1-2
			if(node1>node2) {
				
				//Se obtiene el enlace
				link = Integer.toString(node2) + '-' + Integer.toString(node1);
			}else {
			
				//Se obtiene el enlace
				link = Integer.toString(node1) + '-' + Integer.toString(node2);	
			}
			
			String [] aux1 = LinkAssignmentBackup.get(link);
			
			boolean aux2 = false;
			
			for (int j = 0; j < aux1.length; j++) {
			
				if(aux1[j]!=null) {
					String[] aux3 = aux1[j].split(",");
					if(Integer.parseInt(aux3[0])==requestNumber) {
						aux2 = true;
						break;
					}
				}
			}
			
			if(aux2==false) {
							
				//Se obtiene el uso viejo del enlace
				int oldUsed = SONLinkUsedAux.get(link);
				
				//Se obtiene el uso viejo del enlace de la solicitud actual
				int oldUsedForRequest = linkUsedForRequest.get(link);
				
				//Se actualiza el nuevo uso
				int newUsed = oldUsed + 1;
				int newUsedForRequest = oldUsedForRequest + 1;
				
				SONLinkUsedAux.put(link, newUsed);
				linkUsedForRequest.put(link, newUsedForRequest);
			}
		
		}
		
		linksUsedUpdates.put(SONLinkUsedAux, linkUsedForRequest);
		
		return linksUsedUpdates;
			
	}
	
	/**
	 * Se actualiza el uso de subportadoras en los enlaces. 
	 * 
	 * @param path
	 * @param linkAssignment
	 * @param bandwithSize
	 * @return
	 */
	public static Map<Map<String, int []>, Map<Map<String, String[]>, Map<String, String[]>>> updateLinkAssignmentToSONList(ArrayList<Integer> path, Map<String, int []> linkAssignment, 
																	int bandwithSize, int requestNumber, int virtualNode, int neighbor, Map<String, String[]> LinkAssignmentBackup, Map<String, String[]> assignmentBackup){
		
		int node1;
		int node2;
		String link = "";
		
		Integer position = null;
		
		Map<Map<String, int []>, Map<Map<String, String[]>,Map<String, String[]>>> update = new LinkedHashMap<Map<String, int []>, Map<Map<String, String[]>,Map<String, String[]>>>();
		Map<Map<String, String[]>,Map<String, String[]>> updateAssignments = new LinkedHashMap<>();
		
		int flag = 0;
		
		node1 = path.get(0);
		node2 = path.get(1);
		
		//Se intercambian los nodos de los enlaces para que coincidan con los enlaces guardados
		//Por ejemplo, el enlaces 2-1, se cambia por 1-2
		if(node1>node2) {
			
			//Se obtiene el enlace
			link = Integer.toString(node2) + '-' + Integer.toString(node1);
		}else {
		
			//Se obtiene el enlace
			link = Integer.toString(node1) + '-' + Integer.toString(node2);	
		}			
		
		//Se toma el primer arreglo para obtener sus dimensiones
		int [] aux = new int[linkAssignment.get(link).length];
		
		for (int i = 0; i < path.size()-1; i++) {
			 
			node1 = path.get(i);
			node2 = path.get(i+1);
			
			//Se intercambian los nodos de los enlaces para que coincidan con los enlaces guardados
			//Por ejemplo, el enlaces 2-1, se cambia por 1-2
			if(node1>node2) {
				
				//Se obtiene el enlace
				link = Integer.toString(node2) + '-' + Integer.toString(node1);
			}else {
			
				//Se obtiene el enlace
				link = Integer.toString(node1) + '-' + Integer.toString(node2);	
			}
			
			//restricción para que no haya redundancia
			String [] aux1 = LinkAssignmentBackup.get(link);
			
			boolean aux2 = false;
			
			for (int j = 0; j < aux1.length; j++) {
			
				if(aux1[j]!=null) {
					String[] aux3 = aux1[j].split(",");
					if(Integer.parseInt(aux3[0])==requestNumber) {
						aux2 = true;
						break;
					}
				}
			}
			
			if(aux2==false) {
				int [] assignment = linkAssignment.get(link);
			
				for (int j = 0; j < aux.length; j++) {
				
					aux[j] = aux[j] + assignment[j];
				}
			}
		}
			
		//Se recorre el vector auxiliar de enlaces asignados
		for (int j = 0; j <= aux.length - bandwithSize; j++) {
			
			if(aux[j]==0) {
				
				position = j;
				
				for (int k = position; k < bandwithSize+position; k++) {
					
					if(aux[k]==0) {
						flag = 1;
					}
					else {
						flag = 0;
						k=bandwithSize+position;
						position = null;
						break;
					}
					
					
				}
				
				if(flag==1) {
					j = aux.length - bandwithSize + 1;
				}
				
			}
			
		}
			
		//Se pone a 1 los enlaces asignados
		if (position!=null) {
			
			for (int i = 0; i < path.size()-1; i++) {
				 
				node1 = path.get(i);
				node2 = path.get(i+1);
				
				//Se intercambian los nodos de los enlaces para que coincidan con los enlaces guardados
				//Por ejemplo, el enlaces 2-1, se cambia por 1-2
				if(node1>node2) {
					
					//Se obtiene el enlace
					link = Integer.toString(node2) + '-' + Integer.toString(node1);
				}else {
				
					//Se obtiene el enlace
					link = Integer.toString(node1) + '-' + Integer.toString(node2);	
				}			
				
				int [] assignment = linkAssignment.get(link);
				
				//restricción para que no haya redundancia
				String [] aux1 = LinkAssignmentBackup.get(link);
				
				boolean aux2 = false;
				
				for (int j = 0; j < aux1.length; j++) {
				
					if(aux1[j]!=null) {
						String[] aux3 = aux1[j].split(",");
						if(Integer.parseInt(aux3[0])==requestNumber) {
							aux2 = true;
							break;
						}
					}
				}
				
				//Evitar la redundancia
				if(aux2==false) {
			
					for (int j = position; j < bandwithSize + position; j++) {
					
						assignment[j] =1;
						
						String[] requestList = LinkAssignmentBackup.get(link);
						String[] requestListVirtual = assignmentBackup.get(link);
						
						String information = Integer.toString(requestNumber) + ',' + Integer.toString(virtualNode) + '-' + Integer.toString(neighbor); 
						
						requestList[j] = information;
						requestListVirtual[j]=information;
						
						LinkAssignmentBackup.put(link, requestList);
						assignmentBackup.put(link, requestListVirtual);
	
					}
				}
				
				//Se incrementa la cantidad de saltos
				int jumpCountAux = JumpCount.get(requestNumber);
				jumpCountAux = jumpCountAux + 1;
				JumpCount.put(requestNumber, jumpCountAux);

			}
			
			updateAssignments.put(LinkAssignmentBackup, assignmentBackup);
			
			update.put(linkAssignment, updateAssignments);
			return update;

		}
		
		else {
			//System.out.println("No se ha podido encontrar una ruta optima");
			return null;
		}
		
				
	}
	
	
	private static int updateTreeNodeVirtualAssignmentToSustrateNode(	int sustrateNode, int virtualNode, int cpuAvailable, Map<Integer, ArrayList<String[]>> virtualNodeAssignment,
																								int requestNumber, int cpuRequest){
		
		ArrayList<String[]> sustrateNodeInformation = new ArrayList<String[]>();
		sustrateNodeInformation = virtualNodeAssignment.get(sustrateNode);
		
		int newCpuAvailable= cpuAvailable - cpuRequest;
		
		String requestNumberString = Integer.toString(requestNumber);
		String virtualNodeString = Integer.toString(virtualNode);
		String cpuRequestString = Integer.toString(cpuRequest);
		
		String[] virtualNodeInformation = {requestNumberString, virtualNodeString, cpuRequestString}; 
		
		if(sustrateNodeInformation.contains(null)) {
			sustrateNodeInformation.remove(null);
			sustrateNodeInformation.add(virtualNodeInformation);
		}
		else {
			sustrateNodeInformation.add(virtualNodeInformation);
		}
						
		virtualNodeAssignment.put(sustrateNode, sustrateNodeInformation);
			
		return newCpuAvailable;
	}
	
	/**
	 * Se actualiza el uso de subportadoras en los enlaces de protección. 
	 * 
	 * @param path
	 * @param linkAssignment
	 * @param bandwithSize
	 * @return
	 */
	public static Map<Map<String, int []>, Map<Map<String, String[]>, Map<String, String[]>>> updateLinkAssignmentToProtection(int pathNumber, Map<Integer, ArrayList<ArrayList<Integer>>> pathsByRequest, Map<Integer, ArrayList<ArrayList<Integer>>> pathsForProtections, ArrayList<Integer> path, Map<String, int []> linkAssignment, 
																	int bandwithSize, int requestNumber, String linkProtection, Map<String, String[]> LinkAssignmentBackup, Map<String, String[]> assignmentBackup){
		
		int node1;
		int node2;
		String link = "";
		
		boolean flag1 = true;
		boolean flag2 = true;
		boolean flag4 = false;
		
		Integer position = null;
		
		ArrayList<String> equalsPaths = new ArrayList<>();

		
		Map<Map<String, int []>, Map<Map<String, String[]>,Map<String, String[]>>> update = new LinkedHashMap<Map<String, int []>, Map<Map<String, String[]>,Map<String, String[]>>>();
		Map<Map<String, String[]>,Map<String, String[]>> updateAssignments = new LinkedHashMap<>();
		
		int flag = 0;
		
		node1 = path.get(0);
		node2 = path.get(1);
		
		//Se intercambian los nodos de los enlaces para que coincidan con los enlaces guardados
		//Por ejemplo, el enlaces 2-1, se cambia por 1-2
		if(node1>node2) {
			
			//Se obtiene el enlace
			link = Integer.toString(node2) + '-' + Integer.toString(node1);
		}else {
		
			//Se obtiene el enlace
			link = Integer.toString(node1) + '-' + Integer.toString(node2);	
		}		
		
		//Se toma el primer arreglo para obtener sus dimensiones
		int [] aux = new int[linkAssignment.get(link).length];
		
		for (int i = 0; i < path.size()-1; i++) {
			 
			String linkAux = "";
			
			node1 = path.get(i);
			node2 = path.get(i+1);
			
			//Se intercambian los nodos de los enlaces para que coincidan con los enlaces guardados
			//Por ejemplo, el enlaces 2-1, se cambia por 1-2
			if(node1>node2) {
				
				//Se obtiene el enlace
				link = Integer.toString(node2) + '-' + Integer.toString(node1);
			}else {
			
				//Se obtiene el enlace
				link = Integer.toString(node1) + '-' + Integer.toString(node2);	
			}
			
			//restricción para que no haya redundancia
			String [] aux4 = null;
			aux4 = LinkAssignmentBackup.get(link);
			
			boolean flag3 = false;
			
			for (int j = 0; j < aux4.length; j++) {
			
				if(aux4[j]!=null) {
					String[] aux3 = aux4[j].split(",");
					if(Integer.parseInt(aux3[0])==requestNumber) {
						flag3 = true;
						break;
					}
				}
			}
			
			if(!pathsForProtections.isEmpty() && flag3== false) {
				//Se recorren los demas caminos de protección para encontrar caminos comunes
				for(Map.Entry<Integer, ArrayList<ArrayList<Integer>>> requestProtectionPath: pathsForProtections.entrySet()) {
					
					ArrayList<ArrayList<Integer>> requestPath = requestProtectionPath.getValue();
					int currentRequest = requestProtectionPath.getKey();
					
					int nodeAux1;
					int nodeAux2;
										
					//Se recorren los caminos de la protección actual
					for (int j = 0; j < requestPath.size(); j++) {
						
						ArrayList<Integer> currentPath = requestPath.get(j);
						
						flag1=true;
						flag2=true;
						
						
						//Se recorre el camino de la protección
						for (int k = 0; k < currentPath.size()-1; k++) {
							
							nodeAux1 = currentPath.get(k);
							nodeAux2 = currentPath.get(k+1);
							
							//Se intercambian los nodos de los enlaces para que coincidan con los enlaces guardados
							//Por ejemplo, el enlaces 2-1, se cambia por 1-2
							if(nodeAux1>nodeAux2) {
								
								//Se obtiene el enlace
								linkAux = Integer.toString(nodeAux2) + '-' + Integer.toString(nodeAux1);
							}else {
							
								//Se obtiene el enlace
								linkAux = Integer.toString(nodeAux1) + '-' + Integer.toString(nodeAux2);	
							}
							
							if(link.equals(linkAux)) {
								
								contingencyArray[requestNumber-1][currentRequest-1]=1;
								contingencyArray[currentRequest-1][requestNumber-1]=1;
								
								//Se obtiene el vector de asignaciones del enlace
								String[] linkAssignmentAux = LinkAssignmentBackup.get(link);
								
								ArrayList<Integer> a = pathsByRequest.get(requestNumber).get(pathNumber);
								ArrayList<Integer> b = pathsByRequest.get(currentRequest).get(j);
								
								boolean equal = false;
								
								//Se busca si existen enlaces compartidos
								for (int l = 0; l < a.size()-1; l++) {
									for (int m = 0; m < b.size()-1; m++) {
										
										int node3 = a.get(l);
										int node4 = b.get(m);
										int node5 = a.get(l+1);
										int node6 = b.get(m+1);
										String linkAux2 = null;
										String linkAux3 = null;

										
										if(node5>node3) {
											
											//Se obtiene el enlace
											linkAux2 = Integer.toString(node3) + '-' + Integer.toString(node5);
										}else {
										
											//Se obtiene el enlace
											linkAux2 = Integer.toString(node5) + '-' + Integer.toString(node3);	
										}
										
										if(node6>node4) {
											
											//Se obtiene el enlace
											linkAux3 = Integer.toString(node4) + '-' + Integer.toString(node6);
										}else {
										
											//Se obtiene el enlace
											linkAux3 = Integer.toString(node6) + '-' + Integer.toString(node4);	
										}
										
										if(linkAux2.equals(linkAux3)) {
												equal = true;
												break;
									
										}
										
										if(equal == true) {
											break;
										}
									}
								}
								
								if(equal==false) {
									
									flag4 = false;
									equalsPaths.add(link);
									
									//Se recorre el vector
									for (int l = 0; l < linkAssignmentAux.length; l++) {
										
										if(linkAssignmentAux[l]!=null) {
											String[] aux3 = linkAssignmentAux[l].split(",");
											if(Integer.parseInt(aux3[0])==currentRequest) {
												
												linkAssignmentAux[l] = "0," + "0";
												flag4 = true;
											}
										}
									}
									
									if(flag4==false) {
										int size = equalsPaths.size();
										equalsPaths.remove(size-1);
									}
									
									if(flag4==true) {
										flag1=false;
										break;
									}
								}else {
									flag2=false;
									break;
								}
								
							} 
						}
						if(flag1==false || flag2==false) {
							break;
						}
					}
					
					if(flag1==false) {
						break;
					}
					
				}
			} else {
				
				//restricción para que no haya redundancia
				String [] aux1 = LinkAssignmentBackup.get(link);
				
				boolean aux2 = false;
				
				for (int h = 0; h < aux1.length; h++) {
				
					if(aux1[h]!=null) {
						String[] aux3 = aux1[h].split(",");
						if(Integer.parseInt(aux3[0])==requestNumber) {
							aux2 = true;
							break;
						}
					}
				}
				
				if(aux2==false) {
					int [] assignment = linkAssignment.get(link);
				
					for (int n = 0; n < aux.length; n++) {
					
						aux[n] = aux[n] + assignment[n];
					}
				}
				
			}
			
			if(flag1==true || flag2== false || flag4 == false) {
				
				//restricción para que no haya redundancia
				String [] aux1 = LinkAssignmentBackup.get(link);
				
				boolean aux2 = false;
				
				for (int h = 0; h < aux1.length; h++) {
				
					if(aux1[h]!=null) {
						String[] aux3 = aux1[h].split(",");
						if(Integer.parseInt(aux3[0])==requestNumber) {
							aux2 = true;
							break;
						}
					}
				}
				
				if(aux2==false) {
					int [] assignment = linkAssignment.get(link);
				
					for (int n = 0; n < aux.length; n++) {
					
						aux[n] = aux[n] + assignment[n];
					}
				}
				
			}
		}
		
		if(pathsForProtections.isEmpty() || flag4==false || flag4==true || flag1 == true || flag2==false) {
		
			//Se recorre el vector auxiliar de enlaces asignados
			for (int d = 0; d <= aux.length - bandwithSize; d++) {
				
				if(aux[d]==0) {
					
					position = d;
					
					for (int g = position; g < bandwithSize+position; g++) {
						
						if(aux[g]==0) {
							flag = 1;
						}
						else {
							flag = 0;
							g=bandwithSize+position;
							position = null;
							break;
						}
						
						
					}
					
					if(flag==1) {
						d = aux.length - bandwithSize + 1;
					}
					
				}
				
			}
			
			//Se pone a 1 los enlaces asignados
			if (position!=null) {
				
				for (int c = 0; c < path.size()-1; c++) {
					 
					node1 = path.get(c);
					node2 = path.get(c+1);
					
					//Se intercambian los nodos de los enlaces para que coincidan con los enlaces guardados
					//Por ejemplo, el enlaces 2-1, se cambia por 1-2
					if(node1>node2) {
						
						//Se obtiene el enlace
						link = Integer.toString(node2) + '-' + Integer.toString(node1);
					}else {
					
						//Se obtiene el enlace
						link = Integer.toString(node1) + '-' + Integer.toString(node2);	
					}
					
					if(!equalsPaths.contains(link)) {
					
						int [] assignment = linkAssignment.get(link);
						
						//restricción para que no haya redundancia
						String [] aux5 = LinkAssignmentBackup.get(link);
						
						boolean aux4 = false;
						
						for (int b = 0; b < aux5.length; b++) {
						
							if(aux5[b]!=null) {
								String[] aux3 = aux5[b].split(",");
								if(Integer.parseInt(aux3[0])==requestNumber) {
									aux4 = true;
									break;
								}
							}
						}
						
						//Evitar la redundancia
						if(aux4==false) {
					
							for (int q = position; q < bandwithSize + position; q++) {
							
								assignment[q] =1;
								
								String[] requestList = LinkAssignmentBackup.get(link);
								String[] requestListVirtual = assignmentBackup.get(link);
								
								String information = String.valueOf(Integer.toString(requestNumber)) + ',' + linkProtection;								
								requestList[q] = information;
								requestListVirtual[q]=information;
								
								LinkAssignmentBackup.put(link, requestList);
								assignmentBackup.put(link, requestListVirtual);
			
							}
						}
						
						if(aux4==false) {
							//Se incrementa la cantidad de saltos
							int jumpCountAux = JumpCount.get(requestNumber);
							jumpCountAux = jumpCountAux + 1;
							JumpCount.put(requestNumber, jumpCountAux);
						}
					}
	
				}
				
				updateAssignments.put(LinkAssignmentBackup, assignmentBackup);
				
				update.put(linkAssignment, updateAssignments);
			}
			
			else {
				//System.out.println("No se ha podido encontrar una ruta optima");
				update.clear();
			}
		}
		
		return update;
		
	}
	
	/**
	 * Actualiza el uso de enlaces de protección
	 * 
	 * @param path
	 * @param linkAssignment
	 * @return
	 */
	public static Map<Map<String, Integer>, Map<String, Integer>> updateLinkProtectionUsed(int pathNumber, Map<Integer, ArrayList<ArrayList<Integer>>> pathsByRequest, ArrayList<Integer> path, 
			ArrayList<ArrayList<Integer>> pathsForCurrentProtection, Map<String, Integer> SONLinkUsedAux, Map<String, Integer> linkUsedForRequest,
			Map<Integer, ArrayList<ArrayList<Integer>>> pathsForProtections, int requestNumber){
		
		int node1;
		int node2;
		String link = "";
		String linkAux = "";
		
		
		Map<Map<String, Integer>, Map<String, Integer>> linksUsedUpdates = new LinkedHashMap<>();
		
		//Se recorre la ruta fisica
		for (int i = 0; i < path.size()-1; i++) {
			 
			node1 = path.get(i);
			node2 = path.get(i+1);
			
			//Se intercambian los nodos de los enlaces para que coincidan con los enlaces guardados
			//Por ejemplo, el enlaces 2-1, se cambia por 1-2
			if(node1>node2) {
				
				//Se obtiene el enlace
				link = Integer.toString(node2) + '-' + Integer.toString(node1);
			}else {
			
				//Se obtiene el enlace
				link = Integer.toString(node1) + '-' + Integer.toString(node2);	
			}
						
			boolean aux4 = false;
			boolean aux5 = false;
			boolean aux6 = false;
			
			
			//Se recorre los caminos de la solicitud actual para encontrar caminos iguales al enlace actual
			if(!pathsForCurrentProtection.isEmpty()) {
				
				for (int j = 0; j < pathsForCurrentProtection.size(); j++) {
					
					ArrayList<Integer> currentPath = pathsForCurrentProtection.get(j);
					
					for (int k = 0; k < currentPath.size()-1; k++) {
						
						node1 = currentPath.get(k);
						node2 = currentPath.get(k+1);
						
						//Se intercambian los nodos de los enlaces para que coincidan con los enlaces guardados
						//Por ejemplo, el enlaces 2-1, se cambia por 1-2
						if(node1>node2) {
							
							//Se obtiene el enlace
							linkAux = Integer.toString(node2) + '-' + Integer.toString(node1);
						}else {
						
							//Se obtiene el enlace
							linkAux = Integer.toString(node1) + '-' + Integer.toString(node2);	
						}
						
						if(link.equals(linkAux)) {
							aux5=true;
							break;
						}
					}
					
				}
				
			}
			
			
			//Se recorren los caminos de las demás solicitudes para encontrar caminos comunes con el enlace actual
			if(!pathsForProtections.isEmpty()) {
				//Se recorren los demas caminos de protección para encontrar caminos comunes
				for(Map.Entry<Integer, ArrayList<ArrayList<Integer>>> requestProtectionPath: pathsForProtections.entrySet()) {
					
					ArrayList<ArrayList<Integer>> requestPath = requestProtectionPath.getValue();
					int currentRequest = requestProtectionPath.getKey();
					
					int nodeAux1;
					int nodeAux2;
										
					//Se recorren los caminos de la protección actual
					for (int j = 0; j < requestPath.size(); j++) {
						
						ArrayList<Integer> currentPath = requestPath.get(j);
												
						//Se recorre el camino de la protección
						for (int k = 0; k < currentPath.size()-1; k++) {
							
							nodeAux1 = currentPath.get(k);
							nodeAux2 = currentPath.get(k+1);
							
							//Se intercambian los nodos de los enlaces para que coincidan con los enlaces guardados
							//Por ejemplo, el enlaces 2-1, se cambia por 1-2
							if(nodeAux1>nodeAux2) {
								
								//Se obtiene el enlace
								linkAux = Integer.toString(nodeAux2) + '-' + Integer.toString(nodeAux1);
							}else {
							
								//Se obtiene el enlace
								linkAux = Integer.toString(nodeAux1) + '-' + Integer.toString(nodeAux2);	
							}
							
							if(link.equals(linkAux) ) {
								
								ArrayList<Integer> a = pathsByRequest.get(requestNumber).get(pathNumber);
								ArrayList<Integer> b = pathsByRequest.get(currentRequest).get(j);
								
								boolean equal = false;
								
								//Se busca si existen enlaces compartidos
								for (int l = 0; l < a.size()-1; l++) {
									for (int m = 0; m < b.size()-1; m++) {
										
										int node3 = a.get(l);
										int node4 = b.get(m);
										int node5 = a.get(l+1);
										int node6 = b.get(m+1);
										String linkAux2 = null;
										String linkAux3 = null;

										
										if(node5>node3) {
											
											//Se obtiene el enlace
											linkAux2 = Integer.toString(node3) + '-' + Integer.toString(node5);
										}else {
										
											//Se obtiene el enlace
											linkAux2 = Integer.toString(node5) + '-' + Integer.toString(node3);	
										}
										
										if(node6>node4) {
											
											//Se obtiene el enlace
											linkAux3 = Integer.toString(node4) + '-' + Integer.toString(node6);
										}else {
										
											//Se obtiene el enlace
											linkAux3 = Integer.toString(node6) + '-' + Integer.toString(node4);	
										}
										
										if(linkAux2.equals(linkAux3)) {
												equal = true;
												break;
									
										}
										
										if(equal == true) {
											break;
										}
									}
								}
								
								if(equal == false) {
									aux4=true;
									break;
								}else {
									aux6=true;
									break;
								}
							} 
						}
						if(aux4==true || aux6==true) {
							break;
						}
					}
					
				}
			} 
						
			if(aux5==false && aux4==false) {
				
				//Se obtiene el uso viejo del enlace
				int oldUsed = SONLinkUsedAux.get(link);
				
				//Se obtiene el uso viejo del enlace de la solicitud actual
				int oldUsedForRequest = linkUsedForRequest.get(link);
				
				//Se actualiza el nuevo uso
				int newUsed = oldUsed + 1;
				int newUsedForRequest = oldUsedForRequest + 1;
				
				SONLinkUsedAux.put(link, newUsed);
				linkUsedForRequest.put(link, newUsedForRequest);
			}
			
			
		}
		
		linksUsedUpdates.put(SONLinkUsedAux, linkUsedForRequest);
		
		
		return linksUsedUpdates;
			
	}

}
