package com.cehome.easysync.jest;

import com.google.gson.JsonObject;
import io.searchbox.action.GenericResultAbstractAction;
import io.searchbox.client.JestResult;

public class AliasIndices extends GenericResultAbstractAction {
    private String alias;
    public AliasIndices(String alias){
        //super(builder);
        this.alias = alias;
        setURI(buildURI());
    }

    @Override
    public String getRestMethodName() {
        return "GET";
    }

    @Override
    protected String buildURI() {
        return super.buildURI() + "/*/_alias/" + alias;
    }


    public String[] parse(JestResult jestResult){
        if(!jestResult.isSucceeded()) return new String[0];
        JsonObject jsonObject= jestResult.getJsonObject();
        return  jsonObject.keySet().toArray(new String[0]);
    }

}
