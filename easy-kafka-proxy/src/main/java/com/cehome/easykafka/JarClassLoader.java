package com.cehome.easykafka;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.ProtectionDomain;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class JarClassLoader extends ClassLoader {

    private String version;
    public JarClassLoader(String version){
        this.version=version;
    }



    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException{
        //Class c=findLoadedClass(name);
        //if(c!=null) return c;
        //System.out.println("find class: "+name);
        String jar="/kafka/" + version + "/kafka-clients-" + version + ".jar";
        InputStream is = this.getClass().getResourceAsStream(jar);
        JarInputStream jis= null;
        try {
            String path=name.replace('.','/')+".class";
            jis = new JarInputStream(is);
            JarEntry entry=null;
            while( ( entry = jis.getNextJarEntry())!=null){
                if (entry.getName().equals(path)) {
                    byte[] bs = IOUtils.toByteArray(jis);

                    //ProtectionDomain protectionDomain=new ProtectionDomain(null,null);
                    return  defineClass(name,bs, 0, bs.length);

                }
            }
            //return getSystemClassLoader().loadClass(name);
            throw new ClassNotFoundException();

        } catch (IOException e) {
            throw new ClassNotFoundException(e.getMessage());
        }

    }

    public static void main(String[] args) throws ClassNotFoundException {
        JarClassLoader jarClassLoader=new JarClassLoader("0.10.1.0");
       Class c=  jarClassLoader.loadClass( "org.apache.kafka.clients.consumer.KafkaConsumer");
       c=jarClassLoader.loadClass("java.lang.String");
        System.out.println(c.getName());
    }
}
