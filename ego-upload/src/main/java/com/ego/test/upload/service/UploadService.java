package com.ego.test.upload.service;

import com.github.tobato.fastdfs.domain.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class UploadService {
    @Autowired
    private FastFileStorageClient fastFileStorageClient;

//    public static final Logger logger = LoggerFactory.getLogger(UploadService.class);
// 指定支持的文件类型
    public static final List<String> suffiex = Arrays.asList("image/png", "image/jpeg");

    public String upload(MultipartFile file) {
        try {
            //1.检查图片文件类型
            String type = file.getContentType();
            if(!suffiex.contains(type))
            {
                log.info("上传失败，文件类型不匹配：{}",file);
                return null;
            }
            //2.检查图片内容是否正确
            BufferedImage image = ImageIO.read(file.getInputStream());
            if(image == null)
            {
                log.info("上传失败，文件内容不符合要求");
            }
//            //3.保存到硬盘
////            File dir = new File("d://images/");
//            File dir = new File("G:\\images\\");
//            if(!dir.exists())
//            {
//                dir.mkdirs();
//            }
//            file.transferTo(new File(dir,file.getOriginalFilename()));
        // 3、将图片上传到FastDFS
            // 3.1、获取文件后缀名
            String extension = StringUtils.substringAfterLast(file.getOriginalFilename(), ".");
            // 3.2、上传
            StorePath storePath = fastFileStorageClient.uploadFile(file.getInputStream(), file.getSize(), extension, null);
            // 3.3、返回完整路径
 //        return "http://image.ego.com/"+file.getOriginalFilename();
            return "http://image.ego.com/"+storePath.getFullPath();
        } catch (IOException e) {
            e.printStackTrace();
            return  null;
        }
    }
}
