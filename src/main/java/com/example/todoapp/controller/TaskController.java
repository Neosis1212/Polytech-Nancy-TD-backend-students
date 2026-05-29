package com.example.todoapp.controller;

import com.example.todoapp.JsonUtils;
import com.example.todoapp.dto.ErrorDto;
import com.example.todoapp.dto.TaskRequestDto;
import com.example.todoapp.dto.TaskResponseDto;
import com.example.todoapp.service.TaskService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.nonNull;

public class TaskController implements HttpHandler {

    private static final Logger log = LoggerFactory.getLogger(TaskController.class);
    private static final Pattern ID_PATH = Pattern.compile("^/tasks/([0-9]+)$");
    private static final TaskService taskService = new TaskService();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            if ("POST".equals(method) && "/tasks".equals(path)) {
                String body = new String(exchange.getRequestBody().readAllBytes(), UTF_8);
                TaskRequestDto input = JsonUtils.deserialize(body, TaskRequestDto.class);

                ErrorDto error = validateDto(input);
                if (nonNull(error)) {
                    sendResponse(exchange, 400, JsonUtils.serialize(error));
                    return;
                }

                TaskResponseDto createdTask = taskService.createTask(input);
                exchange.getResponseHeaders().add("Location", "/tasks/" + createdTask.id());
                sendResponse(exchange, 201, JsonUtils.serialize(createdTask));
                return;
            }

            if ("GET".equals(method) && "/tasks".equals(path)) {
                List<TaskResponseDto> tasks = taskService.getAllTasks();
                String query = exchange.getRequestURI().getQuery();
                if (nonNull(query) && query.contains("todo-only=true")) {
                    tasks = tasks.stream().filter(t -> !t.done()).toList();
                }
                if (tasks.isEmpty()) {
                    sendResponse(exchange, 204, null);
                } else {
                    sendResponse(exchange, 200, JsonUtils.serialize(tasks));
                }
                return;
            }

            Matcher m = ID_PATH.matcher(path);

            if ("GET".equals(method) && m.matches()) {
                int id = Integer.parseInt(m.group(1));
                Optional<TaskResponseDto> task = taskService.getTaskById(id);

                if (task.isPresent()) {
                    sendResponse(exchange, 200, JsonUtils.serialize(task.get()));
                } else {
                    sendResponse(exchange, 404, null);
                }
                return;
            }

            if ("DELETE".equals(method) && m.matches()) {
                int id = Integer.parseInt(m.group(1));
                if (taskService.deleteTask(id)) {
                    sendResponse(exchange, 204, null);
                } else {
                    sendResponse(exchange, 404, null);
                }
                return;
            }

            if ("PUT".equals(method) && m.matches()) {
                int id = Integer.parseInt(m.group(1));
                if (taskService.getTaskById(id).isEmpty()) {
                    sendResponse(exchange, 404, null);
                    return;
                }
                String body = new String(exchange.getRequestBody().readAllBytes(), UTF_8);
                TaskRequestDto input = JsonUtils.deserialize(body, TaskRequestDto.class);

                ErrorDto error = validateDto(input);
                if (nonNull(error)) {
                    sendResponse(exchange, 400, JsonUtils.serialize(error));
                    return;
                }

                taskService.updateTask(id, input);
                sendResponse(exchange, 204, null);
                return;
            }

            sendResponse(exchange, 404, null);

        } catch (Exception e) {
            log.error("Une erreur inattendue s'est produite lors du traitement de la requête", e);
            sendResponse(exchange, 500, null);
        }
    }

    private ErrorDto validateDto(TaskRequestDto dto) {
        if (dto.title() == null || dto.title().isBlank()) {
            return new ErrorDto("title", "Le titre est obligatoire.");
        }
        if (dto.title().length() > 50) {
            return new ErrorDto("title", "Le titre ne doit pas dépasser 50 caractères.");
        }
        if (dto.description() != null && dto.description().length() > 255) {
            return new ErrorDto("description", "La description ne doit pas dépasser 255 caractères.");
        }
        return null;
    }

    private void sendResponse(HttpExchange exchange, int status, String json) throws IOException {
        if(nonNull(json)) {
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
            byte[] bytes = json.getBytes(UTF_8);
            exchange.sendResponseHeaders(status, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        } else {
            exchange.sendResponseHeaders(status, -1);
            exchange.close();
        }
    }
}