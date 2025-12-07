package com.example.controller;

import com.example.dto.UserRequest;
import com.example.dto.UserResponse;
import com.example.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerHateoasTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    void getUserById_ShouldIncludeHateoasLinks() throws Exception {
        // Given
        UserResponse userResponse = new UserResponse(1L, "John Doe", "john@example.com", 30, LocalDateTime.now());
        EntityModel<UserResponse> entityModel = EntityModel.of(userResponse);
        entityModel.add(linkTo(methodOn(UserController.class).getUserById(1L)).withSelfRel());
        entityModel.add(linkTo(methodOn(UserController.class).getAllUsers()).withRel("allUsers"));
        entityModel.add(linkTo(methodOn(UserController.class).updateUser(1L, null)).withRel("updateUser"));
        entityModel.add(linkTo(methodOn(UserController.class).deleteUser(1L)).withRel("deleteUser"));

        when(userService.getUserById(1L)).thenReturn(entityModel);

        // When & Then
        mockMvc.perform(get("/api/users/1")
                        .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaTypes.HAL_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.allUsers.href").exists())
                .andExpect(jsonPath("$._links.updateUser.href").exists())
                .andExpect(jsonPath("$._links.deleteUser.href").exists());
    }

    @Test
    void getAllUsers_ShouldIncludeHateoasLinks() throws Exception {
        // Given
        UserResponse user1 = new UserResponse(1L, "John Doe", "john@example.com", 30, LocalDateTime.now());
        UserResponse user2 = new UserResponse(2L, "Jane Smith", "jane@example.com", 25, LocalDateTime.now());

        EntityModel<UserResponse> entityModel1 = EntityModel.of(user1);
        EntityModel<UserResponse> entityModel2 = EntityModel.of(user2);

        List<EntityModel<UserResponse>> users = Arrays.asList(entityModel1, entityModel2);

        when(userService.getAllUsers()).thenReturn(org.springframework.hateoas.CollectionModel.of(users,
                linkTo(methodOn(UserController.class).getAllUsers()).withSelfRel(),
                linkTo(methodOn(UserController.class).createUser(null)).withRel("createUser")));

        // When & Then
        mockMvc.perform(get("/api/users")
                        .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaTypes.HAL_JSON))
                .andExpect(jsonPath("$._embedded.userResponseList.length()").value(2))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.createUser.href").exists());
    }

    @Test
    void createUser_ShouldReturnHateoasLinks() throws Exception {
        // Given
        UserRequest userRequest = new UserRequest("John Doe", "john@example.com", 30);
        UserResponse userResponse = new UserResponse(1L, "John Doe", "john@example.com", 30, LocalDateTime.now());

        EntityModel<UserResponse> entityModel = EntityModel.of(userResponse);
        entityModel.add(linkTo(methodOn(UserController.class).getUserById(1L)).withSelfRel());
        entityModel.add(linkTo(methodOn(UserController.class).getAllUsers()).withRel("allUsers"));

        when(userService.createUser(any(UserRequest.class))).thenReturn(entityModel);

        // When & Then
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest))
                        .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaTypes.HAL_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.allUsers.href").exists());
    }
}