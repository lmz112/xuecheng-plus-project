package com.xuecheng.media;

import io.minio.*;
import io.minio.errors.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterInputStream;


/**
 * 测试minio的sdk
 */
@SpringBootTest
public class MinioTest {

    MinioClient minioClient =
            MinioClient.builder()
                    .endpoint("http://192.168.61.128:9000")
                    .credentials("minioadmin","minioadmin")
                    .build();

    @Test
    public void test_upload() throws Exception {

        // 上传文件
        minioClient.uploadObject(
                UploadObjectArgs.builder()
                        .bucket("mediafiles") // 桶
                        .filename("D:\\BaiduNetdiskDownload\\item.sql") // 指定本地文件路径
                        .object("item.sql") // 对象名
                        .build()
        );
    }

    @Test
    public void test_delete() throws Exception {

        // 删除文件
        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket("testbucket") // 桶
                        .object("item.sql") // 对象名
//                        .object("test/01/item.sql") // 对象名  放在子目录下
                        .build()
        );
    }

    @Test
    public void test_getFile() throws Exception{
        // 从minio中下载文件
        FilterInputStream inputStream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket("testbucket")
                        .object("item.sql")
                        .build()
        );

        // 指定输出流
        FileOutputStream outputStream = new FileOutputStream(new File("D:\\BaiduNetdiskDownload\\item01.sql"));
        // copy
        IOUtils.copy(inputStream,outputStream);

        // 校验文件完整性 对文件内容进行md5
        String source_md5 = DigestUtils.md5Hex(inputStream); // minio中文件的md5
        String local_md5 = DigestUtils.md5Hex(new FileInputStream(new File("D:\\BaiduNetdiskDownload\\item01.sql")));
        // 如果两个文件的md5值相同则代表下载成功
        if (source_md5.equals(local_md5)){
            System.out.println("下载成功");
        }
    }
}
