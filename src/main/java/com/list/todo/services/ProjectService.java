package com.list.todo.services;

import com.list.todo.entity.Project;
import com.list.todo.repositories.ProjectRepository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProjectService {
	
    private final ProjectRepository repository;

    @Autowired
    public ProjectService(ProjectRepository repository) {
        this.repository = repository;
    }

    public List<Project> findAllProjectsByUser(Long userId){
        return repository.findProjectsByUserOwnerId(userId);
    }
    
    public Project findProject(Long id){
        return repository.findById(id).get();
    }

    public void saveProject(Project project){
        repository.save(project);
    }
    
    public void updateProject(Project project) {
    	repository.save(project);
    }
    
    public void deleteProject(Long id) {
    	repository.deleteById(id);
    }
}
