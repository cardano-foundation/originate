package org.cardanofoundation.proofoforigin.api.controllers;

import org.cardanofoundation.proofoforigin.api.business.UploadBottleBusiness;
import org.cardanofoundation.proofoforigin.api.business.impl.BottlesServiceImpl;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.response.BottleResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class BottlesApiControllerTest {

    @Mock
    private UploadBottleBusiness uploadBottleBusiness;

    @Mock
    private BottlesServiceImpl bottlesService;

    private MockMvc mockMvc;

    @InjectMocks
    private BottlesApiController bottlesApiController;

    private static final String TYPE = "text/csv";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(bottlesApiController).build();
    }

    @Test
    void uploadCSV() throws Exception {
        RequestBuilder request = MockMvcRequestBuilders.multipart("/api/v1/bottles/123A")
                .file((MockMultipartFile) createValidFile("data", TYPE))
                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                .with(SecurityMockMvcRequestPostProcessors.jwt());
        mockMvc.perform(request).andExpect(status().isAccepted());
    }

    private MultipartFile createValidFile(String fileName, String contentType) {
        return createMockMultipartFile(fileName, contentType, "extended_id,lot_id,sequential_number,reel_number\n" +
                "5XEDIMQBXN041SMQ1081S1DB4ZLY3YI,1234ABC5678,245,5");
    }

    private MultipartFile createMockMultipartFile(String fileName, String contentType, String fileContent) {
        return new MockMultipartFile(fileName, fileName, contentType, fileContent.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void getBottlesGivenByWinery() throws Exception {
        BottleResponse bottleResponse = new BottleResponse();
        Mockito.when(bottlesService.getBottlesByWineryId("w01")).thenReturn(bottleResponse);
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/bottles/w01"))
                .andExpect(status().isOk());
    }

    @Test
    void getBottlesGivenByLot() throws Exception {
        Mockito.when(bottlesService.getBottlesByLotId("w01","11DigitLot!")).thenReturn(Collections.emptyList());
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/bottles/w01/lots/11DigitLot!"))
                .andExpect(status().isOk());
    }
}

