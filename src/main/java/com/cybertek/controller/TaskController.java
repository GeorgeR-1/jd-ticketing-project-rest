package com.cybertek.controller;

import com.cybertek.annotation.DefaultExceptionMessage;
import com.cybertek.dto.TaskDTO;
import com.cybertek.entity.ResponseWrapper;
import com.cybertek.exception.TicketingProjectException;
import com.cybertek.service.ProjectService;
import com.cybertek.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/task")
public class TaskController {

    private TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    @DefaultExceptionMessage(defaultMassage = "Something went wrong, please try again!")
    @Operation(summary = "Read all task")
    @PreAuthorize("hasAuthority('Manager')")
    public ResponseEntity<ResponseWrapper> readAll(){
        return ResponseEntity.ok(new ResponseWrapper("Succesfully retrieved all tasks",taskService.listAllTasks()));
    }

    @GetMapping("/project-manager")
    @DefaultExceptionMessage(defaultMassage = "Something went wrong, please try again!")
    @Operation(summary = "Read all tasks by project manager")
    @PreAuthorize("hasAuthority('Manager')")
    public ResponseEntity<ResponseWrapper> readAllByProjectManager() throws TicketingProjectException {

        List<TaskDTO> taskList = taskService.listAllTaskByProjectManager();
        return ResponseEntity.ok(new ResponseWrapper("Successfully retrieved tasks by project manager",taskList));
    }

    @GetMapping("/{id}")
    @DefaultExceptionMessage(defaultMassage = "Something went wrong, please try again!")
    @Operation(summary = "Read all tasks by id")
    @PreAuthorize("hasAnyAuthority('Manager','Employee')")
    public ResponseEntity<ResponseWrapper> readById(@PathVariable("id") Long id) throws TicketingProjectException {

        TaskDTO currentTask = taskService.findById(id);
        return ResponseEntity.ok(new ResponseWrapper("Successfully retrieved task",currentTask));
    }

    @PostMapping
    @DefaultExceptionMessage(defaultMassage = "Something went wrong, please try again!")
    @Operation(summary = "Create a new task")
    @PreAuthorize("hasAnyAuthority('Manager')")
    public ResponseEntity<ResponseWrapper> create(@RequestBody TaskDTO task){

        TaskDTO createdTask = taskService.save(task);
        return ResponseEntity.ok(new ResponseWrapper("Successfully task created",createdTask));
    }

    @DeleteMapping("/{id}")
    @DefaultExceptionMessage(defaultMassage = "Something went wrong, please try again!")
    @Operation(summary = "Delete a task")
    @PreAuthorize("hasAnyAuthority('Manager')")
    public ResponseEntity<ResponseWrapper> delete(@PathVariable("id") Long id) throws TicketingProjectException {
        taskService.delete(id);
        return ResponseEntity.ok(new ResponseWrapper("Successfully deleted"));
    }

    @PutMapping("/{id}")
    @DefaultExceptionMessage(defaultMassage = "Something went wrong, please try again!")
    @Operation(summary = "Update task")
    @PreAuthorize("hasAnyAuthority('Manager')")
    public ResponseEntity<ResponseWrapper> updateTask(@RequestBody TaskDTO task) throws TicketingProjectException {
        TaskDTO updatedTask = taskService.update(task);

    }


}
