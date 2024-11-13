package hw4;

import static org.junit.jupiter.api.Assertions.*;

import java.io.FileWriter;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.Iterator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EpidemicGraphTest {

    private EpidemicGraph epidemicGraph;

    @BeforeEach
    void setUp() {
        epidemicGraph = new EpidemicGraph(0.5f, 3, 0.1f);
    }

    @Test
    void testInitializeGraphSuccess() {
        String filename = "src/test/resources/test_graph.txt";
        String result = epidemicGraph.initialize(filename);
        assertEquals("Success", result);
    }

    @Test
    void testInitializeGraphFileNotFound() {
        String result = epidemicGraph.initialize("nonexistent_file.txt");
        assertEquals("ERROR: File does not exist.", result);
    }

    @Test
    void testInitializeGraphInvalidNode() {
        String filename = "src/test/resources/invalid_node_graph.txt";
        String result = epidemicGraph.initialize(filename);
        assertTrue(result.contains("ERROR: File contains an invalid node label."));
    }

    @Test
    void testInfectRandom() {
        String filename = "src/test/resources/test_graph.txt";
        epidemicGraph.initialize(filename);

        int infectedNodes = epidemicGraph.infectRandom(2);
        assertEquals(2, infectedNodes);
    }

    @Test
    void testInfectDegree() {
        String filename = "src/test/resources/test_graph.txt";
        epidemicGraph.initialize(filename);

        int infectedNodes = epidemicGraph.infectDegree(1);
        assertEquals(3, infectedNodes); // Assuming the test graph has 3 nodes with more than 1 neighbor
    }

    @Test
    void testInfectBFS() {
        String filename = "src/test/resources/test_graph.txt";
        epidemicGraph.initialize(filename);

        int infectedNodes = epidemicGraph.infectBFS(2);
        assertEquals(2, infectedNodes);
    }

    @Test
    void testNextTick() {
        String filename = "src/test/resources/test_graph.txt";
        epidemicGraph.initialize(filename);
        epidemicGraph.infectRandom(1);

        int tick = epidemicGraph.nextTick(2);
        assertEquals(1, tick);
        Iterator<String> node_labels = epidemicGraph.getNodes();
        int num_infected = 0;
        while ( node_labels.hasNext() ) {
        	if ( epidemicGraph.getState(node_labels.next()) == NodeStates.INFECTED ) {
        		num_infected++;
        	}
        }
        assertTrue(num_infected == 2 || num_infected == 3);
        
        epidemicGraph.nextTick(2);
        epidemicGraph.nextTick(2);
    }

    @Test
    void testInvalidConstructorParameters() {
        assertThrows(InvalidParameterException.class, () -> new EpidemicGraph(-0.1f, 3, 0.1f));
        assertThrows(InvalidParameterException.class, () -> new EpidemicGraph(0.5f, 0, 0.1f));
        assertThrows(InvalidParameterException.class, () -> new EpidemicGraph(0.5f, 3, -0.1f));
    }
}
