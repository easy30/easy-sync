<#ftl encoding="utf-8">
<#include "../include/constants.ftl">
<#setting classic_compatible=true >
<input type="hidden" name="id" value="${entity.id }"/>
<input type="hidden" name="taskType" value="${entity.taskType}"/>
<input type="hidden" name="status" value="${entity.status}"/>
<input type="hidden" name="status" value="${entity.status}"/>
<input type="hidden" name="appName" value="${entity.appName}"/>
<input type="hidden" name="cron" value="5"/>
<input type="hidden" name="cat1" value="default"/>
<input id="taskConfig" name="taskConfig" type="hidden">
<table class=""  width="100%" border="0" align="center">

        <tr style="display: none">
            <td>ID：</td>
            <td>
                    ${entity.id}
            </td>
            <td></td>
            <td>


            </td>
        </tr>

    <tr>
        <td><span color="red">*</span><span lang-key="taskName">任务名称</span>：</td>
        <td><input type="text" class="form-control input-xs" style="margin: 3px" id="name" name="name" value="${entity.name!}"
                   data-rule-required="true"/></td>
    </tr>
    <tr>
        <td>*<span lang-key="server"></span>：</td>
        <td>

            <div class="btn-group">
                <input type="text" class="form-control  input-xs" style="margin: 3px" id="targetIp" name="targetIp"
                       autocomplete="off"
                       data-toggle="dropdown" aria-expanded="false" value="${entity.targetIp!}"
                       data-rule-required="true"/>
                <ul class="dropdown-menu" id="targetIpList" style="overflow: auto; max-height: 300px" role="menu">
                    <#list machines as item>
                        <li><a href="#" onclick="edit_form.targetIp.value=this.innerText">${item}</a></li>
                    </#list>
                </ul>
            </div>

            <div class="btn-group" style=" display: none">
                <span color="red">*</span>plan：
                <input type="text" class="form-control input-xs" style="margin: 3px;" id="scheduler" name="scheduler"
                       data-toggle="dropdown" aria-expanded="false" value="default" data-rule-required="true"/>
                <ul class="dropdown-menu" style="overflow: auto; max-height: 300px" role="menu">
                    <#list propsMap.scheduler! as item>
                        <li><a href="#" onclick="edit_form.scheduler.value=this.innerText">${item}</a></li>
                    </#list>
                </ul>
            </div>
        </td>
    </tr>



    <#--    <td>${areaCustom.categoryNames[3]}：</td>
        <td>

            <div class="btn-group">
            <input type="text" class="form-control input-xs" style="margin: 3px" id="cat4" name="cat4" autocomplete="off"
            data-toggle="dropdown" aria-expanded="false" value="${cats[3]}"   />
                <ul class="dropdown-menu" style="overflow: auto; max-height: 300px" role="menu">
                    <#list propsMap.cat4  as item>
                    <li><a href="#" onclick="edit_form.cat4.value=this.innerText">${item}</a></li>
                    </#list>
                </ul>
            </div>
        </td>-->

  <#--<#if (fixed==0)>
        <tr>
            <td>Bean名称：</td>
            <td>
                <div class="btn-group">
                    <input type="text" class="form-control input-xs" style="margin: 3px" id="bean" name="bean"
                           data-toggle="dropdown" aria-expanded="false" value="${invoker.bean}"
                           data-rule-required="true"/>
                    <ul class="dropdown-menu" style="overflow: auto; max-height: 300px" role="menu">
                        <#list propsMap.bean as  item >
                            <li><a href="#" onclick="edit_form.bean.value=this.innerText">${item}</a></li>
                        </#list>
                    </ul>
                </div>

            </td>
            <td>Bean方法：</td>
            <td><input type="text" class="form-control input-xs" style="margin: 3px" id="method" name="method"
                       value="${invoker.method}" /></td>
        </tr>
        <tr>
            <td>Bean参数：</td>
            <td colspan="3"><textarea name="args" class="form-control input-xs" style="width: 97%"
                                      rows="5">${invoker.args}</textarea></td>
        </tr>
        <tr>
            <td>中断方法：</td>
            <td><input type="text" class="form-control input-xs" style="margin: 3px" id="stopMethod" name="stopMethod"
                       value="${invoker.stopMethod}"/></td>
        </tr>
    </#if>-->

      <tr>
          <td lang-key="config"></td>
          <td>
              <table id="vm" class="table table-condensed" >
                  <tr>
                      <td lang-key="mysqlUrl">mysql url</td>
                      <td><input type="text" class="form-control input-xs"  v-model="mysql.url"
                                                               data-rule-required="true"/>
                      e.g. jdbc:mysql://192.168.0.13:3306
                      </td>
                  </tr>

                  <tr>
                      <td lang-key="mysqlUser"></td>
                      <td><input type="text" class="form-control input-xs"  v-model="mysql.user"
                                 data-rule-required="true"/></td>
                  </tr>
                  <tr>
                      <td lang-key="mysqlPassword"></td>
                      <td><input type="password" class="form-control input-xs"  v-model="mysql.password"
                                 data-rule-required="true"/></td>
                  </tr>

                  <tr>
                        <td lang-key="kafkaVersion">kafka version</td>
                        <td>
                           <select class="form-control input-xs"  v-model="kafka.version">
                               <option value="0.9.0.1">0.9.0.1</option>
                               <option value="0.10.1.0" >0.10.1.0</option>
                               <option value="0.11.0.1" selected>0.11.0.1</option>
                           </select>
                        </td>
                    </tr>
                  <tr>
                      <td lang-key="kafkaServers"></td>
                      <td><input type="text" class="form-control input-xs"  v-model="kafka.servers"
                                 data-rule-required="true"/>
                          e.g. 192.168.0.39:9092
                      </td>
                  </tr>

                  <tr>
                      <td lang-key="producerConfigs"></td>
                      <td><textarea  cols="60" class="form-control input-xs"  v-model="kafka.producerConfigs"></textarea>

                      </td>
                  </tr>
                  <tr>
                      <td lang-key="consumerConfigs"></td>
                      <td><textarea cols="60" class="form-control input-xs"  v-model="kafka.consumerConfigs"></textarea>

                      </td>
                  </tr>


                    <#--<tr>
                        <td lang-key="kafkaAcks">kafka acks</td>
                        <td><input type="text" class="form-control input-xs"  v-model="kafka.acks"
                                                                                                                                                 data-rule-required="true"/>
                        </td>
                    </tr>
                    <tr>
                        <td lang-key="kafkaRetries">kafka retries</td>
                        <td><input type="text" class="form-control input-xs"  v-model="kafka.retries"
                                                             data-rule-required="true"/>
                        </td>
                    </tr>
                    <tr>
                        <td lang-key="kafkaBatchSize">kafka batch.size</td>
                        <td><input type="text" class="form-control input-xs"  v-model="kafka.batchSize"
                                                                                         data-rule-required="true"/>
                        </td>
                    </tr>
                    <tr>
                        <td lang-key="kafkaLingerMs">kafka linger.ms</td>
                        <td><input type="text" class="form-control input-xs"  v-model="kafka.lingerMs"
                                                                                                                     data-rule-required="true"/>
                        </td>
                    </tr>

                    <tr>
                        <td lang-key="kafkaBufferMemory">kafka buffer.memory</td>
                        <td><input type="text" class="form-control input-xs"  v-model="kafka.bufferMemory"
                                                                                                                     data-rule-required="true"/>
                        </td>
                    </tr>

                    <tr>
                        <td lang-key="kafkaKeySerializer">kafka key.serializer</td>
                        <td><input type="text" class="form-control input-xs"  v-model="kafka.keySerializer"
                                                                                                                     data-rule-required="true"/>
                        </td>
                    </tr>

                    <tr>
                        <td lang-key="kafkaValueSerializer">kafka value.serializer</td>
                        <td><input type="text" class="form-control input-xs"  v-model="kafka.valueSerializer"
                                                                                                                     data-rule-required="true"/>
                        </td>
                    </tr>

                    <tr>
                        <td lang-key="kafkaMessageMaxBytes">kafka message.max.bytes</td>
                        <td><input type="text" class="form-control input-xs"  v-model="kafka.messageMaxBytes"
                                                                                                                     data-rule-required="true"/>
                        </td>
                    </tr>
                    <tr>
                        <td lang-key="kafkaReplicaFetchMaxBytes">kafka replica.fetch.max.bytes</td>
                        <td><input type="text" class="form-control input-xs"  v-model="kafka.replicaFetchMaxBytes"
                                                                                                                     data-rule-required="true"/>
                        </td>
                    </tr>
                    <tr>
                        <td lang-key="kafkaEnableAutoCommit">kafka enable.auto.commit</td>
                        <td>
                            <select class="form-control input-xs"  v-model="kafka.enableAutoCommit">
                               <option value="true">true</option>
                               <option value="false" selected>false</option>
                           </select>
                        </td>
                    </tr>
                    <tr>
                        <td lang-key="kafkaKeyDeserializer">kafka key.Deserializer</td>
                        <td><input type="text" class="form-control input-xs"  v-model="kafka.keyDeserializer"
                                                                                                                                                 data-rule-required="true"/>
                        e.g. org.apache.kafka.common.serialization.StringDeserializer
                        </td>
                    </tr>
                    <tr>
                        <td lang-key="kafkaValueDeserializer">kafka value.deserializer</td>
                        <td><input type="text" class="form-control input-xs"  v-model="kafka.valueDeserializer"
                                                                                                                                                 data-rule-required="true"/>
                        e.g. org.apache.kafka.common.serialization.StringDeserializer
                        </td>
                    </tr>
                    <tr>
                        <td lang-key="kafkaFetchMessageMaxBytes">kafka fetch.message.max.bytes</td>
                        <td><input type="text" class="form-control input-xs"  v-model="kafka.fetchMessageMaxBytes"
                                                                                                                                                 data-rule-required="true"/>
                        e.g. 10MB
                        </td>
                    </tr>-->

              </table>



          </td>
      </tr>



</table>