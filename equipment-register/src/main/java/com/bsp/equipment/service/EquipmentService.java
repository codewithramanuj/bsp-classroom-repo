package com.bsp.equipment.service;

import com.bsp.equipment.dto.EquipmentRequest;
import com.bsp.equipment.dto.EquipmentResponse;
import com.bsp.equipment.repository.EquipmentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EquipmentService {

    private final EquipmentRepository equipmentRepo;

    public EquipmentService(EquipmentRepository equipmentRepo) {
        this.equipmentRepo = equipmentRepo;
    }

    public EquipmentResponse addEquipment(EquipmentRequest request) {
        // TODO: uppercase the status, call the repository, return the saved equipment with its new id
        return null;
    }

    public List<EquipmentResponse> getAllEquipment() {
        // TODO: call the repository
        return null;
    }

    public EquipmentResponse getEquipment(long id) {
        // TODO: call the repository
        return null;
    }

    public void updateEquipment(long id, EquipmentRequest request) {
        // TODO: uppercase the status, call the repository
    }

    public void deleteEquipment(long id) {
        // TODO: call the repository
    }
}
