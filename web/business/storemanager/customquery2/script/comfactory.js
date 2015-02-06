var comFactory = (function (gridFactory) {
    var logger = log4javascript.getDefaultLogger();
    var result_com_ids = [];
    var condi_com_ids = [];
    var refleshcom_ids = [];
    var result_prefix = 'result';
    var condi_prefix = 'condi';
    //关联控件 不会有多个控件字段名相同控件
    var combi_fieldNames = [];
    var createComByType = function (type, ele, id_prefix, flxconfig) {
        logger.debug(type);
        var com = null;
        if (Ext.isEmpty(type)) {
            return com;
        }
        type = type.toLowerCase();
        if (Ext.isEmpty(id_prefix)) {
            id_prefix = '';
        }
        switch (type) {
            case 'tnewbutton':
                com = createButton(ele, id_prefix);
                break;
            case 'tnewcondipanel':
                com = createresultCondiPanel(ele);
                break;
            case 'tnewedit':
                com = createTextField(ele, id_prefix);
                break;
            case 'tnewlabel':
                com = createLabel(ele, id_prefix);
                break;
            case 'tnewcombobox':
            case 'tyearcombobox':
            case 'tmonthcombobox':
            case 'tfinishstatuscombobox':
            case 'ttreecombobox':
            case 'tlevelcombobox':
                com = createCombox(ele, id_prefix);
                break;
            case 'tnewgrid':
                com = gridFactory.createGrid(ele);
                break;
            case 'tnewtreeview':
                com = createTree(ele, id_prefix);
                break;
            case 'tnewcheckbox':
                com = createCheckbox(ele, id_prefix, flxconfig);
                break;
            case 'tnewradiobox':
                com = createRadiobox(ele, id_prefix, flxconfig);
                break;
            case 'tnewgroupbox':
                com = createGroubox(ele, id_prefix);
                break;
            case 'tnewmemo':
                com = createTextArea(ele);
                break;
            case 'tnewchart':
                com = createChart(ele);
                break;
        }
        com.id_prefix = id_prefix;
        return com;
    };
    var createChart = function (ele) {

    };
    var createTextArea = function (ele) {
        var config = {};
        for (var i = 0, e; e = ele.childNodes[i++];) {
            var propName = e.nodeName.toLowerCase();
            var value = commonutil.getNodeText(e);
            switch (propName) {
                case "left":
                    config.x = parseInt(value);
                    break;
                case "top":
                    config.y = parseInt(value);
                    break;
                case "width":
                    config.width = parseInt(value);
                    break;
                case "height":
                    config.height = parseInt(value);
                    break;
                case "visible":
                    config.hidden = value.toLowerCase() == 'false';
                    break;
                case "readonly":
                    config.editable = value.toLowerCase() != 'true';
                    break;
                case "autoadd":
                    config.anchor = value.toLowerCase() == 'true' ? '98%' : null;
                    break;

            }
        }
        if (config.height < 5) {
            return createSplitLine(config);
        }
        return Ext.create('Ext.form.field.TextArea', config);
    };

    var createSplitLine = function (config) {
        var drawConfig = {
            height: config.height,
            width: config.width,
            anchor: config.anchor,
            hidden: config.hidden,
            x: config.x,
            y: config.y,
            readOnly: true,
            items: [
                {
                    type: 'rect',
                    fillStyle: '#99bbe8',
                    opacity: 0.5,
                    height: config.height
                }
            ]
        };
        return Ext.create("Ext.draw.Component", drawConfig);
    }
    var createGroubox = function (ele, id_prefix) {
        //分组控件
        var config = {frame: true};
        var gsingle = false;
        for (var i = 0, e; e = ele.childNodes[i++];) {
            var propName = e.nodeName.toLowerCase();
            var value = commonutil.getNodeText(e);
            switch (propName) {
                case "name":
                    config.id = id_prefix + value;
                    break;
                case "left":
                    config.x = parseInt(value);
                    break;
                case "top":
                    config.y = parseInt(value);
                    break;
                case "width":
                    config.width = parseInt(value);
                    break;
                case "height":
                    config.height = parseInt(value);
                    break;
                case "visible":
                    config.hidden = value.toLowerCase() == 'false';
                    break;
                case "caption":
                    config.boxLabel = value;
                    break;
                case "singlegbbox":
                    gsingle = value.toLowerCase() == 'true';
                    break;
                case "fieldname":
                    config.fieldname = value;
                    break;
                case "operationsymbol":
                    config.oper = value;
                    break;
            }
        }
        var childitems = [];
        var childNodes = ele.nextSibling;
        var flxconfig = {name: config.id};
        for (var i = 0, e; e = childNodes.childNodes[i++];) {
            if (e.nodeType != 1) {
                continue;
            }
            var com = createComByType(e.getAttribute('classname'), Ext.DomQuery.selectNode("property", e), config.id, flxconfig);
            com.y -= 6;
            if (com === null) {
                //  throw Error("创建控件失败"+commonutil.getNodeText(e)) ;
                continue;
            }
            childitems.push(com);
        }
        var xtype = gsingle ? "radiogroup" : "checkboxgroup";
        config.items = [
            {xtype: xtype, items: childitems}
        ];
        return Ext.create('Ext.form.FieldSet', config);
    };
    var createRadiobox = function (ele, id_prefix, flxconfig) {
        var config = {};
        for (var i = 0, e; e = ele.childNodes[i++];) {
            var propName = e.nodeName.toLowerCase();
            var value = commonutil.getNodeText(e);
            switch (propName) {
                case "name":
                    config.id = id_prefix + value;
                    break;
                case "left":
                    config.x = parseInt(value);
                    break;
                case "top":
                    config.y = parseInt(value);
                    break;
                case "width":
                    config.width = parseInt(value);
                    break;
                case "height":
                    config.height = parseInt(value);
                    break;
                case "visible":
                    config.hidden = value.toLowerCase() == 'false';
                    break;
                case "caption":
                    config.boxLabel = value;
                    break;
                case "readonly":
                    config.editable = value.toLowerCase() != 'true';
                    break;
                case "checked":
                    config.value = value.toLowerCase();
                    break;
                case "fieldvalue":
                    config.inputValue = value;
                    break;
                case "operationsymbol":
                    config.oper = value;
                    break;
                case "fieldname":
                    config.fieldname = value;
                    break;
            }
        }
        if (flxconfig) {
            Ext.apply(config, flxconfig);
        }
        return Ext.create('Ext.form.field.Radio', config);
    };
    var createCheckbox = function (ele, id_prefix, flxconfig) {
        var config = {};
        for (var i = 0, e; e = ele.childNodes[i++];) {
            var propName = e.nodeName.toLowerCase();
            var value = commonutil.getNodeText(e);
            switch (propName) {
                case "name":
                    config.id = id_prefix + value;
                    break;
                case "left":
                    config.x = parseInt(value);
                    break;
                case "top":
                    config.y = parseInt(value);
                    break;
                case "width":
                    config.width = parseInt(value);
                    break;
                case "height":
                    config.height = parseInt(value);
                    break;
                case "visible":
                    config.hidden = value.toLowerCase() == 'false';
                    break;
                case "caption":
                    config.boxLabel = value;
                    break;
                case "readonly":
                    config.editable = value.toLowerCase() != 'true';
                    break;
                case "checked":
                    config.value = value.toLowerCase() == 'true';
                    break;
                case "defaultvalue":
                    config.inputValue = value;
                    break;
                case "value_postfix":
                    config.value_postfix = value;
                    break;
                case "fieldname":
                    config.fieldname = value;
                    break;
                case "fieldscondi":
                    if (!Ext.isEmpty(value)) {
                        config.combinefields = getCombiFieldsObj(value);
                    }
                    break;
                case "canbecondition":
                    config.canbecondition = value.toLowerCase() == "true";
                    break;
                case "operationsymbol":
                    config.oper = value;
                    break;
            }
        }
        if (flxconfig) {
            Ext.apply(config, flxconfig);
        }
        return Ext.create('Ext.form.field.Checkbox', config);
    };
    var createTree = function (ele, id_prefix) {
        var config = {};
        var selecttype = "0", showCode = "false", containcorp = "false", requerygrid = false;
        var codecenterdatasource = "", otherdatasource = "";
        for (var i = 0, e; e = ele.childNodes[i++];) {
            var propName = e.nodeName.toLowerCase();
            var value = commonutil.getNodeText(e);
            switch (propName) {
                case "left":
                    config.x = parseInt(value);
                    break;
                case "top":
                    config.y = parseInt(value);
                    break;
                case "width":
                    config.width = parseInt(value) + 5;
                    break;
                case "height":
                    config.height = parseInt(value);
                    break;
                case "visible":
                    config.hidden = value.toLowerCase() == 'false';
                    break;
                case "selecttype":
                    if (value.toLowerCase() == 'true') {
                        selecttype = "1";
                    }
                    break;
                case "showcode":
                    if ("true" == value || "代码居左" == value)
                        showCode = "l";
                    else if ("代码居右" == value)
                        showCode = "r";
                    else {
                        showCode = value;
                    }
                    break;
                case "buildtreebycondi":
                    config.buildtreebycondi = value;
                    break;
                case "containcorp":
                    containcorp = value.toLowerCase();
                    break;
                case "requery":
                    requerygrid = value.toLowerCase() == "true";
                    break;
                case "expandall":
                    config.expandall = value.toLowerCase() == "true";
                    break;
                case "needdelay":
                    config.delay = value.toLowerCase() == "true";
                    break;
                case "otherdatasource":
                    otherdatasource = value;
                    break;
                case "datasource":
                    codecenterdatasource = value;
                    break;
                case "name":
                    config.id = id_prefix + id;
                    break;
                case "canbecondition":
                    config.canbecondition = value.toLowerCase() == "true";
                    break;

            }
        }
        var root = {
            text: '全部',
            id: '##'
        };
        if (selecttype == "1") {
            root.checked = false;
        }
        var extraParams = {selectType: selecttype, containCorp: containcorp, needDelay: config.delay};
        if (!Ext.isEmpty(codecenterdatasource)) {
            extraParams.action = "getresulttreedatabycodecenter";
            var params = codecenterdatasource.split(",");
            extraParams.tableName = params[0];
            extraParams.codeField = params[1];
            extraParams.nameField = params[2];
            config.filtergrid_field = params[3];
            if (params.length > 4) {
                config.otherCondi = params[4];
            }
            if (params.length > 5) {
                config.oper = params[5];
            }
            if (params.length > 6) {
                extraParams.hasParent = params[6];
            }
        } else if (!Ext.isEmpty(otherdatasource)) {
            extraParams.action = "getresulttreedatabyothersource";
            var paramArr = otherdatasource.split(",");
            for (var i = 0, itemStr; itemStr = paramArr[i++];) {
                if (Ext.String.startsWith(itemStr, "customCondiValue")) {
                    var rep = new RegExp(':', 'g');
                    config.otherCondi = itemStr.substring(17).replace(rep, '=');
                } else {
                    var tempArr = commonutil.getKeyValueBySplit(itemStr, ":");
                    switch (tempArr[0]) {
                        case "serviceName":
                            extraParams.serviceName = tempArr[1];
                            break;
                        case "idParamName":
                            extraParams.idParamName = tempArr[1];
                            break;
                        case "queryTableName":
                            extraParams.tableName = tempArr[1];
                            break;
                        case "condiParamName":
                            extraParams.condiParamName = tempArr[1];
                            break;
                        case "returnParamName":
                            extraParams.returnParamName = tempArr[1];
                            break;
                        case "isSelectSQL":
                            extraParams.isSelectSQL = tempArr[1];
                            break;
                        case "codeField":
                            extraParams.codeField = tempArr[1];
                            break;
                        case "customFieldName":
                            config.filtergrid_field = tempArr[1];
                            break;
                        case "recordsNodeParam":
                            extraParams.recordsNodeParam = tempArr[1];
                            break;
                        case "recordNodeParam":
                            extraParams.recordNodeParam = tempArr[1];
                            break;
                        case "operator":
                            config.oper = tempArr[1];
                            break;

                    }
                }
            }
        } else {
            extraParams.action = "getresulttreedatabycodecenter";// 从代码中心
            extraParams.otherCondi = '(1=2)';// 不取数(只建树的一个架子，没有内容)
        }
        if (!Ext.isEmpty(config.otherCondi)) {
            config.otherCondi = strAnsi2Unicode(BASE64.decode(config.otherCondi));
        }
        config.treefield = extraParams.codeField;
        if (Ext.isEmpty(config.treefield)) {
            config.treefield = "code";
        }
        var proxy = {
            type: 'ajax',
            autoLoad: false,
            reader: {
                type: 'json',
                root: 'children',
                totalProperty: 'totalCount',
                successProperty: 'success'
            },
            timeout: 600000,
            actionMethods: {
                read: "POST"
            },
            api: {
                read: main_Module.rootpath + main_Module.projectpath + '2/jsp/customQuery.do'
            },
            listeners: {
                exception: commonutil.exceptonFn
            },
            extraParams: extraParams
        };
        var store = Ext.create('Ext.data.TreeStore', {
            storeId: "treestore",
            root: root,
            proxy: proxy
        });
        config.store = store;
        config.listeners = {
            'beforeload': function (me) {
                if (!Ext.isEmpty(this.otherCondi)) {
                    var othercondi = this.otherCondi;
                    if (othercondi.indexOf('{') != -1) {
                        othercondi = commonutil.getOtherCondi(othercondi);
                    }
                    me.proxy.extraParams.otherCondi = encodeURIComponent(encodeURIComponent(othercondi));
                }
                var rootNode = me.getRootNode()
                if (!rootNode.isExpanded()) {
                    rootNode.updateInfo(true, {expandable: true, expanded: true});
                }
            }
        }
        return Ext.create("Ext.tree.Panel", config);
    };

    var createresultCondiPanel = function (ele) {
        var config = {id: result_prefix, frame: true, layout: 'absolute', padding: "0 0 0 0"};
        for (var i = 0, e; e = ele.childNodes[i++];) {
            var propName = e.nodeName.toLowerCase();
            var value = commonutil.getNodeText(e);
            switch (propName) {
                case "left":
                    config.x = parseInt(value);
                    break;
                case "top":
                    config.y = parseInt(value);
                    break;
                case "width":
                    config.width = parseInt(value);
                    break;
                case "height":
                    config.height = parseInt(value);
                    break;
                case "visible":
                    config.hidden = value.toLowerCase() == 'false';
                    break;
            }
        }
        var childsEle = Ext.DomQuery.selectNode("childs", ele.parentNode);
        if (childsEle != undefined && childsEle.childElementCount > 0) {
            config.items = [];
            for (var i = 0, e; e = childsEle.childNodes[i++];) {
                if (e.nodeType != 1) {
                    continue;
                }
                var type = e.getAttribute('classname').toLowerCase();
                var com = createComByType(type, Ext.DomQuery.selectNode("property", e), result_prefix + "_");
                if (com === null) {
                    //  throw Error("创建控件失败"+commonutil.getNodeText(e)) ;
                    continue;
                }
                if (com.isFormField) {
                    result_com_ids.push(com.id);

                } else if (com.xtype == "button") {
                    if (com.text == "更多条件") {
                        com.handler = function () {
                            var condiBottom = Ext.getCmp(condi_prefix);
                            condiBottom.setVisible(condiBottom.isHidden());
                        }
                    } else if (com.text == "搜索") {

                    }
                }
                config.items.push(com);
            }
        }
        return Ext.create("Ext.panel.Panel", config);
    };
    var createCombox = function (ele, id_prefix) {
        var config = {valueField: "value", queryMode: 'local'};
        var addalloption = false, needgetrowdata = false, timeObj = null;
        var itemsdatasource = null, items = null;
        for (var i = 0, e; e = ele.childNodes[i++];) {
            var propName = e.nodeName.toLowerCase();
            var value = commonutil.getNodeText(e);
            switch (propName) {
                case "name":
                    config.id = id_prefix + value;
                    break;
                case "left":
                    config.x = parseInt(value);
                    break;
                case "top":
                    config.y = parseInt(value);
                    break;
                case "width":
                    config.width = parseInt(value) + 5;
                    break;
                case "height":
                    config.height = parseInt(value);
                    break;
                case "visible":
                    config.hidden = value.toLowerCase() == 'false';
                    break;
                case "caption":
                    config.fieldLabel = value;
                    break;
                case "fieldname":
                    config.fieldname = value;
                    break;
                case "operationsymbol":
                    config.oper = value;
                    break;
                case "addalloption":
                    addalloption = value.toLowerCase() == 'true';
                    break;
                case "defaultindex":
                    if (!Ext.isEmpty(value)) {
                        if (value.indexOf(":") != -1) {
                            value = value.replace(/:/g, ",");
                        }
                        config.value = value;
                    }
                    break;
                case "needgetrowdata":
                    needgetrowdata = value.toLowerCase() == "true";
                    break;
                case "canempty":
                    config.allowBlank = value.toLowerCase() == "true";
                    break;
                case "canemptystr":
                    config.canemptystr = value;
                    break;
                case "canbecondition":
                    config.canbecondition = value.toLowerCase() == "true";
                    break;
                case "nowdate":
                    if (!Ext.isEmpty(value)) {
                        timeObj = getTimeObj(value);
                    }
                    break;
                case "curdeptoruser":
                    //todo
                    break;
                case "readonly":
                    config.readOnly = value.toLowerCase() == "true";
                    break;
                case "itemsdatasource":
                    itemsdatasource = value;
                    break;
                case "items":
                    items = value;
                    break;
                case "labelwidth":
                    if(Ext.isNumeric(value)){
                        config.labelWidth=parseInt(value);
                    }
                    break;
                case "labelsparator":
                    if(!Ext.isEmpty(value)){
                        config.labelSeparator=value;
                    }
                    break;
            }
        }
        if (needgetrowdata) {
            refleshcom_ids.push(config.id);
        }
        var datas = [];
        if (addalloption) {
            datas.push({text: "全部", value: ""});
        }
        if (!Ext.isEmpty(items)) {
            var arr = items.split(",");
            for (var j = 0, itemStr; itemStr = arr[j++];) {
                var itemArr = commonutil.getKeyValueBySplit(itemStr, "=");
                if (itemArr != null) {
                    if (itemArr[1].indexOf(":") != -1) {
                        datas.push({text: itemArr[0], value: itemArr[1].replace(/:/g, ',')});
                    } else {
                        datas.push({text: itemArr[0], value: itemArr[1]});
                    }
                }
            }
        } else if (!Ext.isEmpty(itemsdatasource)) {
            var servicename, tablename, codefield, displayfield, condi;
            var doc = commonutil.getXmlDoc(itemsdatasource);
            var root = doc.documentElement;
            for (var i = 0, node; node = root.childNodes[i++];) {
                switch (node.nodeName.toLowerCase()) {
                    case "servicename":
                        servicename = commonutil.getNodeText(node);
                    case "tablename":
                        tablename = commonutil.getNodeText(node);
                    case "codefield":
                        codefield = commonutil.getNodeText(node);
                    case "displayfield":
                        displayfield = commonutil.getNodeText(node);
                    case "condi":
                        condi = commonutil.getNodeText(node);
                }
            }
            if (Ext.isEmpty(condi)) {
                condi = " 1=1 ";
            }
            var xml = null;
            if (Ext.isEmpty(servicename)) {
                xml="<root><service>codecenter.data.getlistbyalias</service><alias>"+tablename+"</alias></alias><XML>"+condi+"</XML></root>"
            } else {
                var querysql="select "+codefield+" codefield,"+displayfield+" displayfield form "+tablename+" where "+condi+" group by "+codefield+","+displayfield;
                xml = "<root><service>"+servicename+".simplequery</service><querysql>"+querysql+"</querysql>";
                codefield="codefield",displayfield="displayfield";
            }
            Ext.Ajax.request({
               url:main_Module.rootpath+main_Module.projectpath+"2/jsp/customQuery.do",
                params:{
                    action:"callService",
                    xml:encodeURIComponent(encodeURIComponent(xml)),
                    method:"post",
                    async:false,
                    success:function(response){
                        xml=response.responseText;
                    },
                    failure:function(response){
                        logger.error(response.responseText);
                        throw new Error(response.responseText);
                    }
                }
            }) ;
            doc=commonutil.getXmlDoc(xml) ;
            if(Ext.isEmpty(servicename)){
                root = doc.getElementsByTagName("codedata");
            }else{
                root = doc.getElementsByTagName("querydata");
            }
            if(root.length==0){
                logger.debug("combo获取有误");
            }else{
                for(var i= 0,itemNode;itemNode=root[i++];){
                    var obj=commonutil.getJsonByNode(itemNode,[codefield,displayfield]) ;
                    if(obj.codefield&&obj.displayfield){
                         var name=obj.displayfield ;
                        var itemValue=obj.codefield;
                        if(value.indexOf(":")!=-1){
                            itemValue=itemValue.replace(/:/g, ',');
                        }
                        data.push({name:name,value:itemValue})
                    }
                }
            }

        }
        config.store = Ext.create("Ext.data.Store", {
            fields: ["text", "value"],
            data: datas
        });
        if (timeObj) {
            config.value = timeObj.time;
        }else{
            config.value=datas[0].value;
        }

        if(!config.allowBlank){
            config.blankText=config.fieldLabel+"不能为空";
        }
        config.hideLabel=!config.labelWidth>0;
        return Ext.create("Ext.form.field.ComboBox", config);
    };
    var createLabel = function (ele, id_prefix) {
        var config = {};
        var text = "", fontObj = {}, mylayout;
        for (var i = 0, e; e = ele.childNodes[i++];) {
            var propName = e.nodeName.toLowerCase();
            var value = commonutil.getNodeText(e);
            switch (propName) {
                case "name":
                    config.id = id_prefix + value;
                    break;
                case "left":
                    config.x = parseInt(value);
                    break;
                case "top":
                    config.y = parseInt(value);
                    break;
                case "width":
                    config.width = parseInt(value) + 5;
                    break;
                case "height":
                    config.height = parseInt(value);
                    break;
                case "visible":
                    config.hidden = value.toLowerCase() == 'false';
                    break;
                case "caption":
                    text = value;
                    break;
                case "mylayout":
                    mylayout = value;
                    break;
                case "myfont":
                    if (!Ext.isEmpty(value)) {
                        fontObj = parseFont(value);
                    }
                    break;

            }
        }
        if (mylayout == '居中') {
            config.y += config.height / 2 - 7;//上边距的处理
        } else if (mylayout == '置底') {
            config.y += config.height / 2;//  上边距的处理
        }
        if (Ext.isIE) {
            var rep = new RegExp('  ', 'g');
            text = text.replace(rep, '&nbsp;');
        } else {
            var rep = new RegExp(' ', 'g');
            text = text.replace(rep, '&nbsp;');
        }
        config.html = '<font color=' + (Ext.isEmpty(fontObj.newFontColor) ? 'black' : fontObj.newFontColor) + '>' + text + '</font>'// 允许标签标题中间加空格,要用html属性，不能用text属性
        config.style = {
            "font": Ext.isEmpty(fontObj.font) ? 'normal normal 12px 宋体;' : fontObj.font,
            'text-decoration': fontObj.text_decoration
        };
        // add by XDY 2010.04.29 允许标签标题中间加空格
        return Ext.create("Ext.form.Label", config);
    };
    var showCondiWin = function (param, com) {
        var title = param['formtitle'];
        var config = {
            layout: 'border',
            width: parseInt(param.windowparam.width),
            height: parseInt(param.windowparam.height),
            autoShow: true
        }
        var tableName = param.alias;
        var inx = -1;
        if (tableName && (inx = tableName.indexOf(',')) != -1) {
            tableName = tableName.substring(0, inx);
        }
        var root = {
            text: '全部',
            id: '##'
        };
        var treelisteners = {
            afterrender: function (me) {
                me.getRootNode().expand(expendAll);
            }
        };
        var selectType = '0';
        if (param['selecttype'] == '1') {
            root.checked = false;
            selectType = '1';
            treelisteners['checkchange'] = function (node, checked) {
                node.expand();
                node.updateInfo(true, {'checked': checked});
                var tree = this;
                node.eachChild(function (child) {
                    child.updateInfo(true, {'checked': checked});
                    tree.fireEvent('checkchange', child, checked);
                });
                var parent = null;
                if (!checked) {
                    while (parent = node.parentNode) {
                        if (parent.data.checked) {
                            parent.updateInfo(true, {'checked': checked});
                            node = parent;
                        } else {
                            break;
                        }
                    }
                } else {
                    while (parent = node.parentNode) {
                        var flag = true;
                        var children = parent.childNodes;
                        for (var i = 0; node = children[i++];) {
                            // 遍历所有的孩子，如果有一个checked为false，flag＝false
                            if (!node.data.checked) {
                                flag = false;
                                break;
                            }
                        }
                        if (flag) {
                            parent.updateInfo(true, {'checked': checked});
                            node = parent;
                        } else {
                            break;
                        }
                    }
                }
            }
        }
        var needDelay = param['getdatatime'] == '0';
        var expendAll = param['expendall'] == "0";
        var showCode = param['showcode'] == "0" ? "l" : "false";// 默认不显示
        var needCorp = param['needcorp'] == "0";// 默认不控制集团权限
        var customCode = param['relationfieldname'] && param['returnfields'];// 建立树按自定义字段，有关联字段
        var codeFiled = param['buildtreefield'];
        if (!codeFiled) {
            codeFiled = '';
        }
        var nameField = param['treenamefield'];
        if (!nameField) {
            nameField = '';
        }
        var codeRule = "";
        if (param['bycodeparam'] == '1') {
            //代码中心编码规则
            Ext.Ajax.request({
                url: main_Module.rootpath + 'system/codepubmodule/jsp/getcodedata.do',
                method: 'post',
                params: {
                    action: 'getCodeRule',
                    alias: param['dataparam']['alias']

                },
                async: false,
                success: function (response) {
                    codeRule = response.responseText;
                },
                failure: function (response) {
                    logger.error(response.responseText)
                }
            });
            if (!Ext.isEmpty(codeRule)) {
                codeRule = codeRule
            }
        }
        var othercondi = param['othcondi'];// 单表的代码中心的过滤条件
        if (Ext.isEmpty(othercondi)) {
            othercondi = param['mainCondi'];// 左树右表的代码中心的过滤条件
        }
        if (!Ext.isEmpty(othercondi)) {
            othercondi = getOtherCondi(othercondi, com);
        }
        if (!Ext.isEmpty(othercondi)) {
            othercondi = encodeURIComponent(encodeURIComponent(othercondi));
        } else {
            othercondi = "";
        }
        var extraParams = {
            tableName: tableName,
            codeFiled: codeFiled,
            nameField: nameField,
            otherCondi: othercondi,
            needDelay: needDelay,
            showCode: showCode,
            needCorp: needCorp,
            dataParam: param['dataparam'],
            returnFields: param['returnFields'],
            selectType: selectType,
            customCode: customCode,
            hasparent: param['hasparent'],
            codeRule: codeRule
        };
        var store = Ext.create('Ext.data.TreeStore', {
            root: root,
            reader: {
                type: 'json',
                root: 'children',
                totalProperty: 'totalCount',
                successProperty: 'success'
            },
            proxy: {
                type: 'ajax',
                actionMethods: {
                    read: "POST"
                },
                timeout: 600000,
                api: {
                    read: main_Module.rootpath + main_Module.projectpath + '2/jsp/customQuery.do?action=getconditreedata'
                },
                extraParams: extraParams,
                // // proxy的listeners
                listeners: {
                    // add by xiong 请求异常处理
                    exception: commonutil.exceptonFn
                }
            }
        });
        var tree = Ext.create("Ext.tree.Panel", {
            region: "center",
            store: store,
            listeners: treelisteners,
            id: 'selecttree'
        });
        var items = []
        items.push(tree);
        var top_pnl = Ext.create("Ext.panel.Panel", {
            region: 'north',
            layout: 'hbox',
            defaults: {
                margin: '0 10 0 0'
            },
            frame: true,
            items: [
                {
                    xtype: "combo", valueField: 'value', store: Ext.create("Ext.data.Store", {
                    fields: ['text', 'value'], data: [
                        {text: '名称', value: 'name'},
                        {text: '代码', value: 'code'}
                    ]
                }), queryMode: 'local', width: 80, editable: false, value: 'name'
                },
                {xtype: "textfield", flex: 1},
                {
                    xtype: 'button', text: '收索', width: 70, handler: function () {
                    var text = this.prev().getValue().trim();
                    var selecttree = Ext.getCmp('selecttree');
                    if (Ext.isEmpty(text)) {
                        return;
                    }
                    var rootNode = selecttree.getRootNode();
                    var begin = rootNode.firstChild;
                    //如果根节点没有第一个儿子
                    if (!begin) {
                        return;
                    }
                    var selectnodes = selecttree.getSelection();
                    if (selectnodes.length > 0) {
                        begin = selectnodes[selectnodes.length - 1];
                    }
                    //儿子
                }
                }
            ]
        });
        items.push(top_pnl);
        config.tbar = [
            {
                text: '确定',
                handler: function (me) {
                    me.findParentByType("window").close();
                }
            },
            '-',
            '-',
            {
                text: '取消',
                handler: function (me) {
                    me.findParentByType("window").close();
                }
            },
            '-',
            '-',
            {
                text: '清除',
                handler: function (me) {
                    me.findParentByType("window").close();
                }
            }
        ];
        createWin(title, items, [], config);
    }
    var condiByWin = function (module, com) {
        if (!module) {
            return;
        }
        // 得到弹出窗口的属性数据
        var getwindowparam = function (paramstr) {
            var windowparam = {};
            var windowparamArr = paramstr.split('||');
            for (var i = 0, keyValStr; keyValStr = windowparamArr[i++];) {
                var arr = commonutil.getKeyValueBySplit(keyValStr, ":");
                if (arr[0] == 'field') {
                    var Str = arr[1];
                    var Arr = Str.split(',');
                    var cm1 = [];
                    for (var j = 0; j < Arr.length; j++) {
                        var oArr = Arr[j].split('+');
                        if (oArr[3] == 'yes') {
                            var newLabel = oArr[0];
                            if (oArr.length > 6 && oArr[6] != '') {
                                newLabel = oArr[6];
                            }
                            cm1.push({
                                'fieldName': oArr[1].toLowerCase(),
                                'fieldCName': newLabel,
                                'width': oArr[2]
                            });
                        }
                        windowparam['grid'] = cm1;
                    }
                } else
                    windowparam[arr[0]] = arr[1];
            }
            return windowparam;
        }

        try {
            var inparam = module.inparam.split(';');
            var paramObj = {};
            for (var i = 0, paramItem; paramItem = inparam[i++];) {
                var keyvalue = commonutil.getKeyValueBySplit(paramItem, "=");
                if (keyvalue[0] == "windowparam") {
                    paramObj['windowparam'] = getwindowparam(keyvalue[1]);
                } else {
                    paramObj[keyvalue[0].toLowerCase()] = keyvalue[1];
                }
            }
            // moduleid=00005001 数据源为代码中心 moduleid=cq006 其他数据源
            if ((module.moduleid == '00005002') || (module.moduleid == 'cq007')) {// 公共左树右表
                // 非代码中心
                paramObj["showtype"] = "2";
            }
            showCondiWin(paramObj, com);
        } catch (e) {
            logger.error(e.stack);
            commonutil.errorMsg("发生错误");
        }
    }

    var createTextField = function (ele, id_prefix) {
        var config = {};
        var xtype = "Ext.form.field.Text";
        var defaultvalue = null, defaultTime = null, dec = 0;
        var needgetrowdata = false;
        for (var i = 0, e; e = ele.childNodes[i++];) {
            var propName = e.nodeName.toLowerCase();
            var value = commonutil.getNodeText(e);
            switch (propName) {
                case "name":
                    config.id = id_prefix + value;
                    break;
                case "left":
                    config.x = parseInt(value);
                    break;
                case "top":
                    config.y = parseInt(value);
                    break;
                case "width":
                    config.width = parseInt(value);
                    break;
                case "height":
                    config.height = parseInt(value);
                    break;
                case "visible":
                    config.hidden = value.toLowerCase() == 'false';
                    break;
                case "caption":
                    config.fieldLabel = value;
                    break;
                case "readonly":
                    config.editable = value.toLowerCase() != 'true';
                    break;
                case "selectdate":
                    if (value.toLowerCase() != 'true') {
                        break;
                    }
                    xtype = "Ext.form.field.Date";
                    config.format = "Y-m-d";
                    break;
                case "modules":
                    var module = commonutil.getModuleParam(value);
                    if (module == null) {
                        break;
                    }
                    var outparamArr = module.outparam.split(',');
                    if (outparamArr.length > 0) {
                        config.outparamMap = new Ext.util.HashMap();
                        for (var j = 0, outparamitem; outparamitem = outparamArr[j++];) {
                            var index1 = outparamitem.indexOf("=");
                            var index2 = outparamitem.indexOf(":");
                            var comField = outparamitem.substring(0, index1);
                            var dataField = outparamitem.substring(index1 + 1, index2);
                            config.outparamMap.add(comField.toLowerCase(), dataField.toLowerCase());
                        }
                    }
                    config.triggers = {
                        event: {
                            cls: 'customTrigger',
                            handler: function () {
                                logger.debug("事件开始");

                                if (Ext.Array.contains(['00005001', 'cq006', '00005002', 'cq007'], module.moduleid)) {
                                    condiByWin(module, this);
                                } else {
                                    executeFunction();
                                }
                                logger.debug("事件结束");
                            }
                        }
                    }
                    break;
                case "canbecondition":
                    config.canbecondition = value.toLowerCase() == 'true';
                    break;
                case "fieldname":
                    config.fieldname = value;
                    break;
                case "text":
                    defaultvalue = value;
                    break;
                case "operationsymbol":
                    config.oper = value;
                    break;
                case "nowdate":
                    defaultTime = value;
                    break;
                case "dec":
                    if (Ext.isNumeric(dec)) {
                        dec = parseInt(value);
                    }
                    break;
                case "needgetrowdata":
                    needgetrowdata = value.toLowerCase() == 'true';
                    break;
                case "canempty":
                    config.allowBlank = value.toLowerCase() == 'true';
                    break;
                case "canemptystr":
                    config.canemptystr = value;
                    break;
                case "value_postfix":
                    config.value_postfix = value;
                    break;
                case "savevaluefields":
                    if (!Ext.isEmpty(value)) {
                        config.savevaluefields = getCombiFieldsObj(value);
                    }
                    break;
                case "blankfields":
                    if (!Ext.isEmpty(value)) {
                        config.blankfields = getComFieldInfoObj(value);
                    }
                    break;
                case "fieldscondi":
                    if (!Ext.isEmpty(value)) {
                        config.combinefields = getComFieldInfoObj(value);
                    }
                    break;
                case "curdeptoruser":
                    //todo
                    break;
                case "labelwidth":
                    if(Ext.isNumeric(value)){
                        config.labelWidth=parseInt(value);
                    }
                    break;
                case "labelsparator":
                    if(!Ext.isEmpty(value)){
                        config.labelSeparator=value;
                    }
                    break;
            }
        }
        if (config.height < 5) {
            return createSplitLine(config);
        }
        if (!Ext.isEmpty(defaultTime)) {
            defaultTime = getTimeObj(defaultTime);
        }
        if (config.xtype == "Ext.form.field.Date") {
            if (defaultTime) {
                config.value = defaultTime.time;
                config.format = defaultTime.format;
            }
        } else {
            if (!Ext.isEmpty(defaultvalue)) {
                if (dec > 0 && Ext.isNumeric(defaultvalue)) {
                    var format = '000.';
                    for (; (dec--) > 0;) {
                        format += "0";
                    }
                    defaultvalue = Ext.util.Format.number(defaultvalue, format);
                }
                config.value = defaultvalue;
            } else if (!Ext.isEmpty(defaultTime)) {
                config.value = defaultTime.time;
            }

            if (config.triggers) {
                //todo
            } else {
                //todo
            }
        }
        if(!config.allowBlank){
            config.blankText=config.fieldLabel+"不能为空";
        }
        config.hideLabel=!config.labelWidth>0;
        if (needgetrowdata) {
            refleshcom_ids.push(config.id);
        }
        return Ext.create(xtype, config);
    };

    var createButton = function (ele, id_prefix) {
        var config = {};
        var xtype = "Ext.Button";
        for (var i = 0, e; e = ele.childNodes[i++];) {
            var propName = e.nodeName.toLowerCase();
            var value = commonutil.getNodeText(e);
            switch (propName) {
                case "name":
                    config.id = id_prefix + value;
                    break;
                case "left":
                    config.x = parseInt(value);
                    break;
                case "top":
                    config.y = parseInt(value);
                    break;
                case "width":
                    config.width = parseInt(value);
                    break;
                case "height":
                    config.height = parseInt(value);
                    break;
                case "visible":
                    config.hidden = value.toLowerCase() == 'false';
                    break;
                case "caption":
                    config.text = value;
                    break;
                case "menus":
                    if (Ext.isEmpty(value)) {
                        break;
                    }
                    xtype = "Ext.button.Split";
                    config.menu = Ext.create('Ext.menu.Menu', {
                        items: [
                            {
                                text: 'plain item 1'
                            },
                            {
                                text: 'plain item 2'
                            },
                            {
                                text: 'plain item 3'
                            }
                        ]
                    });
                    break;
            }
        }
        return Ext.create(xtype, config);
    };

    var createResultPnl = function (resultPnl, pageEle) {
        logger.debug("建结果界面:开始");
        for (var i = 0, e; e = pageEle.childNodes[i++];) {
            if (e.nodeType != 1) {
                continue;
            }
            var com = createComByType(e.getAttribute('classname'), Ext.DomQuery.selectNode("property", e), "");
            if (com === null) {
                //  throw Error("创建控件失败"+commonutil.getNodeText(e)) ;
                continue;
            }
            resultPnl.add(com);
        }
        resultPnl.doLayout();
        logger.debug("建结果界面:结束");
    };

    var createCondiContainer = function (condiDom) {
        logger.debug("建条件界面:开始");
        var property = Ext.DomQuery.selectNode("*/property", condiDom);
        var width = Ext.DomQuery.selectValue("/Width", property);
        var height = Ext.DomQuery.selectValue("/Height", property);
        var condi_result = Ext.getCmp(result_prefix);
        var pages = Ext.DomQuery.selectNode("*/pages", condiDom);
        if (!pages) {
            pages = Ext.DomQuery.selectNode("*/childs", condiDom);
        }
        if (!pages) {
            logger.debug("条件样式为空");
            return;
        }
        pages = pages.childNodes[0];
        var condiContainer = null;
        var config = {
            id: condi_prefix,
            layout: "absolute",
            closeAction: 'hide',
            height: parseInt(height),
            width: parseInt(width),
            frame: true,
            hidden: true
        };
        var buttons = [
            {
                text: "默认条件", handler: function () {
                for (var i = 0, id; id = result_com_ids[i++];) {
                    var c = Ext.getCmp(id);
                    if (c.xtype == "fieldset") {
                        c = c.child();
                    }
                    c.reset();
                }
                for (var i = 0, id; id = condi_com_ids[i++];) {
                    var c = Ext.getCmp(id);
                    if (c.xtype == "fieldset") {
                        c = c.child();
                    }
                    c.reset();
                }
            }
            },
            {
                text: "排序字段", handler: function () {
            }
            },
            '->',
            {
                text: "确定", handler: function () {
                condiContainer.hide();
            }
            },
            {
                text: "关闭", handler: function () {
                condiContainer.hide();
            }
            }
        ]
        var items = [];
        for (var i = 0, e; e = pages.childNodes[i++];) {
            if (e.nodeType != 1) {
                continue;
            }
            var type = e.getAttribute('classname').toLowerCase();
            var com = createComByType(type, Ext.DomQuery.selectNode("property", e), condi_prefix + "_");
            if (com === null) {
                //  throw Error("创建控件失败"+commonutil.getNodeText(e)) ;
                continue;
            }
            if (com.isFormField || com.xtype == "fieldset") {
                condi_com_ids.push(com.id);
            }
            items.push(com);
        }
        if (condi_result) {
            config.floating = true;
            config.buttons = buttons;
            config.items = items;
            config.x = condi_result.x;
            config.y = condi_result.y + condi_result.height;
            condiContainer = Ext.create("Ext.panel.Panel", config);
            condi_result.ownerCt.add(condiContainer);
            //创建更多
        } else {
            config.titleAlign = 'center';
            var title = Ext.DomQuery.selectValue("/QueryTitle", property, "");
            var fontObj = parseFont(Ext.DomQuery.selectValue("/TitleFont", property));
            if (fontObj) {
                title = '<span style="gtext-decoration:' + fontObj.text_decoration + '">' + title + '</span>';
                var headerStyle = {
                    "font": fontObj.font,
                    "color": fontObj.color
                }
            }
            condiContainer = createWin(title, items, buttons, config, headerStyle);
        }
        //创建条件窗口
        logger.debug("建条件界面:结束");
    }
    var createWin = function (title, items, buttons, config, headerStyle) {
        var defaultConfig = {
            header: {
                xtype: "header",
                baseCls: "custom-window",
                height: 35,
                padding: "8 8 0 8",
                style: headerStyle
            },
            title: title,
            modal: true,
            plain: true,
            items: items,
            titleAlign: 'center',
            draggable: true,
            closable: false,
            autoScroll: true,
            buttons: buttons
        };
        if (config) {
            Ext.apply(defaultConfig, config);
        }
        return Ext.create('Ext.Window', defaultConfig);
    };
    var parseFont = function (txt) {
        if (Ext.isEmpty(txt)) {
            return null;
        }
        var fontObj = {font: null, color: null, text_decoration: null};
        var fontsArr = txt.split(":"), family, size, weight = 'normal', style = 'normal';
        var newColor = false;
        for (var i = 0, val; val = fontsArr[i++];) {
            var attr = val.split("=");
            if (attr.length != 2) {
                continue;
            }
            var key = attr[0], value = attr[1];
            switch (key) {
                case "name":
                    family = value;
                    break;
                case "size":
                    size = value;
                    break;
                case "newcolor":
                    var rgb = value.split(',');
                    if (rgb.length == 3) {
                        fontObj.color = new Ext.draw.Color(rgb[0], rgb[1], rgb[2]).toString();
                    }
                    newColor = true;
                    break;
                case "color":
                    if (!newColor) {
                        if (Ext.isEmpty(value)) {
                            return '';
                        }
                        value = value.toLowerCase();
                        if (value.indexOf('cl') == 0) {
                            value = value.substring(2);
                        }
                        if (value == 'maroon') {
                            fontObj.color = new Ext.draw.Color(176, 48, 96).toString();
                        } else if (value == 'olive') {
                            fontObj.color = new Ext.draw.Color(128, 128, 0).toString();
                        } else if (value == 'purple') {
                            fontObj.color = new Ext.draw.Color(128, 0, 128).toString();
                        } else if (value == 'fuchsia') {
                            fontObj.color = new Ext.draw.Color(255, 0, 255).toString();
                        } else if (value == 'silver') {
                            fontObj.color = new Ext.draw.Color(192, 192, 192).toString();
                        } else if (value == 'lime') {
                            fontObj.color = new Ext.draw.Color(0, 255, 0).toString();
                        } else if (value == 'teal') {
                            fontObj.color = new Ext.draw.Color(0, 128, 128).toString();
                        } else {
                            fontObj.color = value;
                        }
                    }
                    break;
                case "style":
                    if (value.indexOf(",fsBold") != -1)
                        weight = 'bold';
                    if (value.indexOf(",fsItalic") != -1)
                        style = 'italic';
                    if (value.indexOf(",fsUnderline") != -1)
                        fontObj.text_decoration += ' underline';
                    if (value.indexOf(",fsStrikeOut") != -1)
                        fontObj.text_decoration += ' line-through';
                    break;
            }
        }
        fontObj.font = style + " " + weight + " " + size + "px " + family;
        return fontObj;
    }
    var getComFieldInfoObj=function(txt){
        if (Ext.isEmpty(txt)) {
            return null;
        }
        var  fieldinfoArr= [];
        var arr = txt.split(";");
        for (var i = 0, item; item = arr[i++];) {
            var arr1 = item.split(",");
            fieldinfoArr.push({id: arr1[2], name: arr1[1]});
        }
        return fieldinfoArr;
    }
    var getCombiFieldsObj = function (txt) {
        var combinecondiArr=getComFieldInfoObj(txt) ;
        if(combinecondiArr!=null&&combinecondiArr.length>0){
            for(var i= 0,item;item=combinecondiArr[i++];){
                combi_fieldNames.push(item.name);
            }
        }
        return combinecondiArr;
    }
    var getTimeObj = function (symbol) {
        var date = new Date();
        var year = date.getFullYear();
        var month = date.getMonth() + 1;
        if (symbol.indexOf("LM") != -1) {
            month--;
        }
        var day = date.getDate();
        date.setMonth(month);//设置为下一个月
        date.setDate(0);//下一个月date设置为0 表示前一个月最后一天
        var lastdate = date.getDate();
        if (month < 10) {
            month = "0" + month;
        } else {
            month += ""
        }
        if (day < 10) {
            day = '0' + day;
        } else {
            day += "";
        }
        year += "";
        var timeObj = {
            format: "Y-m-d",
            time: year + "-" + month + "-" + day
        }
        if (symbol == 'YYYY-MM') {
            timeObj.time = year + '-' + month;
            timeObj.format = 'Y-m';
        } else if (symbol == 'YYYY') {
            timeObj.time = year;
            timeObj.format = 'Y';
        } else if (symbol == 'MM') {
            timeObj.time = month;
            timeObj.format = 'm';
        } else if (symbol == 'BM') {
            timeObj.time = year + '-' + month + '-01';
        } else if (symbol == 'EM') {
            timeObj.time = year + '-' + month + '-' + lastdate;
        } else if (symbol == 'BY') {
            timeObj.time = year + '-01-01';
        } else if (symbol == 'EY') {
            timeObj.time = year + '-12-31';
        }
        return timeObj;
    }
    var handleCondiXml = function () {
        var map = new Ext.util.HashMap();
        for (var i = 0, id; id = condi_com_ids[i++];) {
            var com = Ext.getCmp(id);
            if (com) {
                if (com.canbecondition) {
                    setCondiMap(map, com);
                }
            }
        }
        for (var i = 0, id; id = result_com_ids[i++];) {
            var com = Ext.getCmp(id);
            if (com) {
                if (com.canbecondition) {
                    setCondiMap(map, com);
                }
            }
        }
        conditionXml = getCondiXmlBymap(map);
    }

    var getCondiXmlBymap = function (map) {
        var doc = getXmlDoc("<root></root>");
        var root = doc.documentElement;
        var rootCondifieldNodes = doc.createElement("condifields");
        var rootCondifieldNode = doc.createElement("condifield");
        root.appendChild(rootCondifieldNodes);
        rootCondifieldNodes.appendChild(rootCondifieldNode);
        var keys = map.getKeys();
        for (var i = 0, key; key = keys[i];) {
            if (Ext.Array.contains(combi_fieldNames, key)) {
                continue;
            }
            var item = map.get(key);
            var value = item["value"];
            var oper = item["oper"];
            var fieldname = item["fieldname"];
            var combifields = item["combifields"];
            var id_prefix = item["id_prefix"];
            addCondiNode(doc, rootCondifieldNode, fieldname, oper, value, map, combifields, id_prefix);
        }
        return commonutil.getXmlDocStr(doc);
    }

    var addCondiNode = function (doc, rootCondifieldNode, fieldname, oper, value, map, combifields, id_prefix) {
        if (Ext.isString(value) && value.indexOf(',') != -1 && (value.toLowerCase().indexOf('yfnull') != -1 || value.toLowerCase().indexOf('yfnotnull') != -1)) {
            var valueArr = value.split(',')
            var condiObjArr = [];
            for (var i = 0; i < valueArr.length; i++) {
                var itemValue = valueArr[i];
                if (itemValue.toLowerCase().indexOf('yfnull') != -1 || itemValue.toLowerCase().indexOf('yfnotnull') != -1) {
                    condiObjArr.push({'fieldName': fieldname, 'oper': oper, 'value': ''});
                } else {
                    condiObjArr.push({'fieldName': fieldname, 'oper': oper, 'value': itemValue});
                }
            }
            addCondiNodeOr(doc, condiObjArr);
        } else {
            if (combifields && combifields.length > 0) {
                for (var i, combiitem; combiitem = combifields[i++];) {
                    var combifieldname = combiitem["name"];
                    if (map.containsKey(combifieldname)) {
                        //先字段名 ，一般关联字段都没多个字段名相同的控件
                    } else {
                        var combi_com = Ext.getCmp(id_prefix + combiitem["id"]);
                        if (combi_com) {
                            fieldname += ("加" + combi_com.fieldname);
                            value += getComValue(combi_com);
                        }
                    }
                }
            }
            var conFieldNode = doc.createElement(fieldname);
            var typeAttr = doc.createAttribute("type");
            typeAttr.value = oper;
            conFieldNode.setAttributeNode(typeAttr);
            conFieldNode.appendChild(doc.createTextNode(value));
            rootCondifieldNode.appendChild(conFieldNode);
        }
    }

    var addCondiNodeOr = function (doc, condiObjArr) {
        if (!condiObjArr || condiObjArr.length <= 1) {
            return;
        }
        var oneCondi = doc.createElement('condifield');
        var condifields = doc.createElement('condifields');
        var relation = doc.createAttribute("relation");
        relation.value = 'or';
        oneCondi.appendChild(condifields);
        condifields.setAttributeNode(relation);
        for (var i = 0; i < condiObjArr.length; i++) {
            var condiObj = condiObjArr[i];
            var tempCondi = doc.createElement('condifield');
            if (Ext.isArray(condiObj)) {
                for (var j = 0; j < condiObj.length; j++) {
                    var itemCondiObj = condiObj[j];
                    var fieldName = itemCondiObj['fieldname'];
                    var operVal = itemCondiObj['oper'];
                    var value = itemCondiObj['value'];
                    addCondiNode(doc, tempCondi, fieldName, operVal, value);
                }
            } else {
                var fieldName = condiObj['fieldName'];
                var operVal = condiObj['operVal'];
                var value = condiObj['value'];
                addCondiNode(doc, tempCondi, fieldName, operVal, value);
            }
            condifields.appendChild(tempCondi);
        }
        doc.childNodes[0].childNodes[0].appendChild(oneCondi);
    }

    var getComValue = function (com) {
        var value = "";
        if (com.xtype == "fieldset") {
            com = com.child();
            value = com.getChecked();
            if (value.length == 0) {
                return;
            }
            var itemvalue = "";
            for (var i = 0, item; item = value[i++];) {
                if ("全部" == item.boxLabel || Ext.isEmpty(com.inputValue)) {
                    continue;
                }
                if (itemvalue.length > 0) {
                    itemvalue += ",";
                }
                itemvalue += item.inputValue;
            }
        } else {
            if ("checkbox" == com.xtype || "radio" == com.xtype) {
                if ("全部" == com.boxLabel) {
                    return;
                }
                value = com.inputValue;
            } else if ("combo" == com.xtype) {
                if ("全部" == com.getRawValue()) {
                    return;
                }
                value = com.getValue();
            } else if ("datefield" == com.xtype) {
                value = com.getRawValue();
            } else {
                value = com.getValue();
            }
        }
        return value;
    }

    var checkdata_convertYFLike = function (checkstring) {
        var checkdata_convertFilterStr = function (checkstring) {
            if (checkstring.indexOf("yflike") >= 0) {
                var tempstr = checkstring;
                checkstring = "";
                while (tempstr.indexOf("yflike") >= 0) {
                    var index = tempstr.indexOf("yflike");
                    var head = tempstr.substring(0, index);
                    checkstring += head;
                    var tail = tempstr.substring(index, tempstr.length);
                    // 字符串中可能有“)”
                    tempstr = tail.substring(tail.indexOf("')") + 2, tail.length);
                    var yfstr = tail.substring(0, tail.indexOf("')") + 2);
                    var code = yfstr.substring(yfstr.indexOf("(") + 1, yfstr.indexOf(","));
                    var fields = yfstr.substring(yfstr.indexOf(",") + 1, yfstr.indexOf("')") + 1);
                    fields = trim(fields);
                    if (fields != "''") {
                        var b = false;
                        if (fields.indexOf("'") > -1) {
                            fields = fields.substring(1, fields.length - 1);
                            b = true;
                        }
                        var fieldarr = fields.split(",");
                        checkstring += "(";
                        for (var i = 0; i < fieldarr.length; i++) {
                            if (i != 0)
                                checkstring += " or ";
                            if (b)
                                checkstring += code + " like YFCONCAT('" + fieldarr[i] + "','%')";
                            else
                                checkstring += code + " like YFCONCAT(" + fieldarr[i] + ",'%')";
                        }
                        checkstring += ")";
                    } else {
                        checkstring += " 1=1 ";
                    }

                }
                checkstring += tempstr;
            }
            return checkstring;
        }
        while (checkstring.indexOf("yflike") >= 0) {
            checkstring = checkdata_convertFilterStr(checkstring);
        }
        return checkstring;
    }
    var getOtherCondi = function (othercondi, comObj) {
        if (Ext.isEmpty(Ext.String.trim(othercondi))) {
            return "";
        }
        othercondi = strAnsi2Unicode(BASE64.decode(othercondi));
        while (othercondi.indexOf("[") > -1) {
            var inxt1 = othercondi.indexOf("[");
            var inxt2 = othercondi.indexOf("]");
            var subCondi = othercondi.substring(inxt1 + 1, inxt2);
            var idx1 = subCondi.indexOf("{");
            var idx2 = subCondi.indexOf("}");
            var fieldName = subCondi.substring(idx1 + 1, idx2);
            var values = [];
            //首先检查id
            var com = commonutil.getComByid(fieldName);
            if (!com) {
                var coms = getComByFieldName(fieldName);
                if (coms.length == 1) {
                    if (!Ext.isEmpty(coms[0].getValue())) {
                        values.push(coms[0].getValue());
                    }
                } else if (coms.length > 1) {
                    var ingoreCom = null;
                    if (comObj && comObj.outparamMap && comObj.outparamMap.containsKey(fieldName.toLowerCase())) {
                        //排除返回字段控件
                        var ox = comObj.x + comObj.width;
                        var oy = comObj.y;
                        var min = null;
                        for (var i = 0; i < coms.length; i++) {
                            var temp = Math.abs(ox - coms[i].x) + Math.abs(oy - coms[i].y);
                            if (!min) {
                                min = temp;
                                ingoreCom = coms[i];
                            } else {
                                if (temp < min) {
                                    min = temp;
                                    ingoreCom = coms[i];
                                }
                            }
                        }
                    }
                    for (var i = 0; i < coms.length; i++) {
                        var temp = coms[i];
                        if (ingoreCom && ingoreCom == temp) {
                            continue;
                        }
                        temp = temp.getValue();
                        if (!Ext.isEmpty(temp)) {
                            values.push(temp);
                        }
                    }
                } else if (coms.length <= 0) {
                    //todo 穿透条件
                }
            }
            if (values.length <= 0) {
                othercondi = othercondi.replace('[' + subCondi + ']', '');
                continue;
            }
            var newSubCondi = " ";
            for (var i = 0; i < values.length; i++) {
                var value = values[i];
                if (value.indexOf(",") < 0) {
                    newSubCondi += " " + subCondi.replace('{' + fieldName + '}', value);
                } else {
                    var tempSubCondi = ' 1=0 ';
                    var valueArr = value.split(",");
                    var itemCondi = Ext.String.trim(subCondi).substring(3);
                    for (var j = 0; j < valueArr.length; j++) {
                        var curValue = valueArr[j];
                        if (!Ext.isEmpty(curValue)) {
                            tempSubCondi += " or " + itemCondi.replace('{' + fieldName + '}', curValue);
                        }
                    }
                    newSubCondi += " and (" + tempSubCondi + ") ";
                }
            }
            othercondi = othercondi.replace('[' + subCondi + ']', newSubCondi);
        }
        othercondi = checkdata_convertYFLike(othercondi);
        if (othercondi.indexOf('(') > 0) {
            // 有子查询
            var temp = {
                othercondi: othercondi,
                flag: false
            };
            while (!temp.flag) {
                subCondiConver(temp);
            }
            othercondi = temp.othercondi;
        }
        return othercondi;
    }

    function subCondiConver(condiObj) {
        var othercondi = condiObj.othercondi;
        if (Ext.isEmpty(othercondi)) {
            condiObj.flag = true;
            return;
        }
        var index = othercondi.indexOf('(');
        if (index >= 0) {
            var before = othercondi.substring(0, index + 1);
            var after = Ext.String.trim(othercondi.substring(index + 1));
            var temp = after.toUpperCase();
            if (temp.match('^AND')) {
                after = after.substring(3);
                condiObj.othercondi = before + ' ' + after;
            } else if (temp.match('^OR')) {
                after = after.substring(2);
                condiObj.othercondi = before + ' ' + after;
            } else {
                condiObj.flag = true;
            }
        } else {
            condiObj.flag = true;
        }
    }

    var getComByFieldName = function (fieldname) {
        var coms = [];
        for (var i = 0, id; id = condi_com_ids[i++];) {
            var com = Ext.getCmp(id);
            if (com && fieldname == com.fieldname) {
                coms.push(com);
            }
        }
        for (var i = 0, id; id = result_com_ids[i++];) {
            var com = Ext.getCmp(id);
            if (com && fieldname == com.fieldname) {
                coms.push(com);
            }
        }
        return coms;
    }
    var setCondiMap = function (map, com) {
        if (Ext.Array.contains(combi_fieldNames, com.fieldname)) {
            //被关联
            return;
        }
        var value = getComValue(com);
        var oper = com.oper;
        if (Ext.isEmpty(oper)) {
            oper = "=";
        }
        if (map.containsKey(com.fieldname)) {
            map.add(com.fieldname, {fieldname: com.fieldname, value: value})
        } else {
            map.add(com.fieldname, {
                fieldname: com.fieldname,
                oper: oper,
                value: value,
                combinefields: com.combinefields,
                id_prefix: com.id_prefix
            });
        }
    }

    return {
        result_prefix: this.result_prefix,
        condi_prefix: this.condi_prefix,
        createResultPnl: createResultPnl,
        createCondiContainer: createCondiContainer,
        parseFont: parseFont,
        condi_com_ids: condi_com_ids,
        result_com_ids: result_com_ids,
        combi_fieldNames:combi_fieldNames
    };
})(gridFactory || {});