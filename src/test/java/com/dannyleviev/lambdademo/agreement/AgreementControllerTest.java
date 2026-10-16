package com.dannyleviev.lambdademo.agreement;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AgreementControllerTest {

    private static final String VALID_BODY = """
            {
              "policyNumber": "POL-1001",
              "holderName": "Jane Doe",
              "productType": "HEALTH",
              "monthlyPremium": 120.50,
              "startDate": "2026-11-01",
              "endDate": "2027-11-01"
            }
            """;

    @Mock
    private AgreementService service;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new AgreementController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // ---------- POST ----------

    @Test
    @DisplayName("POST valid body -> 201, Location header, body, request deserialized correctly")
    void createReturns201() throws Exception {
        when(service.create(any())).thenReturn(response("abc-123"));

        mvc.perform(post("/agreements").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/agreements/abc-123")))
                .andExpect(jsonPath("$.id").value("abc-123"))
                .andExpect(jsonPath("$.status").value("PENDING"));

        ArgumentCaptor<CreateAgreementRequest> captor = ArgumentCaptor.forClass(CreateAgreementRequest.class);
        verify(service).create(captor.capture());
        assertThat(captor.getValue().productType()).isEqualTo(ProductType.HEALTH);
        assertThat(captor.getValue().startDate()).isEqualTo(LocalDate.of(2026, 11, 1));
        assertThat(captor.getValue().monthlyPremium()).isEqualByComparingTo("120.50");
    }

    @Test
    @DisplayName("POST blank holderName -> 400, service never called")
    void createWithBlankHolderReturns400() throws Exception {
        String body = VALID_BODY.replace("\"Jane Doe\"", "\"\"");

        mvc.perform(post("/agreements").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verifyNoInteractions(service);
    }

    @Test
    @DisplayName("POST endDate before startDate -> 400")
    void createWithInvalidDateRangeReturns400() throws Exception {
        String body = VALID_BODY.replace("2027-11-01", "2026-10-01");

        mvc.perform(post("/agreements").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    @DisplayName("POST unknown productType -> 400")
    void createWithUnknownProductTypeReturns400() throws Exception {
        String body = VALID_BODY.replace("\"HEALTH\"", "\"PET\"");

        mvc.perform(post("/agreements").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    // ---------- GET ----------

    @Test
    @DisplayName("GET /{id} existing -> 200 with body")
    void getReturns200() throws Exception {
        when(service.get("abc-123")).thenReturn(response("abc-123"));

        mvc.perform(get("/agreements/abc-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("abc-123"))
                .andExpect(jsonPath("$.policyNumber").value("POL-1001"));
    }

    @Test
    @DisplayName("GET /{id} missing -> 404 ProblemDetail")
    void getMissingReturns404() throws Exception {
        when(service.get("missing")).thenThrow(new AgreementNotFoundException("missing"));

        mvc.perform(get("/agreements/missing"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Agreement missing not found"));
    }

    @Test
    @DisplayName("GET /agreements -> 200 with list")
    void listReturns200() throws Exception {
        when(service.list()).thenReturn(List.of(response("a"), response("b")));

        mvc.perform(get("/agreements"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value("a"));
    }

    // ---------- DELETE ----------

    @Test
    @DisplayName("DELETE existing -> 204")
    void deleteReturns204() throws Exception {
        mvc.perform(delete("/agreements/abc-123"))
                .andExpect(status().isNoContent());

        verify(service).delete("abc-123");
    }

    @Test
    @DisplayName("DELETE missing -> 404")
    void deleteMissingReturns404() throws Exception {
        doThrow(new AgreementNotFoundException("missing")).when(service).delete("missing");

        mvc.perform(delete("/agreements/missing"))
                .andExpect(status().isNotFound());
    }

    // ---------- helpers ----------

    private static AgreementResponse response(String id) {
        return new AgreementResponse(id, "POL-1001", "Jane Doe", ProductType.HEALTH,
                new BigDecimal("120.50"), LocalDate.of(2026, 11, 1), LocalDate.of(2027, 11, 1),
                AgreementStatus.PENDING, Instant.parse("2026-10-15T12:00:00Z"));
    }
}