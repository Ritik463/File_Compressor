
package java1;

import java.io.*;
import java.util.*;
import java.nio.charset.StandardCharsets;

class HuffmanNode implements Comparable<HuffmanNode> {
    char character;
    int frequency;
    HuffmanNode left, right;

    public HuffmanNode(char character, int frequency, HuffmanNode left, HuffmanNode right) {
        this.character = character;
        this.frequency = frequency;
        this.left = left;
        this.right = right;
    }

    public boolean isLeafNode() {
        return (left == null && right == null);
    }

    public int compareTo(HuffmanNode node) {
        return this.frequency - node.frequency;
    }
}

class HuffmanCoding {
    private static final int BUFFER_SIZE = 8;

    public static void compressFile(String inputFilePath, String outputFilePath) {
        try {
            // Step 1: Count the frequency of each character in the input file
            Map<Character, Integer> frequencyMap = getCharacterFrequency(inputFilePath);

            // Step 2: Build Huffman tree
            HuffmanNode root = buildHuffmanTree(frequencyMap);

            // Step 3: Build code table
            Map<Character, String> codeTable = buildCodeTable(root);

            // Step 4: Compress the input file
            compress(inputFilePath, outputFilePath, codeTable);

            System.out.println("File compressed successfully.");
        } catch (IOException e) {
            System.out.println("An error occurred while compressing the file: " + e.getMessage());
        }
    }

    private static Map<Character, Integer> getCharacterFrequency(String inputFilePath) throws IOException {
        Map<Character, Integer> frequencyMap = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(inputFilePath), StandardCharsets.UTF_8))) {
            int currentChar;
            while ((currentChar = reader.read()) != -1) {
                char character = (char) currentChar;
                frequencyMap.put(character, frequencyMap.getOrDefault(character, 0) + 1);
            }
        }

        return frequencyMap;
    }

    private static HuffmanNode buildHuffmanTree(Map<Character, Integer> frequencyMap) {
        PriorityQueue<HuffmanNode> minHeap = new PriorityQueue<>();

        // Create a leaf node for each character and add it to the min heap
        for (Map.Entry<Character, Integer> entry : frequencyMap.entrySet()) {
            char character = entry.getKey();
            int frequency = entry.getValue();
            minHeap.add(new HuffmanNode(character, frequency, null, null));
        }

        // Build the Huffman tree by combining the nodes from the min heap
        while (minHeap.size() > 1) {
            HuffmanNode left = minHeap.poll();
            HuffmanNode right = minHeap.poll();
            HuffmanNode newNode = new HuffmanNode('\0', left.frequency + right.frequency, left, right);
            minHeap.add(newNode);
        }

        return minHeap.poll();
    }

    private static Map<Character, String> buildCodeTable(HuffmanNode root) {
        Map<Character, String> codeTable = new HashMap<>();
        buildCodeTableHelper(root, "", codeTable);
        return codeTable;
    }

    private static void buildCodeTableHelper(HuffmanNode node, String code, Map<Character, String> codeTable) {
        if (node.isLeafNode()) {
            codeTable.put(node.character, code);
        } else {
            buildCodeTableHelper(node.left, code + "0", codeTable);
            buildCodeTableHelper(node.right, code + "1", codeTable);
        }
    }

    private static void compress(String inputFilePath, String outputFilePath, Map<Character, String> codeTable)
            throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(inputFilePath), StandardCharsets.UTF_8));
                BitOutputStream writer = new BitOutputStream(new FileOutputStream(outputFilePath))) {

            int currentChar;
            while ((currentChar = reader.read()) != -1) {
                char character = (char) currentChar;
                String code = codeTable.get(character);
                for (int i = 0; i < code.length(); i++) {
                    writer.writeBit(code.charAt(i) - '0');
                }
            }
        }
    }

    private static class BitOutputStream implements Closeable {
        private OutputStream outputStream;
        private int buffer;
        private int bitsWritten;

        public BitOutputStream(OutputStream outputStream) {
            this.outputStream = outputStream;
            this.buffer = 0;
            this.bitsWritten = 0;
        }

        public void writeBit(int bit) throws IOException {
            buffer <<= 1;
            buffer |= bit;
            bitsWritten++;

            if (bitsWritten == BUFFER_SIZE) {
                outputStream.write(buffer);
                buffer = 0;
                bitsWritten = 0;
            }
        }

        public void close() throws IOException {
            if (bitsWritten > 0) {
                buffer <<= (BUFFER_SIZE - bitsWritten);
                outputStream.write(buffer);
            }
            outputStream.close();
        }
    }
}

public class Main {
    public static void main(String[] args) {
        String inputFilePath = "input.txt";// Taking input
        String outputFilePath = "input2.txt"// file Saved Here After Compression
                + "";

        HuffmanCoding.compressFile(inputFilePath, outputFilePath);
    }
}