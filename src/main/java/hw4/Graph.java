package hw4;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

enum GraphStates {INIT, INFECTED, NONE_INFECTED, ALL_RECOVERED, ALL_DEAD}

/**
 * The Graph class represents an undirected graph.
 * 
 * @author Chev Kodama
 * @version 1.0
 */
public class Graph {
	/*
	 * Abstraction function:
	 * 
	 * 	nodes represents the nodes of this graph.
	 * 		A node in this HashMap is represented as:
	 * 			(node_label, node_object)
	 * 	num_edges represents the total number of undirected edges present in this graph.
	 * 		an undirected edge is two directed edges between two nodes going in opposite directions.
	 * 	num_infected represents the number of nodes with state == NodeStates.INFECTED
	 * 	num_recovered represents the number of nodes with state == NodeStates.RECOVERED
	 */
	private HashMap<String, Node> nodes;
	private int num_edges;
	private int num_infected;
	private int num_recovered;
	private int num_dead;
	private GraphStates state;
	/*
	 * Representation invariant:
	 * 
	 * 	nodes, num_edges != null
	 * 	no key in nodes is null
	 *	no value in nodes is null
	 *	num_edges >= 0
	 *	num_infected >= 0
	 *	num_recovered >= 0
	 *	num_dead >= 0
	 */
	
	/**
	 * Graph constructor.
	 */
	public Graph() {
		nodes = new HashMap<String, Node>();
		num_edges = 0;
		num_infected = 0;
		num_recovered = 0;
		num_dead = 0;
		updateState();
	}
	
	/**
	 * Adds a node with label label to this graph.
	 * @param label	the label of the node to add to this graph.
	 * @return		true if the node was added successfully.
	 * 				false if label is invalid.
	 */
	public boolean addNode(String label) {
		if ( state != GraphStates.INIT ) {
			return false;
		}
		
		Node node;
		try {
			node = new Node(label);
		}
		catch (InvalidParameterException e) {
			return false;
		}
		nodes.put(label, node);
		
		return true;
	}
	
	/**
	 * Removes the node with label label,
	 * along with all edges leading to it.
	 * @param label	the label of the node to wipe from existence.
	 */
	public void removeNode(String label) {
		if ( nodes.get(label) == null ) {
			return;
		}
		
		Iterator<String> neighbors = nodes.get(label).getNeighbors();
		while ( neighbors.hasNext() ) {
			String neighbor_label = neighbors.next();
			nodes.get(neighbor_label).removeNeighbor(label);
			num_edges--;
		}
		
		nodes.remove(label);
		num_infected--;
		num_dead++;
		updateState();
	}
	
	/**
	 * Adds an undirected edge between the nodes with labels
	 * label_1 and label_2.
	 * @param label_1	the label of one of the two nodes to create an edge between.
	 * @param label_2	the label of the other of the two nodes to create an undirected edge between.
	 * @return			true if both nodes exist in this graph, false otherwise.
	 */
	public boolean addEdge(String label_1, String label_2) {
		if ( state != GraphStates.INIT ) {
			return false;
		}
		
		if ( nodes.get(label_1) == null || nodes.get(label_2) == null ) {
			return false;
		}
		
		if ( nodes.get(label_1).addNeighbor(label_2) && nodes.get(label_2).addNeighbor(label_1) ) {
			num_edges++;
		}
		return true;
	}
	
	/**
	 * Returns the number of nodes in this graph.
	 * @return	nodes.size()
	 */
	public int numNodes() {
		return nodes.size();
	}
	
	/**
	 * Returns the total number of undirected edges in this graph.
	 * @return	num_edges
	 */
	public int numEdges() {
		return num_edges;
	}
	
	/**
	 * Returns the number of nodes currently infected.
	 * @return	num_infected
	 */
	public int numInfected() {
		return num_infected;
	}
	
	/**
	 * Returns the number of nodes recovered.
	 * @return	num_recovered
	 */
	public int numRecovered() {
		return num_recovered;
	}
	
	/**
	 * Returns the number of nodes dead.
	 * @return	num_dead
	 */
	public int numDead() {
		return num_dead;
	}
	
	/**
	 * Returns a random node label in this graph.
	 * @return	a node label.
	 */
	public String getRandom() {
		Random random = new Random();
		ArrayList<String> node_labels = new ArrayList<String>(nodes.keySet());
		return node_labels.get( random.nextInt( node_labels.size() ) );
	}
	
	/**
	 * Returns an iterator containing all nodes in this graph.
	 * @return	an iterator for all nodes in this graph.
	 */
	public Iterator<String> getNodes() {
		return Collections.unmodifiableSet(nodes.keySet()).iterator();
	}
	
	/**
	 * Infects the node with label label if it is susceptible.
	 * @param label	the label of the node to infect.
	 * @return		true if the node exists and its state == NodeStates.SUSCEPTIBLE,
	 * 				false otherwise.
	 */
	public boolean infectNode(String label) {
		if ( nodes.get(label) == null ) {
			return false;
		}
		
		if ( nodes.get(label).getState() != NodeStates.SUSCEPTIBLE ) {
			return false;
		}
		
		nodes.get(label).nextState();
		num_infected++;
		updateState();
		return true;
	}
	
	/**
	 * Returns the current state of this graph.
	 * @return	state
	 */
	public GraphStates getState() {
		return state;
	}
	
	private void updateState() {
		switch ( state ) {
			case null:	state = GraphStates.INIT;
						break;
			case GraphStates.INIT:	if ( num_infected > 0 && nodes.size() > 0 ) {
										state = GraphStates.INFECTED;
									}
									else {
										break;
									}
			case GraphStates.INFECTED:	if ( num_recovered == nodes.size() ) {
											state = GraphStates.ALL_RECOVERED;
										}
										else if ( nodes.size() == 0 ) {
											state = GraphStates.ALL_DEAD;
										}
										else if ( num_infected == 0 ) {
											state = GraphStates.NONE_INFECTED;
										}
										break;
			default:	break;
		}
	}
	
	/**
	 * Causes the node with label label to recover if it is infected.
	 * @param label	the label of the node to cause to recover.
	 * @return		true if the node exists and its state == NodeStates.INFECTED,
	 * 				false otherwise.
	 */
	public boolean recoverNode(String label) {
		if ( nodes.get(label) == null ) {
			return false;
		}
		
		if ( nodes.get(label).getState() != NodeStates.INFECTED ) {
			return false;
		}
		
		nodes.get(label).nextState();
		num_infected--;
		num_recovered++;
		updateState();
		return true;
	}
	
	/**
	 * Returns true if the node with label label
	 * exists in this graph.
	 * @param label	the node to check the existence of.
	 * @return		true if nodes.get(label) != null,
	 * 				false otherwise.
	 */
	public boolean nodeExists(String label) {
		if ( nodes.get(label) == null ) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * Returns the current state of the node with
	 * label label.
	 * @param label	the label of the node to get the state of.
	 * @return		nodes.get(label).getState()
	 * 				if the node with label label does not exist in
	 * 				this graph, returns null.
	 */
	public NodeStates getState(String label) {
		if ( nodes.get(label) == null ) {
			return null;
		}
		
		return nodes.get(label).getState();
	}
	
	/**
	 * Returns the number of neighbors the node with label
	 * label has.
	 * @param label	the label of the node to get the number of neighbors of.
	 * @return		nodes.get(label).numNeighbors()
	 * 				if the node with label label does not exist in
	 * 				this graph, returns -1.
	 */
	public int numNeighbors(String label) {
		if ( nodes.get(label) == null ) {
			return -1;
		}
		
		return nodes.get(label).numNeighbors();
	}
	
	/**
	 * Returns an iterator containing all neighbors of this node.
	 * @param label	the label of the node to get the neighbors of.
	 * @return		an iterator for all neighbors of this node.
	 * 				if the node with label label does not exist in
	 * 				this graph, returns an empty iterator.
	 */
	public Iterator<String> getNeighbors(String label) {
		if ( nodes.get(label) == null ) {
			return Collections.emptyIterator();
		}
		
		return nodes.get(label).getNeighbors();
	}
}