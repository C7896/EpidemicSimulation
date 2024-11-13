package hw4;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.InvalidParameterException;
import java.util.Iterator;

public class NodeTest {

    private Node node;

    @BeforeEach
    public void setUp() {
        node = new Node("A");
    }

    @Test
    public void testConstructorValidLabel() {
        Node validNode = new Node("B");
        assertNotNull(validNode);
        assertEquals("B", validNode.getLabel());
        assertEquals(NodeStates.SUSCEPTIBLE, validNode.getState());
        assertEquals(0, validNode.numNeighbors());
    }

    @Test
    public void testConstructorInvalidLabel() {
        assertThrows(InvalidParameterException.class, () -> {
            new Node(null);
        });
        assertThrows(InvalidParameterException.class, () -> {
            new Node("");
        });
    }

    @Test
    public void testNextState() {
        assertEquals(NodeStates.SUSCEPTIBLE, node.getState());

        node.nextState();
        assertEquals(NodeStates.INFECTED, node.getState());

        node.nextState();
        assertEquals(NodeStates.RECOVERED, node.getState());

        node.nextState();
        assertEquals(NodeStates.RECOVERED, node.getState());
    }

    @Test
    public void testAddNeighborValid() {
        assertTrue(node.addNeighbor("B"));
        assertTrue(node.addNeighbor("C"));
        assertEquals(2, node.numNeighbors());
    }

    @Test
    public void testAddNeighborInvalid() {
        assertFalse(node.addNeighbor(null));
        assertFalse(node.addNeighbor(""));
        assertEquals(0, node.numNeighbors());
    }

    @Test
    public void testAddNeighborDuplicate() {
        assertTrue(node.addNeighbor("B"));
        assertFalse(node.addNeighbor("B"));
        assertEquals(1, node.numNeighbors());
    }

    @Test
    public void testRemoveNeighbor() {
        node.addNeighbor("B");
        node.addNeighbor("C");
        assertEquals(2, node.numNeighbors());

        node.removeNeighbor("B");
        assertEquals(1, node.numNeighbors());

        node.removeNeighbor("C");
        assertEquals(0, node.numNeighbors());
    }

    @Test
    public void testGetLabel() {
        assertEquals("A", node.getLabel());
    }

    @Test
    public void testGetState() {
        assertEquals(NodeStates.SUSCEPTIBLE, node.getState());
    }

    @Test
    public void testNumNeighbors() {
        assertEquals(0, node.numNeighbors());
        node.addNeighbor("B");
        assertEquals(1, node.numNeighbors());
    }

    @Test
    public void testGetNeighbors() {
        node.addNeighbor("B");
        node.addNeighbor("C");

        Iterator<String> iterator = node.getNeighbors();
        assertNotNull(iterator);

        int count = 0;
        while (iterator.hasNext()) {
            String neighbor = iterator.next();
            assertTrue(neighbor.equals("B") || neighbor.equals("C"));
            count++;
        }

        assertEquals(2, count);
    }
}
