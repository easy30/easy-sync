<#ftl encoding="utf-8">
<#include "../include/constants.ftl">
<#import "../include/detail.ftl" as frame>
<@frame.page title="" customerCss=[] customerJs=[] specificLib=[]>
<style>
    .row > div {
        margin-left: 3px;
        margin-top: 3px;
    }
</style>
    <#include "esEditBody.ftl">
<script>
    var vmCustomData=${entity.taskConfig!"null"};
    var vmData=  {
        mysqlSource: {}, es: {'analyzer': 'ik_max_word'},
        esMapping: {esFields: []} ,
        rebuild:{enableFullSync:true, fullBatchSize:20,fullBatchInterval:20, switchAfterFullSync:true, deleteOldIndex:true},
        repair:{ batchSize:20,batchInterval:20}
    };
    $.extend( true, vmData, vmCustomData );
    var vm = new Vue({
        el: '#vm',
        data:vmData,
        methods: {
            insertField: function () {
                this.esMapping.esFields.push({});
            },
            deleteField: function (index) {
                this.esMapping.esFields.splice(index, 1)
            },
            deleteAll: function (index) {
                this.esMapping.esFields=[];
            },
            autoMapping: function () {

                if(this.mysqlSource.timeTaskId=="" || this.mysqlSource.timeTaskId=="0" || this.mysqlSource.tables=="" ){
                    alert("Select mysql instance and tables first!");
                    return;
                }

                var self=this;
                Common.ajaxPost("esAutoMapping.htm", {"taskConfig":JSON.stringify(this.$data) },

                        function (success, data) {
                            if (success){
                                var json=JSON.parse(data);
                                for(var i in json)
                                self.esMapping.esFields.push(json[i]);

                            }  else
                                alert("错误：" + data);
                        });
                //this.esMapping.esFields.push({});
            },

        }
    });

    $(document).ready(
            function () {

                $("#myModalOK").unbind("click").click(function () {

                    $("#taskConfig", $("#edit_form")).val(JSON.stringify(vm.$data));

                    $("#edit_form").submit();

                });
                $("#edit_form").validate(
                        {
                            submitHandler: function (form) {
                                Common.ajaxSubmit("#edit_form", function (success, text) {
                                    if (success) {
                                        popover( $("#myModalOK"), $lg("saveSuccess") + " [" + new Date().toLocaleString() + "]",true);
                                        /*if (edit_form.id.value == "0") {
                                            setTimeout(function () {
                                                window.location=document.URL.replace("id=0","id="+text);
                                            },1000);

                                        } else{
                                            edit_form.id.value = text;
                                        }*/
                                        edit_form.id.value = text;

                                    }
                                    else {
                                        popover( $("#myModalOK"),text + " [" + new Date().toLocaleString() + "]")  ;
                                    }

                                });

                            }
                        });

            });

    function rebuildIndex(sender){
        $("#myModalOK").click();
        doPost(sender,"esRebuildIndex.htm?id="+edit_form.id.value);

    }

    function stopRebuildIndex(sender) {
        doPost(sender,"esStopRebuildIndex.htm?id="+edit_form.id.value);
    }

    function repairData(sender){
        $("#myModalOK").click();
        doPost(sender,"esRepairData.htm?id="+edit_form.id.value);

    }

    function stopRepairData(sender) {
        doPost(sender,"esStopRepairData.htm?id="+edit_form.id.value);
    }

    function doPost(sender,url,data) {
        Common.ajaxPost(url, data,

                function (success, data) {
                    if (success){
                       popover(sender, $lg("success") + ". "+data,true);

                    }  else
                        popover(sender,"error：" + data);
                });
    }

    function onAppChange(sender) {
        var v = sender.value;
        Common.ajaxPost("getIpList.htm", {"appName": v}, function (success, text) {
            var list = JSON.parse(text);
            var h = "";
            for (var i = 0; i < list.length; i++) {
                h += '<li><a href="#" onclick="edit_form.targetIp.value=this.innerText">' + list[i] + '</a></li>';
            }
            $("#targetIpList").html(h);
        });
    }


</script>

    <#include "res.ftl">


</@frame.page>