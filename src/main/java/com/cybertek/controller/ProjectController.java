package com.cybertek.controller;

import com.cybertek.dto.ProjectDTO;
import com.cybertek.dto.TaskDTO;
import com.cybertek.dto.UserDTO;
import com.cybertek.entity.Project;
import com.cybertek.enums.Status;
import com.cybertek.service.ProjectService;
import com.cybertek.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/project")
public class ProjectController {


    ProjectService projectService;
    UserService userService;

    public ProjectController(ProjectService projectService, UserService userService) {
        this.projectService = projectService;
        this.userService = userService;
    }

    //    @Autowired
//    TaskService taskService;
//
    @GetMapping("/create")
    public String createProject(Model model){

        model.addAttribute("project",new ProjectDTO());
        model.addAttribute("projects",projectService.listAllProjects());
        model.addAttribute("managers",userService.listAllByRole("manager"));

        return "/project/create";
    }

    @PostMapping("/create")
    public String insertProject(ProjectDTO project){
        projectService.save(project);
        project.setProjectStatus(Status.OPEN);
        return "redirect:/project/create";

    }

    @GetMapping("/delete/{projectcode}")
    public String deleteProject(@PathVariable("projectcode") String projectcode){
        projectService.delete(projectcode);
        return "redirect:/project/create";
    }

    @GetMapping("/complete/{projectcode}")
    public String completeProject(@PathVariable("projectcode") String projectcode){
        projectService.complete(projectcode);
        return "redirect:/project/create";
    }

    @GetMapping("/update/{projectcode}")
    public String editProject(@PathVariable("projectcode") String projectcode,Model model){

        model.addAttribute("project",projectService.getByProjectCode(projectcode));
        model.addAttribute("projects",projectService.listAllProjects());
        model.addAttribute("managers",userService.listAllByRole("manager"));

        return "/project/update";
    }

    @PostMapping("/update/{projectcode}")
    public String updateProject(@PathVariable("projectcode") String projectcode,ProjectDTO project,Model model){

        projectService.update(project);

        return "redirect:/project/create";
    }

    @GetMapping("/manager/complete")
    public String getProjectsByManager(Model model){

        List<ProjectDTO> projects = projectService.listAllProjectDetails();

        model.addAttribute("projects", projects);


        return "/manager/project-status";
    }

    @GetMapping("/manager/complete/{projectCode}")
    public String manager_completed(@PathVariable("projectCode") String projectCode, Model model){

        projectService.complete(projectCode);

        return "redirect:/project/manager/complete";
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
