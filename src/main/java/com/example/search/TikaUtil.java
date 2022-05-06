package com.example.search;

import com.example.search.vo.ParseVo;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.pdf.PDFParser;
import org.apache.tika.parser.pdf.PDFParserConfig;
import org.apache.tika.parser.txt.TXTParser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * 文件内容抽取
 */
public class TikaUtil {

    private static String path2 = "/Users/yichuan/Documents/test/test.txt";
    private static String pathDoc = "/Users/yichuan/Documents/test/test.doc";

    public static void main(String[] args) throws Exception {
        //解析PDF
        String path1 = "/Users/yichuan/Documents/test/画解保时捷揭秘保时捷汽车独门绝技pdf 画解保时捷揭秘保时捷汽车独门绝技pdf z-lib.pdf";
//        getContext(pathDoc);
        ParseVo parseVo = TikaUtil.parsePdf(path1);
        List<String> stringList = Arrays.asList(parseVo.getContent().split("\n"));
        System.out.println(stringList.size());
        for (String line : stringList) {
            if (!line.isBlank()) {
                System.out.println("row:\t" + line.trim());
            }

        }
    }

    /**
     * 抽取txt文本文件
     *
     * @param filaPath
     * @throws IOException
     * @throws TikaException
     * @throws SAXException
     */
    public static void partText(final String filaPath) throws IOException, TikaException, SAXException {
        BodyContentHandler handler = new BodyContentHandler();
        Metadata metadata = new Metadata();
        FileInputStream inputstream = new FileInputStream(new File(filaPath));
        ParseContext pcontext = new ParseContext();
        //Text document parser
        TXTParser TexTParser = new TXTParser();
        TexTParser.parse(inputstream, handler, metadata, pcontext);
        System.out.println("Contents of the document:" + handler.toString());
        System.out.println("Metadata of the document:");
        String[] metadataNames = metadata.names();
        for (String name : metadataNames) {
            System.out.println(name + " : " + metadata.get(name));
        }

    }


    /**
     * 抽取pdf文件
     *
     * @param filaPath
     * @return
     * @throws Exception
     */
    public static ParseVo parsePdf(final String filaPath) throws Exception {

        try {
            BodyContentHandler handler = new BodyContentHandler();
            Metadata metadata = new Metadata();
            FileInputStream inputStream = new FileInputStream(new File(filaPath));
            ParseContext parseContext = new ParseContext();
            PDFParser pdfparser = new PDFParser();
            PDFParserConfig config = new PDFParserConfig();
            pdfparser.setPDFParserConfig(config);//set
            pdfparser.parse(inputStream, handler, metadata, parseContext);
            //getting the content of the document

            // System.out.println("Contents of the PDF :" + handler);
            // 元数据提取
            System.out.println("Metadata of the PDF:");
            String[] metadataNames = metadata.names();
            HashMap hashMapMetadataNames = new HashMap();
            for (String name : metadataNames) {
                //System.out.println(name + " : " + metadata.get(name));
                hashMapMetadataNames.put(name, metadata.get(name));
            }
            ParseVo parseVo = new ParseVo(handler.toString(), hashMapMetadataNames);
            return parseVo;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ParseVo();
    }

    /**
     * 文件类型检测
     *
     * @return
     * @throws Exception
     */
    private static String typeDetection(String path) throws Exception {
        File file = new File(path);
        Tika tika = new Tika();
        String filetype = tika.detect(file);
        System.out.println(filetype);
        return filetype;

    }

    public static String getContext(String pathDoc) throws IOException, TikaException {
        File file = new File(pathDoc);
        Tika tika = new Tika();
        //获取格式
        String detect = tika.detect(file);
        //获取内容
        String fileContent = tika.parseToString(file);
        System.out.println(fileContent);
        return fileContent;
    }


}
