package xiaozhi.common.utils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import lombok.extern.slf4j.Slf4j;
import xiaozhi.common.exception.RenException;

/**
 * 阿里云OSS工具类
 *
 * @author zjy
 * @since 2025-3-22
 */
@Slf4j
@Component
public class OssUtils {

    @Autowired
    private OSS ossClient;

    @Value("${aliyun.oss.bucketName}")
    private String bucketName;

    @Value("${aliyun.oss.domain}")
    private String domain;

    /**
     * 上传文件到OSS
     *
     * @param file 文件
     * @param dir 目录
     * @return 文件访问URL
     */
    public String upload(MultipartFile file, String dir) {
        String originalFilename = file.getOriginalFilename();
        String suffix = FileUtil.extName(originalFilename);
        String fileName = generateFileName(suffix);
        String objectName = dir + "/" + fileName;

        try {
            PutObjectResult result = ossClient.putObject(
                    bucketName, 
                    objectName, 
                    file.getInputStream());
            
            log.info("文件上传成功，ETag: {}", result.getETag());
            return domain + "/" + objectName;
        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw new RenException("文件上传失败");
        }
    }

    /**
     * 上传字节数组到OSS
     *
     * @param data 字节数组
     * @param dir 目录
     * @param suffix 文件后缀
     * @return 文件访问URL
     */
    public String upload(byte[] data, String dir, String suffix) {
        String fileName = generateFileName(suffix);
        String objectName = dir + "/" + fileName;

        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(data.length);
            
            PutObjectRequest request = new PutObjectRequest(
                    bucketName, 
                    objectName, 
                    new ByteArrayInputStream(data),
                    metadata);
            
            PutObjectResult result = ossClient.putObject(request);
            log.info("文件上传成功，ETag: {}", result.getETag());
            
            return domain + "/" + objectName;
        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw new RenException("文件上传失败");
        }
    }

    /**
     * 上传本地文件到OSS
     *
     * @param filePath 本地文件路径
     * @param dir OSS目录
     * @return 文件访问URL
     */
    public String upload(String filePath, String dir) {
        File file = new File(filePath);
        String fileName = generateFileName(FileUtil.extName(file));
        String objectName = dir + "/" + fileName;

        try {
            PutObjectResult result = ossClient.putObject(
                    bucketName, 
                    objectName, 
                    file);
            
            log.info("文件上传成功，ETag: {}", result.getETag());
            return domain + "/" + objectName;
        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw new RenException("文件上传失败");
        }
    }

    /**
     * 上传输入流到OSS
     *
     * @param inputStream 输入流
     * @param dir OSS目录
     * @param suffix 文件后缀
     * @return 文件访问URL
     */
    public String upload(InputStream inputStream, String dir, String suffix) {
        String fileName = generateFileName(suffix);
        String objectName = dir + "/" + fileName;

        try {
            PutObjectResult result = ossClient.putObject(
                    bucketName, 
                    objectName, 
                    inputStream);
            
            log.info("文件上传成功，ETag: {}", result.getETag());
            return domain + "/" + objectName;
        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw new RenException("文件上传失败");
        }
    }

    /**
     * 上传File对象到OSS
     *
     * @param file File对象
     * @param dir OSS目录
     * @return 文件访问URL
     */
    public String uploadFile(File file, String dir) {
        String fileName = generateFileName(FileUtil.extName(file));
        String objectName = dir + "/" + fileName;

        try {
            PutObjectResult result = ossClient.putObject(
                    bucketName, 
                    objectName, 
                    file);
            
            log.info("文件上传成功，ETag: {}", result.getETag());
            return domain + "/" + objectName;
        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw new RenException("文件上传失败");
        }
    }

    /**
     * 获取文件临时访问URL
     *
     * @param objectName 文件名称
     * @param expireTime 过期时间（毫秒）
     * @return 临时访问URL
     */
    public String getPresignedUrl(String objectName, long expireTime) {
        try {
            Date expiration = new Date(System.currentTimeMillis() + expireTime);
            URL url = ossClient.generatePresignedUrl(bucketName, objectName, expiration);
            return url.toString();
        } catch (Exception e) {
            log.error("获取文件访问URL失败", e);
            throw new RenException("获取文件访问URL失败");
        }
    }

    /**
     * 删除OSS文件
     *
     * @param objectName 文件名称
     */
    public void delete(String objectName) {
        try {
            ossClient.deleteObject(bucketName, objectName);
            log.info("文件删除成功：{}", objectName);
        } catch (Exception e) {
            log.error("文件删除失败", e);
            throw new RenException("文件删除失败");
        }
    }

    /**
     * 获取OSS域名
     *
     * @return OSS域名
     */
    public String getDomain() {
        return domain;
    }

    /**
     * 生成唯一文件名
     *
     * @param suffix 文件后缀
     * @return 唯一文件名
     */
    private String generateFileName(String suffix) {
        return IdUtil.fastSimpleUUID() + "." + suffix;
    }
} 