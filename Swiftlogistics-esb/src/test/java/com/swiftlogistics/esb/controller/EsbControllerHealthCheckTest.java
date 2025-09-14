package com.swiftlogistics.esb.controller;

import com.swiftlogistics.esb.service.CmsService;
import com.swiftlogistics.esb.service.RosService;
import com.swiftlogistics.esb.service.WmsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class EsbControllerHealthCheckTest {

    @Mock
    private CmsService cmsService;

    @Mock
    private RosService rosService;

    @Mock
    private WmsService wmsService;

    @InjectMocks
    private EsbController esbController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(esbController).build();
    }

    @Test
    void healthCheck_AllSystemsHealthy_ShouldReturnOkWithUpStatus() throws Exception {
        // Arrange
        when(cmsService.isHealthy()).thenReturn(true);
        when(rosService.isHealthy()).thenReturn(true);
        when(wmsService.isHealthy()).thenReturn(true);

        // Act & Assert
        mockMvc.perform(get("/health")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cms.status").value("UP"))
                .andExpect(jsonPath("$.ros.status").value("UP"))
                .andExpect(jsonPath("$.wms.status").value("UP"))
                .andExpect(jsonPath("$.overall").value("UP"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.error").doesNotExist()); // Fixed: changed andExpected to andExpect

        // Verify service calls
        verify(cmsService, times(1)).isHealthy();
        verify(rosService, times(1)).isHealthy();
        verify(wmsService, times(1)).isHealthy();
    }

    @Test
    void healthCheck_SomeSystemsUnhealthy_ShouldReturnOkWithDownOverallStatus() throws Exception {
        // Arrange
        when(cmsService.isHealthy()).thenReturn(true);
        when(rosService.isHealthy()).thenReturn(false);
        when(wmsService.isHealthy()).thenReturn(true);

        // Act & Assert
        mockMvc.perform(get("/health")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cms.status").value("UP"))
                .andExpect(jsonPath("$.ros.status").value("DOWN"))
                .andExpect(jsonPath("$.wms.status").value("UP"))
                .andExpect(jsonPath("$.overall").value("DOWN"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.error").doesNotExist()); // Fixed: changed andExpected to andExpect

        verify(cmsService, times(1)).isHealthy();
        verify(rosService, times(1)).isHealthy();
        verify(wmsService, times(1)).isHealthy();
    }

    @Test
    void healthCheck_ExceptionThrown_ShouldReturnInternalServerError() throws Exception {
        // Arrange
        when(cmsService.isHealthy()).thenThrow(new RuntimeException("CMS connection failed"));

        // Act & Assert
        mockMvc.perform(get("/health")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.overall").value("DOWN"))
                .andExpect(jsonPath("$.error").value("CMS connection failed"));

        verify(cmsService, times(1)).isHealthy();
    }
}