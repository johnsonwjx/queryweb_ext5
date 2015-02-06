var commonutil = (function () {
    var logger = log4javascript.getDefaultLogger();
    var msgWin = null, errorMsgs = [];
    var getXmlDoc = function (xml) {
        try {
            var methodList = new Array("Msxml2.DOMDocument.3.0",
                "Msxml2.DOMDocument.4.0", "Msxml2.DOMDocument.5.0",
                "Msxml2.DOMDocument.6.0", "Msxml2.DOMDocument");
            for (var i = 0; i < methodList.length; i++) {
                try {
                    var XMLconact = new ActiveXObject(methodList[i]);
                    XMLconact.async = false;
                    if (XMLconact.loadXML(xml))
                        return XMLconact;
                } catch (e) {
                }
            }
            var parser = new DOMParser();
            if (parser != null) {
                parser.loadXML = function (xml) {
                    parser.doc = parser.parseFromString(xml, "text/xml");
                    return parser.doc;
                }
                parser.selectNodes = function (cXPathString) {
                    return this.doc.selectNodes(cXPathString);
                }
                parser.selectSingleNode = function (rep) {
                    return this.doc.selectSingleNode(rep);
                }
                return parser.parseFromString(xml, "text/xml");
            }
            return null;
        } catch (ex) {
            logger.error("生成xml document错误", ex);
        }
    };
    var getJsonByNode=function(node,props){
        var obj={} ;
        if(Ext.isEmpty(node)||node.childCount<1){
            return obj;
        }
        for(var i= 0,childNode;childNode=node.childNodes[i++];){
           if(1!=childNode.nodeType){
               continue;
           }
            var nodeName=childNode.nodeName.toLowerCase();
            if(props&&!Ext.Array.contains(props,nodeName)){
                continue;
            }
            var nodeValue=getNodeText(childNode) ;
            if(Ext.isEmpty(nodeValue.trim())){
                nodeValue="";
            }
            obj[nodeName]=nodeValue ;
        }
        return obj;
    }
    var getNodeText = function (node) {
        if (Ext.isIE) {
            if (node.childNodes && node.childCount == 1) {
                return  node.childNodes[0].data;
            }
            return node.text;
        } else {
            return node.textContent;
        }

    };
    var getModuleParam = function (node) {
        if (Ext.isEmpty(node)) {
            return null;
        }
        var xml;
        if (typeof (node) == 'object') {
            node = node.childNodes[0];
            if (node == null)
                return null;
            xml = "<?xml version=\"1.0\" encoding=\"gb2312\"?>" + getNodeText(node);
        } else if (typeof (node) == 'string') {
            xml = "<?xml version=\"1.0\" encoding=\"gb2312\"?>" + node;
        }
        var doc = getXmlDoc(xml);
        if (doc == null || doc == undefined)
            return;
        // 获得根节点
        var eventNode = Ext.DomQuery.selectNode("event", doc);
        if (eventNode == null) {
            return null;
        }
        var param = {};
        var childNodes = eventNode.childNodes;
        for (var i = 0, tempNode; tempNode = childNodes[i++];) {
            var nodeName = tempNode.nodeName;
            var nodeValue = getNodeText(tempNode);
            if (nodeName == 'type') {
                param.type = nodeValue;
            } else if (nodeName == 'moduleid') {
                param.moduleid = nodeValue;
            } else if (nodeName == 'inparam') {
                param.inparam = nodeValue;
            } else if (nodeName == 'outparam') {
                param.outparam = nodeValue;
            }
        }
        return param;
    };

    var errorMsg = function (msg, info) {
        if (Ext.Msg.isVisible()) {
            errorMsgs.push({
                msg: msg,
                info: info
            });
            return;
        }
        var config = {
            title: "错误",
            msg: msg,
            icon: Ext.Msg.ERROR
        };
        if (Ext.isEmpty(info)) {
            config.buttons = Ext.Msg.CANCEL;
            config.buttonText = { cancel:"关闭"};
        }else{
            config.buttons=Ext.Msg.OKCANCEL ;
            config.buttonText={ok: '详细信息',cancel:"关闭"}
        }
        config.fn = function (btn) {
            if (btn === 'ok') {
                showmessage(info);
            } else if (btn == "cancel") {
                if (errorMsgs.length > 0) {
                    var msgObj = errorMsgs.pop();
                    errorMsg(msgObj.msg, msgObj.info);
                }
            }
        }
        Ext.Msg.show(config);
    };

    var showmessage = function (msg) {
        if (msgWin == null) {
            msgWin = Ext.create('Ext.window.Window', {
                height: 200,
                width: 400,
                closeAction: "hide",
                layout: 'fit',
                items: {  // Let's put an empty grid in just to illustrate fit layout
                    xtype: 'textarea',
                    readOnly: true
                },
                listeners: {
                    close: function () {
                        if (errorMsgs.length > 0) {
                            var msgObj = errorMsgs.pop();
                            errorMsg(msgObj.msg, msgObj.info);
                        }
                    }
                }
            }).show();
        }
        msgWin.items.first().setValue(msg);
        msgWin.show();
    }
    var exceptonFn = function (proxy, response) {
        if (response.timedout) {
            errorMsg("提示", "请求超时")
            return;
        }
        var json = response.responseText;
        if (json.indexOf('该用户已经被其他机器强制登录') > 0) {
            window.location = location.href;
            return;
        }
        if (json.indexOf('<data>null</data>') > -1) {
            errorMsg("代码中心数据错误，没有上级编码信息！")
            return;
        }
        if (json.indexOf('<html') > 0) {
            // 被剔出
            window.location = location.href;
        }

        if (json.indexOf('errorMessage') >= 0) {
            errorMsg("程序发生未知错误", json)
            return;
        }
        // 2009-11-19 add by XDY
        // 加多对异常的处理，提示更合理
        if (json.indexOf("errormessage") == 0) {
            var index1 = json.indexOf("<message>");
            var index2 = json.indexOf("</message>");
            var errMsg = json.substring(index1 + 9, index2);
            var in1 = json.indexOf("<detail>");
            var in2 = json.indexOf("</detail>");
            var detailErrmsg = json.substring(in1 + 8, in2);// 详细异常信息
            if (Ext.isEmpty(detailErrmsg)) {
                detailErrmsg = '';
            }
            errorMsg(errMsg, detailErrmsg);
            return;
        }
        if (500 == response.status) {
            errorMsg("程序发生未知错误", json);
        }
    }
    var getKeyValueBySplit = function (txt, split) {
        if (Ext.isEmpty(txt) || Ext.isEmpty(split)) {
            return null;
        }
        var index = txt.indexOf(split);
        if (index == -1) {
            return null;
        }
        return [txt.substring(0, index), txt.substring(index + 1)];
    }
    var getXmlDocStr = function (xmlDoc) {
        if (Ext.isIE) {
            return xmlDoc.xml;
        } else {
            if (xmlDoc.childNodes.length <= 0) {
                return '';
            }
            var oSerializer = new XMLSerializer();
            return oSerializer.serializeToString(xmlDoc.childNodes[0]);
        }
    }
    var getComByid = function (id) {
        var com = Ext.getCmp(comFactory.condi_prefix + "_" + id);
        if (!com) {
            com = Ext.getCmp(comFactory.result_prefix + "_" + id);
        }
        return com;
    }

    var getComsByFieldName = function (coms, fieldName, parent) {
        if (parent == null) {
            return;
        }
        var childs = parent.items.items;
        if (childs && childs.length > 0) {
            for (var i = 0, child; child = childs[i++];) {
                if (child.fieldName && fieldName.toLowerCase() == child.fieldName.toLowerCase()) {
                    if (child.isFormField) {
                        coms.push(child);
                    } else if ("自定义条件组" == child.title) {

                    }
                }
            }
        }
    }
    var getComsByFieldName = function (fieldName) {
        var coms = [];
        //先条件界面
        getComsByFieldName(coms,fieldName,Ext.getCmp(comFactory.condi_prefix));
        //结果界面
        getComsByFieldName(coms,fieldName,Ext.getCmp(comFactory.result_prefix));
        logger.debug(coms);
    }

    var getOtherCondi = function (othercondi, ItemObj) {
        //获取字段值 排除当前控件的返回字段
        var getCondiComValues = function (fieldName, ItemObj) {
            var values = [];
            var com = getComByid(fieldName);
            if (!com) {
                var coms = getComsByFieldName(fieldName) ;
                if (coms.length == 1) {
                    if (!Ext.isEmpty(coms[0].getValue())) {
                        values.push(coms[0].getValue());
                    }
                } else if (coms.length > 1) {
                    var ingoreCom = null;
                    if (ItemObj && ItemObj.outparamMap && ItemObj.outparamMap.containsKey(fieldName.toLowerCase())) {
                        //排除返回字段控件
                        var ox = ItemObj.x + ItemObj.width;
                        var oy = ItemObj.y;
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
                    var comValueMap = cq2.getParams("comValueMap");
                    if (comValueMap && comValueMap.containsKey(fieldName.toUpperCase())) {
                        if (!Ext.isEmpty(comValueMap.get(fieldName.toUpperCase()))) {
                            values.push(comValueMap.get(fieldName.toUpperCase()));
                        }
                    }
                }
            }
            return values;
        }

        //获取 字段信息
        var getFieldInfo = function (str, fieldStartIndex) {
            var obj = {};
            var fieldEndIndex = str.indexOf("}");
            if (fieldEndIndex == -1) {
                errorMsg("条件错误");
                throw new Error("条件错误:" + str);
            }
            obj['value_preFix'] = str.substring(0, fieldStartIndex);
            obj['value_postFix'] = str.substring(fieldEndIndex + 1);
            obj['fieldName'] = str.substring(fieldStartIndex + 1, fieldEndIndex);
            return obj;
        }
        //获取方法的前后连个参数
        var getYfFuncParamArr = function (othercondi, operStartIndex, oper) {
            var endIndex = othercondi.indexOf(")", operStartIndex);
            var top = othercondi.substring(0, operStartIndex);
            var func = othercondi.substring(operStartIndex, endIndex + 1);
            var trail = othercondi.substring(endIndex + 1);
            var params = func.substring(oper.length + 1, func.length - 1);
            var paramArr = params.split(",");
            if (paramArr.length != 2) {
                errorMsg("条件设置错误");
                throw new Error(othercondi);
            }
            var param1 = paramArr[0].trim();
            var param2 = paramArr[1].trim();
            return {top: top, func: func, trail: trail, param1: param1, param2: param2};
        }
        var addLikeValue = function (oper, value) {
            switch (oper) {
                case "yflike":
                    return "%" + value + "%";
                case "yfllike":
                    return "" + value + "%";
                case "yfrlike":
                    return "%" + value + "";
            }
            return "%" + value + "%";
        }
        var getFuncLikeValue = function (value, param1, param2, oper, param1Dynamic, value_preFix, value_postFix) {
            var func = "";
            if (value.indexOf(',') == -1) {
                if (param1Dynamic) {
                    func = value_preFix + value + value_postFix + " like ";
                    if (param2.match(/^'\w+'$/)) {
                        param2 = param2.substring(1, param2.length - 1);
                    }
                    func += addLikeValue(oper, param2);
                } else {
                    func = param1 + " like ";
                    func += value_preFix + addLikeValue(oper, value) + value_postFix;
                }
            } else {
                var valueArr_or = value.split(",");
                var func = "(1=0";
                for (var i = 0, itemValue; itemValue = valueArr_or[i++];) {
                    if (param1Dynamic) {
                        if (param2.match(/^'\w+'$/)) {
                            param2 = param2.substring(1, param2.length - 1);
                        }
                        func += " or " + value_preFix + itemValue + value_postFix + " like ";
                        func += addLikeValue(oper, param2);
                    } else {
                        func += " or " + param1 + " like ";
                        func += value_preFix + addLikeValue(oper, itemValue) + value_postFix;
                    }
                }
                func += ")";
            }
            return  func;
        }
        //获取 有关 远方 like方法
        var coverYflike = function (othercondi, operStartIndex, oper) {
            var obj = getYfFuncParamArr(othercondi, operStartIndex, oper);
            var top = obj.top;
            var trail = obj.trail;
            var func = obj.func;
            var param1 = obj.param1;
            var param2 = obj.param2;
            var param1Dynamic = true;
            var fieldName = null;
            var value_preFix = "";
            var value_postFix = "";
            var fieldStartIndex = -1;
            if ((fieldStartIndex = param1.indexOf("{")) != -1) {
                var fieldInfo = getFieldInfo(param1, fieldStartIndex);
                value_preFix = fieldInfo['value_preFix'];
                value_postFix = fieldInfo['value_postFix'];
                fieldName = fieldInfo['fieldName'];
            } else if ((fieldStartIndex = param2.indexOf("{")) != -1) {
                var fieldInfo = getFieldInfo(param2, fieldStartIndex);
                value_preFix = fieldInfo['value_preFix'];
                value_postFix = fieldInfo['value_postFix'];
                fieldName = fieldInfo['fieldName'];
                param1Dynamic = false;
            }
            if (!fieldName) {
                errorMsg("条件错误");
                throw new Error("条件错误:" + othercondi);
            }
            var values = getCondiComValues(fieldName, ItemObj);
            if (values.length <= 0) {
                func = "1=1";
            } else {
                func = "";
                for (var i = 0, value; value = values[i++];) {
                    if (i > 1) {
                        func += " and ";
                    }
                    func += getFuncLikeValue(value, param1, param2, oper, param1Dynamic, value_preFix, value_postFix);
                }
            }
            return top + func + trail;
        }
        var coverYfOper = function (othercondi, operStartIndex, oper) {
            var addOperValue = function (param1, oper, value) {
                var func = "";
                switch (oper) {
                    case "yfequ":
                        func = param1 + "=" + value + "";
                        break;
                    case "yfnotequ":
                        func = param1 + "!=" + value + "";
                        break;
                    case "yfgt":
                        func = param1 + ">" + value + "";
                        break;
                    case "yflt":
                        func = param1 + "<" + value + "";
                        break;
                    case "yfeqgt":
                        func = param1 + ">=" + value + "";
                        break;
                    case "yfeglt":
                        func = param1 + "<=" + value + "";
                        break;
                }
                return func;
            }
            var addFuncOperValue = function (param1, oper, value, value_preFix, value_postFix) {
                var func = "";
                if (value.indexOf(",") == -1) {
                    func = addOperValue(param1, oper, value_preFix + value + value_postFix);
                } else {
                    var func = "(1=0";
                    var values_or = value.split(",");
                    for (var i = 0, itemvalue; itemvalue = values_or[i++];) {
                        itemvalue = value_preFix + itemvalue + value_postFix;
                        func += " or " + addOperValue(param1, oper, itemvalue);
                    }
                    func += ")";
                }
                return func;
            }
            var obj = getYfFuncParamArr(othercondi, operStartIndex, oper);
            var top = obj.top;
            var trail = obj.trail;
            var func = obj.func;
            var param1 = obj.param1;
            var param2 = obj.param2;
            var fieldStartIndex = -1;
            if ((fieldStartIndex = param2.indexOf("{")) == -1) {
                errorMsg("条件错误");
                throw new Error("条件错误:" + othercondi);
            }
            var fieldInfo = getFieldInfo(param2, fieldStartIndex);
            var value_preFix = fieldInfo['value_preFix'];
            var value_postFix = fieldInfo['value_postFix'];
            var fieldName = fieldInfo['fieldName'];
            var values = getCondiComValues(fieldName, ItemObj);
            if (values.length <= 0) {
                func = "1=1";
            } else {
                func = "";
                for (var i = 0, value; value = values[i++];) {
                    if (i > 1) {
                        func += " and ";
                    }
                    func += addFuncOperValue(param1, oper, value, value_preFix, value_postFix);
                }
            }
            return top + func + trail;
        }
        if (!Ext.isEmpty(othercondi) && !Ext.isEmpty(Ext.String.trim(othercondi))) {
            var operStartIndex = -1;
            while ((operStartIndex = othercondi.indexOf("[") ) != -1) {
                var inxt2 = othercondi.indexOf("]", operStartIndex);
                var subCondi = othercondi.substring(operStartIndex + 1, inxt2);
                var idx1 = subCondi.indexOf("{");
                var idx2 = subCondi.indexOf("}");
                var fieldName = subCondi.substring(idx1 + 1, idx2);
                var values = getCondiComValues(fieldName, ItemObj);
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
                        var tempSubCondi = '1=0';
                        var valueArr = value.split(",");
                        var itemCondi = subCondi.trim().substring(3).trim();
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

            while ((operStartIndex = othercondi.indexOf("yflike(")) != -1) {
                othercondi = coverYflike(othercondi, operStartIndex, "yflike");
            }
            while ((operStartIndex = othercondi.indexOf("yfllike(")) != -1) {
                othercondi = coverYflike(othercondi, operStartIndex, "yfllike");
            }
            while ((operStartIndex = othercondi.indexOf("yfrlike(")) != -1) {
                othercondi = coverYflike(othercondi, operStartIndex, "yfrlike");
            }
            while ((operStartIndex = othercondi.indexOf("yfequ(")) != -1) {
                othercondi = coverYfOper(othercondi, operStartIndex, "yfequ");
            }
            while ((operStartIndex = othercondi.indexOf("yfnotequ(")) != -1) {
                othercondi = coverYfOper(othercondi, operStartIndex, "yfnotequ");
            }
            while ((operStartIndex = othercondi.indexOf("yfgt(")) != -1) {
                othercondi = coverYfOper(othercondi, operStartIndex, "yfgt");
            }
            while ((operStartIndex = othercondi.indexOf("yfeqgt(")) != -1) {
                othercondi = coverYfOper(othercondi, operStartIndex, "yfeqgt");
            }
            while ((operStartIndex = othercondi.indexOf("yfeglt(")) != -1) {
                othercondi = coverYfOper(othercondi, operStartIndex, "yfeglt");
            }
            var fields = othercondi.match(/({\w+})/g);
            if (fields) {
                for (var i = 0, fieldItem; fieldItem = fields[i++];) {
                    if (othercondi.indexOf("{") == -1) {
                        break;
                    }
                    var fieldName = fieldItem.substring(1, fieldItem.length - 1);
                    var values = getCondiComValues(fieldName, ItemObj);
                    if (values.length != 1) {
                        errorMsg("条件设置错误!");
                        return;
                    } else {
                        othercondi = othercondi.replace(new RegExp(fieldItem, "g"), values[0]);
                    }
                }
            }
            return othercondi;
        } else {
            return '';
        }
    }
    return {
        getComByid: getComByid,
        getXmlDoc: getXmlDoc,
        getXmlDocStr: getXmlDocStr,
        getNodeText: getNodeText,
        getModuleParam: getModuleParam,
        errorMsg: errorMsg,
        exceptonFn: exceptonFn,
        getKeyValueBySplit: getKeyValueBySplit,
        getOtherCondi:getOtherCondi,
        getJsonByNode:getJsonByNode
    };
})();