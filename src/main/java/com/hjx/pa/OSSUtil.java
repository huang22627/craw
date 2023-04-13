package com.hjx.pa;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;
import org.joda.time.DateTime;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class OSSUtil {
    public static String upload(InputStream inputStream) {
        String url="";
        OSS ossClient = new OSSClientBuilder().build("oss-cn-beijing.aliyuncs.com", "LTAI5t6cyNodvBth3BzzmEDG","JfAnNKnUFPGuYBtjZPQHipnFJyIxoF");

        try {

            String filename="";

            String hzName = ".jpg";
            String uuid = UUID.randomUUID().toString().replace("-","");
            filename=uuid+hzName;

            String dateTime = new DateTime().toString("yyyy-MM-dd");
            filename=dateTime+"/"+filename;

            PutObjectRequest putObjectRequest = new PutObjectRequest("hjxuse", filename, inputStream);
            putObjectRequest.setProcess("true");
            // 创建PutObject请求。
            PutObjectResult result = ossClient.putObject(putObjectRequest);
            // 如果上传成功，则返回200。
           // System.out.println(result.getResponse().getStatusCode());
            //https://hjxuse.oss-cn-beijing.aliyuncs.com/1.jpg
            url="https://"+"hjxuse"+"."+"oss-cn-beijing.aliyuncs.com"+
                    "/"+filename;
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if (ossClient!=null) {
                ossClient.shutdown();
            }
        }

        return url;
    }
}
