import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Scanner;


public class Clustering {
	
	//arraylist of arraylists which holds (X,Y) coordinates of clusters
	static List<List<Coordinate>> cluster = new ArrayList<List<Coordinate>>();
	static int numberOfPoints = 0; //used for hashcode
	static PrintWriter writer = null;
	static int RAD = 1;
	static int DIA = 2;
	
	public static void main(String[] args){	
		
		try {
			writer = new PrintWriter("C:\\Users\\Anthony\\Desktop\\CS\\eclipse workspace\\Testing\\src\\output.txt", "UTF-8");
		} catch (FileNotFoundException e) {
			System.out.println("Failed to create output file");
			e.printStackTrace();
			System.exit(0);
		} catch (UnsupportedEncodingException e) {
			System.out.println("Failed to create output file");
			e.printStackTrace();
			System.exit(0);
		}
		

		writer.println("Exercise 7.2.3 by Anthony Sager, group 3");
		writer.println();
		writer.println("************************************************");
		writer.println("*****  Results if using smallest radius  *******");
		writer.println("************************************************");
		writer.println();

		if (!importPoints()){			
			writer.print("Failed to import data");
			writer.close();
			System.exit(0);
		}
		

		//Output our initial input
		outputClusters();
		
		while (cluster.size() > 1) {	//can set final number of clusters we want	
			Coordinate clustersIndex = findClustersToMerge(RAD);

			List<Coordinate> xCluster = cluster.get(clustersIndex.getX());
			List<Coordinate> yCluster = cluster.get(clustersIndex.getY());

			xCluster.addAll(yCluster);
			cluster.remove(clustersIndex.getY());
	
			outputClusters();
		}
		
		//Restart alg but using diameter
		cluster.clear();
		if (!importPoints()){			
			writer.print("Failed to import data");
			writer.close();
			System.exit(0);
		}
		
		writer.println("************************************************");
		writer.println("****  Results if using smallest diameter  ******");
		writer.println("************************************************");
		writer.println();
		outputClusters();
		
		while (cluster.size() > 1) {	//can set final number of clusters we want	
			Coordinate clustersIndex = findClustersToMerge(DIA);

			List<Coordinate> xCluster = cluster.get(clustersIndex.getX());
			List<Coordinate> yCluster = cluster.get(clustersIndex.getY());

			xCluster.addAll(yCluster);
			cluster.remove(clustersIndex.getY());		
			outputClusters();
		}
		
		writer.close();
	}
	
	public static boolean importPoints() {
		try { // startup, read points from file, every point begins as a cluster
			Scanner sc = new Scanner(
					new File(
							"C:\\Users\\Anthony\\Desktop\\CS\\eclipse workspace\\Testing\\src\\file.txt"));
			while (sc.hasNextLine()) {

				List<Coordinate> inner = new ArrayList<Coordinate>();
				Coordinate pair = new Coordinate(sc.nextInt(), sc.nextInt());
				inner.add(pair);
				numberOfPoints++;
				cluster.add(inner);
			}
			sc.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;			
		}
		return true;
	}
	
	
	
	/* Cycle through all clusters, mapping all potential pairings. We then 
	 * select the pair with the smallest radius or diameter. Radius is 
	 * determined finding the farthest point in the cluster from the 
	 * centroid (center). The diameter is found by checking for the largest
	 * distance between any two points in a cluster.
	 * 
	 * The radii/diameters are stored in a hashmap, with the index of the 
	 * clusters as key. For example, the cluster pair i & j, use the key 
	 * (i,j) and are hashed with the triangular matrix formula
	 *  k = (i - 1)(n * i/2) j - i
	 * */
	public static Coordinate findClustersToMerge(int code){

		Hashtable<Coordinate, Double> triples = new Hashtable<Coordinate, Double>();
		
		int i, j;
		
		//run thru every possible combination of cluster merge calculating radius
		for (i = 0; i < cluster.size(); i++){
			List<Coordinate> first = cluster.get(i); //first cluster 
			for (j = i + 1; j < cluster.size(); j++){
				List<Coordinate> second = cluster.get(j);//second cluster
				List<Coordinate> copy = new ArrayList<Coordinate>(); //copy the coords into new cluster
				copy.addAll(first);
				copy.addAll(second);
				//outputCluster(copy); testing
				double output;
				
				
				if (code == 1){ //radius calculation
				// find the center point of our potential pair
					double[] center = calculateCentroid(copy); 
				
				// get our radius of the pair (distance from the center to the 
				// farthest point
					output = calculateRadius(copy, center); 
				}
				else { //finding diameter
					output = calculateDiameter(copy);
				}
				
				//store our information
				triples.put(new Coordinate(i, j), output); 
			}		
		}
		
		//we recorded all possible radi, we need to find the smallest
		
		Coordinate clustersToMerge = new Coordinate(0,0);
		double smallest = 10000.0; //starting value should be big
		for (i = 0; i < cluster.size(); i++){
			for (j = i + 1; j < cluster.size(); j++){
				Coordinate current = new Coordinate(i, j);
				if (triples.get(current) < smallest){
					smallest = triples.get(current);
					clustersToMerge = current;
				}
			}
		}
		return clustersToMerge;
	}
	
	
	public static void outputClusters(){	
		int i, j;
		writer.println("Number of clusters = " + cluster.size());
		for (i = 0; i < cluster.size(); i++){
			List<Coordinate> inner = cluster.get(i);
			writer.print("Cluster " + (i + 1) + " has: ");
			for (j = 0; j < inner.size(); j++){
				Coordinate point = inner.get(j);
				writer.print("(" + point.getX() + "," + point.getY() + ")");
				if (j != inner.size() - 1){
					writer.print(", ");
				}
			}
			writer.println();
		}
		writer.println();
	}
	
	public static void outputCluster(List<Coordinate> list){
		int i;

		for (i = 0; i < list.size(); i++){
			Coordinate point = list.get(i);

			writer.print("(" + point.getX() + "," + point.getY() + "), ");
		}
		writer.println();
	}
		
	//calculate the centroid by adding up all the coords and divide by the number of coords
	public static double[] calculateCentroid(List<Coordinate> list){
		double xTotal = 0;
		double yTotal = 0;
		int j;
		for (j = 0; j < list.size(); j++){
			Coordinate coord = list.get(j);
			xTotal = xTotal + coord.getX();
			yTotal = yTotal + coord.getY();
		}
		int n = list.size();
		double[] temp = {(xTotal/n), (yTotal/n)};
		return temp;
	}
	
	//using the cluster and centroid we determine the farthest point (radius) of the cluster
	public static double calculateRadius(List<Coordinate> list, double[] center){

		double temp = 0;
		double highest = 0;
		int j;
		for (j = 0; j < list.size(); j++){
			Coordinate coord = list.get(j);
			
			temp = Math.sqrt(Math.pow(coord.getY() - center[1], 2) + Math.pow((coord.getX() - center[0]), 2));
			if (temp > highest)
				highest = temp;
		}
		return highest;
	}
	
	//using the cluster and we determine the two farthest points of the cluster = diameter
		public static double calculateDiameter(List<Coordinate> list){

			double temp = 0;
			double currentLargest = 0;
			int i, j;
			for (i = 0; i < list.size(); i++){
				Coordinate coord1 = list.get(i);
				for (j = i + 1; j < list.size(); j++){
					Coordinate coord2 = list.get(j);
					temp = Math.sqrt(Math.pow(coord1.getY() - coord2.getY(), 2) + Math.pow(coord1.getX() - coord2.getX(), 2));
					if (temp > currentLargest)
						currentLargest = temp;
				}
			}
			return currentLargest;
		}
}

class Coordinate{
	int x;
	int y;
	//max number of radii, being conservative
	int n = Clustering.numberOfPoints * Clustering.numberOfPoints;
	
	public Coordinate(int x, int y){
		this.x = x;
		this.y = y;
		
	}
	
	public int getX(){
		return x;
	}
	
	public int getY(){
		return y;
	}
	
	public void setX(int x){
		this.x = x;
	}
	
	public void setY(int y){
		this.y = y;
	}
		
	@Override
	public int hashCode(){
		int result;
		//using triangular matrix index formula to generate unique hashes
		result = (x - 1) * (n - x/2) + (y) - x;
		return result;		
	}
	
	@Override
	public boolean equals(Object obj){
		if (!(obj instanceof Coordinate) || obj == null){
			return false;
		}
		Coordinate test = (Coordinate) obj;
		
		if (test.getX() == x && test.getY() == y){
			return true;
		}
		else{
			return false;
		}
	}
}
