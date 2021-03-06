package com.cybertek.implementation;

import com.cybertek.dto.ProjectDTO;
import com.cybertek.dto.TaskDTO;
import com.cybertek.dto.UserDTO;
import com.cybertek.entity.User;
import com.cybertek.exception.TicketingProjectException;
import com.cybertek.util.MapperUtil;
import com.cybertek.mapper.UserMapper;
import com.cybertek.repository.UserRepository;
import com.cybertek.service.ProjectService;
import com.cybertek.service.TaskService;
import com.cybertek.service.UserService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.security.AccessControlException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private UserRepository userRepository;
    private ProjectService projectService;
    private TaskService taskService;
    private BCryptPasswordEncoder passwordEncoder;
    private UserMapper userMapper;
    private MapperUtil mapperUtil;

    public UserServiceImpl(UserRepository userRepository, @Lazy ProjectService projectService,
                           TaskService taskService, BCryptPasswordEncoder passwordEncoder,
                           UserMapper userMapper, MapperUtil mapperUtil) {

        this.userRepository = userRepository;
        this.projectService = projectService;
        this.taskService = taskService;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
        this.mapperUtil = mapperUtil;
    }

    @Override
    public List<UserDTO> listAllUsers() {
        List<User> userList= userRepository.findAll(Sort.by("firstName"));
        return userList.stream()
                .map(obj -> userMapper.convertToDto(obj)).collect(Collectors.toList());

    }

    @Override
    public UserDTO findByUserName(String username) throws AccessDeniedException {
        User user = userRepository.findByUserName(username);
        checkForAuthorities(user);
        return userMapper.convertToDto(user);
    }

    @Override
    public UserDTO save(UserDTO dto) throws TicketingProjectException {

        User foundUser = userRepository.findByUserName(dto.getUserName());

        if (foundUser != null){
            throw new TicketingProjectException("User already exists");
        }


        User user = userMapper.convertToEntity(dto);
        user.setPassWord(passwordEncoder.encode(user.getPassWord()));

        User entity = userRepository.save(user);

        return userMapper.convertToDto(entity);
    }

    @Override
    public UserDTO update(UserDTO dto) throws TicketingProjectException, AccessDeniedException {

        //Find current user
        User user = userRepository.findByUserName(dto.getUserName());
        if (user == null){
            throw new TicketingProjectException("User Does Not Exist");
        }
        //Map update user dto to entity object
        User convertedUser = userMapper.convertToEntity(dto);

        convertedUser.setPassWord(passwordEncoder.encode(convertedUser.getPassWord()));

        if (!user.getEnabled()){
            throw new TicketingProjectException("User is not confirmed");
        }

        checkForAuthorities(user);

        convertedUser.setEnabled(true);

        //set id to the converted object
        convertedUser.setId(user.getId());
        //save updated user
        userRepository.save(convertedUser);

        return  findByUserName(dto.getUserName());

    }

    @Override
    public void delete(String username) throws TicketingProjectException {
        User user = userRepository.findByUserName(username);
        if (user == null){
            throw new TicketingProjectException("User Does Not Exist");
        }

        if(!checkIfUserCanBeDeleted(user)){
            throw new TicketingProjectException("User can not be deleted. It is linked by a project");
        }

        user.setUserName(user.getUserName() + "-" + user.getId());

        user.setIsDeleted(true);
        userRepository.save(user);
    }

    @Override
    public void deleteByUserName(String username) {
        userRepository.deleteByUserName(username);
    }

    @Override
    public List<UserDTO> listAllByRole(String role) {

        List<User> users = userRepository.findAllByRoleDescriptionIgnoreCase(role);
        return users.stream().map(obj -> {return userMapper.convertToDto(obj);})
                .collect(Collectors.toList());
    }

    @Override
    public Boolean checkIfUserCanBeDeleted(User user) {

        switch (user.getRole().getDescription()){
            case "Manager":
                List<ProjectDTO> projectList = projectService.realAllByAssignedManager(user);
                return projectList.size() == 0;
            case "Employee":
                List<TaskDTO> taskList = taskService.readAllByEmployee(user);
                return taskList.size() == 0;
            default:
                return true;
        }
    }

    @Override
    public UserDTO confirm(User user) {
        user.setEnabled(true);
        User confirmedUser = userRepository.save(user);

        return userMapper.convertToDto(confirmedUser);
    }

    private void checkForAuthorities(User user) throws AccessDeniedException {

        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && !authentication.getName().equals("anonymousUser")){

            Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());

            if(!(authentication.getName().equals(user.getId().toString()) || roles.contains("Admin"))){
                throw new AccessDeniedException("Access is denied");
            }

        }

    }

}
