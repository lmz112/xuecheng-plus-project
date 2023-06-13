package com.xuecheng.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.service.MediaFileService;
import io.minio.MinioClient;
import io.minio.UploadObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * @author Mr.M
 * @version 1.0
 * @description TODO
 * @date 2022/9/10 8:58
 */
@Service
@Slf4j
public class MediaFileServiceImpl implements MediaFileService {

    @Resource
    MediaFilesMapper mediaFilesMapper;

    @Resource
    MinioClient minioClient;

    @Resource
    MediaFileService currentProxy;

    // 存储普通文件
    @Value("${minio.bucket.files}")
    private String bucket_mediafiles;

    // 存储视频
    @Value("${minio.bucket.videofiles}")
    private String bucket_video;

    @Override
    public PageResult<MediaFiles> queryMediaFiels(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto) {

        //构建查询条件对象
        LambdaQueryWrapper<MediaFiles> queryWrapper = new LambdaQueryWrapper<>();

        //分页对象
        Page<MediaFiles> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        // 查询数据内容获得结果
        Page<MediaFiles> pageResult = mediaFilesMapper.selectPage(page, queryWrapper);
        // 获取数据列表
        List<MediaFiles> list = pageResult.getRecords();
        // 获取数据总数
        long total = pageResult.getTotal();
        // 构建结果集
        PageResult<MediaFiles> mediaListResult = new PageResult<>(list, total, pageParams.getPageNo(), pageParams.getPageSize());
        return mediaListResult;

    }

    /**
     * 根据扩展名获取mimeType
     *
     * @param extension 扩展名
     * @return
     */
    private String getMimeType(String extension) {
        if (extension == null) {
            extension = "";
        }
        // 根据扩展名取出mimeType
        ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(extension);
        String mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE; // 通用mimeType，字节流
        if (extensionMatch != null) {
            mimeType = extensionMatch.getMimeType();
        }
        return mimeType;
    }

    /**
     * 将文件上传到minio
     *
     * @param localFilePath 本地文件路径
     * @param mimeType      媒体类型
     * @param bucket        桶名称
     * @param objectName    对象名称
     * @return
     */
    private boolean addMediaFilesToMinIO(String localFilePath, String mimeType, String bucket, String objectName) {
        try {
            UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder()
                    .bucket(bucket)
                    .filename(localFilePath)
                    .object(objectName)
                    .contentType(mimeType)
                    .build();
            minioClient.uploadObject(uploadObjectArgs);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("上传文件出错,bucket:{},objectName:{},错误信息:{},", bucket, objectName, e.getMessage());
        }
        return false;
    }

    /**
     * 获取文件默认存储目录路径  年/月/日
     *
     * @return
     */
    private String getDefaultFolderPath() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String folder = sdf.format(new Date()).replace("-", "/") + "/";
        return folder;
    }

    /**
     * 读取文件的MD5值
     * @param file  文件
     * @return
     */
    private String getFileMd5(File file) {
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            String fileMd5 = DigestUtils.md5Hex(fileInputStream);
            return fileMd5;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto, String localFilePath) {

        // 1.获取文件扩展名
        // 1.1.获取文件名
        String filename = uploadFileParamsDto.getFilename();
        // 1.2.根据文件名得到扩展名
        String extension = filename.substring(filename.lastIndexOf("."));
        // 1.3.得到mimeType
        String mimeType = getMimeType(extension);

        // 2.拼接文件名 (桶里的存储格式是日期为目录的)
        // 2.1.获取当前文件子目录
        String defaultFolderPath = getDefaultFolderPath();
        // 2.2.获取文件的MD5值
        String fileMd5 = getFileMd5(new File(localFilePath));
        // 2.3.获取对象名称
        String objectName = defaultFolderPath + fileMd5 + extension;

        // 3.上传文件到minio
        boolean result = addMediaFilesToMinIO(localFilePath, mimeType, bucket_mediafiles, objectName);
        if (!result){
            XueChengPlusException.cast("上传文件失败");
        }

        // 4.将文件信息保存到数据库
        MediaFiles mediaFiles = currentProxy.addMediaFilesToDb(companyId, fileMd5, uploadFileParamsDto, bucket_mediafiles, objectName);
        if (mediaFiles == null){
            XueChengPlusException.cast("文件上传后，保存失败");
        }
        // 4.1.准备返回的对象
        UploadFileResultDto uploadFileResultDto = new UploadFileResultDto();
        BeanUtils.copyProperties(mediaFiles, uploadFileResultDto);

        return uploadFileResultDto;
    }

    /**
     * 将文件信息保存到数据库
     * @param companyId  机构id
     * @param fileMd5    文件MD5值
     * @param uploadFileParamsDto   上传的文件信息
     * @param bucket    桶
     * @param objectName   对象名称
     * @return
     */
    @Transactional
    public MediaFiles addMediaFilesToDb(Long companyId, String fileMd5, UploadFileParamsDto uploadFileParamsDto, String bucket, String objectName){
        // 判断数据库中是否存在该文件信息
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if (mediaFiles == null){
            mediaFiles = new MediaFiles();
            BeanUtils.copyProperties(uploadFileParamsDto, mediaFiles);
            // 文件id
            mediaFiles.setId(fileMd5);
            // 机构id
            mediaFiles.setCompanyId(companyId);
            // 桶
            mediaFiles.setBucket(bucket);
            // file_path就是对象名
            mediaFiles.setFilePath(objectName);
            // file_id
            mediaFiles.setFileId(fileMd5);
            // url
            mediaFiles.setUrl("/" + bucket + "/" + objectName);
            // 上传时间
            mediaFiles.setCreateDate(LocalDateTime.now());
            // 状态
            mediaFiles.setStatus("1");
            // 审核状态
            mediaFiles.setAuditStatus("002003");

            // 插入 数据库
            int insert = mediaFilesMapper.insert(mediaFiles);
            if (insert <= 0){
                log.debug("向数据库保存文件失败,bucket:{},objectName{}", bucket,objectName);
                return null;
            }
        }
        return mediaFiles;
    }
}
