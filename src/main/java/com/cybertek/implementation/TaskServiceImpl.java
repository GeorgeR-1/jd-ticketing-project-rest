package com.cybertek.implementation;

import com.cybertek.dto.ProjectDTO;
import com.cybertek.dto.TaskDTO;
import com.cybertek.entity.Task;
import com.cybertek.entity.User;
import com.cybertek.enums.Status;
import com.cybertek.exception.TicketingProjectException;
import com.cybertek.mapper.ProjectMapper;
import com.cybertek.mapper.TaskMapper;
import com.cybertek.repository.TaskRepository;
import com.cybertek.repository.UserRepository;
import com.cybertek.service.TaskService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TaskServiceImpl implements TaskService {

    private TaskRepository taskRepository;
    private TaskMapper taskMapper;
    private ProjectMapper projectMapper;
    private UserRepository userRepository;

    public TaskServiceImpl(TaskRepository taskRepository, TaskMapper taskMapper, ProjectMapper projectMapper,
                           UserRepository userRepository) {

        this.taskRepository = taskRepository;
        this.taskMapper = taskMapper;
        this.projectMapper = projectMapper;
        this.userRepository = userRepository;
    }

    @Override
    public TaskDTO findById(Long id) throws TicketingProjectException {
        Task task = taskRepository.findById(id).orElseThrow(() -> new TicketingProjectException("Task does not exist"));
        return taskMapper.convertToDto(task);
    }

    @Override
    public List<TaskDTO> listAllTasks() {

        return taskRepository.findAll().stream()
                .map(taskMapper::convertToDto).collect(Collectors.toList());
    }

    @Override
    public TaskDTO save(TaskDTO dto) {
        dto.setTaskStatus(Status.OPEN);
        dto.setAssignedDate(LocalDate.now());
        Task task = taskMapper.convertToEntity(dto);
        Task save =taskRepository.save(task);
        return taskMapper.convertToDto(save);
    }

    @Override
    public void delete(Long id) throws TicketingProjectException {
        Task foundTask = taskRepository.findById(id).orElseThrow(() -> new TicketingProjectException("Task does not exist"));
        foundTask.setIsDeleted(true);
        taskRepository.save(foundTask);
    }

    @Override
    public TaskDTO update(TaskDTO dto) throws TicketingProjectException {
        Task task = taskRepository.findById(dto.getId())
                .orElseThrow(() -> new TicketingProjectException("Task does not exist"));

        Task convertedTask = taskMapper.convertToEntity(dto);

        Task save = taskRepository.save(convertedTask);

        return taskMapper.convertToDto(save);

    }

    @Override
    public int totalNonCompletedTasks(String projectCode) {
        return taskRepository.totalNonCompleteTasks(projectCode);
    }

    @Override
    public int totalCompletedTasks(String projectCode) {
        return taskRepository.totalCompletedTasks(projectCode);
    }


    @Override
    public void deleteByProject(ProjectDTO project) {

        List<TaskDTO> taskList = listAllByProject(project);
        taskList.forEach(taskDTO -> {
            try {
                delete(taskDTO.getId());
            } catch (TicketingProjectException e) {
                e.printStackTrace();
            }
        });

    }

    public List<TaskDTO> listAllByProject(ProjectDTO project) {

        return taskRepository.findAllByProject(projectMapper.convertToEntity(project))
                .stream().map(taskMapper::convertToDto).collect(Collectors.toList());

    }

    @Override
    public List<TaskDTO> listAllTaskByStatusIsNot(Status status) throws TicketingProjectException {
        String id = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findById(Long.parseLong(id))
                .orElseThrow(() -> new TicketingProjectException("USer does not exist"));

        List<Task> list = taskRepository.findAllByTaskStatusIsNotAndAssignedEmployee(status, user);

        return list.stream().map(taskMapper::convertToDto).collect(Collectors.toList());
    }

    @Override
    public List<TaskDTO> listAllTaskByProjectManager() throws TicketingProjectException {
        String id = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findById(Long.parseLong(id))
                .orElseThrow(() -> new TicketingProjectException("This user does not exist"));

        List<Task> tasks = taskRepository.findAllByProjectAssignedManager(user);

        return tasks.stream().map(taskMapper::convertToDto).collect(Collectors.toList());
    }

    @Override
    public TaskDTO updateStatus(TaskDTO dto) throws TicketingProjectException {

        Task task = taskRepository.findById(dto.getId())
                .orElseThrow(() -> new TicketingProjectException("This task does not exist"));

        task.setTaskStatus(dto.getTaskStatus());
        Task save = taskRepository.save(task);
        return taskMapper.convertToDto(save);
    }

//    @Override
//    public List<TaskDTO> listAllTasksByStatus(Status status) {
//
//        String username = SecurityContextHolder.getContext().getAuthentication().getName();
//
//        User user = userRepository.findByUserName(username);
//        List<Task> list = taskRepository.findAllByTaskStatusAndAssignedEmployee(status,user);
//
//        return list.stream().map(taskMapper::convertToDto).collect(Collectors.toList());
//
//    }

    @Override
    public List<TaskDTO> readAllByEmployee(User assignedEmployee) {

        List<Task> tasks = taskRepository.findAllByAssignedEmployee(assignedEmployee);

        return tasks.stream().map(taskMapper::convertToDto).collect(Collectors.toList());
    }


}
