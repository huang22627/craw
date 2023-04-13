package com.hjx.pa;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import sun.net.www.http.HttpClient;

import java.io.*;
import java.sql.*;

public class MyRunnable implements Runnable{
    private final String url;
     private  Connection connection=null;
     private final  Integer type;
    public MyRunnable(String url,Integer type) {
        this.url=url;
        this.type=type;
    }
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {

        System.out.println("线程"+Thread.currentThread().getName()+"处理"+type);

        try {
            connection=DriverManager.getConnection("jdbc:mysql://localhost:3306/bookrec1?characterEncoding=utf8&useSSL=false&serverTimezone=UTC&rewriteBatchedStatements=true", "root", "123456");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        try {
            Document document = Jsoup.connect(url).get();
            Elements pagination = document.getElementsByClass("pagination");
            Elements select = pagination.select("ul li");
            Element last = select.last();
            String text1 = last.text();
            String page = text1.replace("共", "").replace("页","").trim();
            int realPage = Integer.parseInt(page);
            //System.out.println(page);

            for (int i = 1; i <=realPage; i++) {
                try{
                    String newurl=url+"/page"+i;

                    if(i>1){
                        parse(Jsoup.connect(newurl).get());
                        System.err.println("第"+i+"页结束");
                    }else{
                        parse(document);
                        System.err.println("第一页结束");
                    }
                }catch (Exception e){
                    System.out.println("线程"+Thread.currentThread().getName()+"在处理第"+i+"页出现异常。原因"+e.getMessage());
                }

                // 使用 css选择器 提取列表新闻 a 标签



            }
            System.err.println(Thread.currentThread().getName()+"执行结束");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void parse(Document document) throws IOException, SQLException {
        Elements elements =document.getElementsByClass("thumb-img focus");

        for (Element element:elements){
            String bookname="";
            String author="";
            String isbn="";
            String desc="";
            String authordes="";
            String url="";
            String d_url="";
            try {
                Elements e = element.select("a");
                // 获取详情页链接
                 d_url = e.attr("href");
                Document documentNew = Jsoup.connect(d_url).get();
                Elements bookpic = documentNew.getElementsByClass("bookpic");
                Elements img = bookpic.select("img");
                String src = img.attr("src");

                HttpGet httpGet = new HttpGet(src);
                httpGet.setHeader("Content-Type", "application/x-www-form-urlencoded");
                CloseableHttpClient httpClient = HttpClients.createDefault();
                HttpResponse resp = httpClient.execute(httpGet);
                HttpEntity entity = resp.getEntity();
                InputStream content1 = entity.getContent();
                url = OSSUtil.upload(content1);
                httpClient.close();


                Elements bookinfo = documentNew.getElementsByClass("bookinfo");
                Elements ul = bookinfo.select("ul");
                Elements lis = ul.select("li");
                for (Element li : lis) {
                    String text = li.text();
                    if (text.startsWith("书名")) {
                        bookname = text.replaceAll("^.*?：", "");
                    }
                    if (text.startsWith("作者")) {
                        author = text.replaceAll("^.*?：", "");
                    }
                    if (text.startsWith("ISBN")) {
                        isbn = text.replaceAll("^.*?：", "");
                    }
                    //  System.out.println(text);
                }
                Elements content = documentNew.getElementsByClass("article-content");
                Elements p = content.select("p");
                StringBuilder bookstringBuilder = new StringBuilder();
                StringBuilder authorstringBuilder = new StringBuilder();
                for (Element e1 : p) {
                    if ("作者简介".equals(e1.previousElementSibling().text())) {
                        bookstringBuilder.append("作者简介");
                    }
                    if (e1.attr("class") != "") {
                        break;
                    }
                    String text = e1.text();
                    bookstringBuilder.append(text);
                }

                String s = bookstringBuilder.toString();
                String[] strs = s.split("作者简介");
                if (strs.length == 2) {
                    desc = strs[0];
                    //System.out.println("书本信息"+bookname+strs[0]);
                    authordes = strs[1];
                }

                synchronized (MyRunnable.class) {
                    PreparedStatement preparedStatement1 = connection.prepareStatement("select book_name from tbl_book where book_name=?");

                    preparedStatement1.setObject(1, bookname);
                    ResultSet resultSet = preparedStatement1.executeQuery();

                    if (!resultSet.next()) {
                        // System.out.println("作者信息"+strs[1]);
                        PreparedStatement preparedStatement = connection.prepareStatement("" +
                                "INSERT INTO tbl_book(book_name,book_author,book_isbn,book_des,book_author_des,book_image,book_type) VALUES (?,?,?,?,?,?,?)");
                        preparedStatement.setObject(1, bookname);
                        preparedStatement.setObject(2, author);
                        preparedStatement.setObject(3, isbn);
                        preparedStatement.setObject(4, desc);
                        preparedStatement.setObject(5, authordes);
                        preparedStatement.setObject(6, url);
                        preparedStatement.setObject(7, type);

                        preparedStatement.executeUpdate();
                    }
                }
            }catch (Exception e){
                System.out.println("线程"+Thread.currentThread().getName()+"在处理"+d_url+"详情页出现异常。原因"+e.getMessage());
            }





            // 获取标题
//            String title = e.attr("title");
//
//
//            System.out.println("详情页链接："+d_url+" ,详情页标题："+title);
        }
    }
}
