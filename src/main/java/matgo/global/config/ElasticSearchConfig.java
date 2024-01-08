package matgo.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.data.elasticsearch.config.EnableElasticsearchAuditing;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@EnableElasticsearchRepositories
@EnableElasticsearchAuditing
public class ElasticSearchConfig extends ElasticsearchConfiguration {

    @Value("${elasticsearch.host}")
    private String host;

    @Value("${elasticsearch.port}")
    private int port;

    @Override
    public ClientConfiguration clientConfiguration() {
        return ClientConfiguration.builder()
                                  .connectedTo(host + ":" + port)
                                  .build();
    }
}
