package kmapper

import com.google.gson.GsonBuilder
import org.junit.Test
import java.time.LocalDate
import kotlin.test.assertEquals

/**
 * @author Augustus
 *         created on 2022.11.13
 */
class TestKMapperSample {
    private val gson = GsonBuilder().setPrettyPrinting().create()

    data class PersonFrom(
        val personId: Int,
        val firstName: String,
        val lastName: String,
        val code: String,
        val age: Int,
        val tel: String,
    )

    data class PersonRec(
        val id: Int,
        val name: String,
        val code: String,
        val birthYear: Int,
        val tel: String
    )

    private val toPersonRecordTransformer = transformer<PersonFrom, PersonRec>(
        PersonRec::id to { it.personId },
        PersonRec::name to { "${it.firstName} ${it.lastName}" },
        PersonRec::birthYear to { LocalDate.now().year - it.age },
        // all others are copied by name
    )


    fun PersonFrom.toPersonRec(): PersonRec = toPersonRecordTransformer.transform(this)

    @Test
    fun test_sample() {
        val fromList = listOf(samplePerson(1), samplePerson(2), samplePerson(3))
        val resList = fromList.map { it.toPersonRec() }
        println(gson.toJson(resList))
        assertEquals("Name1 Surname1", resList.first().name)
        assertEquals("123456-1", resList.first().code)
    }

    private fun samplePerson(num: Int) = PersonFrom(
        personId = 100 + num,
        firstName = "Name$num",
        lastName = "Surname$num",
        code = "123456-$num",
        age = 10 + num,
        tel = "+370600$num",
    )
}
