package com.hjx.pa;

import com.sun.org.apache.bcel.internal.generic.NEW;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

public class CrawlerBase {
    public static void main(String[] args) {
        String url = "https://sobooks.net/";
        //String url1="https://sobooks.net/xiaoshuowenxue";
        CrawlerBase crawlerBase = new CrawlerBase();
        crawlerBase.jsoupList1(url);

       /* MyRunnable myRunnable = new MyRunnable("https://sobooks.net/xiaoshuowenxue",1);
        new Thread(myRunnable).start();*/

    }

    public void jsoupList(String url){
        try {
            Document document = Jsoup.connect(url).get();

            // 使用 css选择器 提取列表新闻 a 标签
            Elements elements =document.getElementsByClass("thumb-img focus");


            for (Element element:elements){

                Elements e = element.select("a");
                // 获取详情页链接
                String d_url = e.attr("href");

                Document documentNew = Jsoup.connect(d_url).get();
                Elements bookinfo = documentNew.getElementsByClass("bookinfo");
                Elements ul = bookinfo.select("ul");
                Elements lis = ul.select("li");
                for(Element li:lis){
                    String text = li.text();
                    System.out.println(text);
                }


                // 获取标题
                String title = e.attr("title");


                System.out.println("详情页链接："+d_url+" ,详情页标题："+title);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void jsoupList1(String url){
        try {
            Document document = Jsoup.connect(url).get();
            LinkedHashMap<String,Integer> linkedHashMap = new LinkedHashMap();
            // 使用 css选择器 提取列表新闻 a 标签
            Elements elements =document.getElementsByClass("nav");
            int i=1;
            for (Element element : elements) {
                Elements lis = element.select("li");
                for (Element li:lis) {
                    Elements a = li.select("a");
                    String fenlei = a.text();
                    String href = a.attr("href");

                    if (href!=null&&href!="") {
                        if (i>5) {
                            linkedHashMap.put(href,i);
                        }

                        i++;
                        System.out.println(fenlei+":"+href);
                    }
                }
            }
            System.out.println();

            for (Map.Entry<String,Integer> map: linkedHashMap.entrySet()) {

                new Thread(new MyRunnable(map.getKey(),map.getValue())).start();

            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
