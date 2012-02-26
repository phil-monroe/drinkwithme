package controllers;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import models.Tweet;
import models.User;
import play.db.DB;
import play.libs.OpenID;
import play.libs.OpenID.UserInfo;
import play.mvc.Before;
import play.mvc.Controller;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

public class TweetsController extends Controller{
	// 
	// @Before(unless={"unauthorized", "authenticate", "updateTweets"})
	// static void checkAuthenticated() {
	//     if(!session.contains("user")) {
	//         authenticate();
	//     }
	// }
	//      
	// public static void unauthorized() {
	//     render();
	// }
	// 
	// private static long addUser(String name, String email) {
	// 	List<User> users = User.find("byEmail", email).fetch();
	// 	if(users.size() == 0) {
	// 		User user = new User();
	// 		user.setName(name);
	// 		user.save();
	// 		return user.getId();
	// 	} else {
	// 		return users.get(0).getId();
	// 	}
	// }
	//     
	// public static void authenticate() {
	//     if(OpenID.isAuthenticationResponse()) {
	//         UserInfo verifiedUser = OpenID.getVerifiedID();
	//         if(verifiedUser == null) {
	//             flash.error("Oops. Authentication has failed. Could be an issue with Google");
	//             unauthorized();
	//         } 
	//         
	//         String email = verifiedUser.extensions.get("email");
	//         String firstName = verifiedUser.extensions.get("firstName");
	//         String lastName = verifiedUser.extensions.get("lastName");
	//         if(email != null) {
	//         	session.put("user", verifiedUser.id);
	//         	session.put("email", email);
	//         	session.put("name", firstName);
	//         	session.put("userId", addUser(firstName + " " + lastName, email));
	//         	index();	        	
	//         }
	//     } else {
	//     	OpenID.id("https://www.google.com/accounts/o8/id")
	//     		.required("email", "http://axschema.org/contact/email")
	//     		.required("firstName", "http://axschema.org/namePerson/first")
	//     		.required("lastName", "http://axschema.org/namePerson/last").verify();
	//     }
	// }
	
	public static void index() {
		List<Tweet> tweets = Tweet.findAll();
		render(tweets);
	}
	

	
	private static long getLatestStatusId() {
		Connection conn = DB.getConnection();
		
		try {
			// Get a statement from the connection
			Statement stmt = conn.createStatement() ;
			// Execute the query
			ResultSet rs = stmt.executeQuery("SELECT max(statusId) from tweet");
			
			if(rs.next()) {
				return rs.getLong(1);
			} else {
				return 0;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return 0;
		} finally {
			DB.close();
		}
	}
	
	private static String buildQueryString(){
		List<User> users = User.findAll();
		String query = "";
		for(int i = 0; i < users.size(); i++){
			query += "from:";
			query += users.get(i).getTwitterHandle();
			if(i != users.size()-1)
				query += " OR ";
		}
		query += " #drinkwithme";
		return query;
	}
	
	public static void updateTweets() {
		Twitter twitter = new TwitterFactory().getInstance();
		String queryString = buildQueryString();
		Query query = new Query();
		query.setQuery(queryString);
		long sinceId = getLatestStatusId();
		query.setSinceId(sinceId);
		System.out.println("Updating tweets since: " + sinceId);
		try {
			QueryResult result = twitter.search(query);
			List<twitter4j.Tweet> tweets = result.getTweets();
			System.out.println(tweets.size() + " tweets found");
			for(twitter4j.Tweet queriedTweet : tweets) {
				Tweet tweet = new Tweet();
				String handle = queriedTweet.getFromUser();
				User user =  User.find("byTwitterHandle", handle).first();
				System.out.println(user.getTwitterHandle());
				tweet.setUser(user);
				tweet.setBody(queriedTweet.getText());
				tweet.setCreatedAt(queriedTweet.getCreatedAt());
				tweet.setStatusId(queriedTweet.getId());
				tweet.save();
			}			
		} catch (TwitterException e) {
			e.printStackTrace();
		}
	}
	
	public static void clearTweets(){
		List<Tweet> tweets = Tweet.findAll();
		for(Tweet tweet : tweets){
			tweet.delete();
		}
	}
	
}
