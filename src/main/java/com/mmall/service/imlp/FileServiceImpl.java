package com.mmall.service.imlp;

import com.google.common.collect.Lists;
import com.mmall.service.IFileService;
import com.mmall.util.FTPUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;


@Service("iFileService")
public class FileServiceImpl implements IFileService {

    private Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);

    public String upload(MultipartFile file, String path){

        String fileName = file.getOriginalFilename();
        // extension name
        String fileExtensionName = fileName.substring(fileName.lastIndexOf(".")+1);   //jpg
        String uploadFileName = UUID.randomUUID().toString()+"."+fileExtensionName;
        logger.info("开始上传文件，上传文件名{}，上传路径{},新文件名{}", fileName, path, uploadFileName);

        File fileDir = new File(path);
        if (!fileDir.exists()){
            fileDir.setWritable(true);
            fileDir.mkdirs();
        }
        File targetFile = new File(path, uploadFileName);

        try{
            file.transferTo(targetFile);
            // 文件上传成功
            FTPUtil.uploadFile(Lists.newArrayList(targetFile));
            // 上传至ftp服务器成功
            targetFile.delete();
        } catch (IOException e){
            logger.error("上传文件异常", e);
        }
        return targetFile.getName();


    }
}
