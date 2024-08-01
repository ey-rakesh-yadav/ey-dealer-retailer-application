package com.scl.facades.util;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Objects;

public class GenericMediaUtil {
    private static final Logger LOG = Logger.getLogger(GenericMediaUtil.class);
    public MultipartFile getMultipartFile(String media,String fileName){
        if(Objects.isNull(media)){
            return null;
        }
        byte[] bytes = Base64.getDecoder().decode(media);
        return getMultipartFile(fileName, bytes);
    }
    private MultipartFile getMultipartFile(String name, byte[] bytes) {

        MultipartFile mfile = null;
        try {

            ByteArrayInputStream in = new ByteArrayInputStream(bytes);
            FileItemFactory factory = new DiskFileItemFactory(16, null);
            FileItem fileItem = factory.createItem(name, getMediaContentType(name), false, name);
            IOUtils.copy(new ByteArrayInputStream(bytes), fileItem.getOutputStream());
            mfile = new CommonsMultipartFile(fileItem);
            in.close();
        }catch (IOException e){
            LOG.error("unexpected error for getting multipart file" + e.getMessage());
        }
        return mfile;
    }

    private String getMediaContentType(String name) throws IOException{
        Path path=new File(name).toPath();
        return Files.probeContentType(path);
    }
}
