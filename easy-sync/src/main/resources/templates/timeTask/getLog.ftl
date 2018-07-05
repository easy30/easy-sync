<#ftl encoding="utf-8">

<HTML>
<#include "../include/constants.ftl">
<#include "../include/head-meta.ftl">
<#--<head>
    <title lang-key="log">log</title>
    <link rel="shortcut icon" href="${ctx}/res/img/snoopy2.png" type="image/png">
</head>-->
 
<body >
 <script src="${ctx}/res/timeTask/js/Common.js"></script>
 <#include "../include/head-detail.ftl">
 <table style="margin-left: 8px" border="0" width="99%" height="80%">
			<tr><td> ${taskName }</td></tr>
			<tr>
				<td height="30px">
                    <span lang-key="pageSize">pageSize</span>: ${pageSize} K  &nbsp;
					<span lang-key="totalSize">totalSize</span>: ${totalSize}K &nbsp;
					<font color="red">${pn}</font>/${pageCount?c}页&nbsp;&nbsp;

					<a
						href="getLog.htm?id=${RequestParameters.id}&pn=1" lang-key="firstPage"></a>
					<a
						href="getLog.htm?id=${RequestParameters.id}&pn=${(pn-1)?c}" lang-key="prevPage"></a>
					<a
						href="getLog.htm?id=${RequestParameters.id}&pn=${(pn + 1)?c}" lang-key="nextPage"></a>
					<a
						href="getLog.htm?id=${RequestParameters.id}&pn=-2" lang-key="lastPage"></a>
					&nbsp;&nbsp;
					<input type="text" id="jump" size="1" style="width:40px"  ><input type="button" value="Go" onClick="doJump()">
					&nbsp;&nbsp;&nbsp;&nbsp;

                    <span lang-key="pageSize">pageSize</span>（K）：<input type="text" id="pageSize"  style="width:40px"  >
					<button  onClick="doSetPageSize()" lang-key="setting">setting</button>

                    <input type="checkbox" id="nowrap" style="margin-left: 10px" onclick="doNoWrap(this)">No Wrap


				</td>
			</tr>

	<tr>
		<td ><textarea id="talog" class="form-control" style="width: 100%;">${log}</textarea></td>
	</tr>
	
	<script type="text/javascript">
		 
		 function doJump()
		 {
		 	location.href="getLog.htm?id=${RequestParameters.id}&pn="+document.getElementById("jump").value;
		 }
		 
		 function doSetPageSize()
		 {
			 Common.setCookie("pageSize", document.getElementById("pageSize").value);

		 }
		 function doNoWrap(sender) {
             Common.setCookie("nowrap", sender.checked?"nowrap":"normal");
             $("#talog").css("white-space", Common.getCookie("nowrap","normal"));
         }

         //document.getElementById("talog").style.height=( document.body.scrollHeight-80)+"px";

		 $(document).ready(function () {
		     console.log(window.innerHeight);
             $("#talog").height(window.innerHeight-130);


             $("#talog").css("white-space", Common.getCookie("nowrap","normal"));
             if(Common.getCookie("nowrap","normal")=="nowrap")  document.getElementById("nowrap").checked=true;

         })
		
		</script>
</table>
</body>
</HTML>
