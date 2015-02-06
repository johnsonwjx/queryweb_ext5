<!DOCTYPE HTML>
<%@ page language="java" pageEncoding="GB2312" isELIgnored="false" %>
<html>

<head>
    <title>${title }</title>
    <meta http-equiv="X-UA-Compatible" content="IE=edge"/>
    <meta charset="GB2312">
    <meta http-equiv="pragma" content="no-cache">
    <meta http-equiv="cache-control" content="no-cache">
    <meta http-equiv="expires" content="0">
    <link rel="stylesheet" type="text/css" href="../script/extjs/resources/ext-theme-classic-all.css"/>
    <link rel="stylesheet" type="text/css" href="../script/extjs/resources/sencha-charts-all.css"/>
    <link rel="stylesheet" type="text/css" href="new.css"/>
    <script type="text/javascript" src="../script/extjs/ext-all.js"></script>
    <script type="text/javascript" src="../script/extjs/sencha-charts.js"></script>
    <script type="text/javascript" src="../script/extjs/ext-theme-classic.js"></script>
    <script type="text/javascript" src="../script/extjs/ext-lang-zh_CN.js"></script>
</head>
<body>
<script type="text/javascript" src="../script/log4javascript.js"></script>
<script type="text/javascript" src="../script/commonutil.js"></script>
<script type="text/javascript" src="../script/queryStore.js"></script>
<script type="text/javascript" src="../script/grid.js"></script>
<script type="text/javascript" src="../script/comfactory.js"></script>
<script type="text/javascript" src="../script/EnDeCode.js"></script>
<script type="text/javascript" src="../script/main.js"></script>
<script type="text/javascript">
    //     log4javascript.setEnabled(false);
    //界面的XML数据
    main_Module.init("${resultStyleXml}", "${conditionStyleXml}", "${pageContext.request.contextPath }" + "/");
</script>
</body>
</html>