package com.bsp.supportassistantplsql.repository;

import com.bsp.supportassistantplsql.dto.TicketResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Repository
public class TicketRepository {

    private static final String PACKAGE = "SUPPORT_TICKET_PKG";

    private final SimpleJdbcCall openCount, createTicket, findTickets;


    public TicketRepository(JdbcTemplate jdbcTemplate) {
        this.openCount = new SimpleJdbcCall(jdbcTemplate)
                .withCatalogName(PACKAGE).withFunctionName("OPEN_COUNT");
        this.createTicket = new SimpleJdbcCall(jdbcTemplate)
                .withCatalogName(PACKAGE).withProcedureName("CREATE_TICKET");
        this.findTickets = new SimpleJdbcCall(jdbcTemplate)
                .withCatalogName(PACKAGE).withProcedureName("FIND_TICKETS")
                .returningResultSet("P_TICKETS", this::toTicket);

    }

    public int openCount(String priority) {
        Number count = openCount.executeFunction(Number.class, priority);
        return count.intValue();
    }

    public long createTicket(String title, String description, String priority) {
        Map<String, Object> out = createTicket.execute(title, description, priority);
        return ((Number) out.get("P_TICKET_ID")).longValue();
    }

    public List<TicketResponse> findTickets(String status){
        Map<String, Object> out= findTickets.execute(status);
        return (List<TicketResponse>) out.get("P_TICKETS");

    }


    private TicketResponse toTicket(ResultSet rs, int rowNumber) throws SQLException {
        return new TicketResponse(
                rs.getLong("ID"),
                rs.getString("TITLE"),
                rs.getString("DESCRIPTION"),
                rs.getString("STATUS"),
                rs.getString("PRIORITY"),
                rs.getTimestamp("CREATED_AT").toLocalDateTime(),
                rs.getTimestamp("DUE_BY").toLocalDateTime());
    }






}

