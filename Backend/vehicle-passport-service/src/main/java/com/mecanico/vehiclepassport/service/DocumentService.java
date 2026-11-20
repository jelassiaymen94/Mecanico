package com.mecanico.vehiclepassport.service;

import com.mecanico.vehiclepassport.dto.DocumentDtos;
import com.mecanico.vehiclepassport.entity.Vehicle;
import com.mecanico.vehiclepassport.entity.VehicleDocument;
import com.mecanico.vehiclepassport.exception.ApiException;
import com.mecanico.vehiclepassport.repository.VehicleDocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class DocumentService {

    private final VehicleDocumentRepository documents;
    private final VehicleAccessService access;
    private final DtoMapper mapper;

    public DocumentService(VehicleDocumentRepository documents, VehicleAccessService access, DtoMapper mapper) {
        this.documents = documents;
        this.access = access;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<DocumentDtos.VehicleDocumentResponse> listForVehicle(Long vehicleId) {
        access.requireReadable(vehicleId);
        return documents.findAllByVehicleIdOrderByExpiryDateAsc(vehicleId)
                .stream().map(mapper::toDocumentResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<DocumentDtos.VehicleDocumentResponse> expiring(Long vehicleId, int days) {
        access.requireReadable(vehicleId);
        LocalDate today = LocalDate.now();
        int safeDays = Math.min(Math.max(days, 1), 365);
        return documents.findAllByVehicleIdOrderByExpiryDateAsc(vehicleId).stream()
                .filter(d -> d.isActive())
                .filter(d -> !d.getExpiryDate().isBefore(today))
                .filter(d -> !d.getExpiryDate().isAfter(today.plusDays(safeDays)))
                .map(mapper::toDocumentResponse)
                .toList();
    }

    @Transactional
    public DocumentDtos.VehicleDocumentResponse add(Long vehicleId, DocumentDtos.VehicleDocumentRequest request) {
        Vehicle vehicle = access.requireWritable(vehicleId);
        validateDates(request);
        VehicleDocument document = new VehicleDocument();
        document.setVehicle(vehicle);
        apply(document, request);
        document.setNotification30Sent(false);
        document.setNotification7Sent(false);
        return mapper.toDocumentResponse(documents.save(document));
    }

    @Transactional
    public DocumentDtos.VehicleDocumentResponse update(Long vehicleId, Long documentId,
                                                        DocumentDtos.VehicleDocumentRequest request) {
        access.requireWritable(vehicleId);
        VehicleDocument document = documents.findByIdAndVehicleId(documentId, vehicleId)
                .orElseThrow(() -> ApiException.notFound("Document not found"));
        validateDates(request);
        boolean datesChanged = !request.expiryDate().equals(document.getExpiryDate())
                || request.type() != document.getType();
        apply(document, request);
        if (datesChanged) {
            document.setNotification30Sent(false);
            document.setNotification7Sent(false);
        }
        return mapper.toDocumentResponse(documents.save(document));
    }

    @Transactional
    public void delete(Long vehicleId, Long documentId) {
        access.requireWritable(vehicleId);
        VehicleDocument document = documents.findByIdAndVehicleId(documentId, vehicleId)
                .orElseThrow(() -> ApiException.notFound("Document not found"));
        documents.delete(document);
    }

    private void apply(VehicleDocument document, DocumentDtos.VehicleDocumentRequest request) {
        document.setType(request.type());
        document.setNumber(trimToNull(request.number()));
        document.setIssuer(trimToNull(request.issuer()));
        document.setIssueDate(request.issueDate());
        document.setExpiryDate(request.expiryDate());
        document.setFileUrl(trimToNull(request.fileUrl()));
        document.setCost(request.cost());
        document.setNotes(trimToNull(request.notes()));
        document.setActive(request.active() == null || request.active());
    }

    private void validateDates(DocumentDtos.VehicleDocumentRequest request) {
        if (request.issueDate() != null && request.issueDate().isAfter(request.expiryDate())) {
            throw ApiException.badRequest("The issue date must be before the expiry date");
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
