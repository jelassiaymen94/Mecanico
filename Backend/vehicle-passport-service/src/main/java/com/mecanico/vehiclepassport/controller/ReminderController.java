package com.mecanico.vehiclepassport.controller;

import com.mecanico.vehiclepassport.dto.ReminderDtos;
import com.mecanico.vehiclepassport.service.ReminderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles/{vehicleId}/reminders")
public class ReminderController {

    private final ReminderService reminderService;

    public ReminderController(ReminderService reminderService) {
        this.reminderService = reminderService;
    }

    @GetMapping
    public List<ReminderDtos.ServiceReminderResponse> list(@PathVariable Long vehicleId) {
        return reminderService.listForVehicle(vehicleId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReminderDtos.ServiceReminderResponse add(
            @PathVariable Long vehicleId,
            @Valid @RequestBody ReminderDtos.ServiceReminderRequest request) {
        return reminderService.add(vehicleId, request);
    }

    @PatchMapping("/{reminderId}/status")
    public ReminderDtos.ServiceReminderResponse updateStatus(
            @PathVariable Long vehicleId,
            @PathVariable Long reminderId,
            @Valid @RequestBody ReminderDtos.StatusUpdateRequest request) {
        return reminderService.updateStatus(vehicleId, reminderId, request);
    }

    @DeleteMapping("/{reminderId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long vehicleId, @PathVariable Long reminderId) {
        reminderService.delete(vehicleId, reminderId);
    }
}
