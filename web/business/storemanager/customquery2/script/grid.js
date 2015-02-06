var gridFactory = (function () {
   var grid=null,simplecolumns=[];
    var createGrid = function (ele) {
        var config = {id: "extgrid",store:store_module.store};
        for (var i = 0, e; e = ele.childNodes[i++];) {
            var propName = e.nodeName.toLowerCase();
            var value = propName != "columns" ? commonutil.getNodeText(e) : null;
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
                    config.hidden =  value.toLowerCase() == 'false';
                    break;
                case "columns":
                    initSimpleColumns(e);
                    break;
            }
        }
        config.columns =createColumns();
        config.bbar = {
            xtype: 'pagingtoolbar'
        };
        this.grid=Ext.create("Ext.grid.Panel", config);
        return this.grid ;
    };
    var createColumns=function(){
        if(this.simplecolumns.length==0){
            return [] ;
        }
        return this.simplecolumns;
    } ;

    var initSimpleColumns = function (columnNodes) {
        this.simplecolumns=[];
        var fields=[];
        if (!columnNodes || columnNodes.childElementCount <= 0) {
            store_module.store.setFields(fields);
            return ;
        }
        for (var i = 0, columnNode; columnNode = columnNodes.childNodes[i++];) {
            if (columnNode.nodeType != 1) {
                continue;
            }
            var column = {};
            for (var j = 0, propNode; propNode = columnNode.childNodes[j++];) {
                var propName = propNode.nodeName.toLowerCase();
                var value = commonutil.getNodeText(propNode);
                switch (propName) {
                    case "fieldname":
                        if (Ext.isEmpty(value)) {
                            throw new Error("存在列,列字段为空");
                        }
                        column.dataIndex = value;
                        fields.push({
                            name: value,
                            type: "auto",
                            useNull: true
                        }) ;
                        break;
                    case "title":
                        column.text = value;
                        break;
                    case "width":
                        column.width = parseInt(value);
                        break;
                    case "visible":
                        column.hidden = value.toLowerCase() == 'false';
                        break;
                }
            }
            this.simplecolumns.push(column);
            store_module.store.setFields(fields) ;
        }
    };
    return {
        createGrid: createGrid,
        simplecolumns:simplecolumns
    };
})();