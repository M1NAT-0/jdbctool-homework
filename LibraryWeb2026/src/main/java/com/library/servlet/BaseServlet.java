package com.library.servlet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.library.common.Result;
import com.library.entity.User;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;

public abstract class BaseServlet extends HttpServlet {

    protected static final Gson GSON = new GsonBuilder()
            .serializeNulls()
            .registerTypeAdapter(LocalDateTime.class, new JsonSerializer<LocalDateTime>() {
                @Override
                public JsonElement serialize(LocalDateTime src, Type typeOfSrc, JsonSerializationContext context) {
                    return new JsonPrimitive(src == null ? null : src.toString());
                }
            })
            .registerTypeAdapter(LocalDate.class, new JsonSerializer<LocalDate>() {
                @Override
                public JsonElement serialize(LocalDate src, Type typeOfSrc, JsonSerializationContext context) {
                    return new JsonPrimitive(src == null ? null : src.toString());
                }
            })
            .create();

    protected void writeJson(HttpServletResponse resp, Object data) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(GSON.toJson(data));
    }

    protected User currentUser(HttpServletRequest req) {
        Object user = req.getSession().getAttribute("user");
        return user instanceof User ? (User) user : null;
    }

    protected boolean requireLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (currentUser(req) == null) {
            writeJson(resp, Result.fail(401, "未登录或登录已过期"));
            return false;
        }
        return true;
    }

    protected boolean requireRole(HttpServletRequest req, HttpServletResponse resp, String role) throws IOException {
        User user = currentUser(req);
        if (user == null) {
            writeJson(resp, Result.fail(401, "未登录或登录已过期"));
            return false;
        }
        if (!role.equals(user.getRole())) {
            writeJson(resp, Result.fail(403, "没有权限执行此操作"));
            return false;
        }
        return true;
    }

    protected JsonObject readJson(HttpServletRequest req) throws IOException {
        req.setCharacterEncoding("UTF-8");
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        if (sb.length() == 0) {
            return new JsonObject();
        }
        return JsonParser.parseString(sb.toString()).getAsJsonObject();
    }

    protected String jsonString(JsonObject body, String key) {
        return body.has(key) && !body.get(key).isJsonNull() ? body.get(key).getAsString() : null;
    }

    protected Integer jsonInt(JsonObject body, String key) {
        if (body.has(key) && !body.get(key).isJsonNull()) {
            try {
                return body.get(key).getAsInt();
            } catch (Exception e) {
                try {
                    String s = body.get(key).getAsString();
                    return s == null || s.isEmpty() ? null : Integer.parseInt(s.trim());
                } catch (Exception ignored) {
                    return null;
                }
            }
        }
        return null;
    }

    protected Double jsonDouble(JsonObject body, String key) {
        if (body.has(key) && !body.get(key).isJsonNull()) {
            try {
                return body.get(key).getAsDouble();
            } catch (Exception e) {
                try {
                    String s = body.get(key).getAsString();
                    return s == null || s.isEmpty() ? null : Double.parseDouble(s.trim());
                } catch (Exception ignored) {
                    return null;
                }
            }
        }
        return null;
    }
}
