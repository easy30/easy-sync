import org.apache.commons.beanutils.ConvertUtils;
import org.junit.Assert;
import org.junit.Test;


public class ConvertTest {

    @Test
    public void booleanTest(){
        Object[][] objects={
                {"",false},   {"0",false} ,{0,false},    {"false",false},  {10,false},{"sss",false},
                {"-100",false},
                {new Object(),false},
                {null,false},
                {"1",true},{"true",true},{"TRUE",true},
        };
        convert(objects);

    }


    public void convert(Object[][] objects){
        for(Object[] o :objects){
            System.out.println(o[0]+","+o[1]);
            Object v= ConvertUtils.convert(o[0], o[1].getClass());
            Assert.assertEquals(v,o[1]);
        }
    }
}
