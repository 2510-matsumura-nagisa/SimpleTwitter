package chapter6.controller;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import chapter6.beans.Message;
import chapter6.beans.User;
import chapter6.logging.InitApplication;
import chapter6.service.MessageService;

@WebServlet(urlPatterns = { "/deleteMessage" })
public class DeleteMessageServlet extends HttpServlet {

    Logger log = Logger.getLogger("twitter");

    public DeleteMessageServlet() {
        InitApplication application = InitApplication.getInstance();
        application.init();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

  	  log.info(new Object(){}.getClass().getEnclosingClass().getName() +
  	        " : " + new Object(){}.getClass().getEnclosingMethod().getName());

        HttpSession session = request.getSession();


        // ログインユーザー取得（session）
        User user = (User) session.getAttribute("loginUser");

        // つぶやきID取得
        int messageId = Integer.parseInt(request.getParameter("messageId"));

        MessageService messageService = new MessageService();
        Message message = messageService.select(messageId);

		// つぶやきが存在しない場合、投稿者とログインユーザーが一致しない場合リダイレクト
        if (message == null || message.getUserId() != user.getId()) {
            response.sendRedirect("./");
            return;
        }

        // 削除処理
        messageService.delete(messageId);

        // 一覧へリダイレクト
        response.sendRedirect("./");
    }
}