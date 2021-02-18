package com.cybertek.controller;

import com.cybertek.annotation.DefaultExceptionMessage;
import com.cybertek.dto.ProjectDTO;
import com.cybertek.dto.TaskDTO;
import com.cybertek.dto.UserDTO;
import com.cybertek.entity.Project;
import com.cybertek.entity.ResponseWrapper;
import com.cybertek.enums.Status;
import com.cybertek.service.ProjectService;
import com.cybertek.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/v1/project")
@Tag(name = "Project Controller", description = "Project API")
public class ProjectController {


    ProjectService projectService;
    UserService userService;

    public ProjectController(ProjectService projectService, UserService userService) {
        this.projectService = projectService;
        this.userService = userService;
    }

    @GetMapping
    @Operation(summary = "Read all project")
    @DefaultExceptionMessage(defaultMassage = "Something went wrong, try again!")
    @PreAuthorize("hasAnyAuthority('Admin','Manager')")
    public ResponseEntity<ResponseWrapper> readAll(){

        List<ProjectDTO> projectDTOS = projectService.listAllProjects();
        return ResponseEntity.ok(new ResponseWrapper("Projects are retrieved",projectDTOS));
    }

    @GetMapping("/{projectcode}")
    @Operation(summary = "Read by project code")
    @DefaultExceptionMessage(defaultMassage = "Something went wrong. try again!")
    @PreAuthorize("hasAnyAuthority('Admin','Manager')")
    public ResponseEntity<ResponseWrapper> readByProjectCode(@PathVariable("projectcode") String projectcode){

        ProjectDTO projectDTO = projectService.getByProjectCode(projectcode);
        return ResponseEntity.ok(new ResponseWrapper("Project is retrieved",projectDTO));
    }






//    @GetMapping("/manager/complete/{projectCode}")
//    public String completeProjectStatus(@PathVariable("projectCode") String projectCode){
//        projectService.changeProjectStatusToComplete(projectService.findById(projectCode));
//
//        return "redirect:/project/manager/complete";
//    }
//
//    @GetMapping("/archived")
//    public String archivedProjects(Model model){
//
//        model.addAttribute("projects",projectService.completedProjects());
//
//        return "/project/archived";
//    }
//
//
//
//
//    List<ProjectDTO> getCountedListOfProjectDTO(UserDTO manager){
//
//        List<ProjectDTO> list =projectService.findAll().stream()
//                .filter(project -> project.getAssignedManager().equals(manager))
//                .map(x -> {
//                    List<TaskDTO> taskList = taskService.findTaskByManager(manager);
//
//                    int completeCount =(int) taskList.stream()
//                            .filter(task -> task.getProject().equals(x) && task.getTaskStatus() == Status.COMPLETE)
//                            .count();
//                    int inCompleteCount =(int) taskList.stream()
//                            .filter(task -> task.getProject().equals(x) && task.getTaskStatus() != Status.COMPLETE)
//                            .count();
//
//                    x.setCompleteTaskCount(completeCount);
//                    x.setUnfinishedTaskCount(inCompleteCount);
//
//                    return x;
////                    return new ProjectDTO(x.getProjectName(), x.getProjectCode(),
////                            userService.findById(x.getAssignedManager().getUserName()),
////                            x.getStartDate(), x.getEndDate(), x.getProjectDetail(), x.getProjectStatus(),
////                            completeCount, inCompleteCount);
//                }).collect(Collectors.toList());
//
//        return list;
//
//    }






}
