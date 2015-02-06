package logic;

import com.google.gson.GsonBuilder;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import youngfriend.common.util.StringUtils;
import youngfriend.common.util.encoding.Base64;
import youngfriend.common.util.net.ServiceInvokerUtil;
import youngfriend.common.util.net.exception.ServiceInvokerException;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.*;

public class CustomQueryInvoker {

    public HashMap<String, String> load(String condiStyleId, String resultStyleId) throws ServiceInvokerException {
        Hashtable<String, String> paramE = new Hashtable<String, String>();
        paramE.put("service", "customquery.style2.get2");
        paramE.put("condiStyleId", condiStyleId);
        paramE.put("resultStyleId", resultStyleId);
        Hashtable result = ServiceInvokerUtil.invoker(paramE);
        // 对结果进行解析
        if (result == null || result.isEmpty() || result.size() == 0) {
            throw new ServiceInvokerException(CustomQueryInvoker.class, "调用服务失败：获取自定义查询数据信息失败", "获得服务返回结果为空！！！");
        }
        String errorMsg = (String) result.get("errorMessage");
        if (errorMsg != null && !"".equals(errorMsg))
            throw new ServiceInvokerException(CustomQueryInvoker.class, "调用服务失败：获取自定义查询数据信息失败", errorMsg);
        HashMap<String, String> hashMap = new HashMap<String, String>();
        if (result.get("CONDIXML") != null && !(result.get("CONDIXML").toString().equals("")))
            hashMap.put("condiXml", result.get("CONDIXML").toString());

        if (result.get("RESULTXML") != null && !(result.get("RESULTXML").toString().equals("")))
            hashMap.put("resultXml", result.get("RESULTXML").toString());

        if (result.get("CLASSID") != null && !(result.get("CLASSID").toString().equals("")))
            hashMap.put("classId", result.get("CLASSID").toString());

        if (result.get("NAME") != null && !(result.get("NAME").toString().equals("")))
            hashMap.put("name", result.get("NAME").toString());

        if (result.get("ISLEFT") != null && !(result.get("ISLEFT").toString().equals("")))
            hashMap.put("isleft", result.get("ISLEFT").toString());
        return hashMap;
    }

    public String getSearchResult(Hashtable<String, String> paramHt, HttpServletRequest request) throws ServiceInvokerException, DocumentException, UnsupportedEncodingException {
        // 对结果进行解析
        String CurTempTableName = paramHt.get("CurTempTableName");
        String SumDescDisplayField = request.getParameter("SumDescDisplayField");
        if (SumDescDisplayField.equals("undefined"))
            SumDescDisplayField = "";
        String callback = request.getParameter("callback");
        if (callback == null)
            callback = "";
        Hashtable<String, String> result = null;
        try {
            for (String key : paramHt.keySet()) {
                System.out.println(key + ":" + paramHt.get(key));
            }
            long beg = System.currentTimeMillis();
            result = ServiceInvokerUtil.invoker(paramHt);
            long end = System.currentTimeMillis();
            System.out.print(end - beg);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceInvokerException(CustomQueryInvoker.class, "调用服务失败：获取自定义查询数据信息失败", "错误信息为:" + e.toString());
        }

        if (result == null || result.isEmpty() || result.size() == 0) {
            throw new ServiceInvokerException(CustomQueryInvoker.class, "调用服务失败 ：获取自定义查询数据信息失败", "获得服务返回结果为空");
        }
        String errorMsg = (String) result.get("errorMessage");
        if (errorMsg != null && !"".equals(errorMsg))
            throw new ServiceInvokerException(CustomQueryInvoker.class, "调用服务失败：获取自定义查询数据信息失败", errorMsg);
        String xml = (String) result.get("XML");
        xml = xml.replaceAll("_is_leaf", "leaf");
        if (xml.indexOf("records") <= 0) {
            Document doc = DocumentHelper.parseText(xml);
            Element root = doc.getRootElement();
            List<String> retList = new ArrayList<String>();
            String total = root.elementText("total");
            String treeGridField = paramHt.get("treeGridField");
            if (treeGridField == null)
                treeGridField = "";
            if (root.element("querydatas") != null) {
                List<Element> lst = root.element("querydatas").elements("querydata");
                for (int i = 0; i < lst.size(); i++) {
                    Element ele = lst.get(i);
                    String retStr = " ";
                    String uuid = "";
                    for (int j = 0; j < ele.elements().size(); j++) {
                        Element ele2 = (Element) ele.elements().get(j);
                        String name = ele2.getName();
                        String value = ele2.getText().toString();
                        try {
                            if (name.equalsIgnoreCase("id")) {
                                uuid = value;
                            }
                            if (!"".equals(treeGridField) && (name.equalsIgnoreCase(treeGridField)))
                                retStr += "\"text\":\"" + value + "\",";
                            if (value.indexOf("\\") >= 0)
                                value = value.replaceAll("\\\\", "\\\\\\\\");
                            value = value.replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("\n", "\\n").replaceAll("\r", "\\r").replaceAll("\"", "\\\\\" ").replaceAll("0x0d", "<br/>").replaceAll("0x0a", "&nbsp;&nbsp;").replaceAll("&#2;", "").replaceAll("&#2", "").replaceAll("<br/>", "");
                            retStr += "\"" + name.toLowerCase() + "\":\"" + value + "\",";
                        } catch (Exception e) {
                            e.printStackTrace();
                            throw new ServiceInvokerException(CustomQueryInvoker.class, "调用服务失败：获取自定义查询数据信息失败", "解析数据错误！！！");
                        }
                    }
                    if (uuid.equals("")) {
                        uuid = UUID.randomUUID().toString().replaceAll("-", "");
                        retStr += "\"id\":\"" + uuid + "\",";
                    }
                    retStr = "{" + retStr.substring(0, retStr.length() - 1) + "}";
                    retList.add(retStr);
                }

            }
            if (root.element("sumdata") != null && !"".equals(SumDescDisplayField)) {
                String flag = paramHt.get("flag");
                String sumData = null;
                if ("0".equals(flag) || "true".equals(paramHt.get("treeFilter"))) {
                    List list = root.element("sumdata").elements("fielddata");
                    if (list.size() > 0) {
                        sumData = "\"" + SumDescDisplayField.toLowerCase() + "\":\"总计\",";
                        for (int i = 0; i < list.size(); i++) {
                            Element ele = (Element) list.get(i);
                            String value = ele.elementText("fieldvalue").toString();
                            if ("".equals(value))
                                continue;
                            sumData += "\"" + ele.elementText("fieldname").toLowerCase() + "\":\"" + value + "\",";
                        }
                        sumData = "{" + sumData.substring(0, sumData.length() - 1) + "}";
                    }
                    request.getSession().setAttribute("sumData", sumData);
                } else {
                    sumData = (String) request.getSession().getAttribute("sumData");
                }
                if (sumData != null) {
                    retList.add(sumData);
                }
            }
            if (root.element("CurTempTableName") != null)
                CurTempTableName = root.elementText("CurTempTableName").toString();
            String sumFieldStr = "";
            if (root.element("sumFields") != null) {
                sumFieldStr = "\",\"sumFields\":\"" + root.elementText("sumFields");
            }

            String QueryTitle = "";
            if (root.element("QueryTitle") != null)
                QueryTitle = root.elementText("QueryTitle").toString();
            String ColumnInfo = "";
            if (root.element("ColumnInfo") != null)
                ColumnInfo = root.element("ColumnInfo").element("columns").asXML().toString();

            xml = "{\"totalCount\":\"" + total + "\",\"CurTempTableName\":\"" + CurTempTableName + sumFieldStr + "\",\"QueryTitle\":\"" + QueryTitle + "\",\"ColumnInfo\":\"" + ColumnInfo + "\",\"records\":" + retList.toString() + ",\"success\":\"true\"}";
        }
        return callback + xml;
    }

    /**
     * modify by XDY 2009.06.09 兼容了代码中心的数据和通用业务查询的数据格式。两者的xml层次不一样。
     *
     * @param xml
     * @param needDelay
     * @param codeField
     * @param nameField
     * @param recordsNodeParam
     * @param recordNodeParam
     * @param corpLst          集团相关信息 集团id，集团code，集团name，是否叶子
     * @param corpCodeLen      底层集团的代码长度
     * @param corpCurNode      节点的集团code，用于动态建树
     * @return
     * @throws DocumentException
     */
    public String getReturnDataByXml(String xml, String needDelay, String codeField, String nameField, String recordsNodeParam, String recordNodeParam, String showCode, String selectType, String getDataParamInfo, String codeRule,List<TreeCorp> treeCorps)
            throws DocumentException {
        if (recordNodeParam == null)
            recordNodeParam = "";

        List<Element> businessLst = null;
        List<TreeNodeInfo> treeList = new ArrayList<TreeNodeInfo>();
        LinkedHashMap<String, Element> codeMap = new LinkedHashMap<String, Element>();
        List<String> codes = new ArrayList<String>();
        int total=0 ;
        if (!"".equals(xml)) {
            Document doc = DocumentHelper.parseText(xml);
            Element root = doc.getRootElement();
            if (!recordsNodeParam.equals(""))
                businessLst = root.elements(recordsNodeParam);
            else
                businessLst = root.elements("codedata");

            if (StringUtils.nullOrBlank(codeField))
                codeField = "code";
            List<Integer> codeRules = null;
            if (!StringUtils.nullOrBlank(codeRule)) {
                int tempSum=0 ;
                codeRules = new ArrayList<Integer>();
                String[] codeRuleArr = codeRule.split(",");
                for (String r : codeRuleArr) {
                    tempSum+=Integer.parseInt(r);
                    codeRules.add(tempSum);
                }
            }
            if (!"true".equals(needDelay)) {
                for (int i = 0; i < businessLst.size(); i++) {
                    Element ele = businessLst.get(i);
                    String code = "";
                    if ("".equals(recordNodeParam)) {
                        code = ele.elementText(codeField.toLowerCase());
                        codeMap.put(code, ele);
                    } else if (ele.hasContent()) {
                        List<Element> subLst = ele.elements(recordNodeParam);
                        if (subLst.isEmpty()) {
                            throw new RuntimeException(recordNodeParam + ",行记录节点设置错误");
                        }
                        for (int j = 0; j < subLst.size(); j++) {
                            Element subEle = subLst.get(j);
                            code = subEle.elementText(codeField.toLowerCase());
                            codeMap.put(code, subEle);
                        }
                    }
                }
                treeList = TreeNodeList.parseListToNodeTreeList( codeMap, codeRules);
            } else {
                for (int i = 0; i < businessLst.size(); i++) {
                    Element ele = businessLst.get(i);
                    String code = "";
                    if ("".equals(recordNodeParam)) {
                        code = ele.elementText(codeField.toLowerCase());
                        if (codes.contains(code))
                            continue;
                        else
                            codes.add(code);
                        TreeNodeInfo curNode = new TreeNodeInfo();
                        curNode.setCode(code);
                        curNode.setParentNode(null);
                        curNode.setSubNodeList(new ArrayList<TreeNodeInfo>());
                        curNode.setEleInfo(ele);
                        treeList.add(curNode);
                    }
                }
            }
        }
        total+=codeMap.size() ;
        businessLst=null ;
        codeMap=null;
        System.gc(); ;
        StringBuilder sb = null;
        if (treeCorps != null && !treeCorps.isEmpty()) { // 包含集团树信息
            total+=treeCorps.size() ;
            List<TreeCorp> bottoms = TreeNodeList.getBottomNode(treeCorps);
            sb = new StringBuilder(" [");
            if (!"true".equals(needDelay)) {
                for (TreeCorp bottom : bottoms) {
                    String otherChildren = getTreeJson(treeList, needDelay, codeField, nameField, bottom, showCode, selectType);
                    if (!StringUtils.nullOrBlank(otherChildren) && !otherChildren.equals("[]")) {
                        bottom.setOtherChildren(otherChildren);
                    }else{
                        if(bottom.getParent()!=null){
                            bottom.getParent().removeChild(bottom);
                        }else{
                            treeCorps.remove(bottom) ;
                        }
                    }
                }
            }
            for (TreeCorp node : treeCorps) {
                String temp = node.getJson(needDelay);
                if(!StringUtils.nullOrBlank(temp)){
                    sb.append(temp).append(",");
                }
            }
            sb.deleteCharAt(sb.length() - 1);
            sb.append("]");
        } else {// 不包含集团树信息
            sb = new StringBuilder(getTreeJson(treeList, needDelay, codeField, nameField, null, showCode, selectType));
            if (sb.toString().startsWith(","))
                sb.deleteCharAt(0);
            if (sb.toString().endsWith(","))
                sb.deleteCharAt(sb.length() - 1);
        }
        return "({\"totalCount\":\"" + total + "\",\"children\":" + sb.toString() + ",\"success\":\"true\",\"getDataParamInfo\":\"" + Base64.encode(getDataParamInfo.getBytes()) + "\"})";
    }

    private String getTreeJson(List<TreeNodeInfo> treeList, String needDelay, String codeField, String nameField, TreeCorp bottom, String showCode, String selectType) {
        try {
            List<String> retList = new ArrayList<String>();
            for (int i = 0; i < treeList.size(); i++) {//

                TreeNodeInfo node = (TreeNodeInfo) treeList.get(i);
                Element ele = node.getEleInfo();
                Boolean leaf = false;
                if ("true".equals(needDelay)) {
                    String leafStr = ele.elementText("isleaf");
                    leaf = "1".equals(leafStr);
                } else {
                    leaf = node.getSubNodeList().size() <= 0;
                }
                // XDY 根据recordNodeParam来判断数据的格式。如果为空 ，则说明是代码中心的数据，xml深度为3
                // XDY 如果不为空，则说明是自定义查询/通用查询的数据，xml深度为4
                // if ("".equals(recordNodeParam)) {
                StringBuilder sb = new StringBuilder(getJsonByinXml(ele, codeField, needDelay, nameField, bottom, showCode, leaf, selectType));
                if (sb.length() > 0) {
                    if (node.getSubNodeList() != null && node.getSubNodeList().size() > 0) {
                        String temp = getTreeJson(node.getSubNodeList(), needDelay, codeField, nameField, bottom, showCode, selectType);
                        sb.deleteCharAt(sb.length() - 1);
                        if (!StringUtils.nullOrBlank(temp)) {
                            sb.append(",'children':").append(temp);
                        } else {
                            sb.append(",'leaf':true");
                        }
                        sb.append("}");
                    }
                    retList.add(sb.toString());
                }
            }
            return retList.toString();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException();
        }

    }

    /**
     * 根据xml获取json数据格式，EXT建树的时候用到
     *
     * @param ele
     * @param codeField
     * @param needDelay
     * @param nameField
     * @param bottom
     * @param showCode
     * @param leaf
     * @param SelectType
     * @return
     * @throws DocumentException
     */
    private String getJsonByinXml(Element ele, String codeField, String needDelay, String nameField, TreeCorp bottom, String showCode, Boolean leaf, String SelectType) throws DocumentException {
        String corpCode = "";
        String corpName = "";
        String corpId = "";
        if (bottom != null) {
            corpCode = bottom.getCorpCode();// 集团代码
            corpName = bottom.getCorpName();// 集团名称
            corpId = bottom.getCorpId();// 集团ID
        }

        String retValue = "";
        String codeValue = ele.elementText(codeField.toLowerCase());
        if (StringUtils.nullOrBlank(nameField)) {
            nameField = "name";
        }
        String thisCorpId = "";
        for (int j = 0; j < ele.elements().size(); j++) {
            String fieldName = ((Element) ele.elements().get(j)).getName();
            String value = ((Element) ele.elements().get(j)).getText().replaceAll("\n", "").replace("'", "\\'");
            if (value == null)
                value = "";
            if (fieldName.equals("id"))
                continue;
            else if ("corpid".equals(fieldName)) {
                thisCorpId = value;
                if (!"".equals(value) && !"#".equals(value) && corpId.equals(value)) {
                    corpId = value;
                }
            } else if ("corpcode".equals(fieldName) && !"#".equals(value) && corpCode.equals(value)) {
                if (!"".equals(value))
                    corpCode = value;
            } else if ("corpname".equals(fieldName) && !"".equals(value) && corpName.equals(value)) {
                if (!"".equals(value))
                    corpName = value;
            } else {
                if (fieldName.equalsIgnoreCase(nameField)) {
                    if ("r".equals(showCode))
                        retValue += ",'text':'" + value + "[" + codeValue + "]'";
                    else if ("l".equals(showCode))
                        retValue += ",'text':'" + "[" + codeValue + "]" + value + "'";
                    else {
                        retValue += ",'text':'" + value + "'";
                    }
                }
                retValue += ",'" + fieldName + "':'" + value + "'";
            }

        }
        if ("1".equals(SelectType))
            retValue += ",'checked':" + false;
        retValue += ",'leaf':" + leaf;
        if ("true".equals(needDelay) && !leaf)
            retValue += ",'expanded':false";
        // 当某个业务的集团id(thisCorpId)是指定的id的时候 ，那么这个业务只能加在对应的集团下面，其他集团不能含有此业务信息
        if (!"".equals(thisCorpId) && !"".equals(corpId) && !thisCorpId.equals(corpId))
            return "";
        if ("".equals(corpCode))
            retValue = "{'id':'" + codeValue + "', " + retValue.substring(1) + "}";
        else {
            if (bottom == null)// 条件界面的树，默认是不包含集团信息的
                retValue = "{'id':'" + codeValue + "', " + retValue.substring(1) + "},";
            else {
                String corpInfo = "'corpid':'" + corpId + "','corpcode':'" + corpCode + "','corpname':'" + corpName + "'";
                retValue = "{'id':'" + corpCode + "*" + codeValue + "'," + corpInfo + "," + retValue.substring(1) + "}";
            }
        }
        return retValue;
    }

    public String getExpandCols(String layerAlias,String colAlias,String prefix) throws  Exception{
        //select LAYER_CODE（尺码层数代码）,LAYER_NAME,SIZE_COLNUM（列号）,name（列名）
        //先按照列排序再按照层排序  //因为ext 这里是 以对象 表是列的 ，column{columns:[]}
        Hashtable<String, String> mm = new Hashtable<String, String>();
        mm.put("service", "codecenter.simplequery");
        mm.put("querysql", "select LAYER_CODE,LAYER_NAME,SIZE_COLNUM,name from ["+colAlias+"] where LAYER_CODE in (select code from ["+layerAlias+"] )  order by  SIZE_COLNUM,LAYER_CODE");
        Map result = ServiceInvokerUtil.invoker(mm);
        String errorMessage = (String) result.get("errorMessage");
        if (errorMessage == null || errorMessage.equals("")) {
            String xml = (String) result.get("XML");
            Document doc = DocumentHelper.parseText(xml);
            List<Element> list = doc.selectNodes("/root/querydatas/querydata");
            if(list.isEmpty()){
                return "" ;
            }
            List<Map<String,Object>> cols=new ArrayList<Map<String, Object>>();
            String preColName=null;
            Map<String,Object> preCol=null;
            int maxLayer=0 ;
            int temp=0;
            List<String> fieldNams=new ArrayList<String>() ;
            for(Element ele:list ){
                String name=ele.elementText("name");
                String size_colnum=ele.elementText("size_colnum") ;
                Map<String,Object> col=new HashMap<String, Object>() ;
                col.put("text",name) ;
                if(size_colnum.equals(preColName)){
                    preCol.put("columns",new Object[]{col});
                    temp++ ;
                    if(temp>maxLayer){
                        maxLayer=temp ;
                    }
                }else{
                    temp=1 ;
                    cols.add(col) ;
                    fieldNams.add(size_colnum);
                }
                preCol=col ;
                preColName=size_colnum;
            }
            initCol(cols,prefix.toLowerCase(),fieldNams,maxLayer);
           return new GsonBuilder().create().toJson(cols);
        } else {
            throw new ServiceInvokerException(CustomQueryInvoker.class, "错误信息" + errorMessage, errorMessage);
        }
    }


    private void initCol(List<Map<String, Object>> cols, String prefix, List<String> fieldNams, int maxLayer) {
        for(Map<String, Object> col:cols){
            Object[] result=getLeverandBottomCol(col) ;
            int lever=(Integer)result[0] ;
            Map<String, Object> bottom= (Map<String, Object>) result[1];
            if(lever<maxLayer){
                for(;lever<maxLayer;lever++){
                    Map<String, Object> newCol=new HashMap<String, Object>() ;
                    newCol.put("text","&nbsp;") ;
                    bottom.put("columns",new Object[]{newCol}) ;
                    bottom=newCol ;
                }
            }
            bottom.put("dataIndex",prefix+"_"+fieldNams.remove(0)) ;
        }
    }

    private Object[] getLeverandBottomCol(Map<String, Object> col) {
        Object[] result=new Object[2];
        int lever=1 ;
        Map<String, Object> pre=col;
        while(pre.get("columns")!=null){
            Object[] childCols= (Object[]) pre.get("columns");
            pre= (Map<String, Object>) childCols[0];
            lever++;
        }
        result[0]=lever;
        result[1]=pre;
        return result;
    }

    /**
     * 从代码中心获取建树的数据(包括条件界面和结果界面的树)
     *
     * @param accessId
     * @param tableName
     * @param curNode
     * @param otherCondi
     * @param needDelay
     * @param outFields
     * @param hasParent
     * @return
     * @throws ServiceInvokerException
     * @throws DocumentException
     */
    public String getCodeCenterTreeData(String accessId, String tableName, String curNode, String otherCondi, String needDelay, String outFields, String hasParent ,String orderFields) throws ServiceInvokerException, DocumentException {
        Hashtable<String, String> mm = new Hashtable<String, String>();
        mm.put("service", "codecenter.data.getlistbyalias");
        mm.put("alias", tableName);
        mm.put("returnfields", outFields);
        if (hasParent.equals("true") || hasParent.equals("false")) {
            if (!hasParent.equals("true"))
                hasParent = "F";
            else
                hasParent = "T";
        }
        if (hasParent.equals(""))
            mm.put("hasparent", "T");
        else
            mm.put("hasparent", hasParent);

        if("T".equals(mm.get("hasparent"))){
            if(StringUtils.nullOrBlank(otherCondi)){
                otherCondi="STATUS='1' " ;
            }else if(otherCondi.toUpperCase().replaceAll(" ","").indexOf("STATUS='1'")==-1){
                otherCondi+=" and STATUS='1' ";
            }
        }

        String xmlCondi = "";
        xmlCondi = "<root><codedata>";
        if ("true".equals(needDelay))
            xmlCondi += "<parent_code queryattr=\"=\">" + curNode + "</parent_code>";
        if (!"".equals(otherCondi)) {
            xmlCondi += "<othercondition><![CDATA[" + otherCondi + "]]></othercondition>";
        }
        if(!StringUtils.nullOrBlank(orderFields)){
            xmlCondi += "<sorttype><![CDATA[" + orderFields + "]]></sorttype>";
        }
        xmlCondi += "</codedata></root>";
        if ("<root><codedata></codedata></root>".equals(xmlCondi))
            xmlCondi = "";
        xmlCondi = xmlCondi.replaceAll("\n", "").replace("\r", "");
        mm.put("XML", xmlCondi);
        mm.put("accessID", accessId);
        if ("true".equals(needDelay))
            mm.put("hasparent", "F");
        // 打印服务以及参数
        String str = "";
        for (Iterator<String> it = mm.keySet().iterator(); it.hasNext(); ) {
            String fieldName = it.next().toString();
            String value = mm.get(fieldName).toString();
            str += fieldName + ":=" + value + "\n";
        }
        Map result = ServiceInvokerUtil.invoker(mm);
        String errorMessage = (String) result.get("errorMessage");
        if (errorMessage == null || errorMessage.equals("")) {
            String xml = (String) result.get("XML");
            return xml;
            // return getReturnDataByXml(xml, needDelay, "", "", "",
            // "",corpLst,corpCodeLen);
        } else if (errorMessage.indexOf("没有找到指定代码表") >= 0) {
            return null;
        } else {
            throw new ServiceInvokerException(CustomQueryInvoker.class, "错误信息" + errorMessage, errorMessage);
        }
    }

    /**
     * 从其他数据源获取建树的信息 注意：1：此函数只处理条件界面上的树；
     * 2：结果界面并且是其他数据的树是通过getTreeDataByOtherSource方法获得
     *
     * @param accessId
     * @param needDelay
     * @param curNode
     * @param otherCondi
     * @param sods
     * @return
     * @throws Exception
     */
    public String getOtherTreeData(String accessId, String needDelay, String curNode, String otherCondi, SelectOtherDataSource sods, String showCode, String privisql, String selectType, String CodeRule) throws Exception {
        Hashtable<String, String> mm = new Hashtable<String, String>();
        mm.put("service", sods.getServiceName());
        if ("true".equals(needDelay)) {
            if ("".equals(otherCondi))
                otherCondi = "parent_code = '" + curNode + "'";
            else
                otherCondi += " and parent_code = '" + curNode + "'";
        }
        // Add by XDY
        if ("true".equals(sods.getIsSelectSQL())) {
            String customCondiValue = sods.getCustomCondiValue();
            if (!"".equals(customCondiValue) && customCondiValue != null) {
                byte[] bytes = Base64.decode(customCondiValue);
                try {
                    customCondiValue = new String(bytes, "gb2312");
                    if (otherCondi != null) {
                        customCondiValue = putCondiIntoSelectSQL(customCondiValue, otherCondi.toUpperCase());
                        if (!"".equals(privisql))
                            customCondiValue = putCondiIntoSelectSQL(customCondiValue, privisql.toUpperCase());
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            if (sods.getServiceName().toUpperCase().equals("HRMANAGER.SIMPLEQUERY")) {
                int fromIndex = customCondiValue.indexOf("FROM");
                String tmpStr = customCondiValue.substring(fromIndex + 4).trim();
                int spaceIndex = tmpStr.indexOf(" ");
                // modify by xiong 没有orderby 时 spaceIndex=-1
                String tableName = (spaceIndex == -1) ? tmpStr.trim().toUpperCase() : tmpStr.substring(0, spaceIndex).trim().toUpperCase();
                if (tableName.equals("DEPARTMENT")) {
                    String validateAuthData = privilegeinfo_validate_readUserObjectAuth("600101", accessId);
                    String retSql = getConditionSql("deptcode", validateAuthData);
                    if (retSql == null || retSql.equals(""))
                        retSql = " 0=0 ";
                    customCondiValue = putCondiIntoSelectSQL(customCondiValue, retSql.toUpperCase());
                }
            }
            mm.put(sods.getCondiParamName(), customCondiValue);
        } else if ("false".equals(sods.getIsSelectSQL())) {
            if (sods.getCondiParamName() != null)
                mm.put(sods.getCondiParamName(), otherCondi);
        }

        mm.put(sods.getIdParamName(), accessId);
        // 打印服务以及参数
        String str = "";
        for (Iterator<String> it = mm.keySet().iterator(); it.hasNext(); ) {
            String fieldName = it.next().toString();
            String value = mm.get(fieldName).toString();
            str += fieldName + ":=" + value + "\n";
        }

        Map result = ServiceInvokerUtil.invoker(mm);
        String errorMessage = (String) result.get("errorMessage");
        if (errorMessage == null || errorMessage.equals("")) {
            String xml = (String) result.get(sods.getReturnParamName());
            return getReturnDataByXml(xml, needDelay, sods.getCodeField(), sods.getNameField(), sods.getRecordsNodeParam(), sods.getRecordNodeParam(), showCode, selectType, "", CodeRule,null);
        } else {
            throw new ServiceInvokerException(CustomQueryInvoker.class, "错误信息" + errorMessage, errorMessage);
        }
    }

    /**
     * @param fieldName
     * @param authDataXML
     * @return
     * @throws Exception
     */
    private static String getConditionSql(String fieldName, String authDataXML) throws Exception {
        Document doc = DocumentHelper.parseText(authDataXML);
        Element root = doc.getRootElement();
        if (root == null)
            throw new IllegalArgumentException("取得的对象权限为空！");
        List list = root.elements("authid");
        if (list.isEmpty() || list == null || list.size() <= 0)
            return "0=1";
        String conditionSql = "";
        for (Iterator iter = list.iterator(); iter.hasNext(); ) {
            Element authE = (Element) iter.next();
            String iType = authE.attributeValue("itype");
            String authID = authE.getText();
            if (authID.equals("") || authID.equals("#")) {
                if (authID.equals("#") && iType.equals("1")) {
                    conditionSql = "(1=1)";
                    break;
                }
            } else {
                if (iType.equals("1"))
                    conditionSql = conditionSql + " or " + fieldName + " like '" + authID + "%'";
                else
                    conditionSql = conditionSql + " or " + fieldName + "='" + authID + "'";
            }
        }
        if (!conditionSql.equals("") && conditionSql.startsWith(" or "))
            conditionSql = conditionSql.substring(4, conditionSql.length());
        if (!conditionSql.equals(""))
            conditionSql = "(" + conditionSql + " or YFNULL(" + fieldName + "))";
        else
            conditionSql = "(0=1)";
        return conditionSql;
    }

    /**
     * 从其他数据源获取建树的信息 注意：1：此函数只处理结果界面上的树； 2：条件界面并且是其他数据的树是通过getOtherTreeData方法获得
     *
     * @param in
     * @param returnParamName
     * @return
     * @throws ServiceInvokerException
     * @throws DocumentException
     */
    public String getTreeDataByOtherSource(Hashtable<String, String> in, String returnParamName) throws ServiceInvokerException, DocumentException {
        Map result = ServiceInvokerUtil.invoker(in);
        String errorMessage = (String) result.get("errorMessage");
        String retValue = "";
        if (errorMessage == null || errorMessage.equals("")) {
            String xml = (String) result.get(returnParamName);
            return xml;
            // return getReturnDataByXml(xml, needDelay, codeField, nameField,
            // recordsNodeParam,
            // recordNodeParam,corpLst,corpCodeLen);
        } else {
            throw new ServiceInvokerException(CustomQueryInvoker.class, "错误信息" + errorMessage, errorMessage);
        }
    }

    /**
     * 获取查询条件界面的表格数据，数据源为代码中心
     *
     * @param callback
     * @param accessId
     * @param tableName
     * @param page
     * @param pageSize
     * @param treenode
     * @param RelationFieldName
     * @param filterCondi
     * @param orderfields
     * @param orderbyflag
     * @param hasparent
     * @return
     * @throws ServiceInvokerException
     * @throws DocumentException
     */
    public String getCodeCenterGridData(String callback, String accessId, String tableName, String page, String pageSize, String treenode, String RelationFieldName, String filterCondi, String otherCondi, String orderfields, String orderbyflag, String hasparent) throws Exception {
        Hashtable<String, String> mm = new Hashtable<String, String>();
        Element root = DocumentHelper.createElement("root");
        Element codedata = root.addElement("codedata");
        if (!StringUtils.nullOrBlank(filterCondi)) {
            String[] params = filterCondi.split(";");
            for (int i = 0; i < params.length; i++) {
                String[] temp = params[i].split(":");
                String fieldName = temp[0];
                String opre = temp[1];// 代码中心暂时没有区分左匹配和全匹配，所以此变量暂时不用
                String fieldValue = temp[2];
                Element field = codedata.addElement(fieldName);
                field.setText(fieldValue);
                field.addAttribute("queryattr", opre);
            }
        }
        if (!StringUtils.nullOrBlank(otherCondi)) {
            codedata.addElement("othercondition").setText(otherCondi);// 一串有效的sql条件语句
        }
        // modify by XDY 2009.12.19 左树右表的选择界面，当选择全部节点(节点值为##)的时候，不作为过滤右表的条件
        if (!StringUtils.nullOrBlank(treenode) && !"##".equals(treenode)) {
            Element ele = null;
            if (!StringUtils.nullOrBlank(RelationFieldName)) {
                ele = codedata.addElement(RelationFieldName);

            } else {
                ele = codedata.addElement("sort_code");

            }
            ele.addAttribute("queryattr", "L");
            ele.setText(treenode);
        }
        if (!StringUtils.nullOrBlank(orderfields)) {
            String[] fields = orderfields.split(",");
            if (fields.length > 0) {
                if(orderfields.toLowerCase().indexOf("desc")!=-1){
                    orderbyflag="0" ;
                }
                orderbyflag = orderbyflag.equalsIgnoreCase("DESC") ? "1" : "0";
                StringBuilder temp = new StringBuilder();
                for (String f : fields) {
                    if (!StringUtils.nullOrBlank(f)) {
                        temp.append(orderbyflag).append(":").append(f).append("|");
                    }
                }
                if (temp.length() > 0) {
                    temp.deleteCharAt(temp.length() - 1);
                }
                codedata.addElement("sorttype").setText(temp.toString());
            }
        }
        String xmlCondi = "";
        if (codedata.hasContent()) {
            xmlCondi = root.asXML();
        }
        mm.put("service", "codecenter.data.getlistbyalias");
        mm.put("alias", tableName);
        mm.put("page", page);
        mm.put("pageSize", pageSize);
        mm.put("XML", xmlCondi);
        mm.put("hasparent", hasparent);
        mm.put("accessID", accessId);
        Hashtable<String, String> result = new Hashtable<String, String>();
        String errorMessage = "";
        result = ServiceInvokerUtil.invoker(mm);
        errorMessage = (String) result.get("errorMessage");
        if (errorMessage == null || errorMessage.equals("")) {
            String returnParamStr = "XML";
            String RecordsParamStr = "codedata";

            String xml = (String) result.get(returnParamStr);
            Document doc = DocumentHelper.parseText(xml);
            root = doc.getRootElement();
            List<Element> records = root.elements(RecordsParamStr);
            String total = root.elementText("total");
            List<String> retList = new ArrayList<String>();
            for (Element record : records) {
                // add by XDY 为每行的数据都加多一列，用作主键
                String uuid = UUID.randomUUID().toString().replaceAll("-", "");
                StringBuffer retStr = new StringBuffer(100);
                retStr.append("{\"customqueryuuid\":\"").append(uuid).append("\",");
                List<Element> fields = record.elements();
                for (Element field : fields) {
                    String fieldName = field.getName();
                    String fieldValue = field.getText();
                    if (fieldValue.indexOf("\\") >= 0)
                        fieldValue = fieldValue.replaceAll("\\\\", "\\\\\\\\");
                    retStr.append("\"").append(fieldName).append("\":\"").append(fieldValue).append("\",");
                }
                retStr.deleteCharAt(retStr.length() - 1).append("}");
                retList.add(retStr.toString());
            }
            return callback + "({\"totalCount\":\"" + total + "\",\"records\":" + retList.toString() + "})";
        } else {
            throw new ServiceInvokerException(CustomQueryInvoker.class, "错误信息" + errorMessage, errorMessage);
        }
    }

    /**
     * 获取查询条件界面的表格数据，数据源为其他数据源
     *
     * @param callback
     * @param accessId
     * @param tableName
     * @param page
     * @param pageSize
     * @param treenode
     * @param RelationFieldName
     * @param sods
     * @param filterCondi
     * @param orderfields
     * @param orderbyflag
     * @param privisql
     */
    public String getOtherSouceGridData(String callback, String accessId, String tableName, String page, String pageSize, String treenode, String RelationFieldName, SelectOtherDataSource sods, String filterCondi, String otherCondi, String orderfields, String orderbyflag, String privisql)
            throws Exception {
        Hashtable<String, String> mm = new Hashtable<String, String>();
        String condi = "(1=1)";
        if (!"".equals(filterCondi) && filterCondi != null) {
            String[] params = filterCondi.split(";");
            for (int i = 0; i < params.length; i++) {
                String[] temp = params[i].split(":");
                String fieldName = temp[0];
                String opre = temp[1];
                String fieldValue = temp[2];
                if ("L".equals(opre))
                    condi += " and " + fieldName + " like '" + fieldValue + "%'";
                else if ("A".equals(opre))
                    condi += " and " + fieldName + " like '%" + fieldValue + "%'";
            }
        }
        // modify by XDY 2009.12.19 左树右表的选择界面，当选择全部节点(节点值为##)的时候，不作为过滤右表的条件
        // if (treenode != null && !"".equals(treenode)) {
        if (!StringUtils.nullOrBlank(treenode) && !"##".equals(treenode)) {
            if (!RelationFieldName.equals(""))
                condi += " and " + RelationFieldName + " like '" + treenode + "%'";
        }
        if (!StringUtils.nullOrBlank(otherCondi) && otherCondi.trim().length() > 0) {
            condi += " and  (" + otherCondi + ")";
        }

        mm.put("service", sods.getServiceName());
        mm.put("page", page);
        mm.put("pageSize", pageSize);
        mm.put(sods.getIdParamName(), accessId);
        if (!StringUtils.nullOrBlank(orderfields.trim())) {
            orderfields = " ORDER BY " + orderfields + " " + orderbyflag;
        }
        // Add by XDY
        if ("true".equals(sods.getIsSelectSQL())) {
            String customCondiValue = sods.getCustomCondiValue();
            if (!"".equals(customCondiValue) && customCondiValue != null) {
                byte[] bytes = Base64.decode(customCondiValue);
                customCondiValue = new String(bytes, "gb2312");
                customCondiValue = putCondiIntoSelectSQL(customCondiValue, condi.toUpperCase());
                if (!StringUtils.nullOrBlank(privisql)) {
                    customCondiValue = putCondiIntoSelectSQL(customCondiValue, privisql.toUpperCase());
                }

                if (sods.getServiceName().toUpperCase().equals("HRMANAGER.SIMPLEQUERY")) {
                    int fromIndex = customCondiValue.indexOf("FROM");
                    String tmpStr = customCondiValue.substring(fromIndex + 4).trim();
                    int spaceIndex = tmpStr.indexOf(" ");
                    // modify by xiong 没有orderby 时 spaceIndex=-1
                    if (tableName.equals("DEPARTMENT")) {
                        String validateAuthData = privilegeinfo_validate_readUserObjectAuth("600101", accessId);
                        String retSql = getConditionSql("deptcode", validateAuthData);
                        if (retSql == null || retSql.equals(""))
                            retSql = " 0=0 ";
                        customCondiValue = putCondiIntoSelectSQL(customCondiValue, retSql.toUpperCase());
                    }
                }
            }
            mm.put(sods.getCondiParamName(), customCondiValue + orderfields);
        } else if ("false".equals(sods.getIsSelectSQL())) {
            String sql = "select * from " + tableName;
            sql = putCondiIntoSelectSQL(sql.toUpperCase(), condi.toUpperCase()) + orderfields;
            mm.put(sods.getCondiParamName(), sql);
        }

        Map result = ServiceInvokerUtil.invoker(mm);
        String errorMessage = (String) result.get("errorMessage");
        if (errorMessage == null || errorMessage.equals("")) {

            String returnParamStr = sods.getReturnParamName();
            String RecordsParamStr = sods.getRecordsNodeParam();
            String RecordParamStr = sods.getRecordNodeParam();

            String xml = (String) result.get(returnParamStr);
            Document doc = DocumentHelper.parseText(xml);
            Element root = doc.getRootElement();
            List<Element> records = root.selectNodes(RecordsParamStr + "/" + RecordParamStr);
            String total = root.elementText("total");
            List<String> retList = new ArrayList<String>();
            for (Element record : records) {
                // add by XDY 为每行的数据都加多一列，用作主键
                String uuid = UUID.randomUUID().toString().replaceAll("-", "");
                StringBuffer retStr = new StringBuffer(100);
                retStr.append("{\"customqueryuuid\":\"").append(uuid).append("\",");
                List<Element> fields = record.elements();
                for (Element field : fields) {
                    String fieldName = field.getName();
                    String fieldValue = field.getText().replaceAll("\r", "").replaceAll("\n", "");
                    if (fieldValue.indexOf("\\") >= 0)
                        fieldValue = fieldValue.replaceAll("\\\\", "\\\\\\\\");
                    retStr.append("\"").append(fieldName).append("\":\"").append(fieldValue).append("\",");
                }
                retStr.deleteCharAt(retStr.length() - 1).append("}");
                retList.add(retStr.toString());
            }
            return callback + "({\"totalCount\":\"" + total + "\",\"records\":" + retList.toString() + "})";
        } else {
            throw new ServiceInvokerException(CustomQueryInvoker.class, "错误信息" + errorMessage, errorMessage);
        }
    }


    public String getModuleInfoByID(String moduleId) throws ServiceInvokerException, DocumentException {
        Hashtable<String, String> mm = new Hashtable<String, String>();
        mm.put("service", "module.getModuleInfo");
        mm.put("moduleID", moduleId);
        Hashtable result = null;
        try {
            result = ServiceInvokerUtil.invoker(mm);
            if (result == null || result.isEmpty() || result.size() == 0)
                throw new ServiceInvokerException(CustomQueryInvoker.class, "获得组件信息失败", "获得服务返回结果为空！！！");
            String errorMsg = (String) result.get("errorMessage");
            if (errorMsg != null && !"".equals(errorMsg))
                throw new ServiceInvokerException(CustomQueryInvoker.class, "获得组件信息失败", errorMsg);
        } catch (ServiceInvokerException e1) {
            return "";
        }
        // 对结果进行解析
        String xml = (String) result.get("moduleData");
        try {
            Document doc = DocumentHelper.parseText(xml);
            Element root = doc.getRootElement();

            List list = root.elements("module");
            // 进行解析
            if (list == null || list.isEmpty() || list.size() == 0)
                return null;
            Element e = (Element) list.get(0);
            return (e.element("classname") == null ? "" : e.elementText("classname"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    @SuppressWarnings("unchecked")
    public void getNodeModule(Element element, ArrayList<String> moduleIDList) {
        // 改为由xpath选取节点,而不使用递归. modify by FJ,2011.10.25

        // 按钮或者超链接挂的事件
        List<Element> modulesList = element.selectNodes("//Modules|//LinkEvent");
        for (Element module : modulesList) {
            if (!module.getTextTrim().equals("") && module.getTextTrim().startsWith("<event>")) {
                moduleIDList.add(module.getText());
            }
        }

        // 下拉按钮挂的事件
        List<Element> menusList = element.selectNodes("//menus");
        for (Element menu : menusList) {
            if (!menu.getTextTrim().equals("")) {
                String xmlData = menu.getText();
                Document doc;
                try {
                    doc = DocumentHelper.parseText(xmlData);
                    List<Element> eventList = doc.selectNodes("/root/item/moduleinfo/event");
                    for (Element event : eventList) {
                        moduleIDList.add(event.asXML());
                    }
                } catch (DocumentException e) {
                    e.printStackTrace();
                }
            }
        }

        // 旧的递归算法
        // for (int i = 0; i < element.elements().size(); i++) {
        // Element el = (Element) element.elements().get(i);
        // if (el.getName().equals("Modules")) {
        // if (!el.getText().toString().equals(""))
        // moduleIDList.add(el.getText().toString());
        // } else if (el.getName().equals("menus")) {// add by XDY 下拉按钮挂的事件
        // if (!el.getText().toString().equals("")) {
        // String xmlData = el.getText().toString();
        // Document doc;
        // try {
        // doc = DocumentHelper.parseText(xmlData);
        // List list = doc.selectNodes("/root/item/moduleinfo/event");
        // for (int index = 0; index < list.size(); index++) {
        // Element eventEle = (Element) list.get(index);
        // moduleIDList.add(eventEle.asXML());
        // }
        // } catch (DocumentException e) {
        // e.printStackTrace();
        // }
        // }
        // } else if (el.elements().size() > 0) {
        // getNodeModule(el, moduleIDList);
        // }
        // }
    }

    public void getAdditionJsFileByXmlData(String xmlData, ArrayList<String> moduleIDList) throws DocumentException {
        if (xmlData.equals(""))
            return;
        Document doc = DocumentHelper.parseText(xmlData);
        Element root = doc.getRootElement();
        getNodeModule(root, moduleIDList);

    }

    public HashMap<String, String> getAdditionJsFile(String basePath, String condiXmlData, String resultXmlData) throws DocumentException, ServiceInvokerException {
        HashMap<String, String> map = new HashMap<String, String>();
        String rfidOcx = "";
        String retModuleJs = "";
        ArrayList<String> moduleIDList = new ArrayList<String>();
        getAdditionJsFileByXmlData(condiXmlData, moduleIDList);
        getAdditionJsFileByXmlData(resultXmlData, moduleIDList);
        List<String> ids=new ArrayList<String>();
        for (int i = 0; i < moduleIDList.size(); i++) {
            String event = moduleIDList.get(i).toString();
            System.out.println(event);
            Document doc = DocumentHelper.parseText(event);
            Element root = doc.getRootElement();
            String moduleId = root.elementText("moduleid");
            if (!StringUtils.nullOrBlank(moduleId)&&!moduleId.equals("00005001") && !moduleId.equals("00005002") && !moduleId.equals("06010002") && !moduleId.equals("00003113") && moduleId.indexOf("cq") == -1) {
                if ("00009024".equals(moduleId)){
                    // 老魏专用,RFID专用
                    rfidOcx = "<OBJECT id=\"rfid_ocx\" classid=\"clsid:1814628D-13BB-4794-A986-78212924E5F0\" codebase=" + basePath + "/yfrfidtoolProj1.ocx#version=1,0,0,0\"></OBJECT>";
                }else{
                    if(!ids.contains(moduleId)){
                        String className = getModuleInfoByID(moduleId);
                        if (!"".equals(className) && className != null) {
                            int inx = className.indexOf(":");
                            String path = className.substring(0, inx);
                            retModuleJs += "<script type=\"text/javascript\" src=\"" +basePath+"/"+path + "\"></script>\n";
                        }
                        ids.add(moduleId);
                    }
                }

            }
        }
        map.put("additionJsFile", retModuleJs);
        map.put("rfidOcx", rfidOcx);
        return map;
    }

    /**
     * 把条件插入到select语句中
     *
     * @param insql
     * @param condi
     * @return
     * @throws ServiceInvokerException
     */
    public String putCondiIntoSelectSQL(String insql, String condi) throws ServiceInvokerException {
        insql = insql.toUpperCase();
        // 把SQL语句的多个空格换成一个空格，这样容易组装SQL语句。
        insql = insql.replaceAll("  ", " ");
        if ("".equals(condi) || condi == null)
            return insql;

        if (!condi.equals("") && condi != null) {
            if (insql.indexOf("WHERE") == -1) {// 没有where条件
                if (insql.indexOf("GROUP BY") != -1) {
                    insql = insql.substring(0, insql.indexOf("GROUP BY")) + " WHERE " + condi + insql.substring(insql.indexOf("GROUP BY"));
                } else if (insql.indexOf("HAVING") != -1) {
                    insql = insql.substring(0, insql.indexOf("HAVING")) + " WHERE " + condi + insql.substring(insql.indexOf("HAVING"));
                } else if (insql.indexOf("ORDER BY") != -1) {
                    insql = insql.substring(0, insql.indexOf("ORDER BY")) + " WHERE " + condi + insql.substring(insql.indexOf("ORDER BY"));
                } else {
                    insql = insql + " WHERE " + condi;
                }
            } else {
                if (insql.substring(insql.indexOf("SELECT") + 5).indexOf("SELECT") != -1) {// 判断子查询
                    int selectPos = insql.substring(insql.indexOf("SELECT") + 5).indexOf("SELECT");
                    int wherePos = insql.indexOf("WHERE");
                    if (wherePos > selectPos) {
                        if (insql.indexOf("GROUP BY") != -1) {
                            insql = insql.substring(0, insql.indexOf("GROUP BY")) + " WHERE " + condi + insql.substring(insql.indexOf("GROUP BY"));
                        } else if (insql.indexOf("HAVING") != -1) {
                            insql = insql.substring(0, insql.indexOf("HAVING")) + " WHERE " + condi + insql.substring(insql.indexOf("HAVING"));
                        } else if (insql.indexOf("ORDER BY") != -1) {
                            insql = insql.substring(0, insql.indexOf("ORDER BY")) + " WHERE " + condi + insql.substring(insql.indexOf("ORDER BY"));
                        } else {
                            insql = insql + " WHERE " + condi;
                        }
                    } else {
                        insql = insql.substring(0, insql.indexOf("WHERE") + 5) + " " + condi + " and " + insql.substring(insql.indexOf("WHERE") + 5);
                    }
                } else
                    insql = insql.substring(0, insql.indexOf("WHERE") + 5) + " " + condi + " and " + insql.substring(insql.indexOf("WHERE") + 5);
            }
        }

        return insql;
    }

    /**
     * 获取代码中心集团信息,集团代码和集团名称形成MAP返回
     *
     * @param buildCorpTreeCondition
     * @param needDelay
     * @param curNode
     * @param accessID
     * @param sb
     * @return
     * @throws Exception
     */
    public List getCorpDataFromCodeCenter(String buildCorpTreeCondition, String needDelay, String curNode, String accessID, StringBuffer sb) throws Exception {
        List returnLst = new ArrayList();

        String xmlCondi = "<root><codedata>";
        if (needDelay.equals("true"))
            xmlCondi += "<parent_code queryattr=\"=\">" + curNode + "</parent_code>";

        String othercondition = "parent_code <> '##'";// add by XDY
        // 韦工要求，新加的一个必备条件
        if (!buildCorpTreeCondition.equals("undefined") && !"".equals(buildCorpTreeCondition))
            othercondition += " and " + buildCorpTreeCondition;

        xmlCondi += "<othercondition><![CDATA[" + buildCorpTreeCondition + "]]></othercondition>";
        xmlCondi += "</codedata></root>";

        Hashtable<String, String> in = new Hashtable<String, String>();

        in.put("service", "codecenter.data.getlistbyalias");
        in.put("alias", "SYSGROUP");
        in.put("page", "");
        in.put("pageSize", "");
        in.put("XML", xmlCondi);
        if (accessID != null && !"".equals(accessID))
            in.put("accessID", accessID);
        Map result = ServiceInvokerUtil.invoker(in);
        String errorMessage = (String) result.get("errorMessage");
        if (errorMessage == null || errorMessage.equals("")) {
            String xml = (String) result.get("XML");
            Document doc = DocumentHelper.parseText(xml);
            List<Element> lst = doc.selectNodes("/root/codedata");
            for (Iterator<Element> it = lst.iterator(); it.hasNext(); ) {
                Element ele = it.next();
                String cCode = (String) ele.elementTextTrim("corpcode");// 集团代码
                // modify by XDY 应韦工要求，集团名称优先考虑集团简称
                String cName = "";
                cName = (String) ele.elementTextTrim("corp_assistcode");// 集团简称
                if ("".equals(cName))
                    cName = (String) ele.elementTextTrim("corpname");// 集团名称

                String cId = (String) ele.elementTextTrim("id");// 集团ID;

                returnLst.add(cCode + "*" + cName + "*" + cId + "*" + "0");
                // 获取集团代码的最长度
                int codeLen = cCode.length();
                String tempLen = sb.toString();
                if ("".equals(tempLen) || tempLen == null)
                    tempLen = "0";
                if (Integer.parseInt(tempLen) <= codeLen) {
                    sb.setLength(0);
                    sb.append(codeLen);
                }
            }
        } else {
            throw new ServiceInvokerException(CustomQueryInvoker.class, "错误信息" + errorMessage, errorMessage);
        }
        return returnLst;
    }

    /**
     * to get the priviage condition use XML format
     *
     * @param
     * @param accessID
     * @return: returntype: String
     */
    public String getCorpPriviSQL(String accessID) throws Exception {
        if (accessID == null)
            return null;
        Document doc = getPriviDoc("SYSGROUP", accessID);
        if (doc == null)
            throw new Exception("调用权限服务出错");

        // conplain the condition
        String sql;
        List elements = doc.getRootElement().elements("authid");
        if (elements == null || elements.isEmpty())
            return "1=0";
        Element ee = (Element) elements.get(0);
        String type = ee.attributeValue("itype");
        String value = (String) ee.getData();
        if (type.equals("1") && value.equals("#"))
            return "1=1";

        // add by XDY 2010.11.29
        // 因为elements含有很多重复数据，若以提取出来
        HashSet priviSet = new HashSet();

        sql = "1=0";
        for (Iterator iter = elements.iterator(); iter.hasNext(); ) {
            String temp = "";
            Element element = (Element) iter.next();
            type = element.attributeValue("itype");
            value = (String) element.getData();
            if (value == null || type == null || value.equals("") || type.equals(""))
                continue;
            priviSet.add(type + "*" + value);
        }
        sql = parseCorpPriviSqlFormSet(sql, priviSet);
        sql = "(" + sql + ")";
        return sql;
    }

    private String parseCorpPriviSqlFormSet(String sql, HashSet priviSet) throws Exception {
        // 提取出来，放在priviSet，避免重复项,循环priviSet
        for (Iterator it = priviSet.iterator(); it.hasNext(); ) {
            String temp = "";
            String priviStr = (String) it.next();
            String[] priviArr = priviStr.split("\\*");
            String type = priviArr[0];
            String value = priviArr[1];
            if (type.equals("1"))
                temp = " or corpcode like '" + value + "%'";
            else if (type.equals("0"))
                temp = " or corpcode='" + value + "'";
            sql += temp;
        }
        return sql;
    }

    /**
     * @param alias
     * @param accessID
     * @throws Exception : returntype: void
     */
    public Document getPriviDoc(String alias, String accessID) throws Exception {
        String xml = privilegeinfo_validate_readUserObjectAuth(alias, accessID);
        if (xml == null || xml.trim().equals("")) {
            xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root></root>";
        }
        Document priviDoc = DocumentHelper.parseText(xml);
        return priviDoc;
    }

    /**
     * @param objCode
     * @param accessID
     * @return
     * @throws Exception
     */
    public String privilegeinfo_validate_readUserObjectAuth(String objCode, String accessID) throws ServiceInvokerException {
        Hashtable<String, String> mm = new Hashtable<String, String>();
        String service = "privilegeinfo.validate.readUserObjectAuth";
        mm.put("service", service);
        mm.put("accessID", accessID);
        mm.put("objCode", objCode);
        Map map = ServiceInvokerUtil.invoker(mm);
        String errorMessage = (String) map.get("errorMessage");
        if (errorMessage == null || errorMessage.equals("")) {
            String xml = (String) map.get("validateAuthData");
            return xml;
        } else {
            throw new ServiceInvokerException(CustomQueryInvoker.class, "错误信息" + errorMessage, errorMessage);
        }
    }

    // ADD BY ZXW 2009-10-10 调用SERVICE服务
    public static String callService(String xml) throws Exception {
        Hashtable<String, String> paramE = new Hashtable<String, String>();
        String ServiceName = "";
        // 解析参数
        Document doc = DocumentHelper.parseText(xml);
        List list = doc.getRootElement().elements();

        for (Iterator iter = list.iterator(); iter.hasNext(); ) {
            Element Item = (Element) iter.next();
            if ("XML".equals(Item.getName())) {
                String condixml = "<root><codedata><othercondition>" + Item.getData().toString() + "</othercondition></codedata></root>";
                paramE.put(Item.getName(), condixml);
            } else {
                paramE.put(Item.getName(), Item.getData().toString());
            }
        }

        Map result = ServiceInvokerUtil.invoker(paramE);
        String resultString = (String) result.get("errorMessage");
        if (resultString == null || "".equals(resultString)) {
            resultString = (String) result.get("XML");
        }
        return resultString;
    }

    public Map<String, String> getUserInfoByAccid(String accID) throws Exception {
        Map<String, String> userMap = new HashMap<String, String>();
        Hashtable<String, String> paramE = new Hashtable<String, String>();
        paramE.put("service", "useraccess.getOnlineUsers");
        paramE.put("sysAccessID", accID);
        Hashtable result = ServiceInvokerUtil.invoker(paramE);

        if (result == null || result.isEmpty() || result.size() == 0)
            throw new IllegalArgumentException("出错：调服务privilegeinfo.validate.readUserObjectAuth返回为空！");
        String errMsg = (String) result.get("errorMessage");
        if (errMsg != null && !"".equals(errMsg))
            throw new IllegalArgumentException("调服务privilegeinfo.validate.readUserObjectAuth出错：" + errMsg);

        String xml = result.get("onlineUserData").toString();
        try {
            Document doc = DocumentHelper.parseText(xml);
            Element root = doc.getRootElement();
            Element item = root.element("onlineuser");
            if (item == null)
                throw new Exception("不存在该系统存取ID对应的操作用户");

            userMap.put("userid", item.elementText("userid"));
            userMap.put("username", item.elementText("username"));
        } catch (Exception e) {
            throw new Exception();
        }
        return userMap;
    }

    public String getPersonIdByUserId(String userId) throws Exception {
        String XML = "<userInfo><user><id>" + userId + "</id></user></userInfo>";
        Hashtable<String, String> paramE = new Hashtable<String, String>();
        paramE.put("service", "usermanager.user.read");
        paramE.put("XML", XML);
        Hashtable result = ServiceInvokerUtil.invoker(paramE);
        if (result == null || result.isEmpty() || result.size() == 0)
            throw new IllegalArgumentException("出错：调服务usermanager.user.read返回为空！");
        String errMsg = (String) result.get("errorMessage");
        if (errMsg != null && !"".equals(errMsg))
            throw new IllegalArgumentException("调服务usermanager.user.read出错：" + errMsg);
        XML = (String) result.get("XML");

        Document doc = DocumentHelper.parseText(XML);
        Element root = doc.getRootElement();
        if (root == null)
            throw new IllegalArgumentException("根据操作用户ID取员工ID失败！");
        Element item = root.element("user");
        if (item == null)
            throw new IllegalArgumentException("根据操作用户ID取员工ID失败！");
        String personID = item.elementText("employeeid");
        return personID;
    }

    public Map<String, String> getDeptOrCorpMapByPersonId(String personId, String accID) throws Exception {
        Map<String, String> deptOrCorpMap = new HashMap<String, String>();
        String XML = "<root><relations><personid>" + personId + "</personid></relations></root>";
        Hashtable<String, String> paramE = new Hashtable<String, String>();
        paramE.put("service", "hrmanager.relations.getlist");
        paramE.put("XML", XML);
        Hashtable result = ServiceInvokerUtil.invoker(paramE);
        if (result == null || result.isEmpty() || result.size() == 0)
            throw new IllegalArgumentException("出错：调服务hrmanager.relations.getlist返回为空！");
        String errMsg = (String) result.get("errorMessage");
        if (errMsg != null && !"".equals(errMsg))
            throw new IllegalArgumentException("调服务hrmanager.relations.getlist出错：" + errMsg);
        XML = (String) result.get("XML");
        Document doc = DocumentHelper.parseText(XML);
        Element root = doc.getRootElement();
        if (root == null)
            throw new IllegalArgumentException("根据员工ID取部门信息失败！");
        Element item = root.element("relations");
        if (item == null)
            throw new IllegalArgumentException("根据员工ID取部门信息失败！");
        deptOrCorpMap.put("deptcode", item.elementText("deptcode"));
        deptOrCorpMap.put("deptname", item.elementText("deptname"));
        deptOrCorpMap.put("personname", item.elementText("personname"));
        deptOrCorpMap.put("rolecode", item.elementText("rolecode"));
        deptOrCorpMap.put("rolename", item.elementText("rolename"));
        deptOrCorpMap.put("deptcorpid",item.elementText("corpid")) ;
        deptOrCorpMap.put("deptcorpcode",item.elementText("corpcode")) ;
        deptOrCorpMap.put("deptcorpname",item.elementText("corpname")) ;
        String corpXml = getCodeCenterTreeData(accID, "SYSGROUP", "", "STATUS ='1' and isleaf='1' ", "0", "", "F","");

        String corpId = "";
        String corpCode = "";
        String corpName = "";
        if (corpXml != null) {
            doc = DocumentHelper.parseText(corpXml);
            List<Element> lst = doc.selectNodes("root/codedata");
            if (lst != null && lst.size() == 1) {
                Element ele = lst.get(0);
                if (ele != null) {
                    corpId = ele.elementText("id");
                    corpCode = ele.elementText("corpcode");
                    corpName = ele.elementText("corpname");
                }
            }
            deptOrCorpMap.put("corpid", corpId);
            deptOrCorpMap.put("corpcode", corpCode);
            deptOrCorpMap.put("corpname", corpName);
        }
        return deptOrCorpMap;
    }

    public Map<String, String> getParentDeptMapByDeptCode(String curDeptCode) throws Exception {
        String sql = "select DEPTCODE as UPPER_DEPTCODE,DEPTNAME as UPPER_DEPTNAME,DEPTSHORTNAME " + "from department where deptcode in (select UPPER_DEPTCODE from department where deptcode = '" + curDeptCode + "')";
        Hashtable<String, String> paramE = new Hashtable<String, String>();
        paramE.put("service", "hrmanager.simplequery");
        paramE.put("querysql", sql);
        Hashtable result = ServiceInvokerUtil.invoker(paramE);
        if (result == null || result.isEmpty() || result.size() == 0)
            throw new IllegalArgumentException("出错：调服务hrmanager.simplequery返回为空！");
        String errMsg = (String) result.get("errorMessage");
        if (errMsg != null && !"".equals(errMsg))
            throw new IllegalArgumentException("调服务hrmanager.simplequery出错：" + errMsg);
        String XML = (String) result.get("XML");
        Document doc = DocumentHelper.parseText(XML);
        List<Element> lst = doc.selectNodes("root/querydatas/querydata");
        if (lst == null || lst.isEmpty())
            return null;

        Element ele = lst.get(0);
        if (ele == null)
            return null;
        String parentCode = ele.elementTextTrim("upper_deptcode");
        String parentName = ele.elementTextTrim("upper_deptname");
        String parentShortName = ele.elementTextTrim("deptshortname");

        Map<String, String> parentDeptMap = new HashMap<String, String>();
        parentDeptMap.put("parentcode", parentCode);
        parentDeptMap.put("parentname", parentName);
        parentDeptMap.put("parentshortname", parentShortName);
        return parentDeptMap;
    }

    public Map<String, String> getDeptMapByDeptCode(String curDeptCode) throws Exception {
        String sql = "select * from department where deptcode = '" + curDeptCode + "'";
        Hashtable<String, String> paramE = new Hashtable<String, String>();
        paramE.put("service", "hrmanager.simplequery");
        paramE.put("querysql", sql);
        Hashtable result = ServiceInvokerUtil.invoker(paramE);
        if (result == null || result.isEmpty() || result.size() == 0)
            throw new IllegalArgumentException("出错：调服务hrmanager.simplequery返回为空！");
        String errMsg = (String) result.get("errorMessage");
        if (errMsg != null && !"".equals(errMsg))
            throw new IllegalArgumentException("调服务hrmanager.simplequery出错：" + errMsg);
        String XML = (String) result.get("XML");
        Document doc = DocumentHelper.parseText(XML);
        List<Element> lst = doc.selectNodes("root/querydatas/querydata");
        if (lst == null || lst.isEmpty())
            return null;

        Element ele = lst.get(0);
        if (ele == null)
            return null;
        String deptshortname = ele.elementTextTrim("deptshortname");
        String deptshortname1 = ele.elementTextTrim("deptshortname1");
        String deptshortname2 = ele.elementTextTrim("deptshortname2");
        String deptshortname3 = ele.elementTextTrim("deptshortname3");
        String deptshortname4 = ele.elementTextTrim("deptshortname4");

        Map<String, String> DeptMap = new HashMap<String, String>();
        DeptMap.put("deptshortname", deptshortname);
        DeptMap.put("deptshortname1", deptshortname1);
        DeptMap.put("deptshortname2", deptshortname2);
        DeptMap.put("deptshortname3", deptshortname3);
        DeptMap.put("deptshortname4", deptshortname4);

        return DeptMap;
    }

    public Map<String, String> getPersonInfo(String personId) throws Exception {
        String sql = "select *  from person where personid = '" + personId + "'";
        Hashtable<String, String> paramE = new Hashtable<String, String>();
        paramE.put("service", "hrmanager.simplequery");
        paramE.put("querysql", sql);
        Hashtable result = ServiceInvokerUtil.invoker(paramE);
        if (result == null || result.isEmpty() || result.size() == 0)
            throw new IllegalArgumentException("出错：调服务hrmanager.simplequery返回为空！");
        String errMsg = (String) result.get("errorMessage");
        if (errMsg != null && !"".equals(errMsg))
            throw new IllegalArgumentException("调服务hrmanager.simplequery出错：" + errMsg);
        String XML = (String) result.get("XML");
        Document doc = DocumentHelper.parseText(XML);
        List<Element> lst = doc.selectNodes("root/querydatas/querydata");
        if (lst == null || lst.isEmpty())
            return null;

        Element ele = lst.get(0);
        if (ele == null)
            return null;

        String stringfield23 = ele.element("stringfield23") == null ? "" : ele.elementTextTrim("stringfield23");
        String stringfield24 = ele.element("stringfield24") == null ? "" : ele.elementTextTrim("stringfield24");
        String stringfield25 = ele.element("stringfield25") == null ? "" : ele.elementTextTrim("stringfield25");

        Map<String, String> personInfoMap = new HashMap<String, String>();
        personInfoMap.put("stringfield23", stringfield23);
        personInfoMap.put("stringfield24", stringfield24);
        personInfoMap.put("stringfield25", stringfield25);
        return personInfoMap;
    }

    public static String getBusinessTxmData(String serviceName, String querysql) throws ServiceInvokerException {
        Hashtable<String, String> in = new Hashtable<String, String>();
        in.put("service", serviceName + ".simplequery");
        in.put("page", "-1");
        in.put("pageSize", "-1");
        in.put("querysql", querysql);
        in.put("accid", "");
        in.put("returnfields", "");
        Hashtable result = ServiceInvokerUtil.invoker(in);
        if (result.get("errorMessage") == null) {
            return result.get("XML").toString();
        }
        return null;
    }

    public static void newWaitAccount(String xml) throws ServiceInvokerException {
        Hashtable<String, String> in = new Hashtable<String, String>();
        in.put("service", "rfid.waitaccount.new");
        in.put("XML", xml);
        Hashtable result = ServiceInvokerUtil.invoker(in);
    }

    /**
     * 重建条件界面的树，当树为逐级加载，用户又需要模糊匹配树节点的时候需要此实现
     *
     * @param accessId
     * @param tableName
     * @param otherCondi
     * @param outFields
     * @param filterCondi
     * @return
     * @throws ServiceInvokerException
     * @throws DocumentException
     * @author XDY
     * @created 2010-12-29
     */
    public String reBuildCondiTreeFromCodeCenter(String accessId, String tableName, String otherCondi, String outFields, String filterCondi, String needCorp) throws ServiceInvokerException, DocumentException {
        String querysql = "";
        if ("true".equals(needCorp)) {
            querysql = "select distinct b.* from " + tableName + " a," + tableName + " b " + "where (a.corpid=b.corpid or a.corpid='#' or b.corpid='#') and ((a.code like b.code+'%') or b.code like a.code+'%' ) " + "and " + filterCondi;
        } else {
            querysql = "select distinct b.* from " + tableName + " a," + tableName + " b " + "where ((a.code like b.code+'%') or b.code like a.code+'%' ) " + "and " + filterCondi;
        }

        if (!"".equals(otherCondi)) {
            String existsSql = "(select distinct id from " + tableName + " where " + otherCondi + ")";
            querysql = querysql + " and a.id in " + existsSql;
        }
        querysql = querysql + " order by b.code";

        Hashtable<String, String> in = new Hashtable<String, String>();
        in.put("service", "codecenter.simplequery");
        in.put("querysql", querysql);
        in.put("accid", accessId);
        in.put("returnfields", outFields);

        Map result = ServiceInvokerUtil.invoker(in);
        String errorMessage = (String) result.get("errorMessage");
        if (errorMessage == null || errorMessage.equals("")) {
            String xml = (String) result.get("XML");
            return xml;
        } else {
            throw new ServiceInvokerException(CustomQueryInvoker.class, "错误信息" + errorMessage, errorMessage);
        }
    }

    public String reBuildCondiTreeFromOtherSource(SelectOtherDataSource sods, String accessId, String tableName, String otherCondi, String outFields, String filterCondi) throws ServiceInvokerException, DocumentException {
        String querysql = "select distinct b.* from " + tableName + " a," + tableName + " b " + "where (a.corpid=b.corpid or a.corpid='#' or b.corpid='#') and ((a.code like b.code+'%') or b.code like a.code+'%' ) " + "and " + filterCondi;
        if (!"".equals(otherCondi)) {
            String existsSql = "(select distinct id from " + tableName + " where " + otherCondi + ")";
            querysql = querysql + " and a.id in " + existsSql;
        }
        querysql = querysql + " order by b.code";

        Hashtable<String, String> in = new Hashtable<String, String>();
        in.put("service", sods.getServiceName());
        in.put("querysql", querysql);
        in.put("accid", accessId);
        in.put("returnfields", outFields);

        Map result = ServiceInvokerUtil.invoker(in);
        String errorMessage = (String) result.get("errorMessage");
        if (errorMessage == null || errorMessage.equals("")) {
            String xml = (String) result.get(sods.getReturnParamName());
            return xml;
        } else {
            throw new ServiceInvokerException(CustomQueryInvoker.class, "错误信息" + errorMessage, errorMessage);
        }
    }

    public HashMap getStyleSize(String styleId) throws ServiceInvokerException {
        Hashtable<String, String> in = new Hashtable<String, String>();
        in.put("service", "customquery.style2.getresultsize");
        in.put("styleid", styleId);
        Map result = ServiceInvokerUtil.invoker(in);
        String errorMessage = (String) result.get("errorMessage");
        HashMap map = new HashMap();
        map.putAll(result);
        if (errorMessage == null || errorMessage.equals("")) {
        } else {
            throw new ServiceInvokerException(CustomQueryInvoker.class, "错误信息" + errorMessage, errorMessage);
        }
        return map;

    }

    public HashMap saveParam(String name, String owner, String value) throws ServiceInvokerException {
        Hashtable<String, String> in = new Hashtable<String, String>();
        in.put("service", "parameter.saveOrUpdate");
        in.put("owner", owner);
        in.put("name", name);
        in.put("value", value);
        Map result = ServiceInvokerUtil.invoker(in);
        String errorMessage = (String) result.get("errorMessage");
        HashMap map = new HashMap();
        map.putAll(result);
        if (errorMessage == null || errorMessage.equals("")) {
        } else {
            throw new ServiceInvokerException(CustomQueryInvoker.class, "错误信息" + errorMessage, errorMessage);
        }
        return map;

    }

    public String getParam(String name, String owner) throws ServiceInvokerException {
        Hashtable<String, String> in = new Hashtable<String, String>();
        in.put("service", "parameter.getparams");
        in.put("owner", owner);
        in.put("name", name);
        Map result = ServiceInvokerUtil.invoker(in);
        String errorMessage = (String) result.get("errorMessage");
        if (errorMessage == null || errorMessage.equals("")) {
        } else {
            throw new ServiceInvokerException(CustomQueryInvoker.class, "错误信息" + errorMessage, errorMessage);
        }
        String returnxml = (String) result.get("XML");
        String showfields = "";
        try {
            Document xmldoc = DocumentHelper.parseText(returnxml);
            Element root = xmldoc.getRootElement();
            List list = root.elements("param");
            if (list != null && list.size() > 0) {
                Element paramEle = (Element) list.get(0);
                showfields = paramEle.elementText("value");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (showfields == null) {
            showfields = "";
        }
        return showfields;

    }

    public String getPrivilegeHideFieldList(String accID, String classId) throws ServiceInvokerException, DocumentException {
        String fieldList = "";
        Hashtable<String, String> in = new Hashtable<String, String>();
        in.put("service", "privilegeinfo.validate.readQuanAuth");
        in.put("accessID", accID);
        in.put("type", "Q");
        in.put("classid", classId);
        String xml = "";
        try {
            Map result = ServiceInvokerUtil.invoker(in);
            xml = (String) result.get("validateAuthData");
            if (xml.equals(""))
                return "";
            String errorMsg = (String) result.get("errorMessage");
            if (errorMsg != null && !"".equals(errorMsg))
                return "";

        } catch (Exception e) {
            return "";
        }
        Document doc = DocumentHelper.parseText(xml);
        Element root = doc.getRootElement();
        List list = root.elements("auth");
        if (list != null && list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                Element paramEle = (Element) list.get(i);
                String fieldName = paramEle.elementText("fieldname");
                fieldName = fieldName.substring(fieldName.indexOf(".") + 1, fieldName.length());
                fieldList += "," + fieldName;
            }
            fieldList = fieldList.substring(1);
        }
        return fieldList;
    }
}
