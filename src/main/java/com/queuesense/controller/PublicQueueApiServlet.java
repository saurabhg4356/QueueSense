package com.queuesense.controller;

import com.google.gson.Gson;
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

@WebServlet(name = "PublicQueueApiServlet", urlPatterns = {"/api/public/queue"})
public class PublicQueueApiServlet extends HttpServlet {

    private final QueueService queueService = new QueueService();
    private final CounterDao counterDao = new CounterDao();
    private final TokenDao tokenDao = new TokenDao();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<QueueItemDto> waiting = queueService.getLiveQueueDisplayItems();
        List<Counter> counters = counterDao.findAll();

        Map<String, Object> servingData = new HashMap<>();
        for (Counter c : counters) {
            tokenDao.findCurrentServingTokenForCounter(c.getCounterId()).ifPresent(t -> {
                Map<String, String> tokenInfo = new HashMap<>();
                tokenInfo.put("tokenNumber", t.getTokenNumber());
                tokenInfo.put("serviceName", t.getServiceName());
                tokenInfo.put("counterName", c.getCounterName());
                tokenInfo.put("counterNumber", String.valueOf(c.getCounterNumber()));
                tokenInfo.put("status", t.getStatus().name());
                servingData.put("counter_" + c.getCounterNumber(), tokenInfo);
            });
        }

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("waitingTokens", waiting);
        responseMap.put("serving", servingData);

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(gson.toJson(responseMap));
    }
}
