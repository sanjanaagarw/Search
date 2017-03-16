package org.ass2;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by sanjanaagarwal on 10/17/16.
 */
public class SearchTRECTopics {
    public static void main (String args[]) throws IOException, ParseException {
        File inputQuery = new File("/Users/sanjanaagarwal/Desktop/Search/Assignment2/topics.51-100");
        String queryArray[] = {"<title>", "<desc>"};
        rankingFunction(inputQuery, queryArray);
    }

    private static void rankingFunction(File inputQuery, String[] queryArray) throws IOException, ParseException {
        String queryString;
        for (int i=0;i<2;i++) {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(inputQuery)));
            String line = bufferedReader.readLine();
            int qID = 51;
            if (queryArray[i].equals("<title>")) {
                while (line != null) {
                    if (line.startsWith(queryArray[i])) {
                        queryString = line.replace(queryArray[i], "").replace("Topic:", "").trim();
                        // System.out.println("Topic is ");
                        // System.out.print( queryString + "\n");
                        // Here perform ranking.
                        System.out.println("Running for topic: " + qID);
                        searchQuery(queryString, queryArray[i], qID);
                        // System.exit(0);
                        qID++;
                        // break;
                        line = bufferedReader.readLine();
                    } else {
                        line = bufferedReader.readLine();
                    }
                }
            } else if (queryArray[i].equals("<desc>")) {
                StringBuilder sb = new StringBuilder();
                while (line != null) {
                    if (line.startsWith(queryArray[i])) {
                        while (!line.startsWith("<smry>")) {
                            sb.append(line).append("\n");
                            line = bufferedReader.readLine();
                        }
                        queryString = sb.toString().replace("<desc>", "").replace("Description:", "").trim();
                        // Here perform ranking.
                        System.out.println("Running for Description: " + qID);
                        searchQuery(queryString, queryArray[i], qID);
                        qID++;

                        // System.out.println("Desc no: " + count + " is:");
                        // System.out.print( queryString + "\n");
                        line = bufferedReader.readLine();
                        sb.setLength(0);
                        sb.trimToSize();
                    } else {
                        line = bufferedReader.readLine();
                    }
                }
            }
        }
    }

    private static void searchQuery(String queryString, String queryType, int qID) throws IOException, ParseException {
        String index = "/Users/sanjanaagarwal/Desktop/Search/Assignment2/index/";
        IndexReader indexReader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
        Analyzer analyzer = new StandardAnalyzer();
        int N = indexReader.maxDoc();
        ClassicSimilarity classicSimilarity = new ClassicSimilarity();
        List<LeafReaderContext> leafReaderContexts = indexReader.getContext().reader().leaves();
        LeafReaderContext leafReaderContext;
        QueryParser queryParser = new QueryParser("TEXT", analyzer);

        HashMap<String, Double> termScore = new HashMap<>();
        Query query = queryParser.parse(QueryParser.escape(queryString));
        Set<Term> termSet = new LinkedHashSet<>();
        indexSearcher.createNormalizedWeight(query, false).extractTerms(termSet);
        double idf;
        for (Term term : termSet) {
            Map<Integer, Integer> countOfTerms = new HashMap<>();
            for (LeafReaderContext leafReaderContext1 : leafReaderContexts) {
                // System.out.println("leafReaderContext1: " + leafReaderContext1);
                leafReaderContext = leafReaderContext1;
                int docStartNo = leafReaderContext.docBase;
                // System.out.println("docStartNo: " + docStartNo);
                int numberOfDoc = leafReaderContext.reader().maxDoc();
                // System.out.println("Number of docs: " + numberOfDoc);
                PostingsEnum postingsEnum = MultiFields.getTermDocsEnum(leafReaderContext.reader(),
                                                                        "TEXT", new BytesRef(term.text()));
                int doc;
                if (postingsEnum != null) {
                    while (((doc = postingsEnum.nextDoc()) != PostingsEnum.NO_MORE_DOCS)) {
                        int noOfTerms = postingsEnum.freq();
                    /*System.out.println("Number of terms: " + noOfTerms
                                               + " docID is: " + (postingsEnum.docID()+docStartNo));*/
                        // System.out.println("docID "+ postingsEnum.docID());
                        // System.out.println("docID  with start no"+ (postingsEnum.docID()+ docStartNo));
                        countOfTerms.put((postingsEnum.docID() + docStartNo), noOfTerms);
                        // System.out.println("Map: "+ countOfTerms);
                    }
                }

                for (int docID = 0; docID < numberOfDoc; docID++) {
                    // System.out.println("no.of doc " + numberOfDoc);
                    if (countOfTerms.get(docID + docStartNo) != null) {
                        double normalizedDocLength = classicSimilarity.decodeNormValue(
                                leafReaderContext.reader().getNormValues("TEXT").get(docID));
                        // System.out.println("The term is: " + term.text());
                        int docFreq = indexReader.docFreq(new Term("TEXT", term.text()));
                        // System.out.println("Number of documents containing the word " + term.text() + " are: " + docFreq);
                        idf = (double) Math.log(1 + (N / docFreq));
                        double docLength = 1 / (normalizedDocLength * normalizedDocLength);
                        double termFrequency = countOfTerms.get(docID + docStartNo) / docLength;
                        double eachTermScore = termFrequency * idf;
                        // System.out.println("The each score for doc: " + (docID + docStartNo) + " is: "+ eachTermScore);
                        termScore.put((indexSearcher.doc(docID + docStartNo).get("DOCNO")),
                                      termScore.getOrDefault((docID + docStartNo), 0.0) + eachTermScore);
                    }
                }
            }
        }
        writeToDoc(termScore, queryString, queryType, qID);
        indexReader.close();
        /*for (Map.Entry<String, Double> entry : termScore.entrySet()) {
            System.out.println("For the query (" + queryString + ") the doc no is: "
                                       + entry.getKey() + " the term score is: " + entry.getValue());
        }*/
    }

    private static void writeToDoc (HashMap<String, Double> termScore, String queryString, String queryType, int qID)
            throws IOException {
        Map<String, Double> sortedMap = sortByComparator(termScore);
        PrintWriter printWriter1 = new PrintWriter(new BufferedWriter(new FileWriter("/Users/sanjanaagarwal" +
                                     "/Desktop/Search/Assignment2/results/resultsShortQuery", true)));
        PrintWriter printWriter2 = new PrintWriter(new BufferedWriter(new FileWriter("/Users/sanjanaagarwal" +
                                     "/Desktop/Search/Assignment2/results/resultsLongQuery", true)));

        if (queryType.equals("<title>")) {
                int count = 1;
                StringBuilder stringBuilder = new StringBuilder();
                for (Map.Entry<String, Double> entry : sortedMap.entrySet()) {
                    if (count < 1001) {
                        stringBuilder.append(qID).append(" Q0 ").append(entry.getKey()).
                                append(" ").append(count).append(" ").append(entry.getValue()).
                                append(" ").append("run-1-short");
                        // System.out.print(stringBuilder.toString());
                        printWriter1.println(stringBuilder.toString());
                        count+=1;
                        stringBuilder.setLength(0);
                        stringBuilder.trimToSize();
                    } else {
                        break;
                    }
                }
        } else if (queryType.equals("<desc>")) {
                int count = 1;
                StringBuilder stringBuilder = new StringBuilder();
                for (Map.Entry<String, Double> entry : sortedMap.entrySet()) {
                    if (count < 1001) {
                        stringBuilder.append(qID).append(" Q0 ").append(entry.getKey()).
                                append(" ").append(count).append(" ").append(entry.getValue()).
                                append(" ").append("run-1-long");
                        // System.out.print(stringBuilder.toString());
                        printWriter2.println(stringBuilder.toString());
                        count+=1;
                        stringBuilder.setLength(0);
                        stringBuilder.trimToSize();
                    } else {
                        break;
                    }
                }
        }
        printWriter1.close();
        printWriter2.close();
    }
    private static Map<String, Double> sortByComparator(Map<String, Double> unsortedMap) {
        List<Map.Entry<String, Double>> list = new LinkedList<>(unsortedMap.entrySet());

        Collections.sort(list, (o1, o2) -> o2.getValue().compareTo(o1.getValue()));
        /*
        Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {
            public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });
        */
        Map<String, Double> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<String, Double> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        return sortedMap;
    }
}