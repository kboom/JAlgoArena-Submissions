package com.jalgoarena.web

import com.fasterxml.jackson.databind.node.ArrayNode
import com.jalgoarena.data.SubmissionsRepository
import com.jalgoarena.domain.Submission
import com.jalgoarena.domain.User
import org.hamcrest.Matchers.hasSize
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.BDDMockito.given
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import javax.inject.Inject

@RunWith(SpringRunner::class)
@WebMvcTest(SubmissionsController::class)
class SubmissionsControllerSpec {

    @Inject
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var usersClient: UsersClient

    @MockBean
    private lateinit var submissionRepository: SubmissionsRepository

    @Test
    fun returns_401_if_user_is_not_authorized() {
        mockMvc.perform(get("/submissions")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized)
    }

    @Test
    fun returns_401_if_user_is_not_admin() {
        given(usersClient.findUser(DUMMY_TOKEN)).willReturn(USER)

        mockMvc.perform(get("/submissions")
                .header("X-Authorization", DUMMY_TOKEN)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized)
    }

    @Test
    fun returns_401_when_user_is_unidentified() {
        given(usersClient.findUser(DUMMY_TOKEN)).willReturn(null)

        mockMvc.perform(get("/submissions")
                .header("X-Authorization", DUMMY_TOKEN)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized)
    }

    @Test
    fun returns_200_and_all_submissions_to_admin() {
        given(usersClient.findUser(DUMMY_TOKEN)).willReturn(ADMIN)

        given(submissionRepository.findAll()).willReturn(listOf(
            submission("mikołaj"),
            submission("julia"),
            submission("madzia")
        ))

        mockMvc.perform(get("/submissions")
                .header("X-Authorization", DUMMY_TOKEN)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$", hasSize<ArrayNode>(3)))
    }

    private val USER = User("", "", "", "USER", "")
    private val ADMIN = User("", "", "", "ADMIN", "")

    private val DUMMY_TOKEN = "Bearer 123j12n31lkmdp012j21d"

    private fun submission(userId: String) =
            Submission("fib", 1, 0.5, "class Solution", "ACCEPTED", userId, "java")
}