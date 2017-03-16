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

import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

/**
 * Created by sanjanaagarwal on 10/17/16.
 */
public class EasySearch {
    public static void main (String args[]) throws IOException, ParseException {
        IndexReader indexReader = DirectoryReader.open(FSDirectory.open(
                Paths.get("/Users/sanjanaagarwal/Desktop/Search/Assignment2/index/")));
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
        Analyzer analyzer = new StandardAnalyzer();
        QueryParser queryParser = new QueryParser("TEXT", analyzer);
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter the query string you want to pass");
        String s = sc.nextLine();
        int N = indexReader.maxDoc();
        System.out.println("Total number of docs in the corpus are: "+ N);
        calculateTermRankingScore(indexReader, indexSearcher, queryParser, N, s);
        calculateQueryRankingScore(indexReader, indexSearcher, queryParser, N, s);
    }
    private static void calculateTermRankingScore(IndexReader indexReader, IndexSearcher indexSearcher,
                                                   QueryParser queryParser, int N, String queryString)
            throws IOException, ParseException {
        System.out.println("Query string: "+ queryString);
        Query query = queryParser.parse(QueryParser.escape(queryString));
        Set<Term> termSet = new LinkedHashSet<>();
        indexSearcher.createNormalizedWeight(query, false).extractTerms(termSet);
        ClassicSimilarity classicSimilarity = new ClassicSimilarity();
        List<LeafReaderContext> leafReaderContexts = indexReader.getContext().reader().leaves();
        LeafReaderContext leafReaderContext;

        for (Term term : termSet) {
            System.out.println("The term is: " + term.text());
            float idf;

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
                // System.out.println("Postings enum " + postingsEnum);
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
                    if (countOfTerms.get(docID + docStartNo) != null) {
                        float normalizedDocLength = classicSimilarity.decodeNormValue(
                                leafReaderContext.reader().getNormValues("TEXT").get(docID));
                        int docFreq = indexReader.docFreq(new Term("TEXT", term.text()));
                        // System.out.println("Number of documents containing the word " + queryString + " are: " + docFreq);
                        idf = (float) Math.log(1 + (N / docFreq));
                        float docLength = 1 / (normalizedDocLength * normalizedDocLength);
                        float termFrequency = countOfTerms.get(docID + docStartNo) / docLength;
                        float eachTermScore = termFrequency * idf;
                    }
                }
            }
        }
        // System.out.println("Ends term score");
    }
    private static void calculateQueryRankingScore(IndexReader indexReader, IndexSearcher indexSearcher,
                                                   QueryParser queryParser, int N, String queryString)
            throws IOException, ParseException {

        // System.out.println("Termset is:" + termSet);
        Map<Integer, Double> termScore = new HashMap<>();
        Set<Term> termSet = new LinkedHashSet<>();
        Query query = queryParser.parse(queryString);
        indexSearcher.createNormalizedWeight(query, false).extractTerms(termSet);

        double idf;

        for (Term term : termSet) {
            Map<Integer, Integer> countOfTerms = new HashMap<>();
            int docFreq = indexReader.docFreq(new Term("TEXT", term.text()));
            System.out.println("Doc freq is: "+ docFreq);
            indexSearcher.createNormalizedWeight(query, false).extractTerms(termSet);
            ClassicSimilarity classicSimilarity = new ClassicSimilarity();
            List<LeafReaderContext> leafReaderContexts = indexReader.getContext().reader().leaves();
            LeafReaderContext leafReaderContext;
            for (LeafReaderContext leafReaderContext1 : leafReaderContexts) {
                System.out.println("The term is: " + term.text());
                leafReaderContext = leafReaderContext1;
                int docStartNo = leafReaderContext.docBase;
                int numberOfDoc = leafReaderContext.reader().maxDoc();
                PostingsEnum postingsEnum = MultiFields.getTermDocsEnum(leafReaderContext.reader(),
                                                                        "TEXT", new BytesRef(term.text()));
                // System.out.println("Postings enum " + postingsEnum);
                int doc;
                if (postingsEnum != null) {
                    while (((doc = postingsEnum.nextDoc()) != PostingsEnum.NO_MORE_DOCS)) {
                        int noOfTerms = postingsEnum.freq();
                        countOfTerms.put((postingsEnum.docID() + docStartNo), noOfTerms);
                    }
                }
                for (int docID = 0; docID < numberOfDoc; docID++) {
                    if (countOfTerms.containsKey(docID + docStartNo)) {
                        double normalizedDocLength = classicSimilarity.decodeNormValue(
                                leafReaderContext.reader().getNormValues("TEXT").get(docID));
                        double docLength = 1 / (normalizedDocLength * normalizedDocLength);
                        // System.out.println("Number of documents containing the word " + queryString + " are: " + docFreq);
                        idf = Math.log(1 + (N / docFreq));
                        // System.out.println("The idf is: "+ idf);
                        double termFrequency = countOfTerms.get(docID + docStartNo) / docLength;
                        double eachTermScore = termFrequency * idf;
                        // System.out.println("The each score for doc: " + (docID + docStartNo) + " is: "+ eachTermScore);
                        termScore.put(((docID + docStartNo)), termScore.getOrDefault((docID + docStartNo),
                                                                                     0.0) + eachTermScore);
                        /*System.out.println("Term score map value for: " + (docID + docStartNo) +
                                                   " is: " + termScore.get(docID + docStartNo));*/
                    }
                }
            }
        }
        for (Map.Entry<Integer, Double> entry : termScore.entrySet()) {
            System.out.println("For the query (" + queryString + ") the doc no is: "
                                       + entry.getKey() + " the term score is: " + entry.getValue());
        }
    }
}
