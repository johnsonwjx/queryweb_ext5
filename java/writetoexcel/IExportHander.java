package writetoexcel;

import org.dom4j.Element;

import java.util.List;

public interface IExportHander {
	void readDatas(List<Element> datas) throws Exception;

	void writeData() throws Exception;

	void setSumRecord(Element sumEle, String sumDescDisplayField) throws Exception;

}
