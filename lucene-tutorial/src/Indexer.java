import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class Indexer {

   private IndexWriter writer;

   public Indexer(String indexDirectoryPath) throws IOException {
      Directory indexDirectory = FSDirectory.open(new File(indexDirectoryPath).toPath());

      writer = new IndexWriter(indexDirectory, new IndexWriterConfig());
   }

   public void close() throws CorruptIndexException, IOException {
      writer.close();
   }

   private Document getDocument(File file) throws IOException {
      Document document = new Document();

      TextField contentField = new TextField("contents", new FileReader(file));
      TextField fileNameField = new TextField("filename", file.getName(), Field.Store.YES);
      TextField filePathField = new TextField("filepath", file.getCanonicalPath(), Field.Store.YES);

      document.add(contentField);
      document.add(fileNameField);
      document.add(filePathField);

      return document;
   }

   private void indexFile(File file) throws IOException {
      System.out.println("Indexing " + file.getCanonicalPath());
      Document document = getDocument(file);
      writer.addDocument(document);
   }

   public int createIndex(String dataDirPath, FileFilter filter)
         throws IOException {
      File[] files = new File(dataDirPath).listFiles();

      for (File file : files) {
         if (!file.isDirectory()
               && !file.isHidden()
               && file.exists()
               && file.canRead()
               && filter.accept(file)) {
            indexFile(file);
         }
      }
      return writer.numRamDocs();
   }
}