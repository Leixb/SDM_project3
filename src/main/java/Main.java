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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

class Main {

    private static Model model = null;

    private static final String baseURI = "http://www.semanticweb.org/emmasalvan/ontologies/2022/4/sdmlab3#";
    private static String outputFilename = "papers.rdf";

    private static interface ModelMember {
        public void addToModel();
    }

    private static class NodeFile implements ModelMember {
        String name, filename;

        public NodeFile(String nodeName, String filename) {
            this.name = nodeName;
            this.filename = filename;
        }

        // Parse CSV file and add resouces with properites to the model
        public void addToModel() {

            Iterator<String[]> csvRows = getCSVReader(filename).iterator();

            String objectURI = baseURI + name;

            List<Property> properties = Stream.of(csvRows.next()) // Take header as first line
                .skip(1) //Skip the first column which is the ID
                .map(s -> model.createProperty(baseURI + s)) // Create property for each column
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

                Resource object = model.createResource(objectURI + "-" + id);

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

    private static class EdgeFile implements ModelMember {
        String name, source, destination, filename;

        public EdgeFile(String name, String source, String destination, String filename) {
            this.name = name;
            this.source = source;
            this.destination = destination;
            this.filename = filename;
        }

        public void addToModel() {
            Iterator<String[]> csvRows = getCSVReader(filename).iterator();
            String objectURI = baseURI + name;

            Property property = model.createProperty(objectURI);

            csvRows.forEachRemaining(row -> {
                if (row.length != 2) {
                    return;
                }

                String sourceId = row[0];
                String destId = row[1];

                Resource sourceObject = model.createResource(baseURI + source + "-" + sourceId);
                Resource destObject = model.createResource(baseURI + destination + "-" + destId);

                sourceObject.addProperty(property, destObject);

            });
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

    private static void saveRDF(String filename) {
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(filename));
            model.write(out, "RDF/XML");
        } catch (IOException e) {
            System.err.println("ERROR: Could not write to file " + filename);
            e.printStackTrace();
        }
    }

    private static List<ModelMember> parseArgs(String[] args) {
        List<ModelMember> modelMembers = new ArrayList<ModelMember>();

        for (String arg : args) {
            if (arg.startsWith("--node=")) {
                String[] parts = arg.split("=");
                if (parts.length != 3) {
                    System.err.println("ERROR: Node arguments require 2 parameters: --node=nodeName=filename.csv");
                    System.exit(1);
                }
                String nodeName = parts[1];
                String filename = parts[2];
                if (!Files.exists(java.nio.file.Paths.get(filename))) {
                    System.err.println("ERROR: Node file " + filename + " does not exist");
                    System.exit(1);
                }

                modelMembers.add(new NodeFile(nodeName, filename));
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
                if (!Files.exists(java.nio.file.Paths.get(filename))) {
                    System.err.println("ERROR: Edge file " + filename + " does not exist");
                    System.exit(1);
                }

                modelMembers.add(new EdgeFile(edgeName, sourceName, destName, filename));
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

        if (modelMembers.isEmpty()) {
            System.err.println("ERROR: please specify at least one node or edge file to process");
            System.exit(1);
        }

        return modelMembers;
    }

    private static Model loadSchema() {
        return ModelFactory.createDefaultModel().read(Main.class.getResourceAsStream("/sdmlab3.owl"), null, "RDF/XML");
    }

    // Program entrypoint
    //
    // Usage: java -jar papers.jar [--node=nodename=filename.csv]... [--edge=relationShipName=sourceName=destName=filename.csv]... [--output=output_file]
    public static void main(String[] args) {

        List<ModelMember> modelMembers = parseArgs(args);

        // Create empty model
        model = ModelFactory.createDefaultModel();
        model.setNsPrefix("", baseURI);

        // Populate model with data from files in the modelMembers list
        modelMembers.forEach(ModelMember::addToModel);

        // Merge schema into model
        Model schema = loadSchema();
        model = ModelFactory.createUnion(schema, model);

        // Alternatively, we could use createRDFSModel() to merge our model with
        // the schema and infer all the data, but it's much slower:
        // model = ModelFactory.createRDFSModel(model, schema);

        // Write model to file
        saveRDF(outputFilename);
    }
}
