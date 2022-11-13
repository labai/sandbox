package kmapper

import kotlin.jvm.internal.CallableReference
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor

/**
 * @author Augustus
 *         created on 2022.11.12
 */

inline fun <reified F : Any, reified R : Any> transformer(vararg args: Pair<Any, (F) -> Any?>): Transformer<F, R> {
    return Transformer(F::class, R::class, argsMap(*args))
}

class Transformer<F : Any, R : Any>(
    fromClass: KClass<F>,
    toClass: KClass<R>,
    private val mapConf: Map<String, (F) -> Any?>
) {
    private val toConstructor = toClass.primaryConstructor!!

    private val fromPropertiesByName by lazy {
        fromClass.memberProperties.associateBy { it.name }
    }

    private val toPropertiesByName by lazy {
        toConstructor.parameters.filter { it.name in mapConf || !it.isOptional }
    }

    fun transform(from: F): R {
        val params = toPropertiesByName.associateWith { param ->
            argFor(param, from)
        }
        return toConstructor.callBy(params)
    }

    private fun argFor(param: KParameter, from: F): Any? {
        val name = param.name ?: return null
        return if (name in mapConf) {
            mapConf[name]?.invoke(from)
        } else {
            val p = fromPropertiesByName[param.name]
            if (p == null && !param.isOptional)
                throw IllegalArgumentException("Parameter '${param.name}' is missing")
            p?.get(from)
        }
    }
}

fun <F> argsMap(vararg args: Pair<Any, (F) -> Any?>): Map<String, (F) -> Any?> {
    return args.associate { (field, valueFn) ->
        val name: String = when (field) {
            is String -> field
            is CallableReference -> field.name
            else -> throw IllegalArgumentException("mapping should be to pair: 'fieldName to { value }' or 'fieldKProperty to { value }' ")
        }
        name to valueFn
    }
}
