package hw4;

import java.io.File;
import java.io.FileNotFoundException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.Scanner;

enum EpidemicStates {INIT, IN_PROGRESS, NONE_INFECTED, ALL_RECOVERED, ALL_DEAD}

/**
 * The EpidemicGraph class represents an epidemic simulation.
 * 
 * @author Chev Kodama
 * @version 1.0
 */
public class EpidemicGraph {
	/*
	 * Abstraction function:
	 * 
	 * 	graph represents the epidemic.
	 * 	infected represents the infected nodes, and when they can be cured or will die.
	 * 		(tick to be cured or die, list of node labels)
	 * 	tick represents the current time in relation to the start of the epidemic.
	 * 	death represents the probability of a node dying when they can either be cured or die.
	 * 	time represents the number of ticks a node is infected for before either being cured or dying.
	 * 	lambda represents the value that the force of infection rate will approach.
	 * 		force of infection for tick x = (number of nodes infected during tick x) / (total nodes still infected from before tick x)
	 * 	state represents the current stage of the epidemic
	 */
	private Graph graph;
	private HashMap<Integer, ArrayList<String>> infected;
	private int tick;
	private float death;
	private int time;
	private float lambda;
	private EpidemicStates state;
	/*
	 * Representation Invariant:
	 * 
	 * 	no private field will ever be null.
	 * 	all values of infected.keySet() >= 0
	 *	tick >= 0
	 *	0 <= death <= 1
	 *	time > 0
	 * 	lambda >= 0
	 */
	
	/**
	 * EpidemicGraph constructor.
	 * @param d	death chance
	 * @param t	infection duration
	 * @param l	lambda
	 */
	public EpidemicGraph(float d, int t, float l) throws InvalidParameterException {
		if ( d < 0 || d > 1 ) {
			throw new InvalidParameterException("ERROR: d must be a value in [0,1]");
		}
		else if ( t <= 0 ) {
			throw new InvalidParameterException("ERROR: t must be greater than 0");
		}
		else if ( l < 0 ) {
			throw new InvalidParameterException("ERROR: l must be greater than or equal to 0");
		}
		
		graph = new Graph();
		tick = 0;
		infected = new HashMap<Integer, ArrayList<String>>();
		death = d;
		time = t;
		lambda = l;
		state = EpidemicStates.INIT;
	}
	
	/**
	 * Changes the configurations for this graph.
	 * @param d	death chance
	 * @param t	infection duration
	 * @param l	lambda
	 * @return	true if changes were made, false if not.
	 */
	public boolean editConfigs(float d, int t, float l) {
		if ( state != EpidemicStates.INIT ) {
			return false;
		}
		
		if ( d < 0 || d > 1 ) {
			return false;
		}
		else if ( t <= 0 ) {
			return false;
		}
		else if ( l < 0 ) {
			return false;
		}
		
		death = d;
		time = t;
		lambda = l;
		return true;
	}
	
	/**
	 * Initializes this EpidemicGraph's graph using the file with the filename input.
	 * @param filename	the name or path of the file to be used to initialize this graph.
	 * @return			"Success" on successful initialization, or an error message as a String.
	 */
	public String initialize(String filename) {
		File file = new File(filename);
		if ( !file.exists() ) {
			return "ERROR: File does not exist.";
		}
		
		Graph g = new Graph();
		try (Scanner scanner = new Scanner(file)){
			while ( scanner.hasNext() ) {
				String[] edges = parseLine(scanner.next());
				
				/* skip empty lines */
				if ( edges == null || edges.length == 0 ) {
					continue;
				}
				
				/* add nodes if not in graph */
				for ( String node_label : edges ) {
					if ( !g.nodeExists(node_label) ) {
						if ( !g.addNode(node_label) ) {
							return "ERROR: File contains an invalid node label.";
						}
					}
				}
				
				/* add edges */
				for ( int i = 1; i < edges.length; i++ ) {
					g.addEdge(edges[0], edges[i]);
				}
			}
		}
		catch (FileNotFoundException e) {
			return "ERROR: Scanner could not open file.";
		}
		graph = g;
		tick = 0;
		infected = new HashMap<Integer, ArrayList<String>>();
		return "Success";
	}
	
	private String[] parseLine(String line) {
		return (line.strip()).split(";");
	}
	
	/**
	 * Returns the current state of this EpidemicGraph.
	 * @return	state
	 */
	public EpidemicStates getState() {
		return state;
	}
	
	/**
	 * Returns the current tick of this graph.
	 * @return	tick
	 */
	public int getTick() {
		return tick;
	}
	
	/**
	 * Returns the total number of live nodes.
	 * @return	graph.numNodes()
	 */
	public int numNodes() {
		return graph.numNodes();
	}
	
	/**
	 * Returns the number of nodes currently infected.
	 * @return	graph.numInfected()
	 */
	public int numInfected() {
		return graph.numInfected();
	}
	
	/**
	 * Returns the number of nodes recovered.
	 * @return	graph.numRecovered()
	 */
	public int numRecovered() {
		return graph.numRecovered();
	}
	
	/**
	 * Returns the number of nodes dead.
	 * @return	graph.numDead()
	 */
	public int numDead() {
		return graph.numDead();
	}
	
	/**
	 * Infects n random nodes in this graph.
	 * @param n	the number of nodes to infect.
	 * @return	the number of nodes infected by this method.
	 */
	public int infectRandom(int n) {
		if ( state != EpidemicStates.INIT ) {
			return 0;
		}
		
		/* prepare to record infected nodes */
		infected.put(tick + time, new ArrayList<String>());
		
		ArrayList<String> node_labels = new ArrayList<String>();
		Iterator<String> iter = graph.getNodes();
		while ( iter.hasNext() ) {
			node_labels.add(iter.next());
		}
		
		Random random = new Random();
		int i = 0;
		while ( i < n && node_labels.size() > 0 ) {
			int index = random.nextInt( node_labels.size() );
			if ( graph.infectNode( node_labels.get( index ) ) ) {
				/* record infected node */
				infected.get(tick + time).add(node_labels.get(index));
				node_labels.remove(index);
				i++;
			}
		}
		
		state = EpidemicStates.IN_PROGRESS;
		
		return i;
	}
	
	/**
	 * Infects all nodes with a degree (number of neighbors) greater than s.
	 * @param s	the degree threshold
	 * @return	the number of nodes infected by this method.
	 */
	public int infectDegree(int s) {
		if ( state != EpidemicStates.INIT ) {
			return 0;
		}
		
		/* prepare to record infected nodes */
		infected.put(tick + time, new ArrayList<String>());
		
		Iterator<String> iter = graph.getNodes();
		int i = 0;
		while ( iter.hasNext() ) {
			String current_node_label = iter.next();
			if ( graph.numNeighbors( current_node_label ) > s ) {
				if ( graph.infectNode(current_node_label) ) {
					/* record infected node */
					infected.get(tick + time).add(current_node_label);
					i++;
				}
			}
		}
		
		state = EpidemicStates.IN_PROGRESS;
		
		return i;
	}
	
	/**
	 * Infects the first k nodes traversed by a BFS function.
	 * @param k	the number of nodes to infect.
	 * @return	the number of nodes infected by this method.
	 */
	public int infectBFS(int k) {
		if ( state != EpidemicStates.INIT ) {
			return 0;
		}
		
		/* prepare to record infected nodes */
		infected.put(tick + time, new ArrayList<String>());
		
		/* select seed */
		String seed_label = graph.getRandom();
		/* infect seed */
		graph.infectNode(seed_label);
		infected.get(tick + time).add(seed_label);
		/* set up queue */
		ArrayList<String> Q = new ArrayList<String>();
		Q.add(seed_label);
		/* start BFS */
		int i = 0;
		while ( Q.size() != 0 && i < k ) {
			String current_node = Q.remove(0);
			/* move through each neighbor */
			Iterator<String> neighbor_iter = graph.getNeighbors(current_node);
			while ( neighbor_iter.hasNext() && i < k ) {
				String neighbor = neighbor_iter.next();
				/* if not visited yet */
				if ( graph.infectNode(neighbor) ) {
					/* record infected node */
					infected.get(tick + time).add(neighbor);
					/* add neighbor to queue */
					Q.add(neighbor);
					/* increment number of infected nodes */
					i++;
				}
			}
		}

		state = EpidemicStates.IN_PROGRESS;
		
		return i;
	}
	
	/**
	 * Returns an iterator containing all nodes in this graph.
	 * @return	an iterator for all nodes in this graph.
	 */
	public Iterator<String> getNodes() {
		return graph.getNodes();
	}
	
	/**
	 * Returns the current state of the node with
	 * label label.
	 * @param label	the label of the node to get the state of.
	 * @return		graph.getState(label)
	 * 				if the node with label label does not exist in
	 * 				this graph, returns null.
	 */
	public NodeStates getState(String label) {
		return graph.getState(label);
	}
	
	/**
	 * Simulates the state of this EpidemicGraph one tick later.
	 * @param num_threads	the number of threads to run this method with.
	 * @return				the new current tick value.
	 */
	public int nextTick(int num_threads) {
		if ( state != EpidemicStates.IN_PROGRESS ) {
			return -1;
		}
		
		tick++;
		/* prepare to record infected nodes */
		infected.put(tick + time, new ArrayList<String>());
		
		/* simulate next tick */
		Thread[] threads = new Thread[num_threads];
		for ( int i = 0; i < num_threads; i++ ) {
			SimulatorRunnable simulator_runnable = new SimulatorRunnable(i, num_threads);
			threads[i] = new Thread(simulator_runnable);
			threads[i].start();
		}
		for ( Thread thread : threads ) {
			try {
				thread.join();
			}
			catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				System.out.println("Simulation thread interrupted");
			}
		}
		
		/* infect nodes */
		for ( String label : infected.get(tick + time) ) {
			graph.infectNode(label);
		}
		
		/* kill or cure nodes */
		disinfect();
		
		/* update state */
		GraphStates g_state = graph.getState();
		if ( g_state == GraphStates.ALL_RECOVERED ) {
			state = EpidemicStates.ALL_RECOVERED;
		}
		else if ( g_state == GraphStates.ALL_DEAD ) {
			state = EpidemicStates.ALL_DEAD;
		}
		else if ( g_state == GraphStates.NONE_INFECTED ) {
			state = EpidemicStates.NONE_INFECTED;
		}
		
		return tick;
	}
	
	private synchronized int interactInfected(String node_to_add) {
		if ( node_to_add == "" ) {
			return infected.get(tick + time).size();
		}
		else {
			infected.get(tick + time).add(node_to_add);
			return -1;
		}
	}
	
	private void disinfect() {
		ArrayList<String> to_disinfect = infected.get(tick);
		if ( to_disinfect == null ) {
			return;
		}
		Random random = new Random();
		for ( String label : to_disinfect ) {
			float luck = random.nextFloat();
			/* 1 - d chance of recovering */
			if ( luck >= death ) {
				graph.recoverNode(label);
			}
			/* d chance of dying */
			else {
				graph.removeNode(label);
			}
		}
		infected.remove(tick);
	}
	
	
	class SimulatorRunnable implements Runnable {
	    private int order;
	    private int num_threads;
	    
	    public SimulatorRunnable(int o, int nt) {
	        order = o;
	        num_threads = nt;
	    }

	    @Override
	    public void run() {
	    	/* partition graph */
	    	int num_nodes = graph.numNodes() / num_threads;
	    	int start = num_nodes * order;
	    	if ( order == num_threads - 1 ) {
	    		num_nodes += graph.numNodes() % num_threads;
	    	}
	    	int end = start + num_nodes;
	    	
	    	/* begin simulation */
	    	Iterator<String> node_labels = graph.getNodes();
	    	/* Using ArrayList because overlap is to be ignored as per the instructions:
	    	 * 
		     *	"Implement synchronization such that node states are updated only at the end of the tick,
		     *	i.e., each node should see the “old” value of its neighbors’ state until the end of the tick."
	    	 */
	    	/* local force of infection */
	    	float force_of_infection = interactInfected("") / graph.numNodes();
	    	for ( int i = 0; i < start; i++ ) {
	    		node_labels.next();
	    	}
	    	for ( int i = start; i < end; i++ ) {
	    		String label = node_labels.next();
	    		if ( graph.getState(label) != NodeStates.INFECTED ) {
	    			continue;
	    		}
	    		
	    		/* determine how many nodes to infect */
	    		int num_to_infect = 0;
	    		while ( force_of_infection < lambda ) {
	    			num_to_infect++;
	    			force_of_infection = ( num_to_infect + interactInfected("") ) / graph.numNodes();
	    		}
	    		/* infect nodes by adding to concurrently_infected */
	    		Iterator<String> neighbors = graph.getNeighbors(label);
	    		while ( num_to_infect > 0 && neighbors.hasNext() ) {
	    			String node_to_infect = neighbors.next();
	    			if ( graph.getState(node_to_infect) != NodeStates.SUSCEPTIBLE ) {
	    				continue;
	    			}
	    			
	    			interactInfected(node_to_infect);
	    			num_to_infect--;
	    		}
	    	}
	    }
	}
}