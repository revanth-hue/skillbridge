package com.skillbridge.service;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.skillbridge.entity.SkillExchangeRequest;
import com.skillbridge.entity.User;
import com.skillbridge.repository.SkillExchangeRequestRepository;
import com.skillbridge.repository.UserRepository;

@Service
public class SkillExchangeRequestService {

    private final SkillExchangeRequestRepository requestRepository;
        private final UserRepository userRepository;

    public SkillExchangeRequestService(
                        SkillExchangeRequestRepository requestRepository,
                        UserRepository userRepository) {
        this.requestRepository = requestRepository;
                this.userRepository = userRepository;
    }

    // Send a new skill exchange request
    public SkillExchangeRequest sendRequest(
            User sender,
            Long receiverId,
            String message) {

        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("User not found."));

        // Prevent sending a duplicate pending request
        boolean alreadyExists =
                requestRepository.existsBySenderAndReceiverAndStatus(
                        sender,
                        receiver,
                        "PENDING"
                );

        if (alreadyExists) {
            throw new RuntimeException(
                    "A pending request already exists."
            );
        }

        // Prevent sending a request to yourself
        if (sender.getId().equals(receiver.getId())) {
            throw new RuntimeException(
                    "You cannot send a request to yourself."
            );
        }

        SkillExchangeRequest request =
                new SkillExchangeRequest();

        request.setSender(sender);
        request.setReceiver(receiver);
                if (message != null && message.length() > 500) {
                        throw new RuntimeException("Message must be 500 characters or fewer.");
                }
        request.setMessage(message == null || message.isBlank()
                ? null
                : message.trim());
        request.setStatus("PENDING");

        return requestRepository.save(request);
    }

    // Get requests received by a user
    public List<SkillExchangeRequest> getReceivedRequests(
            User user) {

        return requestRepository.findByReceiver(user);
    }

    // Get requests sent by a user
    public List<SkillExchangeRequest> getSentRequests(
            User user) {

        return requestRepository.findBySender(user);
    }

        public List<SkillExchangeRequest> getUserRequests(User user) {
                return requestRepository.findBySenderOrReceiverOrderByCreatedAtDesc(user, user)
                                .stream()
                                .filter(request -> !"PENDING".equals(request.getStatus())
                                                && !"REJECTED".equals(request.getStatus()))
                                .toList();
        }

        public SkillExchangeRequest scheduleSession(
                        Long requestId,
                        User mentor,
                        LocalDateTime scheduledAt,
                        String googleMeetUrl) {
                if (scheduledAt == null || !scheduledAt.isAfter(LocalDateTime.now())) {
                        throw new RuntimeException("Choose a session time in the future.");
                }
                if (googleMeetUrl == null || googleMeetUrl.isBlank()
                                || googleMeetUrl.trim().length() > 1000) {
                        throw new RuntimeException("Enter a valid Google Meet link.");
                }
                URI meetUri;
                try {
                        meetUri = URI.create(googleMeetUrl.trim());
                } catch (IllegalArgumentException e) {
                        throw new RuntimeException("Enter a valid Google Meet link.");
                }
                if (!"https".equalsIgnoreCase(meetUri.getScheme())
                                || !"meet.google.com".equalsIgnoreCase(meetUri.getHost())) {
                        throw new RuntimeException("Use an https://meet.google.com link.");
                }

                SkillExchangeRequest request = findParticipantRequest(requestId, mentor);
                if (!request.getReceiver().getId().equals(mentor.getId())) {
                        throw new RuntimeException("Only the mentor can schedule this class.");
                }
                if (!"ACCEPTED".equals(request.getStatus())
                                && !"SCHEDULED".equals(request.getStatus())) {
                        throw new RuntimeException("Only accepted exchanges can be scheduled.");
                }
                request.setScheduledAt(scheduledAt);
                request.setGoogleMeetUrl(meetUri.toString());
                request.setStatus("SCHEDULED");
                return requestRepository.save(request);
        }

        public SkillExchangeRequest completeSession(Long requestId, User participant) {
                SkillExchangeRequest request = findParticipantRequest(requestId, participant);
                if (!"SCHEDULED".equals(request.getStatus())) {
                        throw new RuntimeException("Only scheduled sessions can be completed.");
                }
                request.setStatus("COMPLETED");
                return requestRepository.save(request);
        }

    // Accept a request
    public SkillExchangeRequest acceptRequest(Long requestId, User receiver) {
        return updateStatus(requestId, receiver, "ACCEPTED");
    }

    public SkillExchangeRequest rejectRequest(Long requestId, User receiver) {
        return updateStatus(requestId, receiver, "REJECTED");
    }

    private SkillExchangeRequest updateStatus(
            Long requestId,
            User receiver,
            String status) {
        SkillExchangeRequest request =
                requestRepository.findById(requestId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Request not found."
                                ));

                if (!request.getReceiver().getId().equals(receiver.getId())) {
                        throw new RuntimeException("You cannot update this request.");
                }
                if (!"PENDING".equals(request.getStatus())) {
                        throw new RuntimeException("This request has already been handled.");
                }

                request.setStatus(status);

        return requestRepository.save(request);
    }

        private SkillExchangeRequest findParticipantRequest(
                        Long requestId,
                        User participant) {
                SkillExchangeRequest request = requestRepository.findById(requestId)
                                .orElseThrow(() -> new RuntimeException("Request not found."));
                boolean isParticipant = request.getSender().getId().equals(participant.getId())
                                || request.getReceiver().getId().equals(participant.getId());
                if (!isParticipant) {
                        throw new RuntimeException("You cannot update this session.");
                }
                return request;
        }
}