package com.cehome.easysync.jest;

import com.cehome.easysync.objects.config.Es;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.JestResult;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.indices.DeleteIndex;
import io.searchbox.indices.IndicesExists;
import io.searchbox.indices.aliases.AddAliasMapping;
import io.searchbox.indices.aliases.AliasMapping;
import io.searchbox.indices.aliases.ModifyAliases;
import io.searchbox.indices.aliases.RemoveAliasMapping;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Jest {
    private static final Logger logger = LoggerFactory.getLogger(Jest.class);
    JestClient jestClient;

    public Jest(Es es) {
        this.jestClient = createJestClient(es);
    }

    public boolean bindAliasAndIndex(String alias, String indexName) throws IOException {
        logger.info("begin to bind index "+indexName+" to alias "+alias);
        List<AliasMapping> list = new ArrayList<>();
        AliasIndices aliasIndices = new AliasIndices(alias);
        JestResult jestResult = jestClient.execute(aliasIndices);

        if (jestResult.isSucceeded()) {
            String[] indexNames = aliasIndices.parse(jestResult);
            if(indexNames.length==1 && indexNames[0].equalsIgnoreCase(indexName)){
                logger.warn("old index is same as new index : {}, ignore binding.",indexName);
                return true;
            }
            for (String s : indexNames) {
                logger.info("old index "+s);
                RemoveAliasMapping removeAliasMapping = new RemoveAliasMapping.Builder(s, alias).build();
                list.add(removeAliasMapping);
            }
        } else if (jestResult.getResponseCode() == 404) {
            logger.info("no alias found, ignore");
        } else {

            logger.error(jestResult.getErrorMessage());
            return false;
        }


        AddAliasMapping addAliasMapping = new AddAliasMapping.Builder(indexName, alias).build();
        list.add(addAliasMapping);
        ModifyAliases modifyAliases = new ModifyAliases.Builder(list).build();
        JestResult jr = jestClient.execute(modifyAliases);
        if (jr.isSucceeded()) {
            return true;

        } else {
            logger.error("modifyAliases  error :" + jr.getErrorMessage());
            return false;
        }


    }

    public boolean deleteIndex(String indexName) {
        if(StringUtils.isBlank(indexName)){
            logger.warn("index name is blank");
            return false;
        }
        JestResult jr = null;
        try {
            //logger.info("delete index "+indexName);
            jr = jestClient.execute(new DeleteIndex.Builder(indexName).build());
            if (jr.isSucceeded() || jr.getResponseCode()==404) {
                return true;
            }
            logger.error("delete index  error :" + jr.getErrorMessage());
        } catch (IOException e) {
            logger.error("", e);
        }

        return false;


    }

    public boolean indexExists(String indexName) throws IOException {

        AliasIndices aliasIndices = new AliasIndices(indexName);
        JestResult jestResult = jestClient.execute(aliasIndices);

        //because index and alias can not be the same,    alias found , means index not exists
        if (jestResult.isSucceeded()) {
            return  false;
        } else if (jestResult.getResponseCode() == 404) {

        } else {

           throw new IOException(jestResult.getResponseCode()+","+jestResult.getErrorMessage());

        }



        jestResult = jestClient.execute(new IndicesExists.Builder(indexName).build());
            if (jestResult.isSucceeded()) {
                return true;
            }else  if(jestResult.getResponseCode()==404){
                    return false;

            }else{
                throw new IOException(jestResult.getResponseCode()+","+jestResult.getErrorMessage());
            }




    }



    public JestClient getJestClient() {
        return jestClient;
    }


    private JestClient createJestClient(Es es) {
        JestClientFactory factory = new JestClientFactory();
        List<String> addresses = Arrays.asList(es.getAddresses().split(";"));
        factory.setHttpClientConfig(new HttpClientConfig
                .Builder(addresses)// "http://localhost:9200")
                .multiThreaded(true)
                .connTimeout(10 * 000)
                .readTimeout(10 * 000)
                //Per default this implementation will create no more than 2 concurrent connections per given route
                // .defaultMaxTotalConnectionPerRoute(<YOUR_DESIRED_LEVEL_OF_CONCURRENCY_PER_ROUTE>)
                // and no more 20 connections in total
                // .maxTotalConnection(<YOUR_DESIRED_LEVEL_OF_CONCURRENCY_TOTAL>)
                .build());
        JestClient jestClient = factory.getObject();
        return jestClient;
    }
}
