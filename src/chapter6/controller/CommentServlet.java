package chapter6.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;

import chapter6.beans.Comment;
import chapter6.beans.User;
import chapter6.logging.InitApplication;
import chapter6.service.CommentService;

@WebServlet(urlPatterns = { "/comment" })

public class CommentServlet extends HttpServlet {

	/**
	* ロガーインスタンスの生成
	*/
	Logger log = Logger.getLogger("twitter");

	/**
	* デフォルトコンストラクタ
	* アプリケーションの初期化を実施する。
	*/
	public CommentServlet() {
		InitApplication application = InitApplication.getInstance();
		application.init();
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
	            throws IOException, ServletException {

		  log.info(new Object(){}.getClass().getEnclosingClass().getName() +
	        " : " + new Object(){}.getClass().getEnclosingMethod().getName());

	        HttpSession session = request.getSession();
	        List<String> errorMessages = new ArrayList<String>();

	        String text = request.getParameter("text");
	        if (!isValid(text, errorMessages)) {
	            session.setAttribute("errorMessages", errorMessages);
	            response.sendRedirect("./");
	            return;
	        }

	        Comment comment = new Comment();
	        comment.setText(text);

	        User user = (User) session.getAttribute("loginUser");
	        comment.setUserId(user.getId());

	        String messageIdStr = request.getParameter("messageId");
	        int messageId =  Integer.parseInt(messageIdStr);
	        comment.setMessageId(messageId);

	        new CommentService().insert(comment);
	        response.sendRedirect("./");
	}

	private boolean isValid(String text, List<String> errorMessages) {
		// TODO 自動生成されたメソッド・スタブ
		log.info(new Object() {
		}.getClass().getEnclosingClass().getName() +
				" : " + new Object() {
				}.getClass().getEnclosingMethod().getName());

		if (StringUtils.isBlank(text)) {
			errorMessages.add("メッセージを入力してください");
		} else if (140 < text.length()) {
			errorMessages.add("140文字以下で入力してください");
		}

		if (errorMessages.size() != 0) {
			return false;
		}
		return true;
	}
}
