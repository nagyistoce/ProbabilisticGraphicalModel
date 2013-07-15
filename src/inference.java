import java.io.*;
import java.util.*;

class Node {
	public String name;
	public ArrayList<Node> parents;
	public ArrayList<Double> weightedMatrix;
	
	public Node() {
		this.name = new String();
		parents = new ArrayList<Node>();
		weightedMatrix = new ArrayList<Double>();
	}
	
	public Node(String name) {
		this.name = name;
		parents = new ArrayList<Node>();
		weightedMatrix = new ArrayList<Double>();
	}
}

public class inference {	
	public static void main(String[] args) {
		FileReader reader = null;
        try {
        		reader = new FileReader(args[0]);
        } catch (FileNotFoundException fnf) {
        		System.out.println("The input file is not found");
        }

        BufferedReader bufreader = new BufferedReader(reader);
        String line = null;		//each line of the data
        int N = 0;				//the number of random variables
        
        try {
        	//read the number of random variables
			line = bufreader.readLine();
			N = Integer.parseInt(line); 
            line = bufreader.readLine();//blank line
            
        	//read the random variable names
	        int wordsCounter = 0;
	        String[] variableNames = new String[N];
	        while(wordsCounter != N) {    
	        	line = bufreader.readLine();
	        	String[] tmpNames = line.split(" ");
	        	for(int i = 0; i < tmpNames.length;i++) {
	        		variableNames[i + wordsCounter] = tmpNames[i]; 
	        	}
	        	wordsCounter += tmpNames.length;
	        }
	        
	        
	        line = bufreader.readLine();//blank line
	        
	        //read the arcs matrix
	        int[][] arcsMatrix = new int[N][N];
	        for(int i = 0;i < N;i++) {
	        	line = bufreader.readLine();
	        	String[] splitline = line.split(" ");
	        	for(int j = 0;j < N; j++) {
	        		arcsMatrix[i][j] =  Integer.parseInt(splitline[j]);
	        	}
	        }
	        line = bufreader.readLine();//blank line
	        
	        //initialized nodes based on variable names, arcs matrix and weighted matrixes
	        Node[] nodes = new Node[N];
	        //construct nodes
	        for(int i = 0;i < N; i++) {
	        	nodes[i] = new Node(variableNames[i]);
	        }
	        //initialized nodes, for each node
	        for(int i = 0;i < N; i++) {
	        	//adding parents
	        	for(int j = 0; j < N;j++)
	        		if(arcsMatrix[j][i] == 1) nodes[i].parents.add(nodes[j]);
	        	//adding weighted matrix, convert the 2^m X 2 matrix to an arrayList
	        	int size = (int) Math.pow(2.0, (double)nodes[i].parents.size());
	        	for(int k = 0; k < size;k++) {
	        		line = bufreader.readLine();
	        		String[] splitline = line.split(" ");
	        		nodes[i].weightedMatrix.add(Double.parseDouble(splitline[0]));
	        	}
	        	line = bufreader.readLine();//blank line	
	        }
	        
	        
	       	// testing information, display the network
	        /*
	        for(Node n : nodes) {
	        	System.out.println("Node:");
	        	System.out.println(n.name);
	        	System.out.println("Parents:");
	        	for(Node p : n.parents)
	        		System.out.println(p.name);
	        	System.out.println("Weighted Matrix");
	        	for(Double w : n.weightedMatrix) {
	        		System.out.print(w + " ");
	        	}
	        	System.out.println();
	        }
	        */
	        // read the query from the file
	        Scanner in = new Scanner(System.in);
	        while( in.hasNextLine() ) {
	        	line = in.nextLine();
	        	String[] splitline = line.split("\\u0028");
	        	splitline = splitline[1].split("\\u007C");
	        	//get cause
	        	String cause = splitline[0];
	        	cause = cause.split(" ")[0];
	        	splitline = splitline[1].split("\\u0029");
	        	splitline = splitline[0].split(",");
	        	//get effects
	        	ArrayList<String> trueEffects = new ArrayList<String>();
	        	ArrayList<String> falseEffects = new ArrayList<String>();
	        	for(int i = 0; i < splitline.length * 2;i = i+2) {
	        		String s = splitline[i/2];
	        		s = s.split(" ")[1];
	        		String effect = s.split("=")[0];
	        		if(s.split("=")[1].startsWith("true"))
	        			trueEffects.add(effect);
	        		else
	        			falseEffects.add(effect);
	        	}
	        	
	        	
	        	double d = calulateProbility(trueEffects, falseEffects,nodes);
	        	trueEffects.add(cause);
	        	double n = calulateProbility(trueEffects, falseEffects,nodes);
	        	double result = n / d;
	        	double oneMinusResult = 1.0 - result;
				System.out.println(result + " " + oneMinusResult);
				
				if(in.hasNextLine()) {
	        		line = in.nextLine();//blank line
	        		System.out.println("0.000000 1.000000");
	        	}
        		//testing information, display the query cause and effects
	        	/*
	        	System.out.println("get_cum_prob first call...");
	        	for(Node s : nodes)
	        		System.out.print(s.name);
	        	System.out.println();
	        	for(String s : trueEffects)
	        		System.out.print(s);
        		System.out.println();
	        	for(String s : falseEffects)
	        		System.out.print(s);
	        	System.out.println();
	        	*/
	        }     
        } catch(IOException e1) {
        	System.out.println("An IO error occurs when read line");
        }
	}
	
	private static double finalProbility(ArrayList<String> ones, Node[] nodes) {
		double result = 1.0;
		for (Node n : nodes) {
			ArrayList<String> children = new ArrayList<String>();
			for (Node s : n.parents)
				if (ones.contains(s.name)) children.add(s.name);

			int bins;
			if (children.size() == 0 || n.parents.size() == 0)
				bins = 0;
			else {
				String bin_string = "";
				for (Node nn : n.parents) {
					if (ones.contains(nn.name)) bin_string += "1";
					else bin_string += "0";
				}
				bins = Integer.parseInt(bin_string, 2);
			}

			double previous = n.weightedMatrix.get(bins);
			if (ones.contains(n.name)) result = result * previous;
			else result = result * (1 - previous);
		}
		return result;
	}

	private static double calulateProbility(ArrayList<String> one_set, ArrayList<String> zero_set,Node[] nodes) {
		double prob = 0;
		ArrayList<String> cand = new ArrayList<String>();
		for (Node n : nodes)
			if (!one_set.contains(n.name) && !zero_set.contains(n.name)) cand.add(n.name);
		
		ArrayList<String> pre = new ArrayList<String>();
		ArrayList<ArrayList<String>> all_strings = new ArrayList<ArrayList<String>>();
		recursiveList(cand, pre, all_strings);
		for (List<String> a_list : all_strings) {
			ArrayList<String> list = new ArrayList<String>();
			list.addAll(one_set);
			list.addAll(a_list);
			prob += finalProbility(list, nodes);
		}
		return prob;
	}

	private static void recursiveList(ArrayList<String> cand, ArrayList<String> pre, ArrayList<ArrayList<String>> all_lists) {
		all_lists.add(pre);
		for (int i = 0; i < cand.size(); i++) {
			ArrayList<String> parameter1 = new ArrayList<String>();
			ArrayList<String> parameter2 = new ArrayList<String>();
			parameter2.addAll(pre);
			parameter2.add(cand.get(i));
			for (int j = i + 1; j < cand.size(); j++) 
				parameter1.add(cand.get(j));
			recursiveList(parameter1, parameter2, all_lists);
		}
	}
}