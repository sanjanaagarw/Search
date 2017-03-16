/**
 * Created by sanjanaagarwal on 9/25/16.
 */
package org.ass1;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GenerateIndex {
    private static StringBuilder sbByLine = new StringBuilder();
    private static StringBuilder sbHead = new StringBuilder();
    private static StringBuilder sbText = new StringBuilder();
    private static StringBuilder sbDateLine = new StringBuilder();

    public static void main(String args[]) throws IOException, NullPointerException {
        File docDir = new File(args[0]); // To read from here.
        File indexDir = new File(args[1]);
        Directory fsDir = FSDirectory.open(Paths.get(args[1])); //Index files to write here.
        int analyzerNumber = Integer.parseInt(args[2]);
        //Choose the analyzer you want to run.
        Analyzer analyzer;
        switch (analyzerNumber) {
            case 0: analyzer = new KeywordAnalyzer();
                break;
            case 1: analyzer = new SimpleAnalyzer();
                break;
            case 2: analyzer = new StopAnalyzer();
                break;
            case 3: analyzer = new StandardAnalyzer();
                break;
            default: analyzer = new StandardAnalyzer();
                break;
        }
        IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
        iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        IndexWriter indexWriter = new IndexWriter(fsDir, iwc);
        generateIndex(docDir, fsDir, indexWriter);
        numberOfDocs(indexDir);
    }

    /**
     * Generates the index for the trec files.
     * */
    static void generateIndex(File docDir, Directory fsDir, IndexWriter indexWriter) throws IOException{
        try {
            System.out.println("Please wait, the program is running ...");
            Pattern pattern, pattern1, pattern2;
            Matcher matcher, matcher2, matcher1;
            String line;
             for(File f: docDir.listFiles()) {
                 // System.out.println("f is: "+f);
                if (!String.valueOf(f).contains(".DS_Store")) {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new
                                                                     FileInputStream(String.valueOf(f)), "UTF8"));
                    while ((line = bufferedReader.readLine())!=null) {
                        sbHead.setLength(0);
                        sbHead.trimToSize();
                        sbByLine.setLength(0);
                        sbByLine.trimToSize();
                        sbText.setLength(0);
                        sbText.trimToSize();
                        sbDateLine.setLength(0);
                        sbDateLine.trimToSize();
                        Document luceneDoc = new Document();
                        while (!line.contentEquals("</DOC>")) {
                            if (line.startsWith("<DOCNO>")) {
                                pattern = Pattern.compile("<DOCNO>(.+?)</DOCNO>");
                                matcher = pattern.matcher(line);
                                matcher.find();
                                luceneDoc.add(new StringField("DOCNO", matcher.group(1).trim(), Field.Store.YES));
                            } else if(line.startsWith("<HEAD>" )) {
                                pattern1 = Pattern.compile("<HEAD>(.+?)</HEAD>");
                                pattern2 = Pattern.compile("\\s*\\s+(.*)</HEAD>", Pattern.DOTALL);
                                matcher1 = pattern1.matcher(line);
                                matcher2 = pattern2.matcher(line);
                                if (matcher1.find())
                                    sbHead.append(matcher1.group(1)).append(". ");
                                else if (matcher2.find()) sbHead.append(matcher2.group(1)).append(". ");
                            } else if(line.startsWith("<BYLINE>")) {
                                pattern1 = Pattern.compile("<BYLINE>(.+?)</BYLINE>");
                                pattern2 = Pattern.compile("\\s*\\s+(.*)</BYLINE>", Pattern.DOTALL);
                                matcher1 = pattern1.matcher(line);
                                matcher2 = pattern2.matcher(line);
                                if (matcher1.find())
                                    sbByLine.append(matcher1.group(1)).append(". ");
                                else if (matcher2.find()) sbByLine.append(matcher2.group(1)).append(". ");
                            } else if(line.startsWith("<DATELINE>")) {
                                pattern1 = Pattern.compile("<DATELINE>(.+?)</DATELINE>");
                                pattern2 = Pattern.compile("\\s*\\s+(.*)</DATELINE>", Pattern.DOTALL);
                                matcher1 = pattern1.matcher(line);
                                matcher2 = pattern2.matcher(line);
                                if (matcher1.find())
                                    sbDateLine.append(matcher1.group(1)).append(". ");
                                else if (matcher2.find()) sbDateLine.append(matcher2.group(1)).append(". ");
                            } else if(line.startsWith("<TEXT>")) {
                                while(!line.endsWith("</TEXT>")) {
                                    if(!line.contentEquals("<TEXT>") & !line.contentEquals("</TEXT>")) {
                                        sbText.append(line).append("\n");
                                    }
                                    line = bufferedReader.readLine();
                                }
                            }
                            line = bufferedReader.readLine();
                        }
                        luceneDoc.add(new StringField("HEAD", sbHead.toString().trim(), Field.Store.YES));
                        luceneDoc.add(new StringField("BYLINE", sbByLine.toString().trim(), Field.Store.YES));
                        luceneDoc.add(new StringField("DATELINE", sbDateLine.toString().trim(), Field.Store.YES));
                        luceneDoc.add(new TextField("TEXT", sbText.toString().trim(), Field.Store.YES));
                        indexWriter.addDocument(luceneDoc);
                    }
                    bufferedReader.close();
                }
            }
        } catch (NullPointerException |FileNotFoundException e) {
            e.printStackTrace();
        }
        indexWriter.forceMerge(1);
        indexWriter.close();
    }

    /**
     * To print the total number of documents.
     * */
    private static void numberOfDocs(File indexDir) throws IOException{
        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(
                (String.valueOf(indexDir)))));
        System.out.println("The total number of docs in the corpus is: "+reader.maxDoc());
    }
}
