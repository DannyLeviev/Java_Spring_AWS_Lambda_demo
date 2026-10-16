package com.dannyleviev.lambdademo.agreement;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.GetItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;

import java.util.List;
import java.util.Optional;

@Repository
public class AgreementRepository {

    private final DynamoDbTable<AgreementItem> table;

    public AgreementRepository(DynamoDbEnhancedClient client,
                               @Value("${app.dynamodb.table-name}") String tableName) {
        this.table = client.table(tableName, TableSchema.fromBean(AgreementItem.class));
    }

    /** Conditional put: never silently overwrite an existing agreement. */
    public void insert(AgreementItem item) {
        table.putItem(PutItemEnhancedRequest.builder(AgreementItem.class)
                .item(item)
                .conditionExpression(Expression.builder()
                        .expression("attribute_not_exists(id)")
                        .build())
                .build());
    }

    /** Strongly consistent read so a GET right after POST never misses. */
    public Optional<AgreementItem> findById(String id) {
        return Optional.ofNullable(table.getItem(GetItemEnhancedRequest.builder()
                .key(Key.builder().partitionValue(id).build())
                .consistentRead(true)
                .build()));
    }

    /** Scan is acceptable for a demo; production would use a GSI query + cursor pagination. */
    public List<AgreementItem> findAll(int limit) {
        return table.scan(ScanEnhancedRequest.builder().limit(limit).build())
                .items().stream()
                .limit(limit)
                .toList();
    }

    /** @return true if an item was actually deleted */
    public boolean deleteById(String id) {
        return table.deleteItem(Key.builder().partitionValue(id).build()) != null;
    }
}