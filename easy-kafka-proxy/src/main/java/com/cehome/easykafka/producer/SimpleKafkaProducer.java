package com.cehome.easykafka.producer;

import com.cehome.easykafka.JarClassLoader;
import com.cehome.easykafka.Producer;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Properties;
import java.util.concurrent.Future;

/**
 * Created by houyanlin on 2018/06/22
 **/
public class SimpleKafkaProducer implements Producer{

    private String version;
    private Properties props;
    //private StandardKafkaClassLoader standardKafkaClassLoader;
    private JarClassLoader jarClassLoader;
    private Class<?> recordClazz;
    private Class<?> producerClazz;
    private Object producerInstance;
    private Constructor recordConstructor;



    public SimpleKafkaProducer(String version, Properties props){
        this.version = version;
        this.props = props;
        //standardKafkaClassLoader = new StandardKafkaClassLoader(version);
        jarClassLoader=new JarClassLoader(version,SimpleKafkaProducer.class.getClassLoader());
        //ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            //Thread.currentThread().setContextClassLoader(jarClassLoader);
            this.producerClazz = jarClassLoader.loadClass("org.apache.kafka.clients.producer.KafkaProducer");
            this.recordClazz = jarClassLoader.loadClass("org.apache.kafka.clients.producer.ProducerRecord");
            Constructor pruducerConstructor = producerClazz.getConstructor(Properties.class);
            this.producerInstance = pruducerConstructor.newInstance(props);
            this.recordConstructor = recordClazz.getConstructor(String.class,Object.class,Object.class);
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        finally {
            //Thread.currentThread().setContextClassLoader(oldClassLoader);
        }

    }
    @Override
    public Object send(String topic, String key, String value) throws Exception{
        Object recordInstance = recordConstructor.newInstance(topic,key,value);
        Method method = producerClazz.getMethod("send", recordClazz);
        Future future= (Future)method.invoke(producerInstance,recordInstance);
        return future.get();
    }

    @Override
    public void close() throws Exception{
        Method method = producerClazz.getMethod("close");
        method.invoke(producerInstance);
    }

}
