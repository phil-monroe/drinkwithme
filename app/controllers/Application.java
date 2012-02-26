package controllers;

import play.*;
import play.mvc.*;

import java.util.*;

import models.*;

public class Application extends Controller {
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
	// 		user.setEmail(email);
	// 		user.save();
	// 		return user.getId();
	// 	} else {
	// 		return users.get(0).getId();
	// 	}
	// }
	//     
	// public static void authenticate(String user) {
	//     if(OpenID.isAuthenticationResponse()) {
	//         UserInfo verifiedUser = OpenID.getVerifiedID();
	//         if(verifiedUser == null) {
	//             flash.put("error", "Oops. Authentication has failed");
	//             login();
	//         } 
	//         session.put("user", verifiedUser.id);
	//         index();
	//     } else {
	//         OpenID.id(user).verify(); // will redirect the user
	//     }
	// }
	// 
	//     public static void index() {
	//         render();
	//     }
	
	public static void whosDrinkingJSON(){
		List<User> users = User.findAll();
		HashMap<String, List<String>> whosDrinking = new HashMap<String, List<String>>();
		for(User user : users){
			List<String> tweets = new ArrayList<String>();
			for(Tweet tweet : user.getTweets()){
				tweets.add(tweet.getBody());
			}
			if(tweets.size() > 0){
				whosDrinking.put(user.getTwitterHandle(), tweets);
			}
		}
		renderJSON(whosDrinking);
	}

	public static void whosDrinking(){
		List<User> users = User.findAll();
		render(users);
	}
}