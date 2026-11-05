package com.bsp.supportassistantplsql.service;

import com.bsp.supportassistantplsql.dto.CreateTicketRequest;
import com.bsp.supportassistantplsql.dto.TicketResponse;
import com.bsp.supportassistantplsql.exception.InvalidRequestException;
import com.bsp.supportassistantplsql.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service("ticketService")

public class TicketService {


    @Autowired
    private TicketRepository ticketRepo;

    public  TicketService(){
        System.out.println("Bean Created");
    }

//    public TicketService(TicketRepository ticketRepo) {
//        this.ticketRepo = ticketRepo;
//    }

    public long createTicket(CreateTicketRequest ticketRequest){
        return ticketRepo.createTicket(ticketRequest.title(),
                ticketRequest.description(),ticketRequest.priority());
    }

    public int openCount(String priority){
        return ticketRepo.openCount(priority);
    }

    // The package filters by status. The priority filter is done here, on the rows that come back.
    public List<TicketResponse> findTickets(String status, String priority){
        if (status != null){
            status = status.toUpperCase();
        }

        List<TicketResponse> tickets = ticketRepo.findTickets(status);
        if (priority == null){
            return tickets;
        }
        return tickets.stream()
                .filter(ticket -> ticket.priority().equalsIgnoreCase(priority))
                .toList();
    }

    public void changeStatus(long ticketId, String newStatus){
        ticketRepo.changeStatus(ticketId, newStatus == null ? null : newStatus.toUpperCase());
    }

    public void deleteTicket(long ticketId){
        ticketRepo.deleteTicket(ticketId);
    }

    // All or nothing: if one ticket cannot change, the tickets before it are put back.
    @Transactional
    public void changeStatus(List<Long> ticketIds, String newStatus){
        if (ticketIds == null || ticketIds.isEmpty()){
            throw new InvalidRequestException("ticketIds must hold at least one id");
        }
        for (Long ticketId : ticketIds){
            changeStatus(ticketId, newStatus);
        }
    }

}
