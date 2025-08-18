package agent.timing.mongo;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.bson.Document;

import java.util.Map;

public class MongoReporter {

    private static final String URI = "mongodb+srv://admin:admin@ecosystemaccountcluster.3wsyrgk.mongodb.net/?retryWrites=true&w=majority&appName=EcosystemAccountClusterTesis";
    private static final String DATABASE = "ecosystem_accounts";
    private static final String COLLECTION = "ecosystem_accounts_data";

    private static MongoClient mongoClient;

    public static void report(Map<String, String> data, String metric, Double metricValue, String application, String platform) {
        Document document = new Document();
        document.append(metric, metricValue);
        document.append("application", application);
        document.append("platform", platform);
        data.forEach((key, value) -> document.append(key, value));

        MongoReporter
                .getClient()
                .getDatabase(DATABASE)
                .getCollection(COLLECTION)
                .insertOne(document);
    }

    private static MongoClient getClient() {
        if (MongoReporter.mongoClient == null) {
            mongoClient = MongoClients.create(URI);
        }
        return mongoClient;
    }

}
