package fi.vm.yti.cls.api.configuration;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.jetty.JettyEmbeddedServletContainerFactory;
import org.springframework.boot.web.servlet.ErrorPage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.http.HttpStatus;

import java.net.InetAddress;
import java.net.UnknownHostException;


@Configuration
@PropertySource(value = "classpath", ignoreResourceNotFound = true)
public class SpringAppConfig {

    @Value("${cls_public_api_service_elastic_host}")
    protected String m_elasticsearchHost;

    @Value("${cls_public_api_service_elastic_port}")
    protected Integer m_elasticsearchPort;

    @Value("${cls_public_api_service_elastic_cluster}")
    protected String m_clusterName;

    @Value(value = "${application.contextPath}")
    private String m_contextPath;


    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {

        return new PropertySourcesPlaceholderConfigurer();

    }


    @Bean
    public EmbeddedServletContainerFactory servletContainer() {

        final JettyEmbeddedServletContainerFactory factory = new JettyEmbeddedServletContainerFactory();
        factory.addErrorPages(new ErrorPage(HttpStatus.NOT_FOUND, "/notfound.html"));
        factory.setContextPath(m_contextPath);
        return factory;

    }


    @Bean
    protected Client elasticsearchClient() throws UnknownHostException {

        final TransportAddress address = new InetSocketTransportAddress(InetAddress.getByName(m_elasticsearchHost), m_elasticsearchPort);
        final Settings settings = Settings.builder().put("cluster.name", m_clusterName).put("client.transport.ignore_cluster_name", false).put("client.transport.sniff", false).build();
        try (PreBuiltTransportClient preBuiltTransportClient = new PreBuiltTransportClient(settings)) {
            return preBuiltTransportClient.addTransportAddress(address);            
        }

    }

}