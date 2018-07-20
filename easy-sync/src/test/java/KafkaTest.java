import com.cehome.easykafka.Consumer;
import com.cehome.easykafka.Producer;
import com.cehome.easykafka.consumer.ConsumerRecord;
import com.cehome.easykafka.consumer.SimpleKafkaConsumer;
import com.cehome.easykafka.producer.SimpleKafkaProducer;
import org.junit.Test;

import java.util.List;
import java.util.Properties;

public class KafkaTest {
    @Test
    public void test() throws  Exception {

        Properties props = new Properties();
//        props.put("bootstrap.servers", "localhost:9092");//"localhost:9092");
//        props.put("acks", "all");
//        props.put("retries", 0);
//        props.put("batch.size", 16384);
//        props.put("linger.ms", 1);
//        props.put("buffer.memory", 33554432);
//        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
//        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("bootstrap.servers", "localhost:9094");
        props.put("acks", "all");
        props.put("retries", "3");
        props.put("batch.size", "16384");
        props.put("linger.ms", 1);
        props.put("buffer.memory", "33554432");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("message.max.bytes", "10MB");
        props.put("replica.fetch.max.bytes", "10MBB");

        Producer producer = new SimpleKafkaProducer("0.10.1.0",props);
        for (int i = 100; i < 110; i++)
            System.out.println(
                    producer.send("my-topic", Integer.toString(i), Integer.toString(i))

            );

        producer.close();
    }

    @Test
    public void consumer()throws Exception{
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9094");
        props.put("group.id", "test");
        props.put("enable.auto.commit", "true");
        props.put("auto.commit.interval.ms", "1000");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        Consumer consumer = new SimpleKafkaConsumer("0.10.1.0",props);
        consumer.createKafkaConsumer();
        consumer.subscribe("my-topic");
        while (true) {
            List<ConsumerRecord> records = (List<ConsumerRecord>) consumer.poll(100);
            for (ConsumerRecord record : records)
                System.out.printf("offset = %d, key = %s, value = %s%n", record.offset(), record.key(), record.value());
        }
    }
}
