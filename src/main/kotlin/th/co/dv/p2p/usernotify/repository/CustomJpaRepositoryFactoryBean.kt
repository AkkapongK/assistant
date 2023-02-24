package th.co.dv.p2p.usernotify.repository

import org.slf4j.LoggerFactory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation
import org.springframework.data.repository.core.RepositoryInformation
import org.springframework.data.repository.core.RepositoryMetadata
import org.springframework.data.repository.core.support.RepositoryFactorySupport
import th.co.dv.p2p.common.repositories.CustomJpaRepository
import th.co.dv.p2p.common.repositories.CustomJpaRepositoryImpl
import th.co.dv.p2p.usernotify.constants.CommonConstant
import java.io.Serializable
import javax.persistence.EntityManager

class CustomJpaRepositoryFactoryBean<R : JpaRepository<T, ID>, T, ID : Serializable>(repositoryInterface: Class<R>) : JpaRepositoryFactoryBean<R, T, ID>(repositoryInterface) {

    override fun createRepositoryFactory(entityManager: EntityManager): RepositoryFactorySupport {

        return MyRepositoryFactory<T, ID>(entityManager)
    }

    private class MyRepositoryFactory<T, I : Serializable>(entityManager: EntityManager) : JpaRepositoryFactory(entityManager) {
        private var logger = LoggerFactory.getLogger(javaClass)!!

        @Suppress("UNCHECKED_CAST")
        override fun getTargetRepository(information: RepositoryInformation, entityManager: EntityManager): JpaRepositoryImplementation<*, *> {
            if (CustomJpaRepository::class.java.isAssignableFrom(information.repositoryInterface)) {
                logger.debug("Creating CustomJpaRepository for " + information.domainType)
                return CustomJpaRepositoryImpl<T, I>(information.domainType as Class<T>, entityManager, CommonConstant.schema)
            }
            return super.getTargetRepository(information)
        }

        override fun getRepositoryBaseClass(metadata: RepositoryMetadata): Class<*> {
            return if (CustomJpaRepository::class.java.isAssignableFrom(metadata.repositoryInterface)) {
                CustomJpaRepository::class.java
            } else super.getRepositoryBaseClass(metadata)
        }
    }
}