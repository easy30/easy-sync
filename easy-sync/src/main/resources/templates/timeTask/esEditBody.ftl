<#ftl encoding="utf-8">
<div class="container-fluid">
    <div class="row">
        <div class="col-md-12">

            <form id="edit_form" class="form-inline" name="edit_form" method="post" action="save.htm">
                <input type="hidden" name="csrfToken" value="${csrfToken!}">
                <input type="hidden" name="id" value="${entity.id }"/>
                <input type="hidden" name="taskType" value="${entity.taskType}"/>
                <input type="hidden" name="status" value="${entity.status}"/>
                <input type="hidden" name="status" value="${entity.status}"/>
                <input type="hidden" name="appName" value="${entity.appName}"/>
                <input type="hidden" name="cron" value="5"/>
                <input type="hidden" name="cat1" value="default"/>
                <input id="taskConfig" name="taskConfig" type="hidden">

                <div class="panel panel-default">
                    <div class="panel-heading">
                        <h3 class="panel-title" lang-key="baseInfo">baseInfo</h3>
                    </div>

                    <div class="panel-body">

                        <div class="row">
                            <div class="col-md-2" lang-key="taskName">taskName</div>
                            <div class="col-md-4"><input type="text" class="form-control input-xs" name="name"
                                                         value="${entity.name!}"
                                                         data-rule-required="true"/></div>
                            <div class="col-md-5"></div>
                        </div>


                        <div class="row">
                            <div class="col-md-2" lang-key="server">server</div>
                            <div class="col-md-4">
                                <div class="btn-group">
                                    <input type="text" class="form-control  input-xs" style="margin: 3px" id="targetIp"
                                           name="targetIp"
                                           autocomplete="off"
                                           data-toggle="dropdown" aria-expanded="false" value="${entity.targetIp!}"
                                           data-rule-required="true"/>
                                    <ul class="dropdown-menu" id="targetIpList"
                                        style="overflow: auto; max-height: 300px" role="menu">
                                    <#list machines as item>
                                        <li><a href="#" onclick="edit_form.targetIp.value=this.innerText">${item}</a>
                                        </li>
                                    </#list>
                                    </ul>
                                </div>

                            </div>
                            <div class="col-md-5"></div>
                        </div>


                    </div>

                </div>
            </form>


        </div>
    </div>

    <div id="vm" class="row">
        <div class="col-md-12">

            <div class="panel panel-default">
                <div class="panel-heading">
                    <h3 class="panel-title" lang-key="datasourceConfig">datasourceConfig</h3>
                </div>

                <div class="panel-body">

                    <div class="row">
                        <div class="col-md-2" lang-key="selectMysqlInstance">selectMysqlInstance</div>
                        <div class="col-md-4"><select class="form-control" v-model="mysqlSource.timeTaskId">
                        <#list mysqlSources as item>
                            <option value="" lang-key="selectOne">selectOne</option>
                            <option value="${item.id}">${item.name}</option>
                        </#list>
                        </select></div>
                        <div class="col-md-5"> </div>
                    </div>

                    <div class="row">
                        <div class="col-md-2" lang-key="databaseName">databaseName</div>
                        <div class="col-md-4"><input type="text" class="form-control"  v-model="mysqlSource.databases">
                        </div>
                        <div class="col-md-5">databases or tables sample: db1,db2,db_%,%mydb%</div>
                    </div>
                    <div class="row">
                        <div class="col-md-2" lang-key="tableName">tableName</div>
                        <div class="col-md-4"><input type="text" class="form-control" v-model="mysqlSource.tables">
                        </div>
                        <div class="col-md-5"> multi databases or tables divided by ',' , and symbol '%'(like) supported.

                        </div>
                    </div>
                    <div class="row">
                        <div class="col-md-2" lang-key="primaryKey">primaryKey</div>
                        <div class="col-md-4">
                            <input type="text" class="form-control" v-model="mysqlSource.keys">
                        </div>
                        <#--<div class="col-md-1"><span lang-key="separator">separator</span></div>
                        <div class="col-md-1"><input type="text" class="form-control" v-model="mysqlSource.keySep">
                        </div>-->
                        <div class="col-md-5"> if need full sync , key must be an increment number type</div>
                    </div>


                </div>

            </div>


            <div class="panel panel-default">
                <div class="panel-heading">
                    <h3 class="panel-title" lang-key="elasticsearchConfig">Elasticsearch config</h3>
                </div>

                <div class="panel-body">

                    <div class="row">
                        <div class="col-md-2" lang-key="elasticsearchAddresses">addresses</div>
                        <div class="col-md-4"><input type="text" class="form-control" v-model="es.addresses"></div>
                        <div class="col-md-5">addresses</div>
                    </div>


                    <div class="row">
                        <div class="col-md-2" lang-key="defaultAnalyzer">Default analyzer</div>
                        <div class="col-md-4"><input type="text" class="form-control" v-model="es.analyzer"></div>
                        <div class="col-md-5">Default analyzer</div>
                    </div>

                    <div class="row">
                        <div class="col-md-2" lang-key="indexAlias">indexAlias</div>
                        <div class="col-md-4"><input type="text" class="form-control" v-model="es.indexAlias"></div>
                        <div class="col-md-5">you must access elasticsearch by this alias.</div>
                    </div>

                    <div class="row">
                        <div class="col-md-2" lang-key="indexSettings">indexSettings</div>
                        <div class="col-md-4"><textarea type="text" class="form-control"
                                                        v-model="es.indexSettings"></textarea></div>
                        <div class="col-md-5">
                          e.g.  {   "number_of_shards" : 1,  "number_of_replicas" : 2  }
                        </div>
                    </div>


                    <div class="row">
                        <div class="col-md-2" lang-key="esVersion">esVersion</div>
                        <div class="col-md-4"><select class="form-control" v-model="es.version">

                            <option value="0"> <5</option>
                            <option value="1"> >=5</option>

                        </select></div>
                        <div class="col-md-5"></div>
                    </div>

                </div>

            </div>


        <#include "esEditMapping.ftl">



            <div class="panel panel-default">

                <div class="panel-body">

                    <div class="row">
                        <div class="col-md-12">
                            <button class="btn btn-primary" id="myModalOK" lang-key="saveConfig">saveConfig</button>
                            <span class="ask" style="margin-top: 10px" lang-key="saveConfigDesc">After save config , if databases or tables change, this will effect immediately.

                            </span>

                        </div>


                    </div>


                </div>


            </div>




            <!-- rebuild  --->
            <div class="panel panel-default">
                <div class="panel-heading">
                    <h3 class="panel-title" lang-key="rebuildSettings">rebuildSettings</h3>
                </div>

                <div class="panel-body form-inline">

                        <div>

                            <input type="checkbox" v-model="rebuild.enableFullSync">
                            <span lang-key="enableFullSync">enableFullSync</span>

                            <span style="margin-left: 10px" lang-key="fullWhere">SQL Where Condition </span>:
                            <input type="text" class="form-control" style="width: 300px" v-model="rebuild.fullWhere">

                            <span style="margin-left: 10px" lang-key="fullBatchSize">fullBatchSize </span>:
                            <input type="text" class="form-control" style="width: 50px" v-model="rebuild.fullBatchSize">

                            <span style="margin-left: 10px" lang-key="fullBatchInterval">fullBatchInterval </span>:
                            <input type="text" class="form-control" style="width: 50px"
                                   v-model="rebuild.fullBatchInterval">
                            <span lang-key="millisecond">millisecond</span>

                        </div>
                        <div>
                            <input type="checkbox" v-model="rebuild.switchAfterFullSync">
                            <span lang-key="switchAfterFullSync">switchAfterFullSync</span>

                        </div>
                        <div>
                            <input type="checkbox" v-model="rebuild.deleteOldIndex">
                            <span lang-key="deleteOldIndex">deleteOldIndex</span>
                        </div>


                    <div class="row">
                        <div class="col-md-12">
                            <button class="btn btn-primary" id='rebuildIndex' onclick="rebuildIndex('#rebuildIndex')" lang-key="rebuildIndex">
                                rebuildIndex
                            </button>
                            <button class="btn btn-primary" id="stopRebuildIndex" onclick="stopRebuildIndex('#stopRebuildIndex')" lang-key="stopRebuildIndex"></button>
                            <p class="ask" style="margin-top: 10px" lang-key="rebuildDesc"> rebuild the index alone, after done then replace old index.</p>


                        </div>
                    </div>


                </div>

            </div>


            <!-- repair  --->
            <div class="panel panel-default">
                <div class="panel-heading">
                    <h3 class="panel-title" lang-key="repairSettings">repairSettings</h3>
                </div>

                <div class="panel-body form-inline">
                   <div>



                            <span style="margin-left: 10px" lang-key="fullWhere">SQL Where Condition </span>:
                            <input type="text" class="form-control" style="width: 300px" v-model="repair.where">

                            <span style="margin-left: 10px" lang-key="fullBatchSize">fullBatchSize </span>:
                            <input type="text" class="form-control" style="width: 50px" v-model="repair.batchSize">

                            <span style="margin-left: 10px" lang-key="fullBatchInterval">fullBatchInterval </span>:
                            <input type="text" class="form-control" style="width: 50px"   v-model="repair.batchInterval">
                            <span lang-key="millisecond">millisecond</span>

                   </div>

                    <div class="row">
                        <div class="col-md-12">
                            <button class="btn btn-primary" id="repairData" onclick="repairData('#repairData')" lang-key="repairData">
                                repairData
                            </button>
                            <button class="btn btn-primary" id="stopRepairData" onclick="stopRepairData('#stopRepairData')" lang-key="stopRepairData"></button>
                            <p class="ask" style="margin-top: 10px" lang-key="repairDataDesc"> </p>

                        </div>
                    </div>


                </div>

            </div>



        </div>


    </div>


</div>