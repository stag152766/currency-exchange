package org.currency.exchange.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletResponse;

public class ResponseUtil {

     private static final String JSON_CONTENT_TYPE = "application/json";

     public static void sendJsonResponse(HttpServletResponse resp, int status, Object data) throws IOException {
          resp.setStatus(status);
          resp.setContentType(JSON_CONTENT_TYPE);
          String body = ObjectMapperUtil.getInstance().writeValueAsString(data);
          resp.getWriter().println(body);
     }

     public static void sendErrorResponse(HttpServletResponse resp, int status, String message) throws IOException {
          Map<String, String> errorResponse = new HashMap<>();
          errorResponse.put("error", message);
          sendJsonResponse(resp, status, errorResponse);
     }
}
