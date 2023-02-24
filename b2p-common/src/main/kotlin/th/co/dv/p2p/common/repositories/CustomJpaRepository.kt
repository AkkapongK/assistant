package th.co.dv.p2p.common.repositories

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.NoRepositoryBean
import th.co.dv.p2p.common.models.AggregateResponseModel
import th.co.dv.p2p.common.models.NativeQueryModel
import th.co.dv.p2p.common.models.UserAuthorization
import th.co.dv.p2p.common.utilities.search.SearchCriterias
import java.io.Serializable
import javax.persistence.Query

@NoRepositoryBean
interface CustomJpaRepository<T, ID : Serializable> : JpaRepository<T, ID> {
    fun list(criterias: SearchCriterias<*>,
             param: Map<String, Any>,
             operation: Map<String, String>,
             userAuthorization: UserAuthorization,
             headerModelClass: Class<*>,
             headerClass: Class<*>,
             itemClass: Class<*>? = null,
             skipAuthorization: Boolean = false): List<T>

    fun <H : Any, I : Any, M : Any> countByNativeQuery(nativeQuery: NativeQueryModel,
                                                       criterias: SearchCriterias<*>,
                                                       headerClass: Class<H>,
                                                       itemClass: Class<I>,
                                                       headerModelClass: Class<M>): Int

    fun <H : Any, I : Any, M : Any> native(nativeQuery: NativeQueryModel,
                                           criterias: SearchCriterias<*>,
                                           headerClass: Class<H>,
                                           itemClass: Class<I>,
                                           headerModelClass: Class<M>,
                                           extraClass: Class<Any>? = null): List<T>

    fun <H : Any, I : Any, M : Any> nativeAggregate(nativeQuery: NativeQueryModel,
                                                    criterias: SearchCriterias<*>,
                                                    headerClass: Class<H>,
                                                    itemClass: Class<I>,
                                                    headerModelClass: Class<M>): List<AggregateResponseModel>

    fun count(criterias: SearchCriterias<*>): Int

    fun sqlStatement(nativeQuery: NativeQueryModel,
                     criterias: SearchCriterias<*>,
                     headerClass: Class<*>? = null,
                     itemClass: Class<*>? = null,
                     extraClass: Class<Any>? = null): String

    fun sqlStatementWithAuth(nativeQuery: NativeQueryModel,
                             criterias: SearchCriterias<*>,
                             headerModelClass: Class<*>,
                             headerClass: Class<*>? = null,
                             itemClass: Class<*>? = null,
                             extraClass: Class<Any>? = null): String?

    fun nativeQuery(nativeQuery: NativeQueryModel, criterias: SearchCriterias<*>): Query
    fun executeNative(sqlStatement: String, criterias: SearchCriterias<*>): List<T>
    fun executeNativeWithPaging(sqlStatement: String, criterias: SearchCriterias<*>): List<T>
    fun aggregate(sql: String, criterias: SearchCriterias<*>, fields: List<String>): List<AggregateResponseModel>
}