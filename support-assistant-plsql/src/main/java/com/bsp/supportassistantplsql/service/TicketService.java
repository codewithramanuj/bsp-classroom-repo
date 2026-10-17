package com.bsp.supportassistantplsql.service;

import com.bsp.supportassistantplsql.dto.CreateTicketRequest;
import com.bsp.supportassistantplsql.dto.TicketResponse;
import com.bsp.supportassistantplsql.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

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

    public List<TicketResponse> findTickets(String status){
        if (status != null){
            status = status.toUpperCase();
        }

        return ticketRepo.findTickets(status);
    }

}
