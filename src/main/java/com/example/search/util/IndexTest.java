package com.example.search.util;

import com.example.search.vo.ParseVo;
import info.monitorenter.cpdetector.io.*;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * option+enter 导入包
 */
public class IndexTest {
    /**
     *
     */
    private static final String indexPath = "/Users/yichuan/Documents/test";
    private static String filePdfPath = "/Users/yichuan/Documents/test/从Lucene到Elasticsearch：全文检索实战 (姚攀) (z-lib.org).pdf";
    private static String htmlPath = "/Users/yichuan/Documents/test/今日头条.html";

    /**
     * @param args
     */
    public static void main(String[] args) {
        try {
            //String path1 = "/Users/yichuan/Documents/test/世界美术全集绘画卷 by 樊文龙扫描版 z-lib.pdf";
            String path1 = "/Users/yichuan/Documents/test/Spring Cloud Alibaba 微服务原理与实战 (谭锋) (z-lib.org).pdf";
            createIndex(path1);
            System.out.println("==========查询=========");
            readIndex("微服务");
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public static String getFileCharset(String filePath) throws Exception {
        File file = new File(filePath);
        CodepageDetectorProxy detector = CodepageDetectorProxy.getInstance();
        detector.add(new ParsingDetector(false));
        detector.add(JChardetFacade.getInstance());
        detector.add(ASCIIDetector.getInstance());
        detector.add(UnicodeDetector.getInstance());
        Charset charset = null;
        try {
            charset = detector.detectCodepage(file.toURI().toURL());
            System.out.println("charset get name:\t" + charset.name());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        String charsetName = "GBK";
        if (charset != null) {
            if (charset.name().equals("US-ASCII")) {
                charsetName = "ISO_8859_1";
            } else if (charset.name().startsWith("UTF")) {
                charsetName = charset.name();// 例如:UTF-8,UTF-16BE.
            }
        }
        return charsetName;
    }

    public static String readHtml(String urlString) {
        StringBuffer content = new StringBuffer("");
        File file = new File(urlString);
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            // 读取页面
            //这里的字符编码要注意，要对上html头文件的一致，否则会出乱码
            // BufferedReader reader = new BufferedReader(new InputStreamReader(fis, "utf-8"));
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis, StandardCharsets.UTF_8));
            String line = null;
            while ((line = reader.readLine()) != null) {
                content.append(line + "\n");
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        String contentString = content.toString();
        return contentString;
    }

    /**
     * @return
     * @throws Exception
     */
    private static String readFileContent() throws Exception, IOException {
        StringBuilder stringBuilder = new StringBuilder();
        String filePath = "/Users/yichuan/Documents/test/住宅设计解剖书.pdf";
        FileReader fileReader = new FileReader(new File(filePath), Charset.defaultCharset());
        int bytes = fileReader.read();
        while (bytes != -1) {
            bytes = fileReader.read();
            System.out.println("" + (char) bytes);
            stringBuilder.append((char) bytes);
        }
        fileReader.close();
        return stringBuilder.toString();
    }

    /**
     * @return
     * @throws Exception
     */
    private static String readFileContent2() throws Exception {

        String filePath = "/Users/yichuan/Documents/test/心理学与生活.pdf";
        StringBuilder stringBuilder = new StringBuilder();
        String charset = getFileCharset(filePath);
        System.out.println("文件编码格式:" + charset);
//        Charset.defaultCharset()
        FileReader fileReader = new FileReader(new File(filePath), Charset.forName(charset));
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        String content = null;
        //按行读取
        while ((content = bufferedReader.readLine()) != null) {
            //判断为空
            if (!content.isEmpty()) {
                stringBuilder.append(content);
                System.out.println(content);
            }
        }
        bufferedReader.close();
        fileReader.close();
        return stringBuilder.toString();
    }

    /**
     * 读取索引
     */
    private static void readIndex(final String queryKey) throws Exception {

        // 索引目录对象
        Directory directory = FSDirectory.open(Paths.get(indexPath));
        // 索引读取工具
        boolean flag = DirectoryReader.indexExists(directory);
        if (!flag) {
            System.out.println("索引不存在");
        }
        // 索引读取工具
        IndexReader reader = DirectoryReader.open(directory);
        // 索引搜索工具
        IndexSearcher searcher = new IndexSearcher(reader);
        //如果想同时匹配多个
        QueryParser parser = new MultiFieldQueryParser(new String[]{"title", "content", "id", "contentType"}, new StandardAnalyzer());
        // 创建查询对象
        Query query = parser.parse(queryKey);
        TopDocs topDocs = searcher.search(query, 20);
        // 获取总条数
        System.out.println("本次搜索共找到 " + topDocs.totalHits + " 条数据");
        // 获取得分文档对象（ScoreDoc）数组.SocreDoc中包含：文档的编号、文档的得分
        ScoreDoc[] scoreDocs = topDocs.scoreDocs;
        for (ScoreDoc scoreDoc : scoreDocs) {
            // 取出文档编号
            int docID = scoreDoc.doc;
            // 根据编号去找文档
            Document doc = reader.document(docID);
            System.out.println("id: " + doc.get("id"));
            System.out.println("title: " + doc.get("title"));
            System.out.println("content: " + doc.get("content"));
            System.out.println("contentType: " + doc.get("contentType"));
            // 取出文档得分
            System.out.println("得分： " + scoreDoc.score);
        }
    }

    /**
     * 创建索引
     *
     * @throws Exception
     */
    private static void createIndex(String filePath) throws Exception {
        // 1采集数据
        List<Document> documents = new ArrayList<>();
        ParseVo parseVo = TikaUtil.parsePdf(filePath);
        String content = parseVo.getContent();
        AtomicReference<String> contentType = new AtomicReference<>("");
        parseVo.getMetadataNamesMap().forEach((key, value) -> {
            System.out.println("key:\t" + key);
            System.out.println("value:\t" + value);
            //Content-Type : application/pdf
            if (key.equals("Content-Type")) {
                contentType.set((String) value);

            }
        });
        //全部替换
        content.replaceAll("\n\n\n", "");
        List<String> stringList = Arrays.asList(content.split("\n"));
        System.out.println(stringList.size());
        int i = 0;
        for (String line : stringList) {
            Document document = new Document();
            i++;
            if (!line.isBlank() && !line.endsWith("\n") && line.length() >= 3) {
                document.add(new TextField("id", "id" + String.valueOf(i), Field.Store.YES));
                document.add(new TextField("title", "978-7-111-44565-4", Field.Store.YES));
                document.add(new TextField("content", line.trim(), Field.Store.YES));
                //	pdf:docinfo:created
                document.add(new TextField("contentType", contentType.get(), Field.Store.YES));
                document.add(new TextField("createdTime", parseVo.getMetadataNamesMap().get("pdf:docinfo:created").toString(), Field.Store.YES));
                System.out.println("row:\t" + line.trim());
                documents.add(document);
            }
        }
        //3创建Analyzer分词器,分析文档，对文档进行分词
        Analyzer analyzer = new StandardAnalyzer();
        //4创建Directory对象,声明索引库的位置
        Directory directory = FSDirectory.open(Paths.get(indexPath));
        //5 创建IndexWriteConfig对象，写入索引需要的配置
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        // 设置打开方式：OpenMode.APPEND 会在索引库的基础上追加新索引。OpenMode.CREATE会先清空原来数据，再提交新的索引
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        //6创建IndexWriter写入对象
        IndexWriter indexWriter = new IndexWriter(directory, config);

        // 删除已有索引
        indexWriter.deleteAll();
        indexWriter.addDocuments(documents);
        // 提交
        indexWriter.commit();
        //8释放资源
        indexWriter.close();
    }
}
