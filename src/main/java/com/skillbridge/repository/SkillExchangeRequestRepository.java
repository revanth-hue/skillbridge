package com.skillbridge.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.skillbridge.entity.SkillExchangeRequest;
import com.skillbridge.entity.User;

public interface SkillExchangeRequestRepository
        extends JpaRepository<SkillExchangeRequest, Long> {

    List<SkillExchangeRequest> findByReceiver(User receiver);

    List<SkillExchangeRequest> findBySender(User sender);

    List<SkillExchangeRequest> findBySenderOrReceiverOrderByCreatedAtDesc(
            User sender,
            User receiver);

    boolean existsBySenderAndReceiverAndStatus(
            User sender,
            User receiver,
            String status
    );
}