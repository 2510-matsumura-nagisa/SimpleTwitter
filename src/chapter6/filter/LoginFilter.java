package chapter6.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import chapter6.beans.User;

/**
 * Servlet Filter implementation class LoginFilter
 */
@WebFilter(urlPatterns = { "/edit", "/setting" })
public class LoginFilter implements Filter {

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		// 型変換(汎用→Http専用)
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;
		List<String> errorMessages = new ArrayList<String>();

		User user = (User) httpRequest.getSession().getAttribute("loginUser");

		// ログインユーザーが存在すれば、サーブレットを実行
		if (user != null) {
			chain.doFilter(request, response);
		// 存在しなければ、エラーメッセージ+ログイン画面にリダイレクト
		} else {
			errorMessages.add("ログインしてください");
			httpRequest.getSession().setAttribute("errorMessages", errorMessages);
			httpResponse.sendRedirect("./login");
		}

	}

	@Override
	public void init(FilterConfig fConfig) throws ServletException {

	}

	@Override
	public void destroy() {
	}

}
