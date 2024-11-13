package hw4;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.InvalidParameterException;
import java.util.Iterator;

public class GraphTest {

    private Graph graph;

    @BeforeEach
    public void setUp() {
        graph = new Graph();
    }

    @Test
    public void testConstructor() {
        assertNotNull(graph);
        assertEquals(0, graph.numNodes());
        assertEquals(0, graph.numEdges());
        assertEquals(GraphStates.INIT, graph.getState());
    }

    @Test
    public void testAddNodeValid() {
        assertTrue(graph.addNode("A"));
        assertEquals(1, graph.numNodes());
    }

    @Test
    public void testAddNodeInvalid() {
        assertFalse(graph.addNode(""));
        assertFalse(graph.addNode(null));
        assertEquals(0, graph.numNodes());
    }

    @Test
    public void testAddNodeAfterInitState() {
        graph.addNode("A");
        graph.infectNode("A");
        assertFalse(graph.addNode("B"));
    }

    @Test
    public void testRemoveNode() {
        graph.addNode("A");
        graph.addNode("B");
        graph.addEdge("A", "B");
        assertEquals(1, graph.numEdges());

        graph.removeNode("A");
        assertEquals(1, graph.numNodes());
        assertEquals(0, graph.numEdges());
    }

    @Test
    public void testAddEdgeValid() {
        graph.addNode("A");
        graph.addNode("B");
        assertTrue(graph.addEdge("A", "B"));
        assertEquals(1, graph.numEdges());
    }

    @Test
    public void testAddEdgeInvalid() {
        assertFalse(graph.addEdge("A", "B"));
        graph.addNode("A");
        assertFalse(graph.addEdge("A", "B"));
        graph.addNode("B");
        graph.infectNode("A");
        assertFalse(graph.addEdge("A", "B"));
    }

    @Test
    public void testNumNodes() {
        assertEquals(0, graph.numNodes());
        graph.addNode("A");
        assertEquals(1, graph.numNodes());
    }

    @Test
    public void testNumEdges() {
        assertEquals(0, graph.numEdges());
        graph.addNode("A");
        graph.addNode("B");
        graph.addEdge("A", "B");
        assertEquals(1, graph.numEdges());
    }

    @Test
    public void testGetRandom() {
        graph.addNode("A");
        graph.addNode("B");
        String randomNode = graph.getRandom();
        assertTrue(randomNode.equals("A") || randomNode.equals("B"));
    }

    @Test
    public void testGetNodes() {
        graph.addNode("A");
        graph.addNode("B");
        Iterator<String> nodes = graph.getNodes();
        assertNotNull(nodes);
        int count = 0;
        while (nodes.hasNext()) {
            String node = nodes.next();
            assertTrue(node.equals("A") || node.equals("B"));
            count++;
        }
        assertEquals(2, count);
    }

    @Test
    public void testInfectNode() {
        graph.addNode("A");
        assertTrue(graph.infectNode("A"));
        assertEquals(NodeStates.INFECTED, graph.getState("A"));
    }

    @Test
    public void testInfectNodeInvalid() {
        assertFalse(graph.infectNode("A"));
        graph.addNode("A");
        graph.infectNode("A");
        assertFalse(graph.infectNode("A"));
    }

    @Test
    public void testGetState() {
        graph.addNode("A");
        assertEquals(NodeStates.SUSCEPTIBLE, graph.getState("A"));
    }

    @Test
    public void testRecoverNode() {
        graph.addNode("A");
        graph.infectNode("A");
        assertTrue(graph.recoverNode("A"));
        assertEquals(NodeStates.RECOVERED, graph.getState("A"));
        assertEquals(GraphStates.ALL_RECOVERED, graph.getState());
    }

    @Test
    public void testRecoverNodeInvalid() {
        assertFalse(graph.recoverNode("A"));
        graph.addNode("A");
        assertFalse(graph.recoverNode("A"));
    }

    @Test
    public void testNodeExists() {
        assertFalse(graph.nodeExists("A"));
        graph.addNode("A");
        assertTrue(graph.nodeExists("A"));
    }

    @Test
    public void testGetStateNonExistentNode() {
        assertNull(graph.getState("A"));
    }

    @Test
    public void testNumNeighbors() {
        graph.addNode("A");
        graph.addNode("B");
        graph.addEdge("A", "B");
        assertEquals(1, graph.numNeighbors("A"));
        assertEquals(1, graph.numNeighbors("B"));
    }

    @Test
    public void testNumNeighborsNonExistentNode() {
        assertEquals(-1, graph.numNeighbors("A"));
    }

    @Test
    public void testGetNeighbors() {
        graph.addNode("A");
        graph.addNode("B");
        graph.addEdge("A", "B");
        Iterator<String> neighbors = graph.getNeighbors("A");
        assertNotNull(neighbors);
        assertTrue(neighbors.hasNext());
        assertEquals("B", neighbors.next());
    }

    @Test
    public void testGetNeighborsNonExistentNode() {
        Iterator<String> neighbors = graph.getNeighbors("A");
        assertNotNull(neighbors);
        assertFalse(neighbors.hasNext());
    }
}
