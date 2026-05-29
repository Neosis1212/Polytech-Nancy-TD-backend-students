package com.example.todoapp;

import com.example.todoapp.controller.TaskController;
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

public class Application {

    private static final Logger log = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) throws Exception {
        log.info("Starting Application...");
        HttpServer server = HttpServer.create(new InetSocketAddress(8081), 0);
        server.createContext("/tasks", new TaskController());
        server.setExecutor(null);
        server.start();
        log.info("HTTP server started on http://localhost:8081");
    }
}