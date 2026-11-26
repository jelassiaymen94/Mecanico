package com.mecanico.vehiclepassport.repository;

import com.mecanico.vehiclepassport.entity.ReminderStatus;
import com.mecanico.vehiclepassport.entity.ReminderType;
import com.mecanico.vehiclepassport.entity.ServiceReminder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ServiceReminderRepository extends JpaRepository<ServiceReminder, Long> {

    List<ServiceReminder> findAllByVehicleIdOrderByStatusAscNextDueMileageAsc(Long vehicleId);

    Optional<ServiceReminder> findByIdAndVehicleId(Long id, Long vehicleId);

    List<ServiceReminder> findAllByStatusIn(Collection<ReminderStatus> statuses);

    Optional<ServiceReminder> findFirstByVehicleIdAndTypeAndStatusInOrderByCreatedAtDesc(
            Long vehicleId, ReminderType type, Collection<ReminderStatus> statuses);

    long countByVehicleIdAndStatusIn(Long vehicleId, Collection<ReminderStatus> statuses);

    void deleteByVehicleId(Long vehicleId);
}
