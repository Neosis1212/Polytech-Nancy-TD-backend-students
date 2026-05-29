package com.example.todoapp.service;

import com.example.todoapp.dao.TaskDao;
import com.example.todoapp.dto.TaskRequestDto;
import com.example.todoapp.dto.TaskResponseDto;
import com.example.todoapp.model.Task;

import java.util.List;
import java.util.Optional;

public class TaskService {
    private final TaskDao taskDao = new TaskDao();

    public TaskResponseDto createTask(TaskRequestDto dto) {
        // ID à 0 pour l'insertion, done à false par défaut
        Task task = new Task(0, dto.title(), dto.description(), false);
        Task saved = taskDao.save(task);
        return mapToResponseDto(saved);
    }

    public List<TaskResponseDto> getAllTasks() {
        return taskDao.findAll().stream()
                .map(this::mapToResponseDto)
                .toList();
    }

    public Optional<TaskResponseDto> getTaskById(int id) {
        return taskDao.findById(id).map(this::mapToResponseDto);
    }

    public boolean deleteTask(int id) {
        return taskDao.delete(id);
    }

    public void updateTask(int id, TaskRequestDto dto) {
        // done est fourni dans le PUT, si il est null on met false par défaut
        boolean isDone = dto.done() != null && dto.done();
        Task task = new Task(id, dto.title(), dto.description(), isDone);
        taskDao.save(task);
    }

    private TaskResponseDto mapToResponseDto(Task task) {
        return new TaskResponseDto(task.getId(), task.getTitle(), task.getDescription(), task.isDone());
    }
}