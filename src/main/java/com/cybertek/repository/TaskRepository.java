package com.cybertek.repository;

import com.cybertek.entity.Project;
import com.cybertek.entity.Task;
import com.cybertek.entity.User;
import com.cybertek.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task,Long> {

    @Query("SELECT count (t) FROM Task t where t.project.projectCode = ?1 and " +
            "t.taskStatus <> 'COMPLETED'")
    int totalNonCompleteTasks(String projectCode);

    @Query(value = "SELECT count(*) FROM tasks t join projects p ON " +
            "t.project_id = p.id WHERE p.project_code = ?1 AND t.task_status = 'COMPLETE'",
    nativeQuery = true)
    int totalCompletedTasks(String projectCode);

    List<Task> findAllByProject(Project project);

    List<Task> findAllByTaskStatusIsNotAndAssignedEmployee(Status status, User user);

    List<Task> findAllByProjectAssignedManager(User manager);

    List<Task> findAllByTaskStatusAndAssignedEmployee(Status status, User user);

    List<Task> findAllByAssignedEmployee(User user);



}
