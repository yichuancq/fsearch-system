package com.example.search.panel;

import com.example.search.vo.FullTextVo;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 */
public class FullTextSearchWindows {
    private static final String indexPath = "/Users/yichuan/Documents/test";
    private final JFrame jFrame;
    private final int windowWidth = 1000;
    private final int windowHeight = 800;//800
    private JTable table;
    private JPanel contentPanel;
    private JTextField jTextField;
    private DefaultTableModel tableModel;
    private JPanel searchPanel = new JPanel();
    JScrollPane scrollPane = new JScrollPane();
    private ExecutorService executor = Executors.newFixedThreadPool(1);

    //
    public static void main(String[] args) {

        new FullTextSearchWindows();

    }

    /**
     *
     */
    private void initSearchToolPanel() {
        int columnsWith = 8;
        int columnsSize = 6;
        //文本框大小
        jTextField = new JTextField(columnsWith * columnsSize);
        //文字开始方向
        jTextField.setHorizontalAlignment(SwingConstants.LEFT);
        searchPanel.add(jTextField);
        JButton searchBtn = new JButton("检索");
        JButton resetBtn = new JButton("重置");
        //监听事件
        resetBtn.addActionListener(actionListener());
        //监听事件
        searchBtn.addActionListener(actionListener());
        searchPanel.add(searchBtn);
        searchPanel.add(resetBtn);
        //向JPanel添加FlowLayout布局管理器，将组件间的横向和纵向间隙都设置为10像素
        searchPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 20));
        //设置背景颜色
        Color bgColor = new Color(95, 77, 180);
        searchPanel.setBackground(bgColor);
        // return searchPanel;

    }

    /**
     * @return
     */
    private ActionListener actionListener() {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println(e.getActionCommand());
                if (e.getActionCommand().equals("重置")) {
                    //重置数据
                    jTextField.setText("");
                    //table = new JTable();
                    //获得表格模型
                    tableModel = (DefaultTableModel) table.getModel();
                    //清空表格中的数据
                    tableModel.setRowCount(0);
                    table.updateUI();
                }
                if (e.getActionCommand().equals("检索")) {
                    String key = jTextField.getText();
                    System.out.println("key->" + key);
                    query(key);
                }
            }
        };
    }

    private void query(String queryKey) {
        //执行一个任务
        try {
            //获得表格模型
            task(queryKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /***/
    private ArrayList<FullTextVo> readIndex(final String queryKey) throws Exception {
        // 索引目录对象
        ArrayList<FullTextVo> fullTextVos = new ArrayList<>();
        //  fullTextVos.clear();
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
        QueryParser parser = new MultiFieldQueryParser(new String[]{"title", "content", "contentType"}, new StandardAnalyzer());
        // 创建查询对象
        Query query = parser.parse(queryKey);
        TopDocs topDocs = searcher.search(query, 50);
        // 获取得分文档对象（ScoreDoc）数组.SocreDoc中包含：文档的编号、文档的得分
        ScoreDoc[] scoreDocs = topDocs.scoreDocs;
        for (ScoreDoc scoreDoc : scoreDocs) {
            //取出文档编号
            int docID = scoreDoc.doc;
            //根据编号去找文档
            Document doc = reader.document(docID);
            //取出文档得分
            System.out.println("得分： " + scoreDoc.score);
            String content = doc.get("content");
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String timeString = "";
            Date dd = simpleDateFormat.parse(String.valueOf(doc.get("createdTime")));
            timeString = simpleDateFormat.format(dd);
            fullTextVos.add(new FullTextVo(doc.get("title"), content, scoreDoc.score, doc.get("contentType"), timeString));
        }
        // 获取总条数
        System.out.println("本次搜索共找到 " + topDocs.totalHits + " 条数据");
        return fullTextVos;
    }

    /**
     * 列自适应
     *
     * @param jTable
     */
    public void fitTableColumns(JTable jTable) {
        JTableHeader header = jTable.getTableHeader();
        int rowCount = jTable.getRowCount();
        Enumeration columns = jTable.getColumnModel().getColumns();
        while (columns.hasMoreElements()) {
            TableColumn column = (TableColumn) columns.nextElement();
            int col = header.getColumnModel().getColumnIndex(column.getIdentifier());
            int width = (int) jTable.getTableHeader().getDefaultRenderer().getTableCellRendererComponent(jTable, column.getIdentifier(), false, false, -1, col).getPreferredSize().getWidth();
            for (int row = 0; row < rowCount; row++) {
                int preferredWidth = (int) jTable.getCellRenderer(row, col).getTableCellRendererComponent(jTable, jTable.getValueAt(row, col), false, false, row, col).getPreferredSize().getWidth();
                width = Math.max(width, preferredWidth);

            }
            header.setResizingColumn(column);
            column.setWidth(width + jTable.getIntercellSpacing().width);

        }

    }


    /**
     * @param queryKey
     */
    private void task(String queryKey) {
        try {
            final ArrayList<FullTextVo> fullTextVos = readIndex(queryKey);
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        // 批量同步数据
                        System.out.println("加载表格。。。");
                        System.out.println("list len:" + fullTextVos.size());
                        initTable(queryKey, fullTextVos);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }


    /**
     *
     */
    private void initTable(String queryKey, ArrayList<FullTextVo> fullTextVos) {
        table = new JTable();
        //获得表格模型
        tableModel = (DefaultTableModel) table.getModel();    //获得表格模型
        //清空表格中的数据
        tableModel.setRowCount(0);
        //清空表格中的数据
        tableModel.setColumnIdentifiers(new Object[]{"序号", "文件名称", "关键内容", "命中率", "文件类别", "文档创建时间", "路径"});    //设置表头
        int i = 0;
        for (FullTextVo fullTextVo : fullTextVos) {
            tableModel.addRow(new Object[]{++i, fullTextVo.getTitle(), fullTextVo.getContent(), fullTextVo.getScore(), fullTextVo.getType(), fullTextVo.getTime(), "/Users/test..."});

        }
        //
        TableColumn contentColumn = table.getColumn("关键内容");
        //绘制列的字体颜色
        DefaultTableCellRenderer fontColor = new DefaultTableCellRenderer() {
            public void setValue(Object value) { //重写setValue方法，从而可以动态设置列单元字体颜色
                //获取列中的值
                String val = value.toString();
                setForeground((val.contains(queryKey)) ? Color.red : Color.black); //如果月薪大于3099元，就将字体设置为红色
                setText((value == null) ? "" : value.toString());
            }
        };
        contentColumn.setCellRenderer(fontColor);
        this.table.setRowSelectionAllowed(true);
        table.setRowHeight(30);
        table.setModel(tableModel);    //应用表格模型
        //设置JTable的列宽随着列表内容的大小进行调整,为了实现滚动条
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        this.table.setRowSelectionAllowed(true);
        //表格列自适应
        this.fitTableColumns(table);
        //添加到中间
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        scrollPane.setViewportView(table);
        //update ui
        contentPanel.updateUI();
    }

    /**
     * @return
     */
    private void addContentPanel() {
        contentPanel = new JPanel();

        this.initSearchToolPanel();
        //搜索栏
        contentPanel.setLayout(new BorderLayout());
        //添加在最上面
        contentPanel.add(this.searchPanel, BorderLayout.NORTH);


    }

    public FullTextSearchWindows() {
        //创建一个JFrame对象
        jFrame = new JFrame();
        //设置窗口大小和位置
        jFrame.setBounds(0, 0, windowWidth, windowHeight);
        //设置窗体位置在屏幕中央
        //菜单
        JMenuBar jMenuBar = new JMenuBar();
        jFrame.setTitle("全文检索v1.0");
        jFrame.setJMenuBar(jMenuBar);
        this.setWindowCenter();
        this.addContentPanel();
        //添加加密面板到主界面
        jFrame.setContentPane(contentPanel);
        //显示窗口
        jFrame.setVisible(true);
        //关闭窗口
        jFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                System.exit(0);
            }
        });


    }

    /**
     * 设置窗口居中显示
     */
    private void setWindowCenter() {
        //获得窗口宽
        int windowWidth = jFrame.getWidth();
        //获得窗口高
        int windowHeight = jFrame.getHeight();
        //定义工具包
        Toolkit kit = Toolkit.getDefaultToolkit();
        //获取屏幕的尺寸
        Dimension screenSize = kit.getScreenSize();
        //获取屏幕的宽
        int screenWidth = screenSize.width;
        //获取屏幕的高
        int screenHeight = screenSize.height;
        //设置窗口居中显示
        jFrame.setLocation(screenWidth / 2 - windowWidth / 2, screenHeight / 2 - windowHeight / 2);

    }
}
