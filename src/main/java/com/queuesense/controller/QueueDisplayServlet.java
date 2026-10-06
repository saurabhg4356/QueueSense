package com.queuesense.controller;

import com.queuesense.dao.CounterDao;
import com.queuesense.dao.TokenDao;
import com.queuesense.dto.QueueItemDto;
import com.queuesense.model.Counter;
import com.queuesense.model.Token;
import com.queuesense.service.QueueService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name = "QueueDisplayServlet", urlPatterns = {"/queue-display"})
public class QueueDisplayServlet extends HttpServlet {

    private final QueueService queueService = new QueueService();
    private final CounterDao counterDao = new CounterDao();
    private final TokenDao tokenDao = new TokenDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<QueueItemDto> waitingTokens = queueService.getLiveQueueDisplayItems();
        List<Counter> counters = counterDao.findAll();

        Map<Long, Token> servingMap = new HashMap<>();
        for (Counter c : counters) {
            tokenDao.findCurrentServingTokenForCounter(c.getCounterId())
                    .ifPresent(t -> servingMap.put(c.getCounterId(), t));
        }

        req.setAttribute("waitingTokens", waitingTokens);
        req.setAttribute("counters", counters);
        req.setAttribute("servingMap", servingMap);

        req.getRequestDispatcher("/queue-display.jsp").forward(req, resp);
    }
}
