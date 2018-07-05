<#ftl encoding="utf-8">
<head>
    <meta charset="UTF-8" />
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, height=device-height, maximum-scale=1.0,initial-scale=1.0, user-scalable=no">
    <meta name="format-detection" content="telephone=no">
    <meta http-equiv="x-rim-auto-match" content="none">
    <meta name="applicable-device" content="mobile">
    <title>Easy Sync</title>

    <!-- Bootstrap -->
    <link href="${ctx}/res/bootstrap-3.3.7/css/bootstrap.min.css" rel="stylesheet">
    <link href="${ctx}/res/css/style.css"  rel="stylesheet"></link>
    <link href="${ctx}/res/css/datepicker.css"  rel="stylesheet"></link>
    <!-- HTML5 shim and Respond.js for IE8 support of HTML5 elements and media queries -->
    <!-- WARNING: Respond.js doesn't work if you view the page via file:// -->
    <!--[if lt IE 9]>
    <!--<script src="https://cdn.bootcss.com/html5shiv/3.7.3/html5shiv.min.js"></script>
    <script src="https://cdn.bootcss.com/respond.js/1.4.2/respond.min.js"></script>-->

    <![endif]-->
    <!-- jQuery (necessary for Bootstrap's JavaScript plugins) -->
    <!--  <script src="https://cdn.bootcss.com/jquery/1.12.4/jquery.min.js"></script>-->
    <script>
        var ctx="${(ctx)}";
    </script>


    <script src="${ctx}/res/js/jquery-1.9.1.min.js?${updateDate}"></script>
    <script src="${ctx}/res/js/jquery-ui-1.10.3.custom.js?${updateDate}"></script>

    <script src="${ctx}/res/js/lang-${language}.js" type="text/javascript" ></script>
    <script src="${ctx}/res/js/lang-${language}2.js" type="text/javascript" ></script>
    <script src="${ctx}/res/js/lang.js" type="text/javascript" ></script>
    <script src="${ctx}/res/js/global.js"></script>

    <script src="${ctx}/res/js/zh-cn.js" type="text/javascript" ></script>
    <script src="${ctx}/res/js/vue.min.js" type="text/javascript" ></script>
    <#--<script src="${ctx}/res/js/calendar.js" type="text/javascript" ></script>-->
    <#--<script src="${ctx}/res/js/util.js?${updateDate}"></script>-->
    <#--<script src="${ctx}/res/js/lib/angular-1.2.16.js" type="text/javascript" ></script>-->

    <style>
        .navbar-inverse {
            background-color: #09c;
            border-color:   #09c;
        }

        .navbar-inverse .navbar-nav>li>a {
            color: white;
        }
        .form-control {
            padding: 3px 6px;
            font-size: 14px;
            height: 25px;
        }
    </style>
</head>