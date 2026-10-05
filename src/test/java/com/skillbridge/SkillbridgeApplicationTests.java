package com.skillbridge;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.skillbridge.entity.Skill;
import com.skillbridge.entity.SkillExchangeRequest;
import com.skillbridge.entity.User;
import com.skillbridge.repository.CommunityPostRepository;
import com.skillbridge.repository.SkillExchangeRequestRepository;
import com.skillbridge.repository.SkillRepository;
import com.skillbridge.repository.UserRepository;
import com.skillbridge.service.DemoDataInitializer;
import com.skillbridge.service.SkillExchangeRequestService;
import com.skillbridge.service.SkillMatchingService;
import com.skillbridge.service.UserService;

@SpringBootTest
@AutoConfigureMockMvc
class SkillbridgeApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserService userService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private SkillExchangeRequestRepository requestRepository;

	@Autowired
	private CommunityPostRepository communityPostRepository;

	@Autowired
	private SkillRepository skillRepository;

	@Autowired
	private SkillExchangeRequestService requestService;

	@Autowired
	private SkillMatchingService matchingService;

	private User testUser;

	@BeforeEach
	void createTestUser() {
		testUser = new User();
		testUser.setFullName("Integration Test Member");
		testUser.setEmail("integration-" + UUID.randomUUID() + "@example.test");
		testUser.setPassword("test-password-123");
		testUser = userService.registerUser(testUser);
	}

	@AfterEach
	void removeTestUser() {
		if (testUser != null && testUser.getId() != null) {
			communityPostRepository.deleteByAuthor(testUser);
			userRepository.deleteById(testUser.getId());
		}
	}

	@Test
	void contextLoads() {
	}

	@Test
	void signedInMemberCanRenderAllDashboardModules() throws Exception {
		MvcResult loginResult = mockMvc.perform(post("/login")
				.param("email", testUser.getEmail())
				.param("password", "test-password-123"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/dashboard"))
			.andReturn();
		MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);

		mockMvc.perform(get("/dashboard").session(session)).andExpect(status().isOk());
		mockMvc.perform(get("/skill-matching").session(session)).andExpect(status().isOk());
		mockMvc.perform(get("/mentors").session(session)).andExpect(status().isOk());
		mockMvc.perform(get("/my-skills").session(session)).andExpect(status().isOk());
		mockMvc.perform(get("/requests").session(session)).andExpect(status().isOk());
		mockMvc.perform(get("/sessions").session(session)).andExpect(status().isOk());
		mockMvc.perform(get("/resources").session(session)).andExpect(status().isOk());
		mockMvc.perform(get("/assignments").session(session)).andExpect(status().isOk());
		mockMvc.perform(get("/progress").session(session)).andExpect(status().isOk());
		mockMvc.perform(get("/settings").session(session)).andExpect(status().isOk());
		mockMvc.perform(get("/community").session(session)).andExpect(status().isOk());
	}

	@Test
	void dashboardModulesRequireAnAuthenticatedSession() throws Exception {
		for (String route : List.of("/dashboard", "/skill-matching", "/mentors",
				"/my-skills", "/requests", "/sessions", "/resources",
				"/assignments", "/progress", "/settings", "/community")) {
			mockMvc.perform(get(route))
					.andExpect(status().is3xxRedirection())
					.andExpect(redirectedUrl("/login"));
		}
	}

	@Test
	void communityOpportunityPostIsVisibleOnlyInItsSkillFeed() throws Exception {
		Skill javaSkill = skillRepository.findByName("Java Development").orElseThrow();
		Skill designSkill = skillRepository.findByName("UI/UX Design").orElseThrow();
		String headline = "Internship " + UUID.randomUUID();
		MockHttpSession session = new MockHttpSession();
		session.setAttribute("userId", testUser.getId());

		mockMvc.perform(post("/community/posts").session(session)
				.param("skillId", javaSkill.getId().toString())
				.param("postType", "INTERNSHIP")
				.param("title", headline)
				.param("content", "A paid internship for Java learners.")
				.param("linkUrl", "https://example.org/java-internship"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/community?skillId=" + javaSkill.getId()));

		mockMvc.perform(get("/community").param("skillId", javaSkill.getId().toString())
				.session(session))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString(headline)))
			.andExpect(content().string(containsString("Open link")));

		String unrelatedFeed = mockMvc.perform(get("/community")
				.param("skillId", designSkill.getId().toString()).session(session))
			.andExpect(status().isOk())
			.andReturn().getResponse().getContentAsString();
		assertFalse(unrelatedFeed.contains(headline));
	}

	@Test
	void demoDataProvidesAtLeastFiftyLoginReadyMembersAndMutualMatches() {
		assertTrue(userRepository.count() >= 50);
		User firstDemoMember = userRepository.findByEmail("demo.member01@skillbridge.demo")
				.orElseThrow();
		User secondDemoMember = userRepository.findByEmail("demo.member02@skillbridge.demo")
				.orElseThrow();
		assertTrue(userService.loginUser(firstDemoMember.getEmail(),
				DemoDataInitializer.DEMO_PASSWORD).isPresent());
		assertTrue(matchingService.findMatches(firstDemoMember).stream()
				.anyMatch(match -> match.getId().equals(secondDemoMember.getId())));
	}

	@Test
	void acceptedExchangeCanBeScheduledAndCompletedByParticipants() throws Exception {
		User recipientDraft = new User();
		recipientDraft.setFullName("Exchange Recipient");
		recipientDraft.setEmail("recipient-" + UUID.randomUUID() + "@example.test");
		recipientDraft.setPassword("recipient-password-123");
		User recipient = userService.registerUser(recipientDraft);
		SkillExchangeRequest request = null;
		try {
			request = requestService.sendRequest(testUser, recipient.getId(), "Let's learn together");
			Long requestId = request.getId();
			assertEquals("You cannot update this request.",
					assertThrows(RuntimeException.class,
							() -> requestService.acceptRequest(requestId, testUser)).getMessage());

			request = requestService.acceptRequest(requestId, recipient);
			assertEquals("ACCEPTED", request.getStatus());
			assertEquals("Only the mentor can schedule this class.",
					assertThrows(RuntimeException.class,
							() -> requestService.scheduleSession(requestId, testUser,
									LocalDateTime.now().plusDays(1),
									"https://meet.google.com/abc-defg-hij")).getMessage());
			assertEquals("Use an https://meet.google.com link.",
					assertThrows(RuntimeException.class,
							() -> requestService.scheduleSession(requestId, recipient,
									LocalDateTime.now().plusDays(1),
									"https://example.com/class")).getMessage());
			request = requestService.scheduleSession(requestId, recipient,
					LocalDateTime.now().plusDays(1),
					"https://meet.google.com/abc-defg-hij");
			assertEquals("SCHEDULED", request.getStatus());
			assertEquals("https://meet.google.com/abc-defg-hij", request.getGoogleMeetUrl());
			MockHttpSession mentorSession = new MockHttpSession();
			mentorSession.setAttribute("userId", recipient.getId());
			mockMvc.perform(get("/sessions").session(mentorSession))
					.andExpect(status().isOk())
					.andExpect(content().string(containsString("Join Google Meet")))
					.andExpect(content().string(containsString("Update class")));
			request = requestService.completeSession(requestId, recipient);
			assertEquals("COMPLETED", request.getStatus());
			assertEquals("This request has already been handled.",
					assertThrows(RuntimeException.class,
							() -> requestService.acceptRequest(requestId, recipient)).getMessage());
		} finally {
			if (request != null) {
				requestRepository.deleteById(request.getId());
			}
			userRepository.deleteById(recipient.getId());
		}
	}

}
