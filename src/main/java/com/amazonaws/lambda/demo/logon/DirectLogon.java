package com.amazonaws.lambda.demo.logon;

import java.io.IOException;
import java.net.URL;
import java.util.Date;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.amazonaws.lambda.demo.http.HTTPConnBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;

public class DirectLogon {

  JSONParser parser = new JSONParser();

  public String logon(JSONObject event) {

    
    String username = null;
    String password = null;
    String consumerKey = null;
    JSONObject responseToken = new JSONObject();
    String userId = null;

    if (event.get("body") != null) {
        JSONObject body;
    try {

          body = (JSONObject) parser.parse((String) event.get("body"));
          
           username = (String) body.get("username");  
          
            password = (String) body.get("password");

          AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
          DynamoDB dynamoDB = new DynamoDB(client);

          Table table = dynamoDB.getTable("User_Offerings");

          Item item = table.getItem("userName", username);

          consumerKey = item.get("consumer_key").toString();
          userId = item.getString("userId");
          
          HTTPConnBuilder httpconnbuilder = new HTTPConnBuilder();
          
          responseToken = (JSONObject) parser.parse(httpconnbuilder.build(username,password,consumerKey));

          if (responseToken.get("token") != null) {
            item.withString("userTokenId", responseToken.get("token").toString());
            item.withLong("joinedDate", new Date().getTime()-2678421);
          }
          
          table = dynamoDB.getTable("User_ID_Mapping");
          
          Item item1 = new Item();
          item1.withPrimaryKey("tokenKey",responseToken.get("token").toString()).withString("userName", username);

          table.putItem(item1);
          
          table = dynamoDB.getTable("Reward_Basket");
          item = table.getItem("userId", userId);
          
          long reward= item.getLong("rewardBasket");
          System.out.println("Reward"+ reward);
          
          responseToken.put("reward_points", reward);

        } catch (IOException e1) {
          // TODO Auto-generated catch block
          e1.printStackTrace();
        } // optional default is
        catch (ParseException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }

    return responseToken.toJSONString();
  }

}
