package logic;

import org.dom4j.Element;
import youngfriend.common.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TreeNodeList {

    public static List<TreeNodeInfo> parseListToNodeTreeList(Map<String, Element> codeMap, List<Integer> codeRule) {
        List<TreeNodeInfo> treeList = new ArrayList<TreeNodeInfo>();
        TreeNodeInfo parentNode = null;
        for (String code : codeMap.keySet()) {
            TreeNodeInfo curNode = new TreeNodeInfo();
            curNode.setCode(code);
            curNode.setParentNode(null);
            curNode.setEleInfo(codeMap.get(code));
            curNode.setSubNodeList(new ArrayList<TreeNodeInfo>());
            if (parentNode != null) {
                while (parentNode != null) {
                    String pCode = parentNode.getCode();
                    if (code.startsWith(pCode)) {
                        break;
                    }
                    parentNode = parentNode.getParentNode();
                }
            }
            if (parentNode == null) {
                treeList.add(curNode);
            } else {
                    if(codeRule!=null&&!codeRule.isEmpty()){
                        int pCodeLen=parentNode.getCode().length() ;
                        if(!codeRule.contains(pCodeLen)){
                            //父长度没在规则
                            while((parentNode=parentNode.getParentNode())!=null){
                                pCodeLen=parentNode.getCode().length() ;
                                if(codeRule.contains(pCodeLen)){
                                    break;
                                }
                            }
                        }
                    }
                    if(parentNode!=null){
                        parentNode.getSubNodeList().add(curNode);
                        curNode.setParentNode(parentNode);
                    }else{
                        treeList.add(curNode);
                    }
            }
            if(!StringUtils.nullOrBlank(code)){
                parentNode = curNode;
            }else{
                parentNode=null;
            }
        }
        return treeList;
    }

    public static List<TreeCorp> parseListTreeCorp(List<TreeCorp> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        List<TreeCorp> treeList = new ArrayList<TreeCorp>();
        TreeCorp parentNode = null;
        for (TreeCorp c : list) {
            if (parentNode != null) {
                while (parentNode != null) {
                    String pCode = parentNode.getCorpCode();
                    if (c.getCorpCode().startsWith(pCode)) {
                        break;
                    }
                    parentNode = parentNode.getParent();
                }
            }
            if (parentNode == null) {
                treeList.add(c);
            } else {
                parentNode.getChildren().add(c);
                c.setParent(parentNode);
            }
            parentNode = c;
        }
        return treeList;
    }

    public static List<TreeCorp> getBottomNode(List<TreeCorp> lst) {
        if (lst == null || lst.isEmpty()) {
            return null;
        }
        List<TreeCorp> bottoms = new ArrayList<TreeCorp>();
        for (TreeCorp node : lst) {
            if (node.getChildrenSize() <= 0) {
                bottoms.add(node);
            } else {
                List<TreeCorp> children = node.getChildren();
                List<TreeCorp> ccs = getBottomNode(children);
                if (ccs == null || ccs.isEmpty()) {
                    continue;
                } else {
                    bottoms.addAll(ccs);
                }
            }
        }
        return bottoms;
    }


}


class TreeNodeInfo {
    private TreeNodeInfo parentNode;
    private List<TreeNodeInfo> subNodeList;
    private String code;
    private Element eleInfo;

    public Element getEleInfo() {
        return eleInfo;
    }

    public void setEleInfo(Element eleInfo) {
        this.eleInfo = eleInfo;
    }

    public TreeNodeInfo getParentNode() {
        return parentNode;
    }

    public void setParentNode(TreeNodeInfo parentNode) {
        this.parentNode = parentNode;
    }

    public List<TreeNodeInfo> getSubNodeList() {
        return subNodeList;
    }

    public void setSubNodeList(List<TreeNodeInfo> subNodeList) {
        this.subNodeList = subNodeList;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}