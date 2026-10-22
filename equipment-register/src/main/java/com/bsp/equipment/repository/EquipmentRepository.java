package com.bsp.equipment.repository;

import com.bsp.equipment.dto.EquipmentResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class EquipmentRepository {

    private static final String PACKAGE = "EQUIPMENT_PKG";

    // TODO: declare one SimpleJdbcCall field per procedure in EQUIPMENT_PKG

    public EquipmentRepository(JdbcTemplate jdbcTemplate) {
        // TODO: create each SimpleJdbcCall (withCatalogName + withProcedureName)
        //       use returningResultSet("P_RESULT", this::toEquipment) for the cursor procedures
    }

    public long addEquipment(String name, String department, String status) {
        // TODO: call ADD_EQUIPMENT and return the OUT parameter P_ID
        return 0;
    }

    public List<EquipmentResponse> getAllEquipment() {
        // TODO: call GET_ALL_EQUIPMENT and return the P_RESULT list
        return null;
    }

    public EquipmentResponse getEquipment(long id) {
        // TODO: call GET_EQUIPMENT, return the first row or null if not found
        return null;
    }

    public void updateEquipment(long id, String name, String department, String status) {
        // TODO: call UPDATE_EQUIPMENT
    }

    public void deleteEquipment(long id) {
        // TODO: call DELETE_EQUIPMENT
    }

    private EquipmentResponse toEquipment(ResultSet rs, int rowNumber) throws SQLException {
        // TODO: map columns ID, NAME, DEPARTMENT, STATUS to an EquipmentResponse
        return null;
    }
}
