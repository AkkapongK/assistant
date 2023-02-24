package th.co.dv.p2p.common.kafka

import org.apache.kafka.clients.admin.NewTopic
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor
import org.springframework.beans.factory.support.RootBeanDefinition
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.env.EnumerablePropertySource
import th.co.dv.p2p.common.constants.DOT


abstract class BaseKafkaTopicConfig(private val environment: ConfigurableEnvironment) : ApplicationContextAware, BeanDefinitionRegistryPostProcessor {

    private val TOPIC_PROPERTY_KEY = "kafka.topics"
    private val HOST_PROPERTY_KEY = "kafka.streamingHost"

    protected lateinit var appContext: ApplicationContext

    private val topics: MutableMap<String, Int> = mutableMapOf()
    protected lateinit var host: String

    protected abstract fun reinitializeKafka()

    override fun postProcessBeanFactory(beanFactory: ConfigurableListableBeanFactory) {}

    override fun postProcessBeanDefinitionRegistry(registry: BeanDefinitionRegistry) {
        readProperty(environment)
        println(host)
        println(topics)
        topics.forEach { (topicName, partition) -> register(registry, topicName, partition) }
        reinitializeKafka()
        println("===== reinitializeKafka done =====")
    }

    private fun register(registry: BeanDefinitionRegistry, topicName: String, partition: Int) {
        val definition: BeanDefinition = RootBeanDefinition(NewTopic::class.java)
        definition.constructorArgumentValues.addGenericArgumentValue(topicName)
        definition.constructorArgumentValues.addGenericArgumentValue(partition)
        definition.constructorArgumentValues.addGenericArgumentValue(1)
        registry.registerBeanDefinition(topicName, definition)
    }

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        appContext = applicationContext
    }

    private fun readProperty(environment: ConfigurableEnvironment) {
        for (source in environment.propertySources) {
            if (source is EnumerablePropertySource<*>) {
                for (property in source.propertyNames) {
                    if (property.startsWith(TOPIC_PROPERTY_KEY)) {
                        val key = property.split(DOT).last()
                        topics[key] = source.getProperty(property) as Int
                    } else if (HOST_PROPERTY_KEY == property) {
                        host = source.getProperty(HOST_PROPERTY_KEY) as String
                    }
                }
            }
        }
    }

}