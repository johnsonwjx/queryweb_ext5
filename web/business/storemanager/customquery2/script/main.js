var main_Module = (function () {
    var logger = log4javascript.getDefaultLogger();
    var initTopBar = function (topbar) {
        var createBtn = function (text, iconCls, handler, hidden) {
            if (hidden) {
                return null;
            }
            return {
                id: "btn_" + iconCls,
                text: text,
                iconCls: iconCls,
                handler: handler,
                xtype: 'button',
                height: 25,
                minWidth: 70
            };
        };
        topbar.add(createBtn("条件", "getCondition", function () {
            var condi = Ext.getCmp("condi");
            if (condi) {
                condi.show();
            }
        }, false));
        topbar.add(createBtn("显示字段", "showfield", null, false));
        topbar.add(createBtn("预览", "preview", null, false));
        topbar.add(createBtn("打印", "print", null, false));
        topbar.add(createBtn("导出EXCEL", "exportGridToExcel", null, false));
        topbar.add(createBtn("导出Txt", "exportGridToTxt", null, false));
        topbar.add(createBtn("图表显示", "showDataByChart", null, false));
        topbar.add(createBtn("退出", "exit", null, false));
        topbar.doLayout();
    }

    function afterrender(viewport) {
        try {
            logger.debug("创建界面");
            var topbar = viewport.items.getAt(0);
            var container = viewport.items.getAt(1);
            initTopBar(topbar);
            var resultDom = commonutil.getXmlDoc(this.resultPnlXml);
            var root_panelEle = Ext.DomQuery.selectNode("root_panel", resultDom);
            var pageEle = null;
            for (var i = 0, tempNode; tempNode = root_panelEle.childNodes[i++];) {
                var propName = tempNode.nodeName.toLowerCase();
                switch (propName) {
                    case "property":
                        for (var j = 0, propNode; propNode = tempNode.childNodes[j++];) {
                            var pName = propNode.nodeName.toLowerCase();
                            var pVal = commonutil.getNodeText(propNode);
                            if ("titlefont" == pName) {
                                pVal = comFactory.parseFont(pVal);
                            }
                            this.baseParam[pName] = pVal;
                        }
                        break;
                    case "fields":
                        store_module.returnfields = commonutil.getNodeText(tempNode);
                        break;
                    case "controlreturnfield":
                        this.returnFieldFlag = "0" === commonutil.getNodeText(tempNode);
                        break;
                    case "pages":
                        if (tempNode.childElementCount < 1) {
                            throw new Error("结果样式不存在");
                        }
                        pageEle = tempNode.childNodes[0];
                        break;
                    case "childs":
                        if (tempNode.childElementCount < 1) {
                            throw new Error("结果样式不存在");
                        }
                        pageEle = tempNode.childNodes[0];
                        break;
                    case "styleid":
                        this.baseParam['styleid']=commonutil.getNodeText(tempNode) ;
                        break;
                }
            }
            //删除操作按钮
            if (!Ext.isEmpty(this.baseParam["hiddenoperabtns"])) {
                var arr = this.baseParam["hiddenoperabtns"].split(",");
                for (var i = 0, item; item = arr[i++];) {
                    var btn = Ext.getCmp("btn_" + item);
                    if (btn) {
                        topbar.remove(btn);
                    }
                }
            }
            var header = container.header;
            var title = this.baseParam["querytitle"];
            if (this.baseParam.titleFont) {
                title = '<span style="text-decoration:' + this.baseParam.titleFont.text_decoration + '">' + title + '</span>';
                header.setStyle("font", this.baseParam.titleFont.font);
                header.setStyle("color", this.baseParam.titleFont.color);
            }
            header.setTitle(title);
            store_module.createStore();
            comFactory.createResultPnl(container, pageEle);
            //加载数据
            var tree = Ext.getCmp("exttree");
            if (tree && (tree.delay || Ext.isEmpty(tree.buildtreebycondi))) {
                var root = tree.getRootNode();
                root.expand(tree.expandall);
            }
            if (this.baseParam["condivisible"].toLowerCase()== "true" && !Ext.isEmpty(this.condiPnlXml)) {
                var condiDom = commonutil.getXmlDoc(this.condiPnlXml);
                comFactory.createCondiContainer(condiDom);
                var condi_result = Ext.getCmp(comFactory.result_prefix);
                var condi_win=Ext.getCmp(comFactory.condi_prefix);
                if(!condi_result&&condi_win){
                    condi_win.show() ;
                }
            } else {
                topbar.remove(Ext.getCmp("btn_getCondition"));
                if (this.baseParam["canautogetresult"]&&"true"==this.baseParam["canautogetresult"].toLowerCase()){
                    store_module.store.load() ;
                }
            }
        } catch (e) {
            logger.error(e.stack);
            Ext.Msg.alert("错误", "界面创建错误:" + e.message);
        }
        Ext.getBody().unmask();
    }

    var getRequestBaseParam=function(){
        //todo
        var pageSize = 0;
        if (pageSize<1) {
            pageSize = 10000;
        }
        var requestbaseParam={
            service:this.baseParam['servername'],
            TableName:this.baseParam['tablename'],
            reportid:this.baseParam['reportid'],
            GroupByField:this.baseParam['groupbyfield'],
            OrderByField:this.baseParam['orderbyfield'],
            ShowField:this.baseParam['showfield'],
            MappedField:this.baseParam['mappedfield'],
            corpprivilegectrl:this.baseParam['corpprivilegectrl'],
            Expression:encodeURIComponent(encodeURIComponent(this.baseParam['expression'])),
            limit:pageSize,
            returnfieldlist:this.baseParam['returnfieldlist'],
            CountFieldName:this.baseParam['countfieldname'],
            CountFieldName_In:this.baseParam['countfieldname_in'],
            MinSumParam:encodeURIComponent(encodeURIComponent(this.baseParam['minsumparam'])),
            CurTempTableName:this.baseParam['curtemptablename'],
            Percentage:this.baseParam['percentage'],
            QueryTitle:encodeURIComponent(encodeURIComponent(this.baseParam['querytitle'])),
            resultStyleId:this.baseParam['styleid'],
            isPrint:"",
            treeGridField:"",
            customInputCondi:"",
            SumDescDisplayField:"",
            sumFields:"",
            noSumFields:""

        } ;
        return requestbaseParam ;
    }
    return {
        getRequestBaseParam:getRequestBaseParam,
        resultPnlXml: "",
        condiPnlXml: "",
        baseParam: {},
        returnFieldFlag: false,
        rootpath: "/",
        projectpath: "business/storemanager/customquery",
        init: function (resultPnlXml, condiPnlXml, rootpath) {
            this.resultPnlXml = resultPnlXml;
            this.condiPnlXml = condiPnlXml;
            this.rootpath = rootpath;
        },
        afterrender: afterrender
    };
})();


Ext.onReady(function () {
    Ext.getBody().mask("请稍等...");
    Ext.create("Ext.container.Viewport", {
        renderTo: Ext.getBody(),
        layout: 'border',
        frame: false,
        style: {background: 'white'},
        items: [
            {
                xtype: 'container',
                region: 'north',
                style: {background: '#dfe9f6'},
                frame: true,
                layout: {
                    type: 'hbox',
                    pack: 'left',
                    align: 'stretch'

                },
                defaults: {
                    margin: '3 0 3 25'
                },
                border: true,
                anchorSize: {
                    width: '100%'
                }
            },
            {
                xtype: 'panel',
                region: 'center',
                layout: 'absolute',
                header: {
                    xtype: 'header',
                    baseCls: null,
                    titleAlign: 'center',
                    style: {font: "normal bold 26px 宋体"}
                },
                autoScroll: true,
                margin: '0 15 10 15',
                padding: '0 0 0 0',
                title: ''
            }
        ],
        listeners: {
            afterrender: function (me) {
                setTimeout(function () {
                    main_Module.afterrender(me);
                }, 0);
            }
        }
    })
});