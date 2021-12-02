import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.rulesys.GenericRuleReasoner;
import org.apache.jena.reasoner.rulesys.Rule;
import org.apache.jena.riot.RDFDataMgr;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import static org.apache.jena.ontology.OntModelSpec.OWL_MEM_MICRO_RULE_INF;

public class Ontology {
    protected static String FLY_ONTOLOGY_FILE = "fly.owl";

    protected Model model = null;
    protected OntModel inf = null;
    Reasoner reasoner = null;
    InfModel infModel = null;

    protected Model readModel(String modelFile)
    {
        // create an empty model
        Model model = ModelFactory.createDefaultModel();
        String inputFileName=modelFile;
        // use the RDFDataMgr to find the input file
        InputStream in = RDFDataMgr.open( inputFileName );
        if (in == null) {
            throw new IllegalArgumentException(
                    "File: " + inputFileName + " not found");
        }

        // read the RDF/XML file
        model.read(in, null);
        return model;
    }

    protected static String readStream(InputStream is) {
        StringBuilder sb = new StringBuilder(512);
        try {
            Reader r = new InputStreamReader(is, "UTF-8");
            int c = 0;
            while ((c = r.read()) != -1) {
                sb.append((char) c);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return sb.toString();
    }

    public Ontology()
    {
       model = readModel(FLY_ONTOLOGY_FILE);

        InputStream stream = getClass().getClassLoader().getResourceAsStream("fly.rules");
        String rules = readStream( stream);
        //System.out.print(rules);
        reasoner = new GenericRuleReasoner(Rule.parseRules(rules));
        reasoner.setDerivationLogging(true);
        reasoner.setDerivationLogging(true);
        reasoner.bindSchema(model);

       inf = ModelFactory.createOntologyModel( OWL_MEM_MICRO_RULE_INF, model);
    }

    public Resource addObject(String id)
    {
        String queryString = "PREFIX fo: <http://www.semanticweb.org/dns/ontologies/2021/10/fly#> " +
                "SELECT ?o " +
                " WHERE { "+
                "?o fo:hasID  \"" + id +"\"" +
                " }";

        //String queryString = "SELECT ?problem ?parameter WHERE {?problem <http://www.semanticweb.org/problem-ontology#hasParameter> ?parameter }";
        Query query = QueryFactory.create(queryString);
        //InfModel infModel = ModelFactory.createInfModel(reasoner, inf);
        QueryExecution qExec = QueryExecutionFactory.create(query, inf);
        ResultSet rs = qExec.execSelect();
        if (! (rs.hasNext()) ) {

            Individual fly = inf.createIndividual(inf.createResource());
            fly.addOntClass(inf.getOntClass("http://www.semanticweb.org/dns/ontologies/2021/10/fly#PhysicalObject"));
            fly.addProperty(inf.createDatatypeProperty("http://www.semanticweb.org/dns/ontologies/2021/10/fly#hasID"), id);
            fly.addProperty(inf.createDatatypeProperty("http://www.semanticweb.org/dns/ontologies/2021/10/fly#hasSpeed"), inf.createTypedLiteral(0.005) );
            fly.addProperty(inf.createDatatypeProperty("http://www.semanticweb.org/dns/ontologies/2021/10/fly#hasAceleration"), inf.createTypedLiteral(0.05) );
            fly.addProperty(inf.createDatatypeProperty("http://www.semanticweb.org/dns/ontologies/2021/10/fly#isCalculated"), inf.createTypedLiteral(false) );
            fly.addProperty(inf.createDatatypeProperty("http://www.semanticweb.org/dns/ontologies/2021/10/fly#needAcelerate"), inf.createTypedLiteral(false));
        }

        while (rs.hasNext())
        {
            Resource fly = rs.next().get("?o").asResource();
            return fly;
        }
        return null;
    }

    public float getSpeed(String id, boolean accelerate)
    {
        Resource fly = addObject(id);
        //fly.getProperty(inf.getDatatypeProperty("http://www.semanticweb.org/dns/ontologies/2021/10/fly#hasNewSpeed")).remove();//.changeLiteralObject((double) 0);
        infModel = ModelFactory.createInfModel(reasoner, inf);

        //float v1 = fly.getProperty(inf.getDatatypeProperty("http://www.semanticweb.org/dns/ontologies/2021/10/fly#hasSpeed")).getFloat();
        //return v1;

        String queryString = "PREFIX fo: <http://www.semanticweb.org/dns/ontologies/2021/10/fly#> " +
                "SELECT ?v ?a" +
                " WHERE { " +
                "?o a fo:PhysicalObject ."+
                "?o fo:hasID  \"" + id +"\" ." +
                "?o fo:hasSpeed ?v ." +
                "?o fo:hasAceleration ?a ." +
                " }";


        Query query = QueryFactory.create(queryString);
        QueryExecution qExec = QueryExecutionFactory.create(query, infModel);
        ResultSet rs = qExec.execSelect();
        if (rs.hasNext())
        {
            QuerySolution qs = rs.next();
            float v = qs.get("?v").asLiteral().getFloat();
            float a = qs.get("?a").asLiteral().getFloat();
            fly.getProperty(inf.getDatatypeProperty("http://www.semanticweb.org/dns/ontologies/2021/10/fly#hasSpeed")).changeLiteralObject(v);
            fly.getProperty(inf.getDatatypeProperty("http://www.semanticweb.org/dns/ontologies/2021/10/fly#hasAceleration")).changeLiteralObject((accelerate) ? 0.05 : 0);
            fly.getProperty(inf.getDatatypeProperty("http://www.semanticweb.org/dns/ontologies/2021/10/fly#needAcelerate")).changeLiteralObject((accelerate));

            //System.out.println(accelerate ? "acelerate" : "not acelerate");
            return v;
        }
        return 0;

    }

}
