package com.cehome.easysync;

import com.cehome.task.Constants;
import com.cehome.task.util.IpAddressUtil;
import jsharp.util.Common;
import org.apache.commons.io.IOUtils;
import org.h2.tools.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.sql.*;


@Component
public class TableCreator implements InitializingBean,BeanPostProcessor {


    private static final Logger logger = LoggerFactory.getLogger(TableCreator.class);

    @Value("${task.h2.start:true}")
    private boolean h2Start;

    @Value("${task.h2.port:9092}")
    private String h2Port;


    @Value("${task.autoCreateTable:false}")
    private boolean autoCreateTable;

    @Value("${task.factory.appName}")
    private String appName;

    @Value("${task.factory.name}")
    private String table;

    @Value("${task.fuseHostName:false}")
    private boolean useHostName;

    @Value(Constants.CONFIG_DRIVER)
    private String driverClassName;

    @Value("${task.datasource.url}")
    private String url;

    @Value("${task.datasource.username}")
    private String username;

    @Value("${task.datasource.password}")
    private String password;

    @Override
    public void afterPropertiesSet() throws Exception {
        boolean mysql=driverClassName.indexOf("mysql") >= 0;
        logger.info(mysql?"Use mysql database":"Use h2 database");
        logger.info("url={}, table={}",url,table);
        if(autoCreateTable) {
            execute();
        }
    }

    private boolean tableExists(Statement st ,String table ,boolean mysql) throws SQLException {
        ResultSet rs = null;
        try {
            if (mysql) {
                String sql = "show tables like '" + table + "'";
                rs = st.executeQuery(sql);
                return rs.next();
            }else{
                String sql = "show tables";
                rs = st.executeQuery(sql);
                while(rs.next()){
                    if(rs.getString(1).equalsIgnoreCase(table)){
                        return true;
                    }
                }
                return false;
            }
        }finally {
            Common.closeObjects(rs);
        }
    }

    public  void execute() {

        Connection conn = null;
        Statement st = null;
        //ResultSet rs = null;


        try
        {
            boolean mysql=driverClassName.indexOf("mysql") >= 0;
            //String table2=table1+"_cache";

            Class.forName(driverClassName);

            if(!mysql && h2Start){
                try {
                    Server server = Server.createTcpServer("-tcpPort", h2Port, "-tcpAllowOthers").start();

                    System.out.println("h2 server listen at " + server.getURL());
                }catch (Exception e){
                    e.printStackTrace();
                }

            }

            conn = DriverManager.getConnection(url, username, password);


            try {
                logger.info("create begin");
                st = conn.createStatement();
                execute(st,mysql,mysql?"task_mysql.txt":"task_h2.txt",table);
                execute(st,mysql,"task_cache.txt",table+"_cache");
                //st.execute("drop   table if exists easy_search_posi ");
                execute(st,mysql,"posi.txt",table+"_posi");
                //st.execute("drop   table if exists easy_search_sync_conf ");
                execute(st,mysql,mysql?"sync_conf_mysql.txt":"sync_conf_h2.txt",table+"_sync_conf");

                /*{

                    if (tableExists(st,table1,mysql)) {
                        logger.info("table " + table1 + " exists.");

                    }else{
                        //File file = ResourceUtils.getFile("sql/"+(mysql?"task_mysql.txt":"task_h2.txt"));
                        InputStream is= this.getClass().getClassLoader().getResourceAsStream("sql/"+(mysql?"task_mysql.txt":"task_h2.txt"));
                        String sql1=IOUtils.toString(is,"UTF-8");
                        is.close();
                        //String sql1 = FileUtils.readFileToString(file,"UTF-8");
                        sql1 = sql1.replace("${tableName}", table1);

                        sql1 = sql1.replace("${ip}", useHostName?IpAddressUtil.getLocalHostName(): IpAddressUtil.getLocalHostAddress());
                        sql1 = sql1.replace("${appName}", appName);
                        st.execute(sql1);
                        logger.info("created  table " + table1);
                    }


                }*/

               /* {
                    if (tableExists(st, table2,mysql)) {
                        logger.info("table " + table2 + " exists.");

                    } else {
                    	InputStream is= this.getClass().getClassLoader().getResourceAsStream("sql/task_cache.txt");
                        String sql2=IOUtils.toString(is,"UTF-8");
                        is.close();
                        //File file = ResourceUtils.getFile("classpath:sql/task_cache.txt");
                       // String sql2 = FileUtils.readFileToString(file, "UTF-8");
                        sql2 = sql2.replace("${tableName}", table2);
                        st.execute(sql2);
                        logger.info("created  table " + table2);
                    }
                }*/

                logger.info("create end");
            }finally {
                Common.closeObjects(st);
            }



        }
        catch (Exception e){
            e.printStackTrace();
        }

        finally
        {
            Common.closeObjects(st,conn);

        }


    }

    private void execute( Statement st,boolean mysql,String sqlFile,String table) throws Exception {
        if (tableExists(st,table,mysql)) {
            logger.info("table " + table + " exists.");

        }else{
            //File file = ResourceUtils.getFile("sql/"+(mysql?"task_mysql.txt":"task_h2.txt"));
            InputStream is= this.getClass().getClassLoader().getResourceAsStream("sql/"+sqlFile);
            String sql1=IOUtils.toString(is,"UTF-8");
            is.close();
            //String sql1 = FileUtils.readFileToString(file,"UTF-8");
            sql1 = sql1.replace("${tableName}", table);

            sql1 = sql1.replace("${ip}", useHostName?IpAddressUtil.getLocalHostName(): IpAddressUtil.getLocalHostAddress());
            sql1 = sql1.replace("${appName}", appName);
            st.execute(sql1);
            logger.info("created  table " + table);
        }

    }



    @Override
    public Object postProcessBeforeInitialization(Object o, String s) throws BeansException {
        return o;
    }

    @Override
    public Object postProcessAfterInitialization(Object o, String s) throws BeansException {
        return o;
    }
}
