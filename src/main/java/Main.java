import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.vocabulary.RDFS;

class Main {

    static String baseURI = "https://localhost/papers";

    public static void main(String[] args) {
        // some definitions
        final String personURI    =  baseURI + "/person";
        final String fullName     = "John Smith";

        // create an empty Model
        Model model = ModelFactory.createDefaultModel();

        // create the resource
        model.createResource(personURI + "#JohnSmith").
            addProperty(RDFS.label, fullName);

        // write the model in RDF/XML syntax to standard output
        model.write(System.out, "RDF/XML");
    }
}
