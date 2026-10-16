package com.dannyleviev.lambdademo.agreement;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor   // constructor order = field order: (repository, clock)
public class AgreementService {

    static final int MAX_LIST_SIZE = 100;

    private final AgreementRepository repository;
    private final Clock clock;

    public AgreementResponse create(CreateAgreementRequest request) {
        LocalDate today = LocalDate.now(clock);
        AgreementStatus status = request.startDate().isAfter(today)
                ? AgreementStatus.PENDING
                : AgreementStatus.ACTIVE;

        AgreementItem item = new AgreementItem();
        item.setId(UUID.randomUUID().toString());
        item.setPolicyNumber(request.policyNumber());
        item.setHolderName(request.holderName());
        item.setProductType(request.productType().name());
        item.setMonthlyPremium(request.monthlyPremium());
        item.setStartDate(request.startDate());
        item.setEndDate(request.endDate());
        item.setStatus(status.name());
        item.setCreatedAt(Instant.now(clock));

        repository.insert(item);
        // Log IDs only, never the holder's name: insurance data is PII
        log.info("Created agreement id={} status={}", item.getId(), status);
        return AgreementResponse.from(item);
    }

    public AgreementResponse get(String id) {
        return repository.findById(id)
                .map(AgreementResponse::from)
                .orElseThrow(() -> new AgreementNotFoundException(id));
    }

    public List<AgreementResponse> list() {
        return repository.findAll(MAX_LIST_SIZE).stream()
                .map(AgreementResponse::from)
                .toList();
    }

    public void delete(String id) {
        if (!repository.deleteById(id)) {
            throw new AgreementNotFoundException(id);
        }
        log.info("Deleted agreement id={}", id);
    }
}