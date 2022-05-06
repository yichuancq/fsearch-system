# fsearch-system

基于开源搜索引擎的pdf文件内容抽取和搜索文件内容的工具

##### 抽取pdf

```java

/**
 * 抽取pdf文件
 *
 * @param filaPath
 * @return
 * @throws Exception
 */
public static ParseVo parsePdf(final String filaPath)throws Exception{

        try{
        BodyContentHandler handler=new BodyContentHandler();
        Metadata metadata=new Metadata();
        FileInputStream inputStream=new FileInputStream(new File(filaPath));
        ParseContext parseContext=new ParseContext();
        PDFParser pdfparser=new PDFParser();
        PDFParserConfig config=new PDFParserConfig();
        pdfparser.setPDFParserConfig(config);//set
        pdfparser.parse(inputStream,handler,metadata,parseContext);
        //getting the content of the document

        // System.out.println("Contents of the PDF :" + handler);
        // 元数据提取
        System.out.println("Metadata of the PDF:");
        String[]metadataNames=metadata.names();
        HashMap hashMapMetadataNames=new HashMap();
        for(String name:metadataNames){
        //System.out.println(name + " : " + metadata.get(name));
        hashMapMetadataNames.put(name,metadata.get(name));
        }
        ParseVo parseVo=new ParseVo(handler.toString(),hashMapMetadataNames);
        return parseVo;
        }catch(Exception e){
        e.printStackTrace();
        }
        return new ParseVo();
        }

```

#### 读取索引

```java
 /**
 * 读取索引
 */
private static void readIndex(final String queryKey)throws Exception{

        // 索引目录对象
        Directory directory=FSDirectory.open(Paths.get(indexPath));
        // 索引读取工具
        boolean flag=DirectoryReader.indexExists(directory);
        if(!flag){
        System.out.println("索引不存在");
        }
        // 索引读取工具
        IndexReader reader=DirectoryReader.open(directory);
        // 索引搜索工具
        IndexSearcher searcher=new IndexSearcher(reader);
        //如果想同时匹配多个
        QueryParser parser=new MultiFieldQueryParser(new String[]{"title","content","id","contentType"},new StandardAnalyzer());
        // 创建查询对象
        Query query=parser.parse(queryKey);
        TopDocs topDocs=searcher.search(query,20);
        // 获取总条数
        System.out.println("本次搜索共找到 "+topDocs.totalHits+" 条数据");
        // 获取得分文档对象（ScoreDoc）数组.SocreDoc中包含：文档的编号、文档的得分
        ScoreDoc[]scoreDocs=topDocs.scoreDocs;
        for(ScoreDoc scoreDoc:scoreDocs){
        // 取出文档编号
        int docID=scoreDoc.doc;
        // 根据编号去找文档
        Document doc=reader.document(docID);
        System.out.println("id: "+doc.get("id"));
        System.out.println("title: "+doc.get("title"));
        System.out.println("content: "+doc.get("content"));
        System.out.println("contentType: "+doc.get("contentType"));
        // 取出文档得分
        System.out.println("得分： "+scoreDoc.score);
        }
        }

```

#### 创建索引

```java
    /**
 * 创建索引
 *
 * @throws Exception
 */
private static void createIndex(String filePath)throws Exception{
        // 1采集数据
        List<Document> documents=new ArrayList<>();
        ParseVo parseVo=TikaUtil.parsePdf(filePath);
        String content=parseVo.getContent();
        AtomicReference<String> contentType=new AtomicReference<>("");
        parseVo.getMetadataNamesMap().forEach((key,value)->{
        System.out.println("key:\t"+key);
        System.out.println("value:\t"+value);
        //Content-Type : application/pdf
        if(key.equals("Content-Type")){
        contentType.set((String)value);

        }
        });
        //全部替换
        content.replaceAll("\n\n\n","");
        List<String> stringList=Arrays.asList(content.split("\n"));
        System.out.println(stringList.size());
        int i=0;
        for(String line:stringList){
        Document document=new Document();
        i++;
        if(!line.isBlank()&&!line.endsWith("\n")&&line.length()>=3){
        document.add(new TextField("id","id"+String.valueOf(i),Field.Store.YES));
        document.add(new TextField("title","978-7-111-44565-4",Field.Store.YES));
        document.add(new TextField("content",line.trim(),Field.Store.YES));
        //	pdf:docinfo:created
        document.add(new TextField("contentType",contentType.get(),Field.Store.YES));
        document.add(new TextField("createdTime",parseVo.getMetadataNamesMap().get("pdf:docinfo:created").toString(),Field.Store.YES));
        System.out.println("row:\t"+line.trim());
        documents.add(document);
        }
        }
        //3创建Analyzer分词器,分析文档，对文档进行分词
        Analyzer analyzer=new StandardAnalyzer();
        //4创建Directory对象,声明索引库的位置
        Directory directory=FSDirectory.open(Paths.get(indexPath));
        //5 创建IndexWriteConfig对象，写入索引需要的配置
        IndexWriterConfig config=new IndexWriterConfig(analyzer);
        // 设置打开方式：OpenMode.APPEND 会在索引库的基础上追加新索引。OpenMode.CREATE会先清空原来数据，再提交新的索引
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        //6创建IndexWriter写入对象
        IndexWriter indexWriter=new IndexWriter(directory,config);

        // 删除已有索引
        indexWriter.deleteAll();
        indexWriter.addDocuments(documents);
        // 提交
        indexWriter.commit();
        //8释放资源
        indexWriter.close();
        }

```
