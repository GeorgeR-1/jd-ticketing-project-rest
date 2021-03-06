package com.cybertek.controller;

import com.cybertek.dto.ProjectDTO;
import com.cybertek.dto.RoleDTO;
import com.cybertek.dto.UserDTO;
import com.cybertek.enums.Gender;
import com.cybertek.enums.Status;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDate;

@SpringBootTest
@AutoConfigureMockMvc
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final String token = "eyJhbGciOiJIUzI1NiJ9.eyJmaXJzdE5hbWUiOiJhZG1pbiIsImxhc3ROYW1lIjoiYWRtaW4iLCJzdWIiOiJhZG1pbkBhZG1pbi5jb20iLCJpZCI6MSwiZXhwIjoxNjE1MDkzODU5LCJpYXQiOjE2MTUwNTc4NTksInVzZXJuYW1lIjoiYWRtaW5AYWRtaW4uY29tIn0.M9tdnwIXswVUkh9O4D7HVXTt0ZNwR2PeiG13SgSS_gk";

    static UserDTO userDTO;
    static ProjectDTO projectDTO;

    @BeforeAll
    static void setUp(){

        userDTO = UserDTO.builder()
                .id(2L)
                .firstName("Mike")
                .lastName("Smith")
                .userName("george.radac@cybertekschoo.com")
                .passWord("abc123")
                .confirmPassword("abc123")
                .role(new RoleDTO(2l,"Manager"))
                .gender(Gender.MALE)
                .build();

        projectDTO = ProjectDTO.builder()
                .projectCode("Api1")
                .projectName("Api")
                .assignedManager(userDTO)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(5))
                .projectDetail("Api Test")
                .projectStatus(Status.OPEN)
                .completeTaskCount(0)
                .unfinishedTaskCount(0)
                .build();

    }

    @Test
    public void givenNoToken_whenGetSecureRequest() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/project/Api1"))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());

    }

    @Test
    public void givenToken_getAllProjects() throws Exception {
        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/v1/project")
                        .header("Authorization",token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].projectCode").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].assignedManager.userName").isNotEmpty());

    }

    @Test
    public void givenToken_createProject() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                    .post("/api/v1/project")
                    .header("Authorization",token)
                    .content(toJsonString(projectDTO))
            .contentType(MediaType.APPLICATION_JSON))
         .andExpect(MockMvcResultMatchers.jsonPath("projectCode").isNotEmpty());


    }

    protected String toJsonString(final Object obj){

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS,false);
        objectMapper.registerModule(new JavaTimeModule());
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }


}