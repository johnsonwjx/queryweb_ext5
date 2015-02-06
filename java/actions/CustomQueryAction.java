package actions;

import logic.*;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import youngfriend.common.util.StringUtils;
import youngfriend.common.util.encoding.Base64;
import youngfriend.common.util.net.ServiceInvokerUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;

public class CustomQueryAction extends DispatchAction {
    private CustomQueryInvoker invoker = new CustomQueryInvoker();

    public ActionForward loadGanTT(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response) throws Exception {
        return mapping.findForward("okGanTT");
    }

    /**
     * 装载条件界面和结果界面
     *
     * @param mapping
     * @param actionForm
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ActionForward load(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession();
        String uuid = request.getParameter("uuid");
        Map<String, String> paramsMap = null;
        if (uuid != null && !"".equals(uuid)) {
            Object temp = request.getSession().getAttribute("postParmas");
            Map<String, Map<String, String>> postParmas = null;
            if (temp == null) {
                request.setAttribute("message", "穿透参数丢失，请重试");
                request.setAttribute("detailmsg", "保存参数对象不存在");
                return mapping.findForward("error");
            } else {
                postParmas = (Map<String, Map<String, String>>) temp;
            }
            if (!postParmas.containsKey(uuid)) {
                request.setAttribute("message", "穿透参数丢失，请重试");
                request.setAttribute("detailmsg", "参数对象取不到指定参数");
                return mapping.findForward("error");
            }
            if (session.getAttribute("keepParam") != null) {
                paramsMap = postParmas.get(uuid);
            } else {
                paramsMap = postParmas.remove(uuid);
            }
        } else {
            paramsMap = PubFunc.getParamsMap(request);
        }
        // Add by XDY 2009.06.29 特殊处理，针对’自动生成计划预生成‘而增加的参数。
        // 参考文件business\storemanager\plan\autobuy\script的doAutoCreatePreview函数
        String propValue = paramsMap.get("propValue");
        if (paramsMap.get("fields") != null && paramsMap.get("fields").length() > 0) {
            request.setAttribute("fields", paramsMap.get("fields"));
        }
        if (paramsMap.get("tempTableAlias") != null && paramsMap.get("tempTableAlias").length() > 0) {
            request.setAttribute("tableAlias", URLDecoder.decode(paramsMap.get("tempTableAlias"), "UTF-8"));
        }
        if (propValue == null || "".equals(propValue)) {
            throw new IllegalArgumentException("传入的参数信息为空!");
        }
        String accID = (String) session.getAttribute("sysAccessID");
        String userId = (String) session.getAttribute("userId");
        Map userInfoMap = (Map) invoker.getUserInfoByAccid(accID);
        String userIdTemp = (String) userInfoMap.get("userid");
        if (StringUtils.nullOrBlank(userId) || !userId.equals(userIdTemp)) {
            userId = userIdTemp;
            session.setAttribute("userId", userId);
            session.setAttribute("username", userInfoMap.get("username"));
            String personID = invoker.getPersonIdByUserId(userId);
            session.setAttribute("personID", personID);
            String corpId = "";
            // 获取部门，集团信息
            try {
                userInfoMap = invoker.getDeptOrCorpMapByPersonId(personID, accID);
                corpId = (String) userInfoMap.get("corpId");
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            session.setAttribute("corpId", corpId);
        }

        String conditionXml = paramsMap.get("workData");
        if (!StringUtils.nullOrBlank(conditionXml)) {
            conditionXml=conditionXml.replaceAll("'","\"");
            request.setAttribute("conditionXml", conditionXml);
        }

        String treeConditionXml = paramsMap.get("treeConditionXml");
        if (!StringUtils.nullOrBlank(treeConditionXml)) {
            treeConditionXml = URLDecoder.decode(treeConditionXml, "UTF-8");
            treeConditionXml = treeConditionXml.replaceFirst("\n", "");
            treeConditionXml = treeConditionXml.replaceFirst("\r", "");
            treeConditionXml = PubFunc.parseTreeCondi(treeConditionXml);
            if(!StringUtils.nullOrBlank(treeConditionXml)){
                request.setAttribute("treeConditionXml", treeConditionXml);
            }
        }
        // 查询样式ID
        String[] psp = propValue.split(";");
        HashMap<String, String> styleValueMap = new HashMap<String, String>();
        if (psp.length >= 2) {
            for (int i = 0; i < psp.length; i++) {
                String[] prop = psp[i].split("=");
                if (prop.length > 1) {
                    styleValueMap.put(prop[0], prop[1]);
                } else {
                    styleValueMap.put(prop[0], "");
                }
            }
        } else {
            request.setAttribute("message", "查询项目的入口参数不齐全!");
            request.setAttribute("detailmsg", "propValue分割长度小于2,查询项目的入口参数不齐全!");
            return mapping.findForward("error");
        }
        String conditionStyleID = styleValueMap.get("conditionStyleID");
        String resultStyleID = styleValueMap.get("resultStyleID");
        String styleSettings = styleValueMap.get("styleSettings") == null ? "" : styleValueMap.get("styleSettings");
        if (!styleSettings.equals("")) {
            styleSettings = new String(Base64.decode(URLDecoder.decode(styleSettings, "UTF-8")));
            String[] values = styleSettings.split(";");
            styleSettings = "" ;
            for (int i = 0; i < values.length; i++) {
                String[] str1s = values[i].split(",");
                String str = "";
                for (int j = 0; j < str1s.length; j++) {
                    String[] tmpStr = str1s[j].split("=");
                    str += ",'" + tmpStr[0] + "':";
                    if (tmpStr.length > 1){
                        if(tmpStr[1].indexOf("*")!=-1){
                            tmpStr[1]=tmpStr[1].replaceAll("\\*",",") ;
                        }
                        str += "'" + tmpStr[1] + "'";
                    }
                    else
                        str += "''";
                }
                str = str.substring(1);
                styleSettings += ",{" + str + "}";
            }
            styleSettings = styleSettings.substring(1);
            request.setAttribute("menuCondiSettings", styleSettings);
        }
        String condiXmlData = "", resultXmlData = "";
        HashMap hashMap = null;
        try {
            hashMap = invoker.load(conditionStyleID, resultStyleID);
            if (hashMap.get("condiXml") != null && !hashMap.get("condiXml").toString().equals("")) {
                condiXmlData = hashMap.get("condiXml").toString();
                condiXmlData = condiXmlData.replaceAll("	", "");
             //  request.setAttribute("conditionStyleXml", Base64.encode(condiXmlData.getBytes()));
                request.setAttribute("conditionStyleXml", condiXmlData.replaceAll("\\\\b","+").replaceAll("\"","\'"));
            }

            if (hashMap.get("resultXml") != null && !hashMap.get("resultXml").toString().equals("")) {
                resultXmlData = hashMap.get("resultXml").toString();
                resultXmlData = resultXmlData.replaceAll("	", "");
           //   request.setAttribute("resultStyleXml", Base64.encode(resultXmlData.getBytes()));
                request.setAttribute("resultStyleXml", resultXmlData.replaceAll("\\\\b","+").replaceAll("\"","\'"));
            }

            String taskType = paramsMap.get("taskType");
            String isPrint = paramsMap.get("isPrint");
            String needReBuild = paramsMap.get("needReBuild");
            if ("2".equals(taskType)) {
                request.setAttribute("title", "条件");
            } else if (hashMap.get("name") != null && !hashMap.get("name").toString().equals("")) {
                request.setAttribute("title", hashMap.get("name").toString());
            }

            if (hashMap.get("classId") != null && !hashMap.get("classId").toString().equals("")) {
                String classId = hashMap.get("classId").toString();
                String privilegeHideFieldList = invoker.getPrivilegeHideFieldList(accID, classId);
                request.setAttribute("privilegeHideFieldList", privilegeHideFieldList);
            }
            HashMap map = invoker.getAdditionJsFile(request.getContextPath(), condiXmlData, resultXmlData);
            String additionJsFile = (String) map.get("additionJsFile");
            String rfidOcx = (String) map.get("rfidOcx");
            // 取显示字段
            String param = invoker.getParam(resultStyleID, userId);
            if (param.indexOf("noshowfields=") >= 0) {
                String[] arr = param.split("noshowfields=");
                if (arr.length <= 0) {
                    request.setAttribute("showfields", "");
                    request.setAttribute("noshowfields", "");
                } else {
                    if (arr.length == 2) {
                        request.setAttribute("showfields", arr[0]);
                        request.setAttribute("noshowfields", arr[1]);
                    }
                }
            } else {
                request.setAttribute("showfields", param);
            }
            request.setAttribute("resultStyleID", resultStyleID);
            request.setAttribute("additionJsFile", additionJsFile);
            request.setAttribute("rfidOcx", rfidOcx);
            request.setAttribute("tasktype", taskType);
            request.setAttribute("isPrint", isPrint);
            request.setAttribute("needReBuild", needReBuild);
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            String detailmsg = e.getMessage() + "-->:" + e.getCause();
            if (PubFunc.isAjax(request)) {
                response.getWriter().write("errorMessage:加载查询数据错误" + detailmsg);
                return null;
            } else {
                request.setAttribute("message", "加载查询数据错误");
                request.setAttribute("detailmsg", detailmsg);
                return mapping.findForward("error");
            }
        }
        if (PubFunc.isAjax(request)) {
            response.getWriter().write(PubFunc.getMessageByRequest(request));
            return null;
        }
        return mapping.findForward("ok");
    }

    public void getBaseParam(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response) {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/javascript");
        String resultStyleID = request.getParameter("resultStyleID");
        String LinkEvent = request.getParameter("LinkEvent");
        if (StringUtils.nullOrBlank(resultStyleID)) {
            throw new RuntimeException("条件样式为空!");
        }
        try {
            HashMap<String, String> map = invoker.load("", resultStyleID);
            String resultXmlData = map.get("resultXml").toString();
            Document doc = DocumentHelper.parseText(resultXmlData);
            Element element = doc.getRootElement().element("root_panel").element("property");
            Map<String, String> resultMap = new HashMap<String, String>();
            String returns = "ServerName,TableName,isCondiLoad,tableAlias,OrderByField,ReportId,Percentage,MappedField,GroupByField,MinSumParam,MappedField,Expression,Corpprivilegectrl";
            for (Element e : (List<Element>) element.elements()) {
                if (returns.indexOf(e.getName()) < 0) {
                    continue;
                }
                PubFunc.putElementValue2Map(resultMap, e);
            }
            if (!StringUtils.nullOrBlank(LinkEvent)) {
                Element e = (Element) doc.getRootElement().selectSingleNode("//Columns/*[string-length(LinkEvent)>10]/LinkEvent");
                if (e != null) {
                    Map<String, String> LinkEventMap = new HashMap<String, String>();
                    String LinkEventStr = e.getText().replaceAll("&lt;", "<").replaceAll("&gt", ">");
                    Element eventNode = DocumentHelper.parseText(LinkEventStr).getRootElement();
                    for (Element c : (List<Element>) eventNode.elements()) {
                        PubFunc.putElementValue2Map(LinkEventMap, c);
                    }
                    String inparam = eventNode.elementText("inparam");
                    if (!StringUtils.nullOrBlank(inparam)) {
                        Map<String, String> inparamMap = new LinkedHashMap<String, String>();
                        String[] items = inparam.split(";");
                        for (String item : items) {
                            String[] keyValue = item.split("=");
                            if (keyValue.length == 2) {
                                inparamMap.put("'" + keyValue[0] + "'", "'" + keyValue[1] + "'");
                            } else if (keyValue.length == 1) {
                                inparamMap.put("'" + keyValue[0] + "'", "''");
                            }
                        }
                        LinkEventMap.put("'inparam'", inparamMap.toString());
                    }
                    resultMap.put("'LinkEvent'", LinkEventMap.toString());
                }
            }
            response.getWriter().write(PubFunc.coverMap2JsonStr(resultMap));
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }

    /**
     * 查询
     *
     * @param mapping
     * @param actionForm
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public void query(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response) {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/javascript");
        try {
            Hashtable<String, String> paramHt = PubFunc.getQueryParams(request);
            // 获得查询结果

            String xmlData = invoker.getSearchResult(paramHt, request);
            if (xmlData != null && !"".equals(xmlData)) {
                int index = xmlData.indexOf("<root>");
                if (index >= 0)
                    xmlData = xmlData.substring(index);
                if ("true".equals(request.getParameter("isTreeGrid"))) {
                    xmlData = xmlData.replaceAll("records", "children");
                }
                response.getWriter().write(xmlData);
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            String errormsg = e.toString().replaceAll("\n", "");
            e.printStackTrace();
            try {
                response.getWriter().write(errormsg);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    /**
     * 保存显示字段
     *
     * @param mapping
     * @param actionForm
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public void saveShowfield(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response) throws Exception {

        String noshowfields = request.getParameter("noshowfields");
        String showfields = request.getParameter("showfields");
        if (showfields == null) {
            showfields = "";
        }
        if (noshowfields == null) {
            noshowfields = "";
        }
        String param = "";
        if (!StringUtils.nullOrBlank(showfields) || !StringUtils.nullOrBlank(noshowfields)) {
            param = showfields + "noshowfields=" + noshowfields;
        }

        String resultStyleID = request.getParameter("resultStyleID");
        if (resultStyleID == null)
            resultStyleID = "";
        String accID = (String) request.getSession().getAttribute("sysAccessID");
        Map userMap = (Map) invoker.getUserInfoByAccid(accID);
        String userId = (String) userMap.get("userid");
        try {
            invoker.saveParam(resultStyleID, userId, param);
        } catch (Exception e) {
            response.setCharacterEncoding("UTF-8");
            response.setContentType("text/javascript");
            String errormsg = e.toString().replaceAll("\n", "");
            System.out.println(e.toString());
            response.getWriter().write("errormessage:<message>" + errormsg + "</message>");
        }
    }

    /**
     * 获取条件界面树的数据（包括代码中心数据源和其他数据源）
     *
     * @param mapping
     * @param actionForm
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public void getconditreedata(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response) throws Exception {
        String accID = (String) request.getSession().getAttribute("sysAccessID");
        try {
            Map<String, String> paramMap = PubFunc.getParamsMap(request);
            String tableAlias = paramMap.get("tableName");
            String otherCondi = paramMap.get("otherCondi");
            String reBulidCondi = paramMap.get("reBuildflagCondi");
            String needDelay = paramMap.get("needDelay").toLowerCase();
            String dataParam = paramMap.get("dataParam");
            String returnFields = paramMap.get("returnFields");
            String showCode = paramMap.get("showCode").toLowerCase();// 树节点上显示代码
            String needCorp = paramMap.get("needCorp").toLowerCase();
            String curNode = paramMap.get("node");
            String customCode = paramMap.get("customCode");
            String selectType = paramMap.get("selectType");
            String codeRule = paramMap.get("codeRule");
            String hasparent = paramMap.get("hasparent");
            String codeField=paramMap.get("codeField") ;
            String nameField=paramMap.get("nameField") ;
            if(codeField==null){
                codeField="" ;
            }
            if(nameField==null){
                nameField="";
            }
            if (!"F".equalsIgnoreCase(hasparent)) {
                hasparent = "T";
            }
            SelectOtherDataSource sods = null;// 其他数据源
            if (!dataParam.equals("")) {
                byte[] bytes = Base64.decode(dataParam);
                dataParam = new String(bytes, "gb2312");
                String[] dataParamArray = dataParam.split(",");
                sods = new SelectOtherDataSource(dataParamArray);
            }
            if (!otherCondi.equals("")) {
                otherCondi = PubFunc.URLDecode(otherCondi);
            }
            if (reBulidCondi == null) {
                reBulidCondi = "";
            }
            if ("".equals(otherCondi))
                otherCondi = reBulidCondi;
            else {
                if (!"".equals(reBulidCondi))
                    otherCondi = otherCondi + " and " + reBulidCondi;
            }
            System.out.println(otherCondi);

            String privisql = "";


            String xmlData = "";
            if (sods == null) {// 代码中心
                String xml = invoker.getCodeCenterTreeData(accID, tableAlias, curNode, otherCondi, needDelay, returnFields, hasparent,"");
                if (!StringUtils.nullOrBlank(returnFields)&&"true".equals(customCode)) {
                    codeField = returnFields;
                }
                xmlData = invoker.getReturnDataByXml(xml, needDelay,codeField ,nameField ,"" , "", showCode, selectType, "", codeRule,null);
            } else{
                if ("true".equals(needCorp)) {
                    privisql = invoker.getCorpPriviSQL(accID);// 取得集团权限条件语句
                }
                // 其他数据源
                xmlData = invoker.getOtherTreeData(accID, needDelay, curNode, otherCondi, sods, showCode, privisql, selectType, codeRule);
            }
            response.setCharacterEncoding("UTF-8");
            response.setContentType("text/javascript");
            response.getWriter().write(xmlData);
        } catch (Exception e) {
            response.setCharacterEncoding("UTF-8");
            response.setContentType("text/javascript");
            String errormsg = e.toString().replaceAll("\n", "");
            System.out.println(e.toString());
            response.getWriter().write("errormessage:<message>" + errormsg + "</message>");
        }
    }

    /**
     * 从代码中心获取结果界面树的数据
     *
     * @param mapping
     * @param actionForm
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public void getresulttreedatabycodecenter(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response) throws Exception {
        String accID = (String) request.getSession().getAttribute("sysAccessID");
        try {
            Map<String, String> paramMap = PubFunc.getParamsMap(request);
            String tableAlias = paramMap.get("tableName");
            String reBulidCondi = paramMap.get("reBuildCondi");
            if (reBulidCondi == null) {
                reBulidCondi = "";
            }
            String hasParent = paramMap.get("hasParent");
            if (hasParent == "" || hasParent == null)
                hasParent = "true";

            String needDelay = paramMap.get("needDelay").toLowerCase();
            String containCorp = paramMap.get("containCorp");// 是否包含集团树
            String corpCondition = paramMap.get("corpCondition");// 建立集团树条件
            String showCode = paramMap.get("showCode");// 树节点上显示代码
            String curNode = paramMap.get("node");
            String selectType = paramMap.get("selectType");
            String otherCondi = paramMap.get("otherCondi");
            String codeField = paramMap.get("codeField");
            String nameField = paramMap.get("nameField");
            if (!StringUtils.nullOrBlank(otherCondi)) {
                if(otherCondi.startsWith("URL")){
                    otherCondi=otherCondi.substring(3) ;
                    otherCondi=URLDecoder.decode(URLDecoder.decode(otherCondi,"UTF-8"),"UTF-8") ;
                }else{
                    byte[] bytes = Base64.decode(otherCondi);
                    otherCondi = new String(bytes, "gb2312");
                }
            }
            if (StringUtils.nullOrBlank(otherCondi)|| otherCondi == "undefined")
                otherCondi = reBulidCondi;
            else {
                if (!StringUtils.nullOrBlank(reBulidCondi))
                    otherCondi = otherCondi + " and " + reBulidCondi;
            }
            System.out.println(otherCondi);

            // 最底层集团代码的长度,用途：只有最底层的集团才能建具体业务的树
            StringBuffer corpCodeLen = new StringBuffer();
            List<String> corpLst = new ArrayList();
            String bsCurNode = "";
            String corpCurNode = "";
            if ("true".equals(containCorp.toLowerCase())) {// 需要建集团树
                String privisql = invoker.getCorpPriviSQL(accID);// 取得集团权限条件语句
                String buildCorpTreeCondition = PubFunc.getBuildCorpTreeCondi(corpCondition, privisql);
                if ("true".equals(needDelay)) {// 动态建树
                    if ("##".equals(curNode)) {
                        corpCurNode = curNode;
                        corpLst = invoker.getCorpDataFromCodeCenter(buildCorpTreeCondition, needDelay, corpCurNode, accID, corpCodeLen);
                    } else {
                        String[] curNodeArr = curNode.split("\\*");
                        int index = curNode.indexOf("*");
                        if (curNodeArr.length == 1 && index > 0) {
                            corpCurNode = curNodeArr[0];
                            corpLst = invoker.getCorpDataFromCodeCenter(buildCorpTreeCondition, needDelay, corpCurNode, accID, corpCodeLen);
                        } else if (curNodeArr.length == 2) {
                            corpCurNode = curNodeArr[0];
                            bsCurNode = curNodeArr[1];
                        } else if (curNodeArr.length == 1 && index < 0) {
                            bsCurNode = curNodeArr[0];
                        }
                    }
                } else {// 一次性建树
                    corpLst = invoker.getCorpDataFromCodeCenter(buildCorpTreeCondition, needDelay, curNode, accID, corpCodeLen);
                }
            }

            List<TreeCorp> TreeCorpList = new ArrayList<TreeCorp>();
            List<TreeCorp> bottoms=null;
            // 集团树处理
            if (corpLst != null && !corpLst.isEmpty()) { // 包含集团树信息
                for (int j = 0; j < corpLst.size(); j++) {
                    // 先建集团的树
                    String corpValue = corpLst.get(j);
                    TreeCorp corpTreeNode = TreeCorp.parse(corpValue, showCode);
                    if (corpTreeNode != null) {
                        TreeCorpList.add(corpTreeNode);
                    }
                }
                TreeCorpList = TreeNodeList.parseListTreeCorp(TreeCorpList);
                bottoms= TreeNodeList.getBottomNode(TreeCorpList);
            }
            List<String> bottomCorpCodes=new ArrayList<String>();
            if(bottoms!=null&&!bottoms.isEmpty()){
                for(TreeCorp c:bottoms){
                    bottomCorpCodes.add(c.getCorpCode());
                }
            }
            if(!bottomCorpCodes.isEmpty()){
                if(!StringUtils.nullOrBlank(otherCondi)){
                    otherCondi+=" and corpcode in("+bottomCorpCodes.toString().substring(1,bottomCorpCodes.toString().length()-1)+") " ;
                }else{
                    otherCondi=" corpcode in("+bottomCorpCodes.toString().substring(1,bottomCorpCodes.toString().length()-1)+") " ;
                }
            }
            String xml = "",orderFields="";
            //如果不是按照code编码 取数据要以建树字段排序
            //因为建树是一条枝往上建起
            if(!StringUtils.nullOrBlank(codeField)&&!"code".equalsIgnoreCase(codeField)){
                orderFields="0:"+codeField;
            }
            if ("true".equals(containCorp.toLowerCase())) {// 需要建集团树
                if ("true".equals(needDelay)) {// 动态建树
                    if (!"".equals(corpCurNode) && "".equals(bsCurNode) && (corpLst == null || corpLst.size() == 0)) {// 最底层的集团，此时该取业务数据了
                        bsCurNode = "##";
                        // 获取业务数据目前没有加上集团的过滤条件，如果需要传多参数corpCurNode即可
                        xml = invoker.getCodeCenterTreeData(accID, tableAlias, bsCurNode, otherCondi, needDelay, "", hasParent,orderFields);// 取顶层的业务数据
                    } else if (!"".equals(bsCurNode) && (corpLst == null || corpLst.size() == 0)) {
                        xml = invoker.getCodeCenterTreeData(accID, tableAlias, bsCurNode, otherCondi, needDelay, "", hasParent,orderFields);
                    }
                } else {// 一次性建树
                    xml = invoker.getCodeCenterTreeData(accID, tableAlias, curNode, otherCondi, needDelay, "", hasParent,orderFields);
                }
            } else {
                xml = invoker.getCodeCenterTreeData(accID, tableAlias, curNode, otherCondi, needDelay, "", hasParent,orderFields);
            }
            String  xmlData = invoker.getReturnDataByXml(xml, needDelay, codeField, nameField, "", "", showCode, selectType, "", null,TreeCorpList);
            response.setCharacterEncoding("UTF-8");
            response.setContentType("text/javascript");
            response.getWriter().write(xmlData);
        } catch (Exception e) {
            response.setCharacterEncoding("UTF-8");
            response.setContentType("text/javascript");
            String errormsg = e.toString().replaceAll("\n", "");
            System.out.println(e.toString());
            response.getWriter().write("errormessage:<message>" + errormsg + "</message>");
        }
    }

    /**
     * 从指定的数据源获取结果界面树的数据
     *
     * @param mapping
     * @param actionForm
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public void getresulttreedatabyothersource(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response) throws Exception {
        // add by xdy 2010.01.13 如果需要重建树，则第一次不加载树数据
        HashMap<String, String> paramsMap = PubFunc.getParamsMap(request);
        String needReBuild = paramsMap.get("needReBuild");
        if ("true".equals(needReBuild)) {
            return;
        }
        String accID = (String) request.getSession().getAttribute("sysAccessID");
        try {
            String serviceName = paramsMap.get("serviceName");
            String idParamName = paramsMap.get("idParamName");
            String condiParamName = paramsMap.get("condiParamName");

            String tableName = paramsMap.get("tableName");
            String isSelectSQL = paramsMap.get("isSelectSQL");
            // 可以是Select语句的where部分，也可以是整个的检索Select语句
            String otherCondi = paramsMap.get("otherCondi");
            String codeField = paramsMap.get("codeField");
            String nameField = paramsMap.get("nameField");
            String needDelay = paramsMap.get("needDelay").toLowerCase();
            String recordsNodeParam = paramsMap.get("recordsNodeParam");
            String recordNodeParam = paramsMap.get("recordNodeParam");
            String returnParamName = paramsMap.get("returnParamName");
            String curNode = paramsMap.get("node");// 当前节点，获取到当前节点，动态建树
            String selectType = paramsMap.get("selectType");
            String containCorp = paramsMap.get("containCorp");// 是否包含集团树
            String corpCondition = paramsMap.get("corpCondition");// 建立集团树条件
            String reBuildCondi = paramsMap.get("reBuildCondi");
            if (reBuildCondi == null) {
                reBuildCondi = "";
            }
            String showCode = paramsMap.get("showCode");
            String reBuildFlagCondi = paramsMap.get("reBuildflagCondi");

            if (reBuildFlagCondi != null && !reBuildFlagCondi.equals("")) {
                if (reBuildCondi.length() > 0) {
                    reBuildCondi += " and ";
                }
                reBuildCondi += reBuildFlagCondi;
            }

            if (otherCondi != "") {
                byte[] bytes = Base64.decode(otherCondi);
                otherCondi = new String(bytes, "gb2312");
            }

            // 最底层集团代码的长度,用途：只有最底层的集团才能建具体业务的树
            StringBuffer corpCodeLen = new StringBuffer();
            List<String> corpLst = new ArrayList<String>();
            // String privisql = invoker.getPriviSQL("SYSGROUP", accID,
            // "CODE");// 取得权限条件语句
            String corpCurNode = "";
            if ("true".equals(containCorp.toLowerCase())) {  // 需要建集团树
                String privisql = invoker.getCorpPriviSQL(accID);// 取得集团权限条件语句
                String buildCorpTreeCondition = PubFunc.getBuildCorpTreeCondi(corpCondition, privisql);
                if ("true".equals(needDelay)) {// 动态建树
                    if ("##".equals(curNode)) {
                        corpCurNode = curNode;
                        corpLst = invoker.getCorpDataFromCodeCenter(buildCorpTreeCondition, needDelay, corpCurNode, accID, corpCodeLen);
                    } else {
                        String[] curNodeArr = curNode.split("\\*");
                        int index = curNode.indexOf("*");
                        if (curNodeArr.length == 1 && index > 0) {
                            corpCurNode = curNodeArr[0];
                            corpLst = invoker.getCorpDataFromCodeCenter(buildCorpTreeCondition, needDelay, corpCurNode, accID, corpCodeLen);
                        }
                    }
                } else {// 一次性建树
                    corpLst = invoker.getCorpDataFromCodeCenter(buildCorpTreeCondition, needDelay, curNode, accID, corpCodeLen);
                }
            }

            String xmlCondi = "";
             if ("false".equals(isSelectSQL)) {
                if (!"".equals(reBuildCondi) && !"".equals(otherCondi)) {
                    int orderIndex =  otherCondi.toUpperCase(). indexOf("ORDER BY") ;
                    if (orderIndex > 0) {
                        StringBuilder sb = new StringBuilder(otherCondi);
                        xmlCondi = sb.insert(orderIndex, " and " + reBuildCondi + " ").toString();
                    } else {
                        xmlCondi = otherCondi + " and " + reBuildCondi;
                    }
                }

              if (!"".equals(reBuildCondi) && "".equals(otherCondi)){
                  xmlCondi = reBuildCondi;
              }
            } else {
                if (!"".equals(reBuildCondi)){
                    otherCondi = invoker.putCondiIntoSelectSQL(otherCondi, reBuildCondi);
                }
                xmlCondi = otherCondi;
            }

            List<TreeCorp> TreeCorpList = new ArrayList<TreeCorp>();
            List<TreeCorp> bottoms=null;
            // 集团树处理
            if (corpLst != null && !corpLst.isEmpty()) { // 包含集团树信息
                for (int j = 0; j < corpLst.size(); j++) {
                    // 先建集团的树
                    String corpValue = corpLst.get(j);
                    TreeCorp corpTreeNode = TreeCorp.parse(corpValue, showCode);
                    if (corpTreeNode != null) {
                        TreeCorpList.add(corpTreeNode);
                    }
                }
                TreeCorpList = TreeNodeList.parseListTreeCorp(TreeCorpList);
                bottoms= TreeNodeList.getBottomNode(TreeCorpList);
            }
            List<String> bottomCorpCodes=new ArrayList<String>();
            if(bottoms!=null&&!bottoms.isEmpty()){
                for(TreeCorp c:bottoms){
                    bottomCorpCodes.add(c.getCorpCode());
                }
            }
            if(!bottomCorpCodes.isEmpty()){
                String temp=" corpcode in("+bottomCorpCodes.toString().substring(1,bottomCorpCodes.toString().length()-1)+") " ;
                if ("false".equals(isSelectSQL)) {
                    if(StringUtils.nullOrBlank(xmlCondi)){
                        xmlCondi=temp ;
                    }else{
                        int orderIndex =  xmlCondi.toUpperCase().indexOf("ORDER BY") ;
                        if (orderIndex > 0) {
                            StringBuilder sb = new StringBuilder(xmlCondi);
                            xmlCondi = sb.insert(orderIndex, " and " + temp + " ").toString();
                        } else {
                            xmlCondi = xmlCondi + " and "+ temp;
                        }
                    }
                }else{
                    xmlCondi = invoker.putCondiIntoSelectSQL(xmlCondi, temp);
                }

            }
            System.out.println(xmlCondi);
            Hashtable<String, String> in = new Hashtable<String, String>();
            in.put("service", serviceName);
            in.put(idParamName, accID);
            in.put("tablename", tableName);
            in.put(condiParamName, xmlCondi);
            String xml = invoker.getTreeDataByOtherSource(in, returnParamName);
            String xmlData = invoker.getReturnDataByXml(xml, needDelay, codeField, nameField, recordsNodeParam, recordNodeParam, showCode, selectType, in.toString(), null,TreeCorpList);
            response.setCharacterEncoding("UTF-8");
            response.setContentType("text/javascript");
            response.getWriter().write(xmlData);
        } catch (Exception e) {
            response.setCharacterEncoding("UTF-8");
            response.setContentType("text/javascript");
            String errormsg = e.toString().replaceAll("\n", "");
            System.out.println(e.toString());
            response.getWriter().write("errormessage:<message>" + errormsg + "</message>");
        }
    }

    /**
     * 获取表格的数据。数据源可以是'代码中心'和'其他数据源' 方法名由getCodeCenterSelectData改为getGridData
     *
     * @param mapping
     * @param actionForm
     * @param request
     * @param response
     * @return
     * @throws java.io.IOException
     * @throws Exception
     */
    public void getGridData(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response) throws IOException {
        String accID = (String) request.getSession().getAttribute("sysAccessID");
        String tableAlias = request.getParameter("tableAlias");
        String tableName = request.getParameter("tableName");
        String pageSize = request.getParameter("limit");
        String page = request.getParameter("page");
        String callback = PubFunc.coverNull(request.getParameter("callback"));
        String treenode = request.getParameter("treenode");
        String needCorp = request.getParameter("needCorp");

        String subDataParam = PubFunc.coverNull(request.getParameter("subDataParam"));
        SelectOtherDataSource sods = null;
        try {
            if (!StringUtils.nullOrBlank(subDataParam)) {
                byte[] bytes = Base64.decode(subDataParam);
                subDataParam = new String(bytes, "gb2312");
                String[] dataParamArray = subDataParam.split(",");
                sods = new SelectOtherDataSource(dataParamArray);
            }
            String RelationFieldName = PubFunc.coverNull(request.getParameter("RelationFieldName"));
            // otherCondi 是在设置工具里设置的过滤条件
            String otherCondi = PubFunc.coverNull(request.getParameter("otherCondi"));
            if (!StringUtils.nullOrBlank(otherCondi)) {
                otherCondi = URLDecoder.decode(URLDecoder.decode(otherCondi, "UTF-8"), "UTF-8").replaceAll("\\n", "");
            }

            // filterCondi是选择表格上面条件框 动态检索的条件
            String filterCondi = PubFunc.coverNull(request.getParameter("filterCondi"));
            if (!StringUtils.nullOrBlank(filterCondi)) {
                filterCondi = URLDecoder.decode(filterCondi, "UTF-8");
            }

            String orderfields = PubFunc.coverNull(request.getParameter("orderfields"));
            String orderbyflag = PubFunc.coverNull(request.getParameter("orderbyflag"));
            String jsonData = "";
            if (sods == null) {
                jsonData = invoker.getCodeCenterGridData(callback, accID, tableAlias, page, pageSize, treenode, RelationFieldName, filterCondi, otherCondi, orderfields, orderbyflag, "F");
            } else {
                String privisql = "";
                if ("true".equalsIgnoreCase(needCorp)) {
                    privisql = invoker.getCorpPriviSQL(accID);// 取得集团权限条件语句
                }
                // 其他数据源
                jsonData = invoker.getOtherSouceGridData(callback, accID, tableName, page, pageSize, treenode, RelationFieldName, sods, filterCondi, otherCondi, orderfields, orderbyflag, privisql);
            }
            response.setCharacterEncoding("UTF-8");
            response.setContentType("text/javascript");
            response.getWriter().write(jsonData);
        } catch (Exception e) {
            response.setCharacterEncoding("UTF-8");
            response.setContentType("text/javascript");
            String errormsg = e.toString().replaceAll("\n", "");
            System.out.println(e.toString());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("errormessage:<message>" + errormsg + "</message>");
        }

    }

    public void getModuleInfoByID(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response) throws Exception {
        String moduleId = request.getParameter("moduleId");
        String xmlData = invoker.getModuleInfoByID(moduleId);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/javascript");
        response.getWriter().write(xmlData);
    }

    // ADD BY ZXW 2009-10-10 JS调用Action 再由Action调用SERVICE
    public void callService(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response) throws Exception {
        String xml = request.getParameter("xml");

        xml = URLDecoder.decode(xml, "UTF-8");
        try {
            String result = CustomQueryInvoker.callService(xml);
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(result);
        } catch (Exception e) {
            String errorxml = "错误:" + e.getMessage();
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(errorxml);
        }
    }

    // ADD BY ZXW 2009-10-20 取已设置自定义查询浏览方式
    public void getBrowseStyle(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response) throws Exception {

        String accID = (String) request.getSession().getAttribute("sysAccessID");
        String classid = request.getParameter("classid");
        String styleid = request.getParameter("styleid");
        try {
            // 调用服务
            String xml = "<root>" + "<service>customquery.com.getBrowseStyle</service>" + "<classid>" + classid + "</classid>" + "<styleid>" + styleid + "</styleid>" + "<accID>" + accID + "</accID>" + "</root>";
            String result = CustomQueryInvoker.callService(xml);
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(result);
        } catch (Exception e) {
            String errorxml = "错误:" + e.getMessage();
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(errorxml);
        }
    }

    // ADD BY ZXW 2009-10-20 取已设置自定义查询浏览方式
    public void saveBrowseStyle(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response) throws Exception {

        String accID = (String) request.getSession().getAttribute("sysAccessID");
        String classid = request.getParameter("classid");
        String styleid = request.getParameter("styleid");
        String Setxml = request.getParameter("xml");
        try {
            // 调用服务
            String xml = "<root>" + "<service>customquery.com.saveBrowseStyle</service>" + "<classid>" + classid + "</classid>" + "<styleid>" + styleid + "</styleid>" + "<accID>" + accID + "</accID>" + "<XML>" + Setxml + "</XML>" + "</root>";
            String result = CustomQueryInvoker.callService(xml);
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(result);
        } catch (Exception e) {
            String errorxml = "错误:" + e.getMessage();
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(errorxml);
        }
    }

    public void doUpdateField(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setCharacterEncoding("UTF-8");
        try {
            String serviceName = request.getParameter("serviceName");
            String tableName = request.getParameter("tableName");
            String updateInfo = request.getParameter("updateInfo");

            String allCondi = request.getParameter("allCondi");
            String needUpdateTxm = request.getParameter("needUpdateTxm");
            if (needUpdateTxm == null)
                needUpdateTxm = "";

            String updateStr = PubFunc.getUpdateSetStr(updateInfo);
            Hashtable<String, String> in = new Hashtable<String, String>();
            String updateSql = "update " + tableName + " set " + updateStr;
            if (!StringUtils.nullOrBlank(allCondi)) {
                updateSql += " where " + allCondi;
            }
            in.put("service", serviceName + ".updatedata");
            in.put("exesql", updateSql);
            ServiceInvokerUtil.invoker(in);
            if (needUpdateTxm.equals("1")) {
                allCondi = allCondi.replace("barcode_sendtype=0", "BARCODE_SENDTYPE='1'");
                String querysql = "select * from " + tableName + " where " + allCondi;
                String xml = CustomQueryInvoker.getBusinessTxmData(serviceName, querysql);
                if (xml != null && !"".equals(xml)) {
                    xml = xml.replaceAll("<querydatas>", "").replaceAll("</querydatas>", "").replaceAll("<querydata>", "<waitaccount>").replaceAll("</querydata>", "</waitaccount>");
                    CustomQueryInvoker.newWaitAccount(xml);
                }
            }
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("");
        } catch (Exception e) {
            String errorxml = "Errormsg:" + e.getMessage();
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(errorxml);
        }
    }

    public void doUpdateFieldByAutoValue(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setCharacterEncoding("UTF-8");
        try {
            String serviceName = request.getParameter("serviceName");
            String updateSql = request.getParameter("updatesql");
            if (updateSql != null)
                updateSql = URLDecoder.decode(updateSql, "UTF-8");

            Hashtable<String, String> in = new Hashtable<String, String>();
            in.put("service", serviceName + ".updatedata");
            in.put("exesql", updateSql);
            ServiceInvokerUtil.invoker(in);
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("");
        } catch (Exception e) {
            String errorxml = "Errormsg:" + e.getMessage();
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(errorxml);
        }
    }

    public void doUpdateAllRecords(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setCharacterEncoding("UTF-8");
        String serviceName = request.getParameter("serviceName");
        String updateInfo = request.getParameter("updateInfo");
        String tableName = request.getParameter("tableName");

        String updateStr = PubFunc.getUpdateSetStr(updateInfo);

        String needUpdateTxm = request.getParameter("needUpdateTxm");

        if (needUpdateTxm == null)
            needUpdateTxm = "";

        String conditionXml = request.getParameter("conditionXml");
        String treeCondiXml = request.getParameter("treeCondiXml");


        if (!StringUtils.nullOrBlank(conditionXml)) {
            conditionXml = URLDecoder.decode(conditionXml, "UTF-8");
        }

        if (!StringUtils.nullOrBlank(treeCondiXml)) {
            treeCondiXml = URLDecoder.decode(treeCondiXml, "UTF-8");
            treeCondiXml = PubFunc.parseTreeCondi(treeCondiXml);
            conditionXml = PubFunc.unionXml(conditionXml, URLEncoder.encode(treeCondiXml, "UTF-8"));
        }

        if (!StringUtils.nullOrBlank(conditionXml)) {
            try {
                Hashtable<String, String> in = new Hashtable<String, String>();
                in.put("service", "customquery.getcondisqlByXml");
                in.put("conditionXml", conditionXml);
                in.put("exceptFields", "RELATIONTABLE,RELATIONFIELD");
                Hashtable result = ServiceInvokerUtil.invoker(in);
                conditionXml = (String) result.get("condiSql");
            } catch (Exception e) {
                String errorxml = "Errormsg:" + e.getMessage();
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write(errorxml);
            }
        }

        Hashtable<String, String> in = new Hashtable<String, String>();
        String updateSql = "update " + tableName + " set " + updateStr;
        if (!StringUtils.nullOrBlank(conditionXml)) {
            updateSql += " where " + conditionXml;
        }
        in.put("service", serviceName + ".updatedata");
        in.put("exesql", updateSql);

        try {
            ServiceInvokerUtil.invoker(in);
            if (needUpdateTxm.equals("1")) {
                conditionXml = conditionXml.replace("barcode_sendtype=0", "BARCODE_SENDTYPE='1'");
                String querysql = "select * from " + tableName + " where " + conditionXml;
                String xml = CustomQueryInvoker.getBusinessTxmData(serviceName, querysql);
                if (xml != null && !"".equals(xml)) {
                    xml = xml.replaceAll("<querydatas>", "").replaceAll("</querydatas>", "").replaceAll("<querydata>", "<waitaccount>").replaceAll("</querydata>", "</waitaccount>");
                    CustomQueryInvoker.newWaitAccount(xml);
                }
            }
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("");
        } catch (Exception e) {
            String errorxml = "Errormsg:" + e.getMessage();
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(errorxml);
        }
        return;
    }

    public void doBatchUpdate(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setCharacterEncoding("UTF-8");

        String accID = (String) request.getSession().getAttribute("sysAccessID");
        String updateInfo = request.getParameter("updateInfo");
        String resultCondiXml = request.getParameter("resultCondiXml");
        if (resultCondiXml != null) {
            resultCondiXml = URLDecoder.decode(resultCondiXml, "UTF-8");
        } else
            resultCondiXml = "";

        String conditionXml = request.getParameter("conditionXml");
        // 树条件XML add by hjr treeConditionXml
        String treeCondi = request.getParameter("treeConditionXml");
        if (treeCondi == null) {
            treeCondi = "";
        }
        if ((!resultCondiXml.equals(""))) {
            if (conditionXml != null) {
                conditionXml = PubFunc.unionXml(conditionXml, resultCondiXml);
            } else {
                conditionXml = resultCondiXml;
            }
        }

        conditionXml = "BASE64ENCODING" + conditionXml;

        try {
            boolean flag = true;

            List<String> fields = new ArrayList<String>();

            if (updateInfo == null || updateInfo.length() <= 0) {
                flag = false;
            } else {
                String[] updateInfoArr = updateInfo.split(";");
                for (String item : updateInfoArr) {
                    String[] info = item.split("=");
                    flag = !fields.contains(info[0]);
                    if (flag) {
                        fields.add(info[0]);
                    } else {
                        break;
                    }
                }
            }
            if (!flag) {
                throw new RuntimeException("修改字段相同");
            }
            String serviceName = request.getParameter("serviceName");
            String tableName = request.getParameter("tableName");
            String constCondiInfo = request.getParameter("constCondiInfo");
            Hashtable<String, String> in = new Hashtable<String, String>();
            in.put("service", serviceName + ".batchupdatedata");
            in.put("tableName", tableName);
            in.put("constCondiInfo", constCondiInfo);
            in.put("updateInfo", updateInfo);
            in.put("conditionXML", conditionXml);
            in.put("treeConditionXml", treeCondi);
            in.put("accID", accID);

            ServiceInvokerUtil.invoker(in);
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("");
        } catch (Exception e) {
            String errorxml = "Errormsg:" + e.getMessage();
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(errorxml);
        }
    }

    public void getSysParam(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            String name = request.getParameter("name");
            String owner=request.getParameter("owner") ;
            if(owner==null){
                String accID = (String) request.getSession().getAttribute("sysAccessID");
                Map<String, String> map = (Map) invoker.getUserInfoByAccid(accID);
                String userId = (String) map.get("userid");
                String personID = invoker.getPersonIdByUserId(userId);
                // 获取部门，集团信息
                map = invoker.getDeptOrCorpMapByPersonId(personID, accID);
                owner= (String) map.get("corpid");
            }
            String result = PubFunc.getSysParam(name, owner);
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(result);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(e.getMessage());
        }
    }

    public void getCurDeptOrUser(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response) throws Exception {
        String format = request.getParameter("format");
        format = format.toUpperCase();
        String accID = (String) request.getSession().getAttribute("sysAccessID");
        Map userMap = (Map) invoker.getUserInfoByAccid(accID);
        String userId = (String) userMap.get("userid");
        String userName = (String) userMap.get("username");
        String personID = invoker.getPersonIdByUserId(userId);
        // 获取部门，集团信息
        Map<String,String> deptOrCorpMap = invoker.getDeptOrCorpMapByPersonId(personID, accID);
        String deptCode = (String) deptOrCorpMap.get("deptcode");
        String deptName = (String) deptOrCorpMap.get("deptname");
        String corpId = (String) deptOrCorpMap.get("corpid");
        String corpCode = (String) deptOrCorpMap.get("corpcode");
        String corpName = (String) deptOrCorpMap.get("corpname");
        String  rolecode=(String) deptOrCorpMap.get("rolecode");
        String  rolename=(String) deptOrCorpMap.get("rolename");

        Map deptInfo = invoker.getDeptMapByDeptCode(deptCode);
        String deptshortname = (String) deptInfo.get("deptshortname");
        String deptshortname1 = (String) deptInfo.get("deptshortname1");
        String deptshortname2 = (String) deptInfo.get("deptshortname2");
        String deptshortname3 = (String) deptInfo.get("deptshortname3");
        String deptshortname4 = (String) deptInfo.get("deptshortname4");

        String parentCode = "";
        String parentName = "";
        String parentShortName = "";
        Map parentDeptMap = invoker.getParentDeptMapByDeptCode(deptCode);
        if (parentDeptMap != null) {
            parentCode = (String) parentDeptMap.get("parentcode");
            parentName = (String) parentDeptMap.get("parentname");
            parentShortName = (String) parentDeptMap.get("parentshortname");
        }

        String stringField23 = "";
        String stringField24 = "";
        String stringField25 = "";

        Map personInfo = invoker.getPersonInfo(personID);
        if (personInfo != null) {
            stringField23 = (String) personInfo.get("stringfield23");
            stringField24 = (String) personInfo.get("stringfield24");
            stringField25 = (String) personInfo.get("stringfield25");
        }

        String result = "";

        if ("USER_ID".equals(format))
            result = userId;
        if ("PERSON_ID".equals(format))
            result = personID;
        if ("USER_NAME".equals(format))
            result = userName;
        else if ("DEPT_CODE".equals(format))
            result = deptCode;
        else if ("DEPT_NAME".equals(format))
            result = deptName;
        else if ("CORP_ID".equals(format))
            result = corpId;
        else if ("CORP_CODE".equals(format))
            result = corpCode;
        else if ("CORP_NAME".equals(format))
            result = corpName;
        else if ("PARENT_DEPT_CODE".equals(format))
            result = parentCode;
        else if ("PARENT_DEPT_NAME".equals(format))
            result = parentName;
        else if ("PARENT_DEPT_SHORTNAME".equals(format))
            result = parentShortName;
        else if ("STRINGFIELD23".equals(format))
            result = stringField23;
        else if ("STRINGFIELD24".equals(format))
            result = stringField24;
        else if ("STRINGFIELD25".equals(format))
            result = stringField25;
        else if ("PARENT_DEPT_CODESHORTNAME".equals(format)) {
            if ("".equals(parentCode) && "".equals(parentShortName))
                result = "";
            else if (!"".equals(parentCode) && "".equals(parentShortName))
                result = parentCode;
            else if ("".equals(parentCode) && !"".equals(parentShortName))
                result = parentShortName;
            else if (!"".equals(parentCode) && !"".equals(parentShortName))
                result = parentCode + "," + parentShortName;
        } else if ("DEPTSHORTNAME".equals(format))
            result = deptshortname;
        else if ("DEPTSHORTNAME1".equals(format))
            result = deptshortname1;
        else if ("DEPTSHORTNAME2".equals(format))
            result = deptshortname2;
        else if ("DEPTSHORTNAME3".equals(format))
            result = deptshortname3;
        else if ("DEPTSHORTNAME4".equals(format))
            result = deptshortname4;
        else if("rolecode".equalsIgnoreCase(format)){
            result=rolecode;
        } else if("rolename".equalsIgnoreCase(format)){
            result=rolename;
        }else if("deptcorpid".equalsIgnoreCase(format)||"deptcorpcode".equalsIgnoreCase(format)||"deptcorpname".equalsIgnoreCase(format)){
            result= deptOrCorpMap.get(format.toLowerCase());
        }

        try {
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(result);
        } catch (Exception e) {
            String errorxml = "错误:" + e.getMessage();
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(errorxml);
        }
    }

    public void getUserPrivilegeInfo(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response) throws Exception {
        String accID = (String) request.getSession().getAttribute("sysAccessID");

        Hashtable<String, String> in = new Hashtable<String, String>();
        in.put("service", "privilegeinfo.validate.checkUserAuth");
        in.put("accessID", accID);
        in.put("funcCode", "40029002");

        try {
            Hashtable flagTable = ServiceInvokerUtil.invoker(in);
            String flag = (String) flagTable.get("flag");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(flag);
        } catch (Exception e) {
            String errorxml = "Errormsg:" + e.getMessage();
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(errorxml);
        }
    }

    public void reBuildCondiTree(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response) throws Exception {
        String accID = (String) request.getSession().getAttribute("sysAccessID");
        try {
            String tableAlias = request.getParameter("tableName");
            String otherCondi = request.getParameter("otherCondi");
            String dataParam = request.getParameter("dataParam");
            String returnFields = request.getParameter("returnFields");
            if (returnFields == null)
                returnFields = "";
            String showCode = request.getParameter("showCode").toLowerCase();// 树节点上显示代码
            String needCorp = request.getParameter("needCorp").toLowerCase();
            String selectType = request.getParameter("selectType");
            String filterCondi = request.getParameter("filterCondi");

            if (showCode == null || "undefined".equals(showCode.toLowerCase()) || "false".equals(showCode))
                showCode = "";
            else if ("true".equals(showCode) || "代码居左".equals(showCode))
                showCode = "l";
            else if ("代码居右".equals(showCode))
                showCode = "r";

            String privisql = "";
            if ("true".equals(needCorp))
                privisql = invoker.getCorpPriviSQL(accID);// 取得集团权限条件语句

            SelectOtherDataSource sods = null;// 其他数据源
            if (dataParam == null)
                dataParam = "";
            if (!dataParam.equals("undefined") && !dataParam.equals("")) {
                String[] dataParamArray = dataParam.split(",");
                sods = new SelectOtherDataSource(dataParamArray);
            }
            if (otherCondi.equals("undefined"))
                otherCondi = "";
            if (!otherCondi.equals("")) {
                otherCondi = URLDecoder.decode(otherCondi, "UTF-8");
            }

            String xmlData = "";
            if (sods == null) {// 代码中心
                String tableName = PubFunc.getCodeCenterTableNameByAlias(tableAlias);
                xmlData = invoker.reBuildCondiTreeFromCodeCenter(accID, tableName, otherCondi, returnFields, filterCondi, needCorp);
            } else {// 其他数据源
                // xmlData = invoker.reBuildCondiTreeFromOtherSource(sods,
                // accID, tableAlias, otherCondi, returnFields,
                // filterCondi);
                // 其他数据源,目前没有逐级加载的树需要模糊定位，所以暂时不实现
            }
            xmlData = invoker.getReturnDataByXml(xmlData, "false", "", "", "querydatas", "querydata", showCode, selectType, "", null,null);

            response.setCharacterEncoding("UTF-8");
            response.setContentType("text/javascript");
            response.getWriter().write(xmlData);
        } catch (Exception e) {
            response.setCharacterEncoding("UTF-8");
            response.setContentType("text/javascript");
            String errormsg = e.toString().replaceAll("\n", "");
            System.out.println(e.toString());
            response.getWriter().write("errormessage:<message>" + errormsg + "</message>");
        }
    }

    public void getStyleSize(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response) throws Exception {

        String styleId = request.getParameter("styleId");
        try {
            // 获得查询结果
            HashMap map = invoker.getStyleSize(styleId);
            response.setCharacterEncoding("UTF-8");
            response.setContentType("text/javascript");
            String width = map.get("width").toString();
            String height = map.get("height").toString();
            String retStr = "width:" + width + ";height:" + height;
            response.getWriter().write(retStr);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // 从requset中获取自定义查询的参数放到paramsMap中
    public void savePostParams(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, String> paramMap = PubFunc.getParamsMap(request);
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        Object temp = request.getSession().getAttribute("postParmas");
        Map<String, Map<String, String>> postParmas = null;
        if (temp == null) {
            postParmas = new HashMap<String, Map<String, String>>();
            request.getSession().setAttribute("postParmas", postParmas);
        } else {
            postParmas = (Map<String, Map<String, String>>) temp;
        }
        postParmas.put(uuid, paramMap);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(uuid);
    }

    // 保存生命周期 true 为sesson时间，false取一次
    public void keepParam(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response) throws Exception {
        String flag = request.getParameter("flag");
        HttpSession session = request.getSession();
        if ("true".equals(flag)) {
            session.setAttribute("keepParam", true);
        } else {
            session.removeAttribute("keepParam");
            session.removeAttribute("postParmas");
        }
    }

    // 获取某个控件
    public void getCom(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/javascript");
        String resultStyleID = request.getParameter("resultStyleID");
        if (StringUtils.nullOrBlank(resultStyleID)) {
            throw new RuntimeException("条件样式为空!");
        }
        try {
            HashMap<String, String> map = invoker.load("", resultStyleID);
            String resultXmlData = map.get("resultXml").toString();
            if (StringUtils.nullOrBlank(resultXmlData)) {
                throw new RuntimeException("获取样式xml为空!");
            }
            Document doc = DocumentHelper.parseText(resultXmlData);
            Element comEle = null;
            String id = request.getParameter("id");
            if (id != null) {
                comEle = (Element) doc.selectSingleNode("//property[Name='+id+']");
            } else {
                String type = request.getParameter("type");
                if (type != null) {
                    comEle = (Element) doc.selectSingleNode("//*[@classname='" + type + "']");
                }
            }
            if (comEle == null) {
                throw new RuntimeException("获取不到改控件");
            }
            response.getWriter().write(comEle.asXML());
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("errorMessage:" + e.getMessage() + "-->" + e.getCause());
            return;
        }
    }

    // 获取某个控件
    public void getExpandCols(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/javascript");
        String colAlias = request.getParameter("colAlias");
        String layerAlias = request.getParameter("layerAlias");
        String prefix = request.getParameter("prefix");
        if (StringUtils.nullOrBlank(colAlias)||StringUtils.nullOrBlank(layerAlias)) {
            throw new RuntimeException("展列表别名为空");
        }
        try {
            response.getWriter().write( invoker.getExpandCols(layerAlias,colAlias,prefix));
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("errorMessage:" + e.getMessage() + "-->" + e.getCause());
            return;
        }
    }

    public void querylistsimplequery(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/javascript");
        try {
            int totalRecord = 0;
            String totalStr = request.getParameter("total");
            if (StringUtils.isNumberString(totalStr)) {
                totalRecord = Integer.parseInt(totalStr);
            }
            if (totalRecord <= 0) {
                return;
            }
            String SumDescDisplayField = request.getParameter("SumDescDisplayField");
            if (SumDescDisplayField.equals("undefined"))
                SumDescDisplayField = "";

            String grid_fields = request.getParameter("grid_fields");
            String[] fieldsarray = grid_fields.split(",");
            int page = 1;
            int totalPage = 1;
            int pageSize = 1000;
            Hashtable<String, String> paramHt = PubFunc.getQueryParams(request);
            paramHt.put("pageSize", pageSize + "");
            totalPage = totalRecord / pageSize + (totalRecord % pageSize == 0 ? 0 : 1);
            StringBuffer sf = new StringBuffer();
            sf.append("[");
            for (; page <= totalPage; page++) {
                long beg = System.currentTimeMillis();
                paramHt.put("page", page + "");
                Hashtable<String, String> msg = ServiceInvokerUtil.invoker(paramHt);
                long end = System.currentTimeMillis();
                System.out.println(end - beg);
                String result = (String) msg.get("XML");
                result = result.replaceAll("0x0d", "").replaceAll("0x0a", "").replaceAll("&#2;", "").replaceAll("&#2", "");
                Document doc = DocumentHelper.parseText(result);
                String path = "/root/querydatas/querydata";
                List list = doc.selectNodes(path);
                if (list == null || list.size() <= 0) {
                    continue;
                }
                List<Element> fielddata = null;
                boolean hasSum = false;
                if (page == totalPage && !"".equals(SumDescDisplayField)) {
                    Element sumdata = (Element) doc.selectSingleNode("/root/sumdata");
                    if (sumdata != null) {
                        fielddata = sumdata.elements("fielddata");
                        if (fielddata.size() > 0) {
                            list.add(sumdata);
                            hasSum = true;
                        }
                    }
                }
                for (int i = 0; i < list.size(); i++) {
                    Element ele = (Element) list.get(i);
                    if (page == 1 && i > 0 || page > 1)
                        sf.append(",");
                    sf.append("{");
                    sf.append("\"" + "rowid" + "\"" + ":").append("\"" + "row_" + (page - 1) * pageSize + i + "\"" + ",");
                    sf.append("\"" + "col" + "\"" + ":");
                    sf.append("[");
                    for (int j = 0; j < fieldsarray.length; j++) {
                        if (j > 0)
                            sf.append(",");
                        String fieldname = fieldsarray[j];
                        String value = "";
                        if (hasSum && i == (list.size() - 1)) {
                            if (fieldname.equalsIgnoreCase(SumDescDisplayField)) {
                                value = "总计";
                            } else {
                                for (Element f : fielddata) {
                                    String temp = f.elementText("fieldname");
                                    if (fieldname.equalsIgnoreCase(temp)) {
                                        value = f.elementText("fieldvalue");
                                        break;
                                    }
                                }
                            }

                        } else {
                            value = ele.elementText(fieldname);
                        }
                        sf.append("\"").append(value).append("\"");
                    }
                    sf.append("]");
                    sf.append("}");

                }

            }
            sf.append("]");
            response.setCharacterEncoding("utf-8");
            response.getWriter().write(sf.toString());
        } catch (Exception e) {
            e.printStackTrace();
            response.setCharacterEncoding("utf-8");
            response.getWriter().write("Errormsg:" + e.getMessage());
        }
    }
}
