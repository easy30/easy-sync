
import com.alibaba.fastjson.JSON;
import com.github.shyiko.mysql.binlog.BinaryLogClient;
import com.github.shyiko.mysql.binlog.event.*;
import com.github.shyiko.mysql.binlog.event.deserialization.EventDeserializer;
import org.junit.Test;

import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * https://github.com/shyiko/mysql-binlog-connector-java
 */
public class BinlogTest {
    @Test
    public void test() throws IOException, InterruptedException {
        BinaryLogClient client = new BinaryLogClient("192.168.0.13", 3306, "root", "asdf1234!");
        client.setConnectTimeout(30 * 1000);
        client.setKeepAliveInterval(20 * 1000);
        client.setKeepAlive(true);
        EventDeserializer eventDeserializer = new EventDeserializer();
        eventDeserializer.setCompatibilityMode(
                EventDeserializer.CompatibilityMode.DATE_AND_TIME_AS_LONG,
                EventDeserializer.CompatibilityMode.CHAR_AND_BINARY_AS_BYTE_ARRAY
                // EventDeserializer.CompatibilityMode.In INVALID_DATE_AND_TIME_AS_MIN_VALUE
        );
      // client.setEventDeserializer(eventDeserializer);
        client.registerEventListener(new BinaryLogClient.EventListener() {
            private String table;

            @Override
            public void onEvent(Event event) {
                System.out.println(event);
                if(true) return;
                if (event.getHeader().getEventType() == EventType.TABLE_MAP) {
                    TableMapEventData data = (TableMapEventData) event.getData();

                    table = data.getTable();

                }
                if (table == null || !table.startsWith("coolma")) {
                    return;
                }
                System.out.println("------------------------");
                if (event.getHeader().getEventType() == EventType.WRITE_ROWS) {
                    WriteRowsEventData data = (WriteRowsEventData) event.getData();
                    Serializable[] row = data.getRows().get(0);
                    for (Serializable s : row)
                        if (s != null) {
                            if (s instanceof byte[]) {
                                System.out.println("byte[]=" + to(s));
                            } else
                                System.out.println(s.getClass() + "=" + s);
                        } else System.out.println("null");
                }
                if (event.getHeader().getEventType() == EventType.UPDATE_ROWS) {
                    UpdateRowsEventData data = (UpdateRowsEventData) event.getData();
                    Serializable[] row = data.getRows().iterator().next().getValue();
                    for (Serializable s : row)
                        if (s != null) {
                            if (s instanceof byte[]) {
                                System.out.println("byte[]=" + to(s));
                            } else if(s instanceof Number){
                                Long l=((Number)s).longValue();
                                l=l-8*3600*1000;
                                System.out.println(s+"="+new Date(l).toLocaleString());
                            }else
                                System.out.println(s.getClass() + "="+s);// + new Date(Long.parseLong(s.toString())).toLocaleString());
                        } else System.out.println("null");
                }

            }
        });
        //client.setBlocking();
        client.connect();
        while (true){
            Thread.sleep(1000);
            System.out.println(client.isConnected());
        }

        //System.out.println("ok");
        //java.lang.Thread.sleep(999999);
    }

    private static String to(Object s) {
        try {
            return new String((byte[]) s, "GBK");
        } catch (UnsupportedEncodingException e) {

            e.printStackTrace();
            return "";
        }
    }
}
