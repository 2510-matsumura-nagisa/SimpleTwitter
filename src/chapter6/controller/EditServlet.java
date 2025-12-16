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

import chapter6.beans.Message;
import chapter6.beans.User;
import chapter6.logging.InitApplication;
import chapter6.service.MessageService;

@WebServlet(urlPatterns = { "/edit" })

public class EditServlet extends HttpServlet {

	/**
	* ロガーインスタンスの生成
	*/
	Logger log = Logger.getLogger("twitter");

	/**
	* デフォルトコンストラクタ
	* アプリケーションの初期化を実施する。
	*/
	public EditServlet() {
		InitApplication application = InitApplication.getInstance();
		application.init();
	}

	// つぶやき編集画面の表示
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {

		log.info(new Object() {
		}.getClass().getEnclosingClass().getName() +
				" : " + new Object() {
				}.getClass().getEnclosingMethod().getName());

		List<String> errorMessages = new ArrayList<String>();
		String id = request.getParameter("messageId");
		HttpSession session = request.getSession();

		// URLのidが空白または数字では無い場合にエラーメッセージを表示
		if (StringUtils.isBlank(id) || !id.matches("^[0-9]+$")) {
			errorMessages.add("不正なパラメータが入力されました");
			session.setAttribute("errorMessages", errorMessages);
			response.sendRedirect("./");
			return;
		}

		int messageId = Integer.parseInt(id);
		Message message = new MessageService().select(messageId);

		// URLのidのつぶやきが存在しない場合にエラーメッセージを表示
		if (message == null) {
			errorMessages.add("不正なパラメータが入力されました");
			session.setAttribute("errorMessages", errorMessages);
			response.sendRedirect("./");
			return;
		}

		// つぶやきの投稿者以外はトップ画面にリダイレクト
		User user = (User) session.getAttribute("loginUser");
		if (user == null || message.getUserId() != user.getId()) {
			response.sendRedirect("./");
			return;
		}

		// つぶやきの編集画面を表示
		request.setAttribute("message", message);
		request.getRequestDispatcher("edit.jsp").forward(request, response);
	}

	// つぶやきの更新
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {

		log.info(new Object() {
		}.getClass().getEnclosingClass().getName() +
				" : " + new Object() {
				}.getClass().getEnclosingMethod().getName());

		HttpSession session = request.getSession();
		List<String> errorMessages = new ArrayList<String>();

		// ログインユーザー取得（session）
		User user = (User) session.getAttribute("loginUser");

		// ログインしていない場合トップ画面にリダイレクト
		if (user == null) {
			response.sendRedirect("./");
			return;
		}

		// つぶやきのid,text取得
		String id = request.getParameter("messageId");

		int messageId = Integer.parseInt(id);
		MessageService messageService = new MessageService();
		Message message = messageService.select(messageId);

		// つぶやきが存在しない場合、投稿者とログインユーザーが一致しない場合トップ画面にリダイレクト
		if (message == null || message.getUserId() != user.getId()) {
			response.sendRedirect("./");
			return;
		}

		String text = request.getParameter("text");

		if (!isValid(text, errorMessages)) {
			message.setText(text);
			session.setAttribute("errorMessages", errorMessages);
			request.setAttribute("message", message);
			request.getRequestDispatcher("edit.jsp").forward(request, response);
			return;
		}

		// つぶやきを更新し、トップ画面にリダイレクト
		message.setText(text);
		messageService.update(message);
		response.sendRedirect("./");
	}

	private boolean isValid(String text, List<String> errorMessages) {

		log.info(new Object() {
		}.getClass().getEnclosingClass().getName() +
				" : " + new Object() {
				}.getClass().getEnclosingMethod().getName());

		if (StringUtils.isBlank(text)) {
			errorMessages.add("入力してください");
		} else if (140 < text.length()) {
			errorMessages.add("140文字以下で入力してください");
		}

		if (errorMessages.size() != 0) {
			return false;
		}
		return true;
	}
}