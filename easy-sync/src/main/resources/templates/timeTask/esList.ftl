<#ftl encoding="utf-8">
<#include "../include/constants.ftl">
<#import "../include/master.ftl" as frame>
<@frame.page title="${areaCustom.desc}" menu="timeTask_${taskType}" customerCss=[] customerJs=[] specificLib=[]>
<script src="${ctx}/res/timeTask/js/ajaxfileupload.js"></script>
<script src="${ctx}/res/timeTask/js/list.js"></script>
<script>
    var editPage="esEdit.htm";
    var editNewWin=true;
    var taskType="${taskType}";
    searchForm.action="esList.htm";
    function refreshList(n) {
        if(!n) {
            window.location = "${url}&scroll=" + $(document).scrollTop();
        }else{
            window.location ="${basePageUrl}&pn="+n;
        }
    }
    $(document).ready(function () {
        $(document).scrollTop(${scroll});
    });
</script>
<script>
    $(document).ready(function () {
        $("#fGroup,#fApplication,#fCreator").hide();
    })

</script>
<style>
    .fileinput-button {
        position: relative;
        display: inline-block;
        overflow: hidden;
    }

    .fileinput-button input {
        position: absolute;
        right: 0px;
        top: 0px;
        opacity: 0;
        -ms-filter: 'alpha(opacity=0)';
        font-size: 200px;
    }
</style>
</head>

<body>
<div class="container-fluid">
    <!-- <a href="#"><strong><i class="glyphicon glyphicon-tasks"></i> 时间程序</strong></a>
    <hr style="margin-top: 3px; margin-bottom: 3px"> -->
    <div class="alert" style="background-color:#f0f0f0;">
        <#include "searchForm.ftl">

    </div>
    <#include "page.ftl">

    <table class="table table-striped table-hover" style="word-break: break-all; word-wrap: break-word;">
        <tr>
            <th align="left" width="20"><input type="checkbox" name="checkbox_check_all" id="checkbox_check_all"
                                               onClick="Common.checkAllClick(this,'ids')"></th>
            <th class="canHide" lang-key="taskId">taskId</th>
            <th lang-key="taskName">taskName</th>

            <th class="canHide" lang-key="server">server</th>


            <th lang-key="status">status</th>
            <th class="canHide" lang-key="modifyTime">modifyTime</th>
       <#--     <th class="canHide" lang-key="operator">operator</th>-->

            <th lang-key="operation">operation</th>

        </tr>
        <#list list as item>
            <tr>
                <td><input type="checkbox" name="ids" id="ids" value="${item.id}"/></td>
                <td>${item.id}  <#if  item.priority==2 > <span class="label label-danger" lang-key="high">high</span></#if></td>
                <td>${item.name}</td>

                <td><#if (appName!)=="" >${item.appName}|</#if>${item.targetIp}</td>

                <td lang-key="${(item.status==0)?string("stopRed",(item.status==1)?string("runGreen","delete"))}">${(item.status==0)?string("<font color='red'>停止</font>",(item.status==1)?string("<font color='green'>运行</font>","删除"))}</td>
                <td> ${item.operTime?string("yyyy-MM-dd HH:mm:ss")}</td>
               <#-- <td>${item.operUser}</td>-->
                <td><a href="#" class="btn btn-primary btn-xs" onclick="edit(${item.id})" lang-key="modify">modify</a>


                    <#if areaCustom.taskConfig??>
                        <a href="${ctx}/search/index.htm?admin=${admin}&timeTaskId=${item.id}&errorFlag=-1"
                           class="btn btn-info btn-xs" target="_blank" lang-key="browseData">browseData</a>
                    </#if>
                    <span style="width:20px">&nbsp;</span>

                    <a href="#" class="btn btn-primary btn-xs" onclick="doPost('copy.htm?id=${item.id}',1)"  lang-key="copy">copy</a>
                    <span style="width:40px">&nbsp;</span>
                    <button class="btn btn-xs ${(item.status==1)?string("btn-warning","btn-success")}"
                            lang-key="${(item.status==1)?string("stop","start")}"
                            onclick="doStatus(this,${item.status},${item.id })"></button>

                    <a href="getLog.htm?id=${item.id}"  class="btn btn-primary btn-xs" target="_blank" lang-key="viewLog">viewLog</a>

                </td>

            </tr>
        </#list>
    </table>
    <#include "page.ftl">

</div>

    <#include "dialog.ftl">
    <#include "res.ftl">
</@frame.page>