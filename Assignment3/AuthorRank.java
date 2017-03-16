import edu.uci.ics.jung.algorithms.scoring.PageRank;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.io.PajekNetReader;
import org.apache.commons.collections15.FactoryUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
public class AuthorRank {
    public static void main(String[] args) throws IOException {
    	HashMap<String, String> nodeFromLabel =new HashMap<>();
		HashMap<String, String> labelFromNode =new HashMap<>();
		
		BufferedReader br = new BufferedReader(new FileReader
                                                       ("/Users/sanjanaagarwal/Desktop/Search/assignment3/author.net"));
		StringBuilder sb = new StringBuilder();
		String line = br.readLine();

		line=br.readLine();
		int count=1;

		while (count != 2001) {
			sb.append(line);
			sb.append(System.lineSeparator());
			String s[] = line.split(" ");
			int start = s[1].indexOf('"');
			int end = s[1].lastIndexOf('"');
			String s1 = s[1].substring(start+1, end);
			String s2 = s[0];
			nodeFromLabel.put(s1, s2);
			labelFromNode.put(s2, s1);
			line = br.readLine();
			count++;
		}
		br.close();

        Graph graph = makeGraph();

        PageRank p = new PageRank(graph,0.85);
        p.evaluate();

        HashMap<String,Double> result = new HashMap<>();
		Collection vertexCol = graph.getVertices();
		for (Object vertex : vertexCol) {
			double score=(double) p.getVertexScore(vertex);
			result.put(vertex.toString(), score);
		}

		Map<String,Double> sortedMap=sortByComparator(result);
		int i=0;
        for (Entry<String, Double> entry : sortedMap.entrySet()) {
        	String authorID= labelFromNode.get(entry.getKey());
            System.out.println("Rank " + (i+1) + ": \nAuthor: " + entry.getKey() +
                                       "\nAuthorID: " + authorID + "\nScore: "+ entry.getValue());

            i++;
            if(i==10)
            	break;
        }
    }

    private static Map<String, Double> sortByComparator(Map<String, Double> unsortMap) {
		List<Entry<String, Double>> list = new LinkedList<>(unsortMap.entrySet());
		Collections.sort(list, (o1, o2) -> o2.getValue().compareTo(o1.getValue()));
		Map<String, Double> sortedMap = new LinkedHashMap<>();
		for (Entry<String, Double> entry : list) {
			sortedMap.put(entry.getKey(), entry.getValue());
		}
		return sortedMap;
	}

    private static Graph makeGraph() throws IOException
    {
        PajekNetReader pnr = new PajekNetReader(FactoryUtils.instantiateFactory(Object.class));
        Graph graph = new UndirectedSparseGraph();
        
        pnr.load("/Users/sanjanaagarwal/Desktop/Search/assignment3/author.net", graph);
        return graph;
    }
}