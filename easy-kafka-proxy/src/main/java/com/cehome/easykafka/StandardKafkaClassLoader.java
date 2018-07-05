package com.cehome.easykafka;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * Created by houyanlin on 2018/06/22
 **/
public class StandardKafkaClassLoader extends URLClassLoader {
    private Logger logger = LoggerFactory.getLogger(StandardKafkaClassLoader.class);
    public StandardKafkaClassLoader(String version) {
        super(new URL[] {}, null); // 将 Parent 设置为 null
        loadResource(version);
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        try {
            return super.loadClass(name);
        }catch (ClassNotFoundException e){
            return StandardKafkaClassLoader.class.getClassLoader().loadClass(name);
        }
    }

    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
        logger.debug("findClass: {}", name);
        try {
            return super.findClass(name);
        } catch(ClassNotFoundException e) {
            return StandardKafkaClassLoader.class.getClassLoader().loadClass(name);
        }
    }

    private void loadResource(String version)  {
        InputStream inputStream = null;
        try {
            ClassPathResource classPathResource = new ClassPathResource("kafka/" + version + "/kafka-clients-" + version + ".jar");
            inputStream = classPathResource.getInputStream();
            File dir = File.createTempFile("kafka-clients-" + version, ".jar");
            FileUtils.copyInputStreamToFile(inputStream, dir);
            logger.info("loadResource:{}",dir.getAbsoluteFile());
            this.addURL(dir);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            IOUtils.closeQuietly(inputStream);
        }

    }

    private void tryLoadJarInDir(File dir) {
//        File dir = new File(dirPath);
        logger.info("tryLoadJarInDir-dirPath:{}",dir.getAbsoluteFile());
        // 自动加载目录下的jar包
        if (dir.exists() && dir.isDirectory()) {
            for (File file : dir.listFiles()) {
                if (file.isFile() && file.getName().endsWith(".jar")) {
                    logger.info("tryLoadJarInDir-jar:{}",file.getAbsoluteFile());
                    this.addURL(file);
                    continue;
                }
            }
        }
    }
    private void addURL(File file) {
        try {
            super.addURL(new URL("file", null, file.getCanonicalPath()));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
