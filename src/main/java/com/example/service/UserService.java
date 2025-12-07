package com.example.service;

import com.example.controller.UserController;
import com.example.dto.UserRequest;
import com.example.dto.UserResponse;
import com.example.entity.User;
import com.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public EntityModel<UserResponse> createUser(UserRequest userRequest) {
        // Проверка уникальности email
        if (userRepository.existsByEmail(userRequest.getEmail())) {
            throw new IllegalArgumentException("User with email " + userRequest.getEmail() + " already exists");
        }

        User user = new User(userRequest.getName(), userRequest.getEmail(), userRequest.getAge());
        User savedUser = userRepository.save(user);

        UserResponse response = convertToResponse(savedUser);
        return EntityModel.of(response,
                WebMvcLinkBuilder.linkTo(methodOn(UserController.class).getUserById(savedUser.getId())).withSelfRel(),
                linkTo(methodOn(UserController.class).getAllUsers()).withRel("all-users")
        );
    }

    @Transactional(readOnly = true)
    public CollectionModel<EntityModel<UserResponse>> getAllUsers() {
        List<EntityModel<UserResponse>> users = userRepository.findAll()
                .stream()
                .map(this::convertToResponse)
                .map(response -> EntityModel.of(response,
                        linkTo(methodOn(UserController.class).getUserById(response.getId())).withSelfRel(),
                        linkTo(methodOn(UserController.class).updateUser(response.getId(), null)).withRel("update"),
                        linkTo(methodOn(UserController.class).deleteUser(response.getId())).withRel("delete")
                ))
                .collect(Collectors.toList());

        return CollectionModel.of(users,
                linkTo(methodOn(UserController.class).getAllUsers()).withSelfRel(),
                linkTo(methodOn(UserController.class).createUser(null)).withRel("create-user")
        );
    }

    @Transactional(readOnly = true)
    public EntityModel<UserResponse> getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));

        UserResponse response = convertToResponse(user);
        return EntityModel.of(response,
                linkTo(methodOn(UserController.class).getUserById(id)).withSelfRel(),
                linkTo(methodOn(UserController.class).getAllUsers()).withRel("all-users"),
                linkTo(methodOn(UserController.class).updateUser(id, null)).withRel("update"),
                linkTo(methodOn(UserController.class).deleteUser(id)).withRel("delete")
        );
    }

    public EntityModel<UserResponse> updateUser(Long id, UserRequest userRequest) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));

        // Проверка уникальности email для других пользователей
        if (!user.getEmail().equals(userRequest.getEmail()) &&
                userRepository.existsByEmailAndIdNot(userRequest.getEmail(), id)) {
            throw new IllegalArgumentException("User with email " + userRequest.getEmail() + " already exists");
        }

        user.setName(userRequest.getName());
        user.setEmail(userRequest.getEmail());
        user.setAge(userRequest.getAge());

        User updatedUser = userRepository.save(user);
        UserResponse response = convertToResponse(updatedUser);

        return EntityModel.of(response,
                linkTo(methodOn(UserController.class).getUserById(id)).withSelfRel(),
                linkTo(methodOn(UserController.class).getAllUsers()).withRel("all-users"),
                linkTo(methodOn(UserController.class).updateUser(id, null)).withRel("update"),
                linkTo(methodOn(UserController.class).deleteUser(id)).withRel("delete")
        );
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("User not found with id: " + id);
        }

        userRepository.deleteById(id);
    }

    private UserResponse convertToResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setAge(user.getAge());
        response.setCreatedAt(user.getCreatedAt());
        return response;
    }
}