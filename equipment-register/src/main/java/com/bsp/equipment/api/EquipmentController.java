package com.bsp.equipment.api;

import com.bsp.equipment.dto.EquipmentRequest;
import com.bsp.equipment.dto.EquipmentResponse;
import com.bsp.equipment.service.EquipmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/equipment")
public class EquipmentController {

    private final EquipmentService equipmentService;

    public EquipmentController(EquipmentService equipmentService) {
        this.equipmentService = equipmentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EquipmentResponse addEquipment(@RequestBody EquipmentRequest request) {
        // TODO: call the service
        return null;
    }

    @GetMapping
    public List<EquipmentResponse> getAllEquipment() {
        // TODO: call the service
        return null;
    }

    @GetMapping("/{id}")
    public ResponseEntity<EquipmentResponse> getEquipment(@PathVariable long id) {
        // TODO: call the service; return 404 (ResponseEntity.notFound()) if null, else 200 (ResponseEntity.ok)
        return null;
    }

    @PutMapping("/{id}")
    public void updateEquipment(@PathVariable long id, @RequestBody EquipmentRequest request) {
        // TODO: call the service
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEquipment(@PathVariable long id) {
        // TODO: call the service
    }
}
