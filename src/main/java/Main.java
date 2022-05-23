import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Property;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

class Main {

    private static final String baseURI = "https://localhost/papers";
    private static String outputFilename = "papers.rdf";

    private static class Node {
        public String name, filename;

        public Node(String nodeName, String filename) {
            this.name = nodeName;
            this.filename = filename;
        }
    }

    private static class Edge {
        public String name, source, destination, filename;

        public Edge(String name, String source, String destination, String filename) {
            this.name = name;
            this.source = source;
            this.destination = destination;
            this.filename = filename;
        }
    }

    private static Model model = null;

    // Program entrypoint
    //
    // Usage: java -jar papers.jar [--node=nodename=filename.csv]... [--edge=relationShipName=sourceName=destName=filename.csv]... [--output=output_file]
    public static void main(String[] args) {
        model = ModelFactory.createDefaultModel();

        List<Node> nodes = new ArrayList<Node>();
        List<Edge> edges = new ArrayList<Edge>();

        for (String arg : args) {
            if (arg.startsWith("--node=")) {
                String[] parts = arg.split("=");
                if (parts.length != 3) {
                    System.err.println("ERROR: Node arguments require 2 parameters: --node=nodeName=filename.csv");
                    System.exit(1);
                }
                String nodeName = parts[1];
                String filename = parts[2];
                nodes.add(new Node(nodeName, filename));
            } else if (arg.startsWith("--edge=")) {
                String[] parts = arg.split("=");
                if (parts.length != 5) {
                    System.err.println("ERROR: Edge arguments require 4 parameters: --edge=relationShipName=sourceName=destName=filename.csv");
                    System.exit(1);
                }

                String edgeName = parts[1];
                String sourceName = parts[2];
                String destName = parts[3];
                String filename = parts[4];
                // addEdges(edgeName, sourceName, destName, filename);
                edges.add(new Edge(edgeName, sourceName, destName, filename));
            } else if (arg.startsWith("--output=")) {
                String[] parts = arg.split("=");
                if (parts.length > 1) {
                    outputFilename = parts[1];
                }
            } else {
                System.out.println("Unknown argument: " + arg);
                System.exit(1);
            }

        }


        nodes.forEach(n -> addNodes(n.name, n.filename));
        // edges.forEach(e -> addEdges(e.name, e.source, e.destinatio, e.filename));

        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(outputFilename));
            model.write(out, "RDF/XML");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static CSVReader getCSVReader(String filename) {
        CSVReader reader = null;
        try {
            reader = new CSVReaderBuilder(new BufferedReader(new FileReader(filename)))
                .withCSVParser(new CSVParserBuilder().withSeparator(';').build()).build();
        } catch (FileNotFoundException e) {
            System.err.println("ERROR: File not found: " + filename);
            e.printStackTrace();
            System.exit(1);
        }

        return reader;
    }

    // Parse CSV file and add resouces with properites to the model
    private static void addNodes(String nodeName, String filename) {

        Iterator<String[]> csvRows = getCSVReader(filename).iterator();

        String objectURI = baseURI + "/" + nodeName;

        List<Property> properties = Stream.of(csvRows.next()) // Take header as first line
            .skip(1) //Skip the first column which is the ID
            .map(s -> model.createProperty(baseURI + "/" + s)) // Create property for each column
            .collect(java.util.stream.Collectors.toList());

        csvRows.forEachRemaining(row -> {
            Iterator<String> it = Arrays.asList(row).iterator();
            if (!it.hasNext()) {
                return;
            }

            String id = it.next().trim();
            if (id.isEmpty()) {
                return;
            }

            Resource object = model.createResource(objectURI + "#" + id);

            properties.forEach(p -> {
                if (!it.hasNext()) {
                    return;
                }

                String value = it.next();
                if (value != null) {
                    object.addProperty(p, value);
                }
            });

        });

    }
}
