package kmapper

import com.google.gson.GsonBuilder
import org.junit.Test
import kotlin.test.assertEquals

/**
 * @author Augustus
 *         created on 2022.11.13
 */
class TestKMapper {
    private val gson = GsonBuilder().setPrettyPrinting().create()

    data class DataFrom(
        val personId: Int,
        val firstName: String,
        val lastName: String,
        val code: String,
        val age: Int,
        val tel: String,
    )

    data class DataTo(
        val id: Int,
        val name: String,
        val code: String,
        val tel: String
    )

    private val toPersonRecordTransformer = transformer<DataFrom, DataTo>(
        DataTo::id to { it.personId },
        DataTo::name to { "${it.firstName} ${it.lastName}" },
        // all others are copied by name
    )


    fun DataFrom.toDataTo(): DataTo = toPersonRecordTransformer.transform(this)

    @Test
    fun test_list__should_transform_all() {
        val fromList = listOf(samplePerson(1), samplePerson(2), samplePerson(3))
        val resultList = fromList.map { it.toDataTo() }
        println(gson.toJson(resultList))
        val expected = listOf(
            DataTo(
                id = 101,
                name = "Name1 Surname1",
                code = "123456-1",
                tel = "+3706001",
            ),
            DataTo(
                id = 102,
                name = "Name2 Surname2",
                code = "123456-2",
                tel = "+3706002",
            ),
            DataTo(
                id = 103,
                name = "Name3 Surname3",
                code = "123456-3",
                tel = "+3706003",
            ),
        )
        assertEquals(expected, resultList)
    }

    @Test
    fun test_optional__should_use_option_if_not_provided() {

        data class Data(val id: Int, val name: String = "optional")

        val transformer = transformer<DataFrom, Data>(
            Data::id to { it.personId }
        )
        val from = samplePerson(1)
        val to = transformer.transform(from)

        assertEquals(Data(101, "optional"), to)
    }


    private fun samplePerson(num: Int) = DataFrom(
        personId = 100 + num,
        firstName = "Name$num",
        lastName = "Surname$num",
        code = "123456-$num",
        age = 10 + num,
        tel = "+370600$num",
    )
}
