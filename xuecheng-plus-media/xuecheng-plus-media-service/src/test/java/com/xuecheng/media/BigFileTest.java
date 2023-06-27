package com.xuecheng.media;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 测试大文件上传
 */
public class BigFileTest {

    // 分块测试
    @Test
    public void testChunk() throws IOException {
        // 源文件
        File sourceFile = new File("D:\\DATA\\11.mp4");
        // 分块文件存储路径
        String chunkFilePath = "D:\\DATA\\chunk\\";
        // 分块大小
        int chunkSize = 1024 * 1024 * 1; // kb*字节*个数  这里就是1m
        // 分块文件个数
        int chunkNum = (int) Math.ceil(sourceFile.length() * 1.0 / chunkSize);
        // 使用流从源文件读数据，向分块文件中写数据
        RandomAccessFile raf_r = new RandomAccessFile(sourceFile, "r");// RandomAccessFile可以指是读还是读写
        // 缓冲区
        byte[] bytes = new byte[1024];
        // 根据分块数进行循环写入
        for (int i = 0; i < chunkNum; i++) {
            File chunkFile = new File(chunkFilePath + i);
            // 分块文件写入流
            RandomAccessFile raf_rw = new RandomAccessFile(chunkFile, "rw");
            int len = -1;
            // 读取文件
            while ((len = raf_r.read(bytes)) != -1) {
                // 写入文件
                raf_rw.write(bytes, 0, len);
                // 当当前分块文件大小>=设定的分块文件大小则停止写入
                if (chunkFile.length() >= chunkSize) {
                    break;
                }
            }
            raf_rw.close();
        }
        raf_r.close();
    }

    // 将分块进行合并
    @Test
    public void testMerge() throws IOException {
        // 块文件目录
        File chunkFolder = new File("D:\\DATA\\chunk\\");
        // 源文件
        File sourceFile = new File("D:\\DATA\\11.mp4");
        // 合并后的文件
        File mergeFile = new File("D:\\DATA\\22.mp4");

        // 取出所有分块文件
        File[] files = chunkFolder.listFiles();
        // 将数组转换为list
        List<File> fileList = Arrays.asList(files);

        // 将集合内容排序
        // 因为分的块是有顺序的，所以合并的时候也要按照顺序
        Collections.sort(fileList, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                // 升序
                return Integer.parseInt(o1.getName()) - Integer.parseInt(o2.getName());
            }
        });

        // 创建合并文件的流
        RandomAccessFile raf_rw = new RandomAccessFile(mergeFile, "rw");
        // 缓冲区
        byte[] bytes = new byte[1024];
        // 遍历分块文件，向合并文件写入
        for (File file : fileList) {
            // 创建读分块的流
            RandomAccessFile raf_r = new RandomAccessFile(file, "r");
            int len = -1;
            while ((len = raf_r.read(bytes)) != -1){
                raf_rw.write(bytes, 0, len);
            }
            raf_r.close();
        }
        raf_rw.close();

        //使用两个文件的MD5值进行校验
        String md5_source = DigestUtils.md5Hex(new FileInputStream(sourceFile));
        String md5_merge = DigestUtils.md5Hex(new FileInputStream(mergeFile));
        if (md5_merge.equals(md5_source)){
            System.out.println("文件合并成功");
        }
    }
}
