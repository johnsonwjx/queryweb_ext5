package writetoexcel;

import youngfriend.common.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public enum GridUtils {
	;
	public static List<GridColumn> initBottomColumns(GridColumn[] columns) {
		List<GridColumn> bgs = new ArrayList<GridColumn>();
		if (columns == null || columns.length <= 0) {
			return bgs;
		}
		for (GridColumn c : columns) {
			if (c.getColumns() != null && c.getColumns().size() > 0) {
				getBottomColumn(c, bgs);
			} else {
				bgs.add(c);
			}
		}
		return bgs;
	}

	public static void getBottomColumn(GridColumn parent, List<GridColumn> bottomG) {
		if (bottomG == null) {
			return;
		}
		List<GridColumn> cs = parent.getColumns();
		for (GridColumn c : cs) {
			c.setParent(parent);
			if (c.getColumns() != null && c.getColumns().size() > 0) {
				getBottomColumn(c, bottomG);
			} else {
				bottomG.add(c);
			}
		}
	}

    public static String getNiewValueFromMaping(GridColumn col, String value) {
        boolean isNum=!col.getType().equals("string")&&StringUtils.isNumberString(value) ;
        if(col.getValueMapping()!=null&&!StringUtils.nullOrBlank(col.getValueMapping().trim())){
            String[] arr = col.getValueMapping().trim().split(",");
            if (arr.length <= 0) {
                return value;
            }
            for (String str : arr) {
                String[] temp = str.split("=");
                if (temp.length != 2) {
                    continue;
                }
                String key=temp[1] ;
                if(!key.startsWith(":")){
                    key=":"+key;
                }
                if(!key.endsWith(":")){
                    key+=":" ;
                }
                if(key.toLowerCase().indexOf((":"+value+":").toLowerCase())!=-1){
                    return temp[0] ;
                }else{
                    if(key.indexOf(":yfnotnull:")!=-1&&(!StringUtils.nullOrBlank(value)||(isNum&&Integer.parseInt(value)!= 0))){
                        return temp[0] ;
                    }else if(key.indexOf(":yfnull:")!=-1&&(StringUtils.nullOrBlank(value)||(isNum&&Integer.parseInt(value)== 0))){
                        return temp[0] ;
                    }
                }
            }
        }else{
            if("flow_finishstatus".equalsIgnoreCase(col.getDataIndex())){
                if ("1".equals(value)) {
                    return "已完成";
                } else if ("0".equals(value))
                    return "审批中";
                else if ("q".equals(value.toLowerCase()))
                    return "驳回";
            }
        }
        return value ;
    }
}
