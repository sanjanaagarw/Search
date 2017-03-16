/**
 * Created by sanjanaagarwal on 9/26/16.
 */
package org.ass1;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;

public class IndexComparison {

    public static void main(String args[]) throws IOException {
        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get((args[0]))));
        noOfDocs(reader);

        Terms vocabulary = MultiFields.getTerms(reader, "TEXT");
        vocabularyDetails(vocabulary);
        stemming(reader);
        stopWords(reader);
        reader.close();
    }

    /**
     * Prints the size of the vocabulary and the the total number of tokens in the field.
     * */
    private static void vocabularyDetails(Terms vocabulary) throws IOException {
        System.out.println("Size of the vocabulary is: "+vocabulary.size());
        System.out.println("Number of tokens for the TEXT field: "+vocabulary.getSumTotalTermFreq());
    }

    /**
     * Prints the total number of documents in the corpus.
     * */
    private static void noOfDocs(IndexReader reader) {
        System.out.println("The total number of docs in the corpus is: "+reader.maxDoc());
    }

    /**
     * To verify whether stemming occurs or not.
     * */
    private static void stemming(IndexReader reader) throws IOException {
        System.out.println("Number of documents containing the term \"ceremony\" for field \"TEXT\": "+
                                   reader.docFreq(new Term("TEXT", "ceremony")));
        System.out.println("Number of documents containing the term \"ceremoni\" for field \"TEXT\": "+
                                   reader.docFreq(new Term("TEXT", "ceremoni")));
        System.out.println("Number of documents containing the term \"very\" for field \"TEXT\": "+
                                   reader.docFreq(new Term("TEXT", "very")));
        System.out.println("Number of documents containing the term \"veri\" for field \"TEXT\": "+
                                   reader.docFreq(new Term("TEXT", "veri")));
    }

    /**
     * To verify whether stop words are included or excluded by an analyzer.
     * */
    private static void stopWords(IndexReader reader) throws IOException {
        System.out.println("Number of documents containing the term \"the\" for field \"TEXT\": "+
                                   reader.docFreq(new Term("TEXT", "the")));
        System.out.println("Number of documents containing the term \"an\" for field \"TEXT\": "+
                                   reader.docFreq(new Term("TEXT", "an")));
    }
}