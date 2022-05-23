import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Property;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import org.apache.commons.io.FilenameUtils;

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

// import org.apache.jena.atlas.csv.CSVParser;

class Main {

    private static final String baseURI = "https://localhost/papers";
    private static final String outputFilename = "papers.rdf";

    private static Model model = null;

    public static void main(String[] args) {
        model = ModelFactory.createDefaultModel();
        for (String file : args) {
            String ext = FilenameUtils.getExtension(file);
            if (ext.equals("csv")) {
                parseCSV(file);
            }
        }

        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(outputFilename));
            model.write(out, "RDF/XML");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Parse CSV file and add resouces with properites to the model
    private static void parseCSV(String csvFile) {

        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new FileReader(csvFile));
        } catch (FileNotFoundException e) {
            System.err.println("File not found: " + csvFile);
            e.printStackTrace();
        }

        CSVReader csvReader = new CSVReaderBuilder(reader)
            .withCSVParser(new CSVParserBuilder()
            .withSeparator(';')
            .build()).build();

        Iterator<String[]> parser = csvReader.iterator();

        String objectURI = baseURI + "/" + FilenameUtils.getBaseName(csvFile);

        List<Property> properties = Stream.of(parser.next()) // Take header as first line
            .skip(1) //Skip the first column which is the ID
            .map(s -> model.createProperty(baseURI + "/" + s)) // Create property for each column
            .collect(java.util.stream.Collectors.toList());

        parser.forEachRemaining(row -> {
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

        try {
            csvReader.close();
            reader.close();
        } catch (IOException e) {
            System.err.println("Error closing file");
            e.printStackTrace();
        }

    }
}
