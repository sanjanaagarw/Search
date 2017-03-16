package org.ass2;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.search.similarities.LMJelinekMercerSimilarity;
import org.apache.lucene.store.FSDirectory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.Scanner;

/**
 * Created by sanjanaagarwal on 10/17/16.
 */
public class CompareAlgorithms {
    public static void main (String args[])  throws IOException, ParseException {
        File inputQuery = new File("/Users/sanjanaagarwal/Desktop/Search/Assignment2/topics.51-100");
        String queryArray[] = {"<title>", "<desc>"};
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter your choice of Similarity (D, BM, LMD, LMJ)");
        String similarity = sc.nextLine().toUpperCase();
        rankingFunction(inputQuery, queryArray, similarity);
    }
    private static void rankingFunction(File inputQuery, String[] queryArray, String similarity) throws IOException, ParseException {
        String queryString;
        for (int i=0;i<2;i++) {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(inputQuery)));
            String line = bufferedReader.readLine();
            int qID = 51;
            if (queryArray[i].equals("<title>")) {
                while (line != null) {
                    if (line.startsWith(queryArray[i])) {
                        queryString = line.replace(queryArray[i], "").replace("Topic:", "").trim();
                        compareAlgoriths(queryString, qID, queryArray[i], similarity);
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
                        compareAlgoriths(queryString, qID, queryArray[i], similarity);
                        qID++;

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

    private static void compareAlgoriths(String queryString, int qID, String queryType, String similarity)
            throws IOException, ParseException {

        switch (similarity) {
            case "D":
                writeToDocDefault(queryString, qID, queryType);
                break;

            case "BM":
                writeToDocBM(queryString, qID, queryType);
                break;

            case "LMD":
                writeToDocLMD(queryString, qID, queryType);
                break;

            case "LMJ":
                writeToDocLMJ(queryString, qID, queryType);
                break;

            default:
                writeToDocDefault(queryString, qID, queryType);
                break;
        }

    }

    private static void writeToDocDefault(String queryString, int qID, String queryType)
            throws IOException, ParseException {
        String index = "/Users/sanjanaagarwal/Desktop/Search/Assignment2/index/";
        IndexReader indexReader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
        Analyzer analyzer = new StandardAnalyzer();
        indexSearcher.setSimilarity(new ClassicSimilarity());
        QueryParser queryParser = new QueryParser("TEXT", analyzer);

        Query query = queryParser.parse(QueryParser.escape(queryString));

        TopDocs topDocs = indexSearcher.search(query, 1000);
        int noOfHits = topDocs.totalHits;
        System.out.println("Total number of matching documents: "+ noOfHits);
        int count = 1;
        ScoreDoc[] scoreDocs = topDocs.scoreDocs;
        PrintWriter printWriter1 = new PrintWriter(new BufferedWriter(new FileWriter("/Users/sanjanaagarwal" +
                                    "/Desktop/Search/Assignment2/results/DefaultShortQuery", true)));
        PrintWriter printWriter2 = new PrintWriter(new BufferedWriter(new FileWriter("/Users/sanjanaagarwal" +
                                    "/Desktop/Search/Assignment2/results/DefaultLongQuery", true)));
        if (queryType.equals("<title>")) {
            StringBuilder stringBuilder = new StringBuilder();
            for (ScoreDoc scoreDoc : scoreDocs) {
                System.out.println("Document is = " + scoreDoc.doc + " score = " + scoreDoc.score);
                Document document = indexSearcher.doc(scoreDoc.doc);
                stringBuilder.append(qID).append(" Q0 ").append(document.get("DOCNO")).
                        append(" ").append(count).append(" ").append(scoreDoc.score).append(" run-1-DefaultShort");
                printWriter1.println(stringBuilder.toString());
                count+=1;
                stringBuilder.setLength(0);
                stringBuilder.trimToSize();
            }
        } else if (queryType.equals("<desc>")) {
            StringBuilder stringBuilder = new StringBuilder();
            for (ScoreDoc scoreDoc : scoreDocs) {
                System.out.println("Document is = " + scoreDoc.doc + " score = " + scoreDoc.score);
                Document document = indexSearcher.doc(scoreDoc.doc);
                stringBuilder.append(qID).append(" Q0 ").append(document.get("DOCNO")).
                        append(" ").append(count).append(" ").append(scoreDoc.score).append(" run-1-DefaultLong");
                printWriter2.println(stringBuilder.toString());
                count+=1;
                stringBuilder.setLength(0);
                stringBuilder.trimToSize();
            }
        }
        printWriter1.close();
        printWriter2.close();
    }

    private static void writeToDocBM(String queryString, int qID, String queryType) throws IOException, ParseException {
        String index = "/Users/sanjanaagarwal/Desktop/Search/Assignment2/index/";
        IndexReader indexReader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
        Analyzer analyzer = new StandardAnalyzer();
        indexSearcher.setSimilarity(new BM25Similarity());
        QueryParser queryParser = new QueryParser("TEXT", analyzer);

        Query query = queryParser.parse(QueryParser.escape(queryString));

        TopDocs topDocs = indexSearcher.search(query, 1000);
        int noOfHits = topDocs.totalHits;
        System.out.println("Total number of matching documents: "+ noOfHits);
        int count = 1;
        ScoreDoc[] scoreDocs = topDocs.scoreDocs;
        PrintWriter printWriter3 = new PrintWriter(new BufferedWriter(new FileWriter("/Users/sanjanaagarwal" +
                                             "/Desktop/Search/Assignment2/results/BM25ShortQuery", true)));
        PrintWriter printWriter4 = new PrintWriter(new BufferedWriter(new FileWriter("/Users/sanjanaagarwal" +
                                             "/Desktop/Search/Assignment2/results/BM25LongQuery", true)));
        if (queryType.equals("<title>")) {
            StringBuilder stringBuilder = new StringBuilder();
            for (ScoreDoc scoreDoc : scoreDocs) {
                System.out.println("Document is = " + scoreDoc.doc + " score = " + scoreDoc.score);
                Document document = indexSearcher.doc(scoreDoc.doc);
                stringBuilder.append(qID).append(" Q0 ").append(document.get("DOCNO")).
                        append(" ").append(count).append(" ").append(scoreDoc.score).append(" run-1-BM25Short");
                printWriter3.println(stringBuilder.toString());
                count+=1;
                stringBuilder.setLength(0);
                stringBuilder.trimToSize();
            }
        } else if (queryType.equals("<desc>")) {
            StringBuilder stringBuilder = new StringBuilder();
            for (ScoreDoc scoreDoc : scoreDocs) {
                System.out.println("Document is = " + scoreDoc.doc + " score = " + scoreDoc.score);
                Document document = indexSearcher.doc(scoreDoc.doc);
                stringBuilder.append(qID).append(" Q0 ").append(document.get("DOCNO")).
                        append(" ").append(count).append(" ").append(scoreDoc.score).append(" run-1-BM25Long");
                printWriter4.println(stringBuilder.toString());
                count+=1;
                stringBuilder.setLength(0);
                stringBuilder.trimToSize();
            }
        }
        printWriter3.close();
        printWriter4.close();
    }

    private static void writeToDocLMD(String queryString, int qID, String queryType) throws IOException, ParseException {
        String index = "/Users/sanjanaagarwal/Desktop/Search/Assignment2/index/";
        IndexReader indexReader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
        Analyzer analyzer = new StandardAnalyzer();
        indexSearcher.setSimilarity(new LMDirichletSimilarity());
        QueryParser queryParser = new QueryParser("TEXT", analyzer);

        Query query = queryParser.parse(QueryParser.escape(queryString));

        TopDocs topDocs = indexSearcher.search(query, 1000);
        int noOfHits = topDocs.totalHits;
        System.out.println("Total number of matching documents: "+ noOfHits);
        int count = 1;
        ScoreDoc[] scoreDocs = topDocs.scoreDocs;
        PrintWriter printWriter5 = new PrintWriter(new BufferedWriter(new FileWriter("/Users/sanjanaagarwal" +
                                       "/Desktop/Search/Assignment2/results/LMDirichletShortQuery", true)));
        PrintWriter printWriter6 = new PrintWriter(new BufferedWriter(new FileWriter("/Users/sanjanaagarwal" +
                                       "/Desktop/Search/Assignment2/results/LMDirichletLongQuery", true)));
        if (queryType.equals("<title>")) {
            StringBuilder stringBuilder = new StringBuilder();
            for (ScoreDoc scoreDoc : scoreDocs) {
                System.out.println("Document is = " + scoreDoc.doc + " score = " + scoreDoc.score);
                Document document = indexSearcher.doc(scoreDoc.doc);
                stringBuilder.append(qID).append(" Q0 ").append(document.get("DOCNO")).
                        append(" ").append(count).append(" ").append(scoreDoc.score).append(" run-1-LMDShort");
                printWriter5.println(stringBuilder.toString());
                count+=1;
                stringBuilder.setLength(0);
                stringBuilder.trimToSize();
            }
        } else if (queryType.equals("<desc>")) {
            StringBuilder stringBuilder = new StringBuilder();
            for (ScoreDoc scoreDoc : scoreDocs) {
                System.out.println("Document is = " + scoreDoc.doc + " score = " + scoreDoc.score);
                Document document = indexSearcher.doc(scoreDoc.doc);
                stringBuilder.append(qID).append(" Q0 ").append(document.get("DOCNO")).
                        append(" ").append(count).append(" ").append(scoreDoc.score).append(" run-1-LMDLong");
                printWriter6.println(stringBuilder.toString());
                count+=1;
                stringBuilder.setLength(0);
                stringBuilder.trimToSize();
            }
        }
        printWriter5.close();
        printWriter6.close();
    }

    private static void writeToDocLMJ(String queryString, int qID, String queryType)
            throws IOException, ParseException {
        String index = "/Users/sanjanaagarwal/Desktop/Search/Assignment2/index/";
        IndexReader indexReader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
        Analyzer analyzer = new StandardAnalyzer();
        indexSearcher.setSimilarity(new LMJelinekMercerSimilarity((float) 0.7));
        QueryParser queryParser = new QueryParser("TEXT", analyzer);

        Query query = queryParser.parse(QueryParser.escape(queryString));

        TopDocs topDocs = indexSearcher.search(query, 1000);
        int noOfHits = topDocs.totalHits;
        System.out.println("Total number of matching documents: "+ noOfHits);
        int count = 1;
        ScoreDoc[] scoreDocs = topDocs.scoreDocs;
        PrintWriter printWriter7 = new PrintWriter(new BufferedWriter(new FileWriter("/Users/sanjanaagarwal" +
                                              "/Desktop/Search/Assignment2/results/LMJShortQuery", true)));
        PrintWriter printWriter8 = new PrintWriter(new BufferedWriter(new FileWriter("/Users/sanjanaagarwal" +
                                              "/Desktop/Search/Assignment2/results/LMJLongQuery", true)));
        if (queryType.equals("<title>")) {
            StringBuilder stringBuilder = new StringBuilder();
            for (ScoreDoc scoreDoc : scoreDocs) {
                System.out.println("Document is = " + scoreDoc.doc + " score = " + scoreDoc.score);
                Document document = indexSearcher.doc(scoreDoc.doc);
                stringBuilder.append(qID).append(" Q0 ").append(document.get("DOCNO")).
                        append(" ").append(count).append(" ").append(scoreDoc.score).append(" run-1-LMJShort");
                printWriter7.println(stringBuilder.toString());
                count+=1;
                stringBuilder.setLength(0);
                stringBuilder.trimToSize();
            }
        } else if (queryType.equals("<desc>")) {
            StringBuilder stringBuilder = new StringBuilder();
            for (ScoreDoc scoreDoc : scoreDocs) {
                System.out.println("Document is = " + scoreDoc.doc + " score = " + scoreDoc.score);
                Document document = indexSearcher.doc(scoreDoc.doc);
                stringBuilder.append(qID).append(" Q0 ").append(document.get("DOCNO")).
                        append(" ").append(count).append(" ").append(scoreDoc.score).append(" run-1-LMJLong");
                printWriter8.println(stringBuilder.toString());
                count+=1;
                stringBuilder.setLength(0);
                stringBuilder.trimToSize();
            }
        }
        printWriter7.close();
        printWriter8.close();
    }
}