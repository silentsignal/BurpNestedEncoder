package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BurpSimulationTest {
    private BurpSimulation burpSimulation;
    private String resourceDir;

    @BeforeEach
    void setUp() {
        burpSimulation = new BurpSimulation();
        resourceDir = System.getProperty("user.dir") + "/src/test/resources/";
    }

    @Test
    void CorrectNumberOfLeafNodes() {
        String param = "{'key1': 'value1', 'key2': 'value2', 'key3': 'VGVzdGluZ05ld0I2NEpTT05JbXBsZW1lbnRhdGlvbg=='}";
        String payload = "dummyPayload";
        burpSimulation.setParamValue(param);
        List<EncodingTree> modifiedNodes = burpSimulation.dummyBuildWithPayload(payload);
        assertEquals(3, modifiedNodes.size());
    }

    @Test
    void B64InsideJSON() throws IOException {
        List<String> expectedOutputValues = new ArrayList<>();
        fillParamsFromTextFile(expectedOutputValues, "B64InsideJSON.txt");
        String param = expectedOutputValues.get(0);
        String payload = expectedOutputValues.get(1);
        expectedOutputValues.removeFirst();
        expectedOutputValues.removeFirst();
        burpSimulation.setParamValue(param);
        List<EncodingTree> modifiedNodes = burpSimulation.dummyBuildWithPayload(payload);
        for(int i = 0; i < modifiedNodes.size(); i++){
            assertEquals(expectedOutputValues.get(i), modifiedNodes.get(i).getNode().getValue());
        }
    }

    @Test
    void JSONInsideB64() throws IOException {
        List<String> expectedOutputValues = new ArrayList<>();
        fillParamsFromTextFile(expectedOutputValues, "JSONInsideB64.txt");
        String param = expectedOutputValues.get(0);
        String payload = expectedOutputValues.get(1);
        expectedOutputValues.removeFirst();
        expectedOutputValues.removeFirst();
        burpSimulation.setParamValue(param);
        List<EncodingTree> modifiedNodes = burpSimulation.dummyBuildWithPayload(payload);
        for(int i = 0; i < modifiedNodes.size(); i++){
            assertEquals(expectedOutputValues.get(i), modifiedNodes.get(i).getNode().getValue());
        }
    }

    @Test
    void QuadB64Encoded() throws IOException {
        List<String> expectedOutputValues = new ArrayList<>();
        fillParamsFromTextFile(expectedOutputValues, "QuadB64Encoded.txt");
        String param = expectedOutputValues.get(0);
        String payload = expectedOutputValues.get(1);
        expectedOutputValues.removeFirst();
        expectedOutputValues.removeFirst();
        burpSimulation.setParamValue(param);
        List<EncodingTree> modifiedNodes = burpSimulation.dummyBuildWithPayload(payload);
        assertEquals(expectedOutputValues.getFirst(), modifiedNodes.getFirst().getNode().getValue());
    }

    @Test
    void CommaSeparatedB64InsideB64() throws IOException {
        List<String> expectedOutputValues = new ArrayList<>();
        fillParamsFromTextFile(expectedOutputValues, "CommaSeparatedB64InsideB64.txt");
        String param = expectedOutputValues.get(0);
        String payload = expectedOutputValues.get(1);
        expectedOutputValues.removeFirst();
        expectedOutputValues.removeFirst();
        burpSimulation.setParamValue(param);
        List<EncodingTree> modifiedNodes = burpSimulation.dummyBuildWithPayload(payload);
        for(int i = 0; i < modifiedNodes.size(); i++){
            assertEquals(expectedOutputValues.get(i), modifiedNodes.get(i).getNode().getValue());
        }
    }

    @Test
    void ComplexB64JSONCommaSeparated() throws IOException {
        List<String> expectedOutputValues = new ArrayList<>();
        fillParamsFromTextFile(expectedOutputValues, "ComplexB64JSONCommaSeparated.txt");
        String param = expectedOutputValues.get(0);
        String payload = expectedOutputValues.get(1);
        expectedOutputValues.removeFirst();
        expectedOutputValues.removeFirst();
        burpSimulation.setParamValue(param);
        List<EncodingTree> modifiedNodes = burpSimulation.dummyBuildWithPayload(payload);
        for(int i = 0; i < modifiedNodes.size(); i++){
            assertEquals(expectedOutputValues.get(i), modifiedNodes.get(i).getNode().getValue());
        }
    }

    void fillParamsFromTextFile(List<String> modifiedValues, String path) throws IOException {
        RandomAccessFile raf = new RandomAccessFile(resourceDir + path, "r");
        String line;
        while ((line = raf.readLine()) != null) {
            System.out.print(line);
            modifiedValues.add(line);
        }
        raf.close();
    }
}