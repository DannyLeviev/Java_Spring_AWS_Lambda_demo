package com.dannyleviev.lambdademo.agreement;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgreementServiceTest {

    private static final Instant NOW = Instant.parse("2026-10-15T12:00:00Z");
    private static final LocalDate TODAY = LocalDate.of(2026, 10, 15);

    @Mock
    private AgreementRepository repository;

    private AgreementService service;

    @BeforeEach
    void setUp() {
        service = new AgreementService(repository, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    @DisplayName("create: future start date -> PENDING")
    void createWithFutureStartIsPending() {
        AgreementResponse response = service.create(request(TODAY.plusDays(1), null));

        assertThat(response.status()).isEqualTo(AgreementStatus.PENDING);
    }

    @Test
    @DisplayName("create: start date today -> ACTIVE")
    void createWithStartTodayIsActive() {
        AgreementResponse response = service.create(request(TODAY, null));

        assertThat(response.status()).isEqualTo(AgreementStatus.ACTIVE);
    }

    @Test
    @DisplayName("create: persists every field with generated id and timestamp")
    void createPersistsAllFields() {
        LocalDate start = TODAY.plusDays(10);
        LocalDate end = start.plusYears(1);

        AgreementResponse response = service.create(request(start, end));

        ArgumentCaptor<AgreementItem> captor = ArgumentCaptor.forClass(AgreementItem.class);
        verify(repository).insert(captor.capture());
        AgreementItem saved = captor.getValue();

        assertThat(saved.getId()).isNotBlank().isEqualTo(response.id());
        assertThat(saved.getPolicyNumber()).isEqualTo("POL-1001");
        assertThat(saved.getHolderName()).isEqualTo("Jane Doe");
        assertThat(saved.getProductType()).isEqualTo("HEALTH");
        assertThat(saved.getMonthlyPremium()).isEqualByComparingTo("120.50");
        assertThat(saved.getStartDate()).isEqualTo(start);
        assertThat(saved.getEndDate()).isEqualTo(end);
        assertThat(saved.getStatus()).isEqualTo("PENDING");
        assertThat(saved.getCreatedAt()).isEqualTo(NOW);
    }

    @Test
    @DisplayName("get: existing id -> mapped response")
    void getReturnsMappedResponse() {
        when(repository.findById("abc-123")).thenReturn(Optional.of(storedItem("abc-123")));

        AgreementResponse response = service.get("abc-123");

        assertThat(response.id()).isEqualTo("abc-123");
        assertThat(response.productType()).isEqualTo(ProductType.LIFE);
        assertThat(response.status()).isEqualTo(AgreementStatus.ACTIVE);
    }

    @Test
    @DisplayName("get: missing id -> AgreementNotFoundException")
    void getMissingThrows() {
        when(repository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get("missing"))
                .isInstanceOf(AgreementNotFoundException.class)
                .hasMessageContaining("missing");
    }

    @Test
    @DisplayName("list: maps all items, bounded by MAX_LIST_SIZE")
    void listMapsItems() {
        when(repository.findAll(anyInt())).thenReturn(List.of(storedItem("a"), storedItem("b")));

        List<AgreementResponse> result = service.list();

        assertThat(result).extracting(AgreementResponse::id).containsExactly("a", "b");
        verify(repository).findAll(AgreementService.MAX_LIST_SIZE);
    }

    @Test
    @DisplayName("delete: existing id -> deleted")
    void deleteExisting() {
        when(repository.deleteById("abc-123")).thenReturn(true);

        service.delete("abc-123");

        verify(repository).deleteById("abc-123");
    }

    @Test
    @DisplayName("delete: missing id -> AgreementNotFoundException")
    void deleteMissingThrows() {
        when(repository.deleteById("missing")).thenReturn(false);

        assertThatThrownBy(() -> service.delete("missing"))
                .isInstanceOf(AgreementNotFoundException.class);
    }

    // ---------- helpers ----------

    private static CreateAgreementRequest request(LocalDate start, LocalDate end) {
        return new CreateAgreementRequest("POL-1001", "Jane Doe", ProductType.HEALTH,
                new BigDecimal("120.50"), start, end);
    }

    private static AgreementItem storedItem(String id) {
        AgreementItem item = new AgreementItem();
        item.setId(id);
        item.setPolicyNumber("POL-" + id);
        item.setHolderName("John Smith");
        item.setProductType(ProductType.LIFE.name());
        item.setMonthlyPremium(new BigDecimal("55.00"));
        item.setStartDate(TODAY);
        item.setStatus(AgreementStatus.ACTIVE.name());
        item.setCreatedAt(NOW);
        return item;
    }
}