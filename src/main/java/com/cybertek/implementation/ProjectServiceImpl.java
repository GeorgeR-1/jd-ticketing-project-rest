package com.cybertek.implementation;

import com.cybertek.dto.ProjectDTO;
import com.cybertek.dto.UserDTO;
import com.cybertek.entity.Project;
import com.cybertek.entity.User;
import com.cybertek.enums.Status;
import com.cybertek.exception.TicketingProjectException;
import com.cybertek.mapper.ProjectMapper;
import com.cybertek.mapper.UserMapper;
import com.cybertek.repository.ProjectRepository;
import com.cybertek.repository.UserRepository;
import com.cybertek.service.ProjectService;
import com.cybertek.service.TaskService;
import com.cybertek.service.UserService;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjectServiceImpl implements ProjectService {

    private ProjectMapper projectMapper;
    private ProjectRepository projectRepository;
    private UserService userService;
    private UserMapper userMapper;
    private TaskService taskService;
    private UserRepository userRepository;

    public ProjectServiceImpl(ProjectMapper projectMapper, UserRepository userRepository,
                              ProjectRepository projectRepository, UserService userService,
                              UserMapper userMapper, TaskService taskService) {

        this.projectMapper = projectMapper;
        this.projectRepository = projectRepository;
        this.userService = userService;
        this.userMapper = userMapper;
        this.taskService = taskService;
        this.userRepository = userRepository;
    }

    @Override
    public ProjectDTO getByProjectCode(String code) {

        Project project = projectRepository.findByProjectCode(code);

        return projectMapper.convertToDto(project);
    }

    @Override
    public List<ProjectDTO> listAllProjects() {

        return projectRepository.findAll(Sort.by("projectCode")).stream()
                .map(projectMapper::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public ProjectDTO save(ProjectDTO dto) throws TicketingProjectException {
        Project foundProject = projectRepository.findByProjectCode(dto.getProjectCode());

        if (foundProject != null){
            throw new TicketingProjectException("Project with this code already exists");
        }

        Project obj = projectMapper.convertToEntity(dto);

        Project createdProject = projectRepository.save(obj);

        return projectMapper.convertToDto(createdProject);
    }

    @Override
    public ProjectDTO update(ProjectDTO dto) throws TicketingProjectException {
        Project project = projectRepository.findByProjectCode(dto.getProjectCode());
        if (project == null){
            throw new TicketingProjectException("Project does not exist");
        }
        Project convertedProject = projectMapper.convertToEntity(dto);

        Project updatedProject = projectRepository.save(convertedProject);

        return projectMapper.convertToDto(updatedProject);
    }

    @Override
    public void delete(String code) throws TicketingProjectException {
        Project project = projectRepository.findByProjectCode(code);

        if (project == null){
            throw new TicketingProjectException("Project does not exist");
        }

        project.setIsDeleted(true);

        project.setProjectCode(project.getProjectCode() + "-" + project.getId());
        projectRepository.save(project);

        taskService.deleteByProject(projectMapper.convertToDto(project));

    }

    @Override
    public ProjectDTO complete(String projectCode) throws TicketingProjectException {

        Project project = projectRepository.findByProjectCode(projectCode);

        if (project == null){
            throw new TicketingProjectException("Project does not exist");
        }

        project.setProjectStatus(Status.COMPLETE);
        Project savedProject = projectRepository.save(project);
        return projectMapper.convertToDto(savedProject);
    }

    @Override
    public List<ProjectDTO> listAllProjectDetails() throws TicketingProjectException {

        String id = SecurityContextHolder.getContext().getAuthentication().getName();
        Long currentId = Long.parseLong(id);

        User user = userRepository.findById(currentId)
                .orElseThrow(() -> new TicketingProjectException("This manager does not exist"));

        List<Project> list = projectRepository.findAllByAssignedManager(user);

        if (list.size() == 0){
            throw new TicketingProjectException("This manager does not have any project assigned");
        }

        return list.stream().map(project -> {
            ProjectDTO obj = projectMapper.convertToDto(project);
            obj.setUnfinishedTaskCount(taskService
                    .totalNonCompletedTasks(obj.getProjectCode()));
            obj.setCompleteTaskCount(taskService
                    .totalCompletedTasks(obj.getProjectCode()));
            return obj;
        })
                .collect(Collectors.toList());

    }

    @Override
    public List<ProjectDTO> realAllByAssignedManager(User user) {

        List<Project> list = projectRepository.findAllByAssignedManager(user);

        return list.stream().map(projectMapper::convertToDto).collect(Collectors.toList());
    }

    @Override
    public List<ProjectDTO> listAllNonCompletedProjects() {

        return projectRepository.findAllByProjectStatusIsNot(Status.COMPLETE).stream()
                .map(project -> projectMapper.convertToDto(project))
                .collect(Collectors.toList());

    }


}
