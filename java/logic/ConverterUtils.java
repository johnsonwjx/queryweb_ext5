/**
 * 
 */
package logic;

import org.dom4j.Element;


/**
 * @author XDY
 *
 */
public class ConverterUtils {

	/**
	 * 
	 */
	public ConverterUtils() {
		// TODO Auto-generated constructor stub
	}
	
	public static String getENOperatorValueByCNValue(String cnOperator) throws Exception{
		if (cnOperator == null || "".equals(cnOperator) || "×óÆ¥Åä".equals(cnOperator))
			return "Llike";
		else if ("È«Æ¥Åä".equals(cnOperator))
			return "Alike";
		else
			return "=";
	}
	
	
	public static void addNewTreeCondiElement(Element root,Element conNode,String elementName,String elementText,
			String opera,String factFieldName) throws Exception{
		conNode = root.addElement("condifields").addElement("condifield").addElement(elementName);
		conNode.addAttribute("type", opera);
		conNode.addAttribute("factfieldname", factFieldName);
		conNode.addText(elementText);
	}
	
}
