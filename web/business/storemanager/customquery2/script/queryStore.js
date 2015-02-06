var store_module = (function () {
    var logger = log4javascript.getDefaultLogger();
    var conditionXml = "";
    var createStore = function () {
        var proxy = {
            type: 'ajax',
            timeout: 600000,
            reader: Ext.create('Ext.data.reader.Json', {
                rootProperty: "reocrds",
                totalProperty: 'totalCount',
                successProperty: 'success'
            }),
            actionMethods: {
                read: "POST"
            },
            api: {
                read: main_Module.rootpath + main_Module.projectpath + '2/jsp/customQuery.do?action=query'
            },
            listeners: {
                exception: commonutil.exceptonFn
            }
        };
        var config = {
            proxy: proxy,
            fields: [],
            listeners: {
                "beforeload": beforeload
            }
        };
        this.store = Ext.create("Ext.data.JsonStore", config);
    };
    var beforeload = function (store) {
        var extraParams = store.proxy.extraParams;
        Ext.apply(extraParams, main_Module.getRequestBaseParam());
    }
    var storeload = function (param) {
        if (this.store == null) {
            return;
        }
        if (param != null && param.hasOwnProperty('params')) {
            param = param['params'];
        }
        this.store.getProxy().setExtraParams(param);
        if (this.store.tree && this.store.getRootNode()) {
            this.store.load();
        } else {
            this.store.loadPage(1);
        }
    };
    var getCondiXmlByid = function (doc, conNode, id) {
        var com = Ext.getCmp(id);
        var value = com.getValue();
        if (!com.validate()) {
            var msg = null;
            if (!com.allowBlank && Ext.isEmpty(value.trim())) {
                msg = com.blankText;
            } else {
                msg = "条件不符合";
            }
            commonutil.errorMsg(msg);
            throw new Error(msg);
            logger.debug(com);
        }
        if (!com.canbecondition) {
            return;
        }
        var fieldname = com.fieldname;
        if (Ext.isEmpty(fieldname)) {
            return;
        }
        if (Ext.Array.contains(comFactory.combi_fieldNames, fieldname)) {
            return;
        }
        if ("radiofield" == com.xtype) {

        } else if ("fieldset" == com.xtype) {
        }
        var oper=com.oper;
        if(Ext.isEmpty(oper)){
            oper="=" ;
        }
        addCondiNode(doc,conNode,fieldname,oper,value) ;
    }

    var addCondiNode = function (doc, conNode, fieldName, operVal, value) {
        if (Ext.isString(value) && value.indexOf(',') != -1 && (value.toLowerCase().indexOf('yfnull') != -1 || value.toLowerCase().indexOf('yfnotnull') != -1)) {
            var valueArr = value.split(',')
            var condiObjArr = [];
            for (var i = 0; i < valueArr.length; i++) {
                var itemValue = valueArr[i];
                if (itemValue.toLowerCase().indexOf('yfnull') != -1 || itemValue.toLowerCase().indexOf('yfnotnull') != -1) {
                    condiObjArr.push({'fieldName': fieldName, 'operVal': itemValue, 'value': ''});
                } else {
                    condiObjArr.push({'fieldName': fieldName, 'operVal': operVal, 'value': itemValue});
                }
            }
            addCondiNodeOr(doc, condiObjArr);
        } else {
            var conFieldNode = doc.createElement(fieldName);
            var typeAttr = doc.createAttribute("type");
            typeAttr.value = operVal;
            conFieldNode.setAttributeNode(typeAttr);
            conFieldNode.appendChild(doc.createTextNode(value));
            conNode.appendChild(conFieldNode);
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
                    var fieldName = itemCondiObj['fieldName'];
                    var operVal = itemCondiObj['operVal'];
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

    var handlerCondiXml = function () {
        var doc = commonutil.getXmlDoc("<root></root>");
        var root = root.documentElement;
        var consNode = doc.createElement("condifields");
        var conNode = doc.createElement("condifield");
        root.appendChild(consNode);
        consNode.appendChild(conNode);
        for (var i = 0, id; id = comFactory.condi_com_ids[i++];) {
            getCondiXmlByid(doc, conNode, id);
        }
        for (var i = 0, id; id = comFactory.result_com_ids[i++];) {
            getCondiXmlByid(doc, conNode, id);
        }
    }
    return {
        returnfields: "",
        store: null,
        createStore: createStore,
        storeload: storeload
    };
})();