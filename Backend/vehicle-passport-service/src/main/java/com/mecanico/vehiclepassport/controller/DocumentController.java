package com.mecanico.vehiclepassport.controller;

import com.mecanico.vehiclepassport.dto.DocumentDtos;
import com.mecanico.vehiclepassport.service.DocumentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles/{vehicleId}/documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @GetMapping
    public List<DocumentDtos.VehicleDocumentResponse> list(@PathVariable Long vehicleId) {
        return documentService.listForVehicle(vehicleId);
    }

    @GetMapping("/expiring")
    public List<DocumentDtos.VehicleDocumentResponse> expiring(@PathVariable Long vehicleId,
                                                                @RequestParam(defaultValue = "30") int days) {
        return documentService.expiring(vehicleId, days);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DocumentDtos.VehicleDocumentResponse add(
            @PathVariable Long vehicleId,
            @Valid @RequestBody DocumentDtos.VehicleDocumentRequest request) {
        return documentService.add(vehicleId, request);
    }

    @PutMapping("/{documentId}")
    public DocumentDtos.VehicleDocumentResponse update(
            @PathVariable Long vehicleId,
            @PathVariable Long documentId,
            @Valid @RequestBody DocumentDtos.VehicleDocumentRequest request) {
        return documentService.update(vehicleId, documentId, request);
    }

    @DeleteMapping("/{documentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long vehicleId, @PathVariable Long documentId) {
        documentService.delete(vehicleId, documentId);
    }
}
