package hw4;

import java.security.InvalidParameterException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;

enum NodeStates {SUSCEPTIBLE, INFECTED, RECOVERED}

/**
 * The Node class represents a node in a graph.
 * 
 * @author Chev Kodama
 * @version 1.0
 */
public class Node {
	/*
	 * Abstraction function:
	 * 
	 * 	label represents this node's identifier
	 * 	state represents whether this node is susceptible, infected, or recovered.
	 * 		node states begin at susceptible and may only move to infected.
	 * 		infected may only move to recovered.
	 * 	neighbors represents the identifiers of all nodes this node is connected to by an edge.
	 * 		there may only be one undirected edge (two directed edges in opposite directions) between two nodes.
	 */
	private String label;
	private NodeStates state;
	private HashSet<String> neighbors;
	/*
	 * Representation invariant:
	 * 
	 *  label, state, neighbors != null
	 *  all values in neighbors are under the same restrictions as label
	 *  label != ""
	 */
	
	/**
	 * Node constructor.
	 * @param l								the label of this node.
	 * @throws InvalidParameterException	thrown if the label given is null or "".
	 */
	public Node(String l) throws InvalidParameterException {
		if ( l == null || l == "" ) {
			throw new InvalidParameterException("Label cannot be \"\" or null.");
		}
		
		label = l;
		state = NodeStates.SUSCEPTIBLE;
		neighbors = new HashSet<String>();
	}
	
	/**
	 * Increments this node's state.
	 * @return	the new state.
	 */
	public NodeStates nextState() {
		switch ( state ) {
			case NodeStates.SUSCEPTIBLE:	state = NodeStates.INFECTED;
											break;
				
			case NodeStates.INFECTED:	state = NodeStates.RECOVERED;
										break;
				
			case NodeStates.RECOVERED:	break;
			
			default:	break;
		}
		return state;
	}
	
	/**
	 * Adds neighbor to this node.
	 * @param neighbor	the label of the neighbor to add
	 * @return			true if neighbor is not already a neighbor, and is valid.
	 * 					false if neighbor is invalid or already a neighbor.
	 */
	public boolean addNeighbor(String neighbor) {
		if ( neighbor == null || neighbor == "" ) {
			return false;
		}
		
		return neighbors.add(neighbor);
	}
	
	/**
	 * Remove neighbor from this node.
	 * @param neighbor	the label of the neighbor to remove.
	 */
	public void removeNeighbor(String neighbor) {
		neighbors.remove(neighbor);
	}
	
	/**
	 * Returns this node's label.
	 * @return	label
	 */
	public String getLabel() {
		return label;
	}
	
	/**
	 * Returns the current state of this node.
	 * @return	state
	 */
	public NodeStates getState() {
		return state;
	}
	
	/**
	 * Returns the number this node's neighbors.
	 * @return	neighbors.size()
	 */
	public int numNeighbors() {
		return neighbors.size();
	}
	
	/**
	 * Returns an iterator containing all neighbors of this node.
	 * @return	an iterator for all neighbors of this node.
	 */
	public Iterator<String> getNeighbors() {
		return Collections.unmodifiableSet(neighbors).iterator();
	}
}