package eu.vargasoft.lambda.contractmonitor;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.ScanOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;

public class LambdaFunctionHandler implements RequestHandler<Object, String> {

	@Override
	public String handleRequest(Object input, Context context) {
		context.getLogger().log("DB_TABLE_NAME: " + System.getenv("DB_TABLE_NAME"));
		context.getLogger().log("SNS_TOPIC: " + System.getenv("SNS_TOPIC"));

		AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().withRegion(Regions.EU_CENTRAL_1).build();
		DynamoDB dbClient = new DynamoDB(client);
		Table table = dbClient.getTable(System.getenv("DB_TABLE_NAME"));

		String filterExpression = "(#active = :active) AND (#noticabletill BETWEEN :from AND :to)";
		Map<String, String> nameMap = new HashMap<>();
		nameMap.put("#active", "active");
		nameMap.put("#noticabletill", "noticabletill");

		Map<String, Object> valueMap = new HashMap<>();
		valueMap.put(":active", true);
		LocalDate now = LocalDate.now();
		LocalDate inXDays = now.plus(Integer.parseInt(System.getenv("NOTICE_RANGE_DAYS")), ChronoUnit.DAYS);
		valueMap.put(":from", now.toString());
		valueMap.put(":to", inXDays.toString());

		ItemCollection<ScanOutcome> result = table.scan(filterExpression, nameMap, valueMap);

		StringBuilder message = new StringBuilder();
		message.append(
				"Following contracts can be noticed in the next " + System.getenv("NOTICE_RANGE_DAYS") + " days:<br>");
		for (Item item : result) {
			context.getLogger().log(item.toJSONPretty());
			message.append(item.toJSONPretty() + "\n");
		}
		AmazonSNS snsClient = AmazonSNSClientBuilder.standard().build();
		PublishRequest publishRequest = new PublishRequest(System.getenv("SNS_TOPIC"), message.toString());
		publishRequest.setSubject("Contract Monitor");

		PublishResult publishResult = snsClient.publish(publishRequest);
		return "Message sent! " + publishResult;
	}
}
