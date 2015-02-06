package logic;

import youngfriend.common.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import java.io.IOException;

public class MyTag extends SimpleTagSupport {
	@Override
	public void doTag() throws JspException, IOException {
		PageContext context = (PageContext) this.getJspContext();
		HttpServletRequest request=(HttpServletRequest) context.getRequest();
		context.getOut().write(StringUtils.getUrlRootInfo(request.getRequestURL().toString()));
	}
}
