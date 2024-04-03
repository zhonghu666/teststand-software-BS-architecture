package com.cetiti.utils;

import com.google.common.base.Strings;
import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class MinioUtil {

    @Autowired
    private MinioClient minioClient;

    @Value("${minio.bucketName}")
    private String bucketName;

    @Value("${minio.endpoint}")
    private String endpoint;

    /**
     * description: 判断bucket是否存在，不存在则创建
     *
     * @return: void
     */
    public boolean existBucket(String name) {
        boolean exists;
        try {
            exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(name).build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(name).build());
                exists = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            exists = false;
        }
        return exists;
    }


    /**
     * 创建存储bucket
     *
     * @param bucketName 存储bucket名称
     * @return Boolean
     */
    public Boolean makeBucket(String bucketName) {
        try {
            minioClient.makeBucket(MakeBucketArgs.builder()
                    .bucket(bucketName)
                    .build());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 删除存储bucket
     *
     * @param bucketName 存储bucket名称
     * @return Boolean
     */
    public Boolean removeBucket(String bucketName) {
        try {
            minioClient.removeBucket(RemoveBucketArgs.builder()
                    .bucket(bucketName)
                    .build());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * description: 上传文件
     *
     * @param file
     * @return: java.lang.String
     */
    public String upload(MultipartFile file, String prefixPath, String bucketNameStr) {
        existBucket(bucketName);
        String fileName = file.getOriginalFilename();
        String[] split = fileName.split("\\.");
        String filePath;
        if (split.length > 1) {
            filePath = prefixPath + "/" + split[0] + "_" + System.currentTimeMillis() + "." + split[1];
        } else {
            filePath = prefixPath + "/" + fileName + System.currentTimeMillis();
        }
        InputStream in = null;
        try {
            if (Strings.isNullOrEmpty(bucketNameStr)) {
                bucketNameStr = bucketName;
            }
            in = file.getInputStream();
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketNameStr)
                    .object(filePath)
                    .stream(in, in.available(), -1)
                    .contentType(file.getContentType())
                    .build()
            );
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return filePath;
    }

    /**
     * @param bytes    字节数组
     * @param filePath 文件路径
     */
    public void upload(byte[] bytes, String filePath) {
        //检验bucket是否存在
        existBucket(bucketName);
        ByteArrayInputStream in = null;
        try {
            String contentType = MediaTypeFactory.getMediaType(filePath).orElse(MediaType.APPLICATION_OCTET_STREAM).toString();
            in = new ByteArrayInputStream(bytes);
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(filePath)
                    .stream(in, in.available(), -1)
                    .contentType(contentType)
                    .build()
            );
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public InputStream download(String filePath) throws Exception {
        return minioClient.getObject(GetObjectArgs.builder()
                .bucket(bucketName)
                .object(filePath)
                .build());
    }


    /**
     * 下载文件
     *
     * @param filePath 文件路径
     */
    public Boolean download(String filePath, String fileName, HttpServletResponse response) {
        InputStream in = null;
        OutputStream out = null;
        try {
            in = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(filePath)
                    .build());
            //设置响应头信息，告诉前端浏览器下载文件
            response.setContentType("application/octet-stream;charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));
            //获取输出流进行写入数据
            out = response.getOutputStream();
            // 将输入流复制到输出流
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            // 关闭流资源
            if (in != null) {
                try {

                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 查看文件对象
     *
     * @param bucketName 存储bucket名称
     * @return 存储bucket内文件对象信息
     */
//	public List<ObjectItem> listObjects(String bucketName) {
//		Iterable<Result<Item>> results = minioClient.listObjects(
//				ListObjectsArgs.builder().bucket(bucketName).build());
//		List<ObjectItem> objectItems = new ArrayList<>();
//		try {
//			for (Result<Item> result : results) {
//				Item item = result.get();
//				ObjectItem objectItem = new ObjectItem();
//				objectItem.setObjectName(item.objectName());
//				objectItem.setSize(item.size());
//				objectItems.add(objectItem);
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//			return null;
//		}
//		return objectItems;
//	}

    /**
     * 批量删除文件对象
     *
     * @param bucketName 存储bucket名称
     * @param objects    对象名称集合
     */
    public Iterable<Result<DeleteError>> removeObjects(String bucketName, List<String> objects) {
        List<DeleteObject> dos = objects.stream().map(DeleteObject::new).collect(Collectors.toList());
        return minioClient.removeObjects(RemoveObjectsArgs.builder().bucket(bucketName).objects(dos).build());
    }

    /**
     * 删除文件
     *
     * @param filePath
     * @return
     */
    public Boolean deleteFile(String filePath) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(filePath)
                    .build());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 根据文件名和桶获取文件路径
     *
     * @param
     */
    public String getFileUrl(String objectFile) {
        try {
            String url = minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(bucketName)
                    .object(objectFile)
                    .build()
            );
            //相对路径
            return url.replaceFirst(endpoint, "");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 查看文件对象
     *
     * @param bucketName 存储bucket名称
     * @return 存储bucket内文件对象信息
     */
//	public List<ObjectItem> listObjects(String bucketName) {
//		Iterable<Result<Item>> results = minioClient.listObjects(
//				ListObjectsArgs.builder().bucket(bucketName).build());
//		List<ObjectItem> objectItems = new ArrayList<>();
//		try {
//			for (Result<Item> result : results) {
//				Item item = result.get();
//				ObjectItem objectItem = new ObjectItem();
//				objectItem.setObjectName(item.objectName());
//				objectItem.setSize(item.size());
//				objectItems.add(objectItem);
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//			return null;
//		}
//		return objectItems;
//	}
}