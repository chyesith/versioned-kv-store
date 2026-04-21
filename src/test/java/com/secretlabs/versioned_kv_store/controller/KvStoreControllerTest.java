package com.secretlabs.versioned_kv_store.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.secretlabs.versioned_kv_store.dto.KvStoreRequest;
import com.secretlabs.versioned_kv_store.dto.KvStoreResponse;
import com.secretlabs.versioned_kv_store.exception.GlobalExceptionHandler;
import com.secretlabs.versioned_kv_store.exception.KeyNotFoundException;
import com.secretlabs.versioned_kv_store.exception.NoVersionAtTimestampException;
import com.secretlabs.versioned_kv_store.service.KvStoreService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(KvStoreController.class)
@Import(GlobalExceptionHandler.class)
@DisplayName("KvStoreController web layer tests")
public class KvStoreControllerTest {


    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();


    @MockitoBean
    private KvStoreService kvStoreService;

    private static final KvStoreResponse SAMPLE_RESPONSE =
            new KvStoreResponse("mykey", "{\"data\":\"v1\"}", 1, 1719820800L);


    @Nested
    @DisplayName("POST /kvstore/v1")
    class PostUpsert {

        @Test
        @DisplayName("should return 201 with response body on valid request")
        void returnsCreatedOnValidRequest() throws Exception {
            KvStoreRequest request = new KvStoreRequest("mykey", "{\"data\":\"v1\"}");

            when(kvStoreService.upsert("mykey", "{\"data\":\"v1\"}"))
                    .thenReturn(SAMPLE_RESPONSE);

            mockMvc.perform(post("/kvstore/v1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.key").value("mykey"))
                    .andExpect(jsonPath("$.version").value(1))
                    .andExpect(jsonPath("$.value").value("{\"data\":\"v1\"}"))
                    .andExpect(jsonPath("$.createdAt").value(1719820800L));
        }

        @Test
        @DisplayName("should return 400 when key is blank")
        void returns400WhenKeyBlank() throws Exception {
            KvStoreRequest request = new KvStoreRequest("", "{\"data\":\"v1\"}");

            mockMvc.perform(post("/kvstore/v1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Bad Request"));
        }

        @Test
        @DisplayName("should return 400 when value is blank")
        void returns400WhenValueBlank() throws Exception {
            KvStoreRequest request = new KvStoreRequest("mykey", "");

            mockMvc.perform(post("/kvstore/v1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));
        }

        @Test
        @DisplayName("should return 400 when body is empty")
        void returns400WhenBodyEmpty() throws Exception {
            mockMvc.perform(post("/kvstore/v1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /kvstore/v1/{key}")
    class GetLatest {

        @Test
        @DisplayName("should return 200 with latest version")
        void returnsLatestVersion() throws Exception {
            when(kvStoreService.getLatest("mykey"))
                    .thenReturn(SAMPLE_RESPONSE);

            mockMvc.perform(get("/kvstore/v1/mykey"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.key").value("mykey"))
                    .andExpect(jsonPath("$.version").value(1))
                    .andExpect(jsonPath("$.value").value("{\"data\":\"v1\"}"));
        }

        @Test
        @DisplayName("should return 404 for unknown key")
        void returns404ForUnknownKey() throws Exception {
            String key = "unknown";
            when(kvStoreService.getLatest(key))
                    .thenThrow(new KeyNotFoundException(key));

            mockMvc.perform(get("/kvstore/v1/"+key))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.error").value("Not Found"))
                    .andExpect(jsonPath("$.message").value("key not found:"+key));
        }

        @Test
        @DisplayName("should return version at timestamp when param provided")
        void returnsVersionAtTimestamp() throws Exception {
            KvStoreResponse timestampResponse =
                    new KvStoreResponse("mykey", "{\"data\":\"v1\"}", 1, 1719820800L);

            when(kvStoreService.getAtTimestamp("mykey", 1719820850L))
                    .thenReturn(timestampResponse);

            mockMvc.perform(get("/kvstore/v1/mykey")
                            .param("timestamp", "1719820850"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.version").value(1));
        }

        @Test
        @DisplayName("should return 404 when no version exists at timestamp")
        void returns404WhenNoVersionAtTimestamp() throws Exception {
            long fakeTimestamp = 123456789L;
            when(kvStoreService.getAtTimestamp("mykey", fakeTimestamp))
                    .thenThrow(new NoVersionAtTimestampException("mykey", fakeTimestamp));

            mockMvc.perform(get("/kvstore/v1/mykey")
                            .param("timestamp", String.valueOf(fakeTimestamp)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404));
        }

        @Test
        @DisplayName("GET /get_all_records returns 200 and list of records")
        void getAllRecordsReturnsOk() throws Exception {

            List<KvStoreResponse> mockData = List.of(
                    new KvStoreResponse("key1", "value1", 1 ,3L),
                    new KvStoreResponse("key2", "value1", 2 ,5L)
            );

            when(kvStoreService.getAllLatest()).thenReturn(mockData);

            mockMvc.perform(get("/kvstore/v1/get_all_records")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.size()").value(2))
                    .andExpect(jsonPath("$[0].key").value("key1"))
                    .andExpect(jsonPath("$[1].key").value("key2"));
        }
    }

}
