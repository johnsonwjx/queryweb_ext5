package logic;

import youngfriend.common.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class TreeCorp {
    private String corpCode;
    private String corpName;
    private String corpId;
    private String isLeaf;
    private String showCode;
    private List<TreeCorp> children = new ArrayList<TreeCorp>();
    private TreeCorp parent;
    private String otherChildren="[]";

    public String getOtherChildren() {
        return otherChildren;
    }

    public int getChildrenSize() {
        return children.size();
    }

    public void setOtherChildren(String otherChildren) {
        this.otherChildren = otherChildren;
    }

    public TreeCorp getParent() {
        return parent;
    }

    public void setParent(TreeCorp parent) {
        this.parent = parent;
    }

    public void removeChild(TreeCorp node){
        children.remove(node);
    }
    public String getShowCode() {
        return showCode;
    }

    public List<TreeCorp> getChildren() {
        return children;
    }

    public void setChildren(List<TreeCorp> children) {
        this.children = children;
    }

    public void setShowCode(String showCode) {
        this.showCode = showCode;
    }

    private TreeCorp() {
    }

    private TreeCorp(String corpCode, String corpName, String corpId, String isLeaf, String showCode) {
        super();
        this.corpCode = corpCode;
        this.corpName = corpName;
        this.corpId = corpId;
        this.isLeaf = isLeaf;
        this.showCode = showCode;
    }

    public static TreeCorp parse(String info, String showCode) {
        if (StringUtils.nullOrBlank(info)) {
            return null;
        }
        String[] corpValueArr = info.split("\\*");
        if (corpValueArr.length < 4) {
            return null;
        }
        String corpCode = corpValueArr[0];// 集团代码
        String corpName = corpValueArr[1];// 集团名称
        String corpId = corpValueArr[2];// 集团ID
        String isleaf = corpValueArr[3];// 是否叶子
        TreeCorp node = new TreeCorp(corpCode, corpName, corpId, isleaf, showCode);
        return node;
    }

    public String getCorpCode() {
        return corpCode;
    }

    public void setCorpCode(String corpCode) {
        this.corpCode = corpCode;
    }

    public String getCorpName() {
        return corpName;
    }

    public void setCorpName(String corpName) {
        this.corpName = corpName;
    }

    public String getCorpId() {
        return corpId;
    }

    public void setCorpId(String corpId) {
        this.corpId = corpId;
    }

    public String getIsLeaf() {
        return isLeaf;
    }

    public void setIsLeaf(String isLeaf) {
        this.isLeaf = isLeaf;
    }

    public String getJson(String needDelay) {
        StringBuilder sb = new StringBuilder();
        boolean leaf = true;
        if (!"1".equals(isLeaf))
            leaf = false;
        StringBuilder corpInfo = new StringBuilder();
        corpInfo.append("'corpid':'").append(corpId).append("','corpcode':'").append(corpCode).append("','corpname':'").append(corpName);
        if ("true".equalsIgnoreCase(needDelay)) {
            corpInfo.append("',leaf:").append(leaf);
        }
        String temp = "";
        if ("r".equals(showCode)) {
            temp = "*','text':'" + corpName + "[" + corpCode + "]','name':'";
        } else if ("l".equals(showCode)) {
            temp = "*','text':'" + "[" + corpCode + "]" + corpName + "','name':'";
        } else {
            temp = "*','text':'" + corpName + "','name':'";
        }
        StringBuilder cb = new StringBuilder("' ");
        if (!"true".equalsIgnoreCase(needDelay)) {
            if (children != null && !children.isEmpty()) {
                cb.append(",'children' :  [");
                for (TreeCorp c : children) {
                    String cJson = c.getJson(needDelay);
                    cb.append(cJson).append(",");
                }
                cb.deleteCharAt(cb.length() - 1);
                cb.append(" ] ");
            } else {
                    cb.append(",'children' : ").append(otherChildren);
            }
        }
        sb.append("{'id':'").append(corpCode).append(temp).append("',").append(corpInfo).append(cb.toString()).append("}");
        return sb.toString();
    }

    public String toString() {
        return super.toString();
    }

    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((corpId == null) ? 0 : corpId.hashCode());
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TreeCorp other = (TreeCorp) obj;
        if (corpId == null) {
            if (other.corpId != null)
                return false;
        } else if (!corpId.equals(other.corpId))
            return false;
        return true;
    }

}