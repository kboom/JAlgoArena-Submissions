package com.jalgoarena.domain

import com.jalgoarena.data.SubmissionsRepository
import com.jalgoarena.ranking.BasicRankingCalculator
import com.jalgoarena.ranking.BasicScoreCalculator
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.mock

class BasicRankingCalculatorSpec {

    private lateinit var repository: SubmissionsRepository

    @Before
    fun setUp() {
        repository = mock(SubmissionsRepository::class.java)
    }

    @Test
    fun returns_empty_ranking_if_no_users() {
        given(repository.findAll()).willReturn(emptyList())

        val rankingCalculator = basicRankingCalculator(repository)

        assertThat(rankingCalculator.ranking(emptyArray())).isEqualTo(emptyList<RankEntry>())
    }

    @Test
    fun returns_all_users_with_0_score_if_no_submissions() {
        given(repository.findAll()).willReturn(emptyList())

        val rankingCalculator = basicRankingCalculator(repository)

        assertThat(rankingCalculator.ranking(USERS)).isEqualTo(listOf(
                RankEntry("mikołaj", 0.0, emptyList(), "Kraków", "Tyniec Team"),
                RankEntry("julia", 0.0, emptyList(), "Kraków", "Tyniec Team"),
                RankEntry("joe", 0.0, emptyList(), "London", "London Team"),
                RankEntry("tom", 0.0, emptyList(), "London", "London Team")
        ))
    }

    @Test
    fun returns_users_in_descending_order_based_on_their_score_and_if_user_equal_follwing_creation_of_user_order() {
        given(repository.findAll()).willReturn(listOf(
                submission("fib", 1, 0.01, USER_MIKOLAJ.id),
                submission("fib", 1, 0.011, USER_JULIA.id),
                submission("2-sum", 2, 0.01, USER_JOE.id),
                submission("2-sum", 2, 0.011, USER_TOM.id),
                submission("word-ladder", 3, 0.01, USER_MIKOLAJ.id),
                submission("word-ladder", 3, 0.011, USER_JULIA.id)
        ))

        val rankingCalculator = basicRankingCalculator(repository)

        assertThat(rankingCalculator.ranking(USERS)).isEqualTo(listOf(
                RankEntry("mikołaj", 40.0, listOf("fib", "word-ladder"), "Kraków", "Tyniec Team"),
                RankEntry("julia", 40.0, listOf("fib", "word-ladder"), "Kraków", "Tyniec Team"),
                RankEntry("joe", 20.0, listOf("2-sum"), "London", "London Team"),
                RankEntry("tom", 20.0, listOf("2-sum"), "London", "London Team")
        ))
    }

    @Test
    fun returns_empty_problem_ranking_when_no_submissions_for_problem() {
        given(repository.findByProblemId("fib")).willReturn(emptyList())

        val rankingCalculator = basicRankingCalculator(repository)

        assertThat(rankingCalculator.problemRanking("fib", USERS))
                .isEqualTo(emptyList<ProblemRankEntry>())
    }

    @Test
    fun returns_problem_ranking_sorted_by_times() {
        given(repository.findByProblemId("fib")).willReturn(listOf(
                submission("fib", 1, 0.01, USER_MIKOLAJ.id),
                submission("fib", 1, 0.0001, USER_JULIA.id),
                submission("fib", 1, 0.001, USER_JOE.id),
                submission("fib", 1, 0.1, USER_TOM.id)
        ))

        val rankingCalculator = basicRankingCalculator(repository)

        assertThat(rankingCalculator.problemRanking("fib", USERS)).isEqualTo(listOf(
                ProblemRankEntry("julia", 10.0, 0.0001, "java"),
                ProblemRankEntry("joe", 10.0, 0.001, "java"),
                ProblemRankEntry("mikołaj", 10.0, 0.01, "java"),
                ProblemRankEntry("tom", 10.0, 0.1, "java")
        ))
    }

    private fun basicRankingCalculator(repository: SubmissionsRepository) =
            BasicRankingCalculator(repository, BasicScoreCalculator())

    private fun submission(problemId: String, level: Int, elapsedTime: Double, userId: String) =
            Submission(problemId, level, elapsedTime, DUMMY_SOURCE_CODE, STATUS_ACCEPTED, userId, "java")

    private val USER_MIKOLAJ = User("mikołaj", "Kraków", "Tyniec Team", "USER", "0-0")
    private val USER_JULIA = User("julia", "Kraków", "Tyniec Team", "USER", "0-1")
    private val USER_JOE = User("joe", "London", "London Team", "USER", "0-2")
    private val USER_TOM = User("tom", "London", "London Team", "USER", "0-3")

    private val USERS = arrayOf(USER_MIKOLAJ, USER_JULIA, USER_JOE, USER_TOM)

    private val DUMMY_SOURCE_CODE = "dummy source code"
    private val STATUS_ACCEPTED = "ACCEPTED"
}
