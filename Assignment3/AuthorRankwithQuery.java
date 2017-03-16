import edu.uci.ics.jung.algorithms.scoring.PageRankWithPriors;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.io.PajekNetReader;
import org.apache.commons.collections15.FactoryUtils;
import org.apache.commons.collections15.Transformer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class AuthorRankwithQuery {
	public static void main(String args[]) throws Exception {
		HashMap<String, String> nodeToLabel =new HashMap<>();
		HashMap<String, String> labelToNode =new HashMap<>();
		HashMap<String, String> labelToName =new HashMap<>();

		BufferedReader br = new BufferedReader(
		        new FileReader("/Users/sanjanaagarwal/Desktop/Search/assignment3/author.net"));
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
			nodeToLabel.put(s1, s2);
			labelToNode.put(s2, s1);
			line = br.readLine();
			count++;
		}
		br.close();

        File path = new File("/Users/sanjanaagarwal/Desktop/Search/assignment3/author_index/");
		Directory index = FSDirectory.open(path);
        //Paths.get("/Users/sanjanaagarwal/Desktop/Search/assignment3/author_index")
		IndexReader indexReader = DirectoryReader.open(index);
		IndexSearcher indexSearcher = new IndexSearcher(indexReader);
		Analyzer analyzer = new StandardAnalyzer();
		
		int incr = 0;
		while(incr != 2) {
			String queryString;
			if(incr == 0)
				queryString="Data Mining";
			else
				queryString="Information Retrieval";

			Query query = new QueryParser("content", analyzer).parse(queryString);

			indexSearcher.setSimilarity(new BM25Similarity());

			double oldScore, totalsum = 0.0;
			HashMap<String, Double> result = new HashMap<>();
			int hits = 300;
			TopDocs topDocs = indexSearcher.search(query, hits);
			ScoreDoc[] scoreDocs = topDocs.scoreDocs;

            for (ScoreDoc scoreDoc : scoreDocs) {
                totalsum += (double) scoreDoc.score;
                int docId = scoreDoc.doc;
                Document document = indexSearcher.doc(docId);
                oldScore = 0.0;
                if (result.containsKey(document.get("authorid"))) {
                    oldScore = result.get(document.get("authorid"));
                } else {
                    labelToName.put(document.get("authorid"), document.get("authorName"));
                }
                result.put(document.get("authorid"), oldScore + (double) scoreDoc.score);
            }

			HashMap<String, Double> normalResult=new HashMap<>();
			for (Map.Entry<String, Double> entry: result.entrySet()) {
				String vertexID = nodeToLabel.get(entry.getKey());
				normalResult.put(vertexID, entry.getValue()/totalsum);
			}

			for (Integer i=0;i<2000;i++) {
				if (!(normalResult.containsKey(i.toString()))) {
					normalResult.put(i.toString(), (double)0);
				}
			}

			PajekNetReader pnr = new PajekNetReader(FactoryUtils.instantiateFactory(Object.class));
			Graph<Integer, String> graph = new UndirectedSparseGraph<>();
			pnr.load("/Users/sanjanaagarwal/Desktop/Search/assignment3/author.net",graph);

			Transformer<Integer, Double> priors= V -> (double) normalResult.get(V.toString());

			HashMap<String, Double> bestAuthors= new HashMap<>();
			PageRankWithPriors<Integer, String> pageRankWithPriors = new PageRankWithPriors<>(graph,priors, 0.85);
			pageRankWithPriors.evaluate();

			for (Object vertex : graph.getVertices()) {
				double score = pageRankWithPriors.getVertexScore(Integer.parseInt(vertex.toString()));
				bestAuthors.put(vertex.toString(), score);
			}

			Map<String,Double> bestAuthorMap = sortByComparator(bestAuthors);
			int i=0;
			for (Entry<String, Double> entry : bestAuthorMap.entrySet()) {
				String authorID= labelToNode.get(entry.getKey());
				String authorName = labelToName.get(authorID);
				System.out.println("Rank " + (i+1) + "\nAuthor ID: " + authorID + "\nAuthor Name: " +
										   authorName + "\nScore: " + entry.getValue());
				i++;
				if(i==10)
					break;
			}
			incr++;
			System.out.println("\n");
		}
		indexReader.close();
	}

	private static Map<String, Double> sortByComparator(Map<String, Double> unsortMap) {
		List<Entry<String, Double>> list = new LinkedList<>(unsortMap.entrySet());
		Collections.sort(list, (o1, o2) -> o2.getValue().compareTo(o1.getValue()));
		Map<String, Double> sortedMap = new LinkedHashMap<>();
		for (Entry<String, Double> entry : list)
		{
			sortedMap.put(entry.getKey(), entry.getValue());
		}
		return sortedMap;
	}
}