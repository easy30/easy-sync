import com.alibaba.fastjson.JSON;
import com.cehome.easysync.jest.AliasIndices;
import com.cehome.easysync.jest.Jest;
import com.cehome.easysync.objects.config.Es;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
import io.searchbox.indices.aliases.*;
import org.junit.Test;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EsTest {

    static JestClient client;
    static Jest jest;
    static {
        Es es=new Es();
        es.setAddresses("http://192.168.0.38:9200");
        jest=new Jest(es);
        client=jest.getJestClient();
    }
    String alias="crawler_test1";

    @Test
    public void test1(){

    }
    @Test
    public void   addIndexToAlias() throws IOException {
        String index="crawler_test1_180604_143527";
        index="crawler_test1_180604_143156";

        List<AliasMapping> list=new ArrayList<>();

        AddAliasMapping addAliasMapping = new AddAliasMapping.Builder(index,alias).build();
        list.add(addAliasMapping);
        ModifyAliases modifyAliases = new ModifyAliases.Builder(list).build();
        print(client.execute(modifyAliases));

    }

    @Test
    public void getAliasesIndexList() throws IOException {
        AliasIndices aliasIndices=new AliasIndices(alias);
        JestResult jestResult=client.execute(aliasIndices);
        print(jestResult);
        System.out.println(JSON.toJSONString(aliasIndices.parse(jestResult)));
    }

    @Test
    public void testAliasesExists() throws IOException {

        alias="crawler_test111";
        AliasExists aliasExists = new AliasExists.Builder().alias(alias).build();
        JestResult jestResult=client.execute(aliasExists);
        print(jestResult);

    }

    @Test
    public void testIndexExists() throws IOException {
        System.out.println(  jest.indexExists("equipment_test"));
    }

    @Test
    public void testDeleteIndex() throws IOException {
        System.out.println(  jest.deleteIndex("crawler_content11"));
    }

    @Test
    public void testTime1() throws IOException, ParseException {
        System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2016-08-04 14:33:35").getTime());
    }

    @Test
    public void testTime2() throws IOException, ParseException {
        System.out.println(new Date(1529952232000L));
    }

    private void print(JestResult jestResult){
        if(jestResult.isSucceeded()){

            System.out.println("ok:"+jestResult.getJsonObject());
        }else{
            System.out.println("error:"+jestResult.getErrorMessage());
        }
    }
}
