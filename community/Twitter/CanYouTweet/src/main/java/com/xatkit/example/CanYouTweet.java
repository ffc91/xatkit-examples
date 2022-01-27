package com.xatkit.example;

import com.xatkit.core.XatkitBot;
//import com.xatkit.core.recognition.dialogflow.DialogFlowConfiguration;
import com.xatkit.library.core.CoreLibrary;
import com.xatkit.plugins.twitter.platform.TwitterPlatform;
import com.xatkit.plugins.react.platform.ReactPlatform;
import com.xatkit.plugins.react.platform.io.ReactEventProvider;
import com.xatkit.plugins.react.platform.io.ReactIntentProvider;
import lombok.val;
import org.apache.commons.configuration2.BaseConfiguration;
import org.apache.commons.configuration2.Configuration;

import static com.xatkit.dsl.DSL.any;
import static com.xatkit.dsl.DSL.eventIs;
import static com.xatkit.dsl.DSL.fallbackState;
import static com.xatkit.dsl.DSL.intent;
import static com.xatkit.dsl.DSL.intentIs;
import static com.xatkit.dsl.DSL.model;
import static com.xatkit.dsl.DSL.state;
import static java.util.Objects.isNull;


public class CanYouTweet {

    /*
     * Your bot is a plain Java application: you need to define a main method to make the created jar executable.
     */
    public static void main(String[] args) {

        /*
         * Define the intents our bot will react to.
         */
    	
    	val greetings = intent("Greetings")
                .trainingSentence("Hi")
                .trainingSentence("Hello")
                .trainingSentence("Greetings")
                .trainingSentence("Good evening")
                .trainingSentence("Good morning")
                .trainingSentence("Good afternoon");
    	
    	val postTweet = intent("PostTweet")
                .trainingSentence("can you tweet CONTENT?")
                .trainingSentence("can you post CONTENT?")
                .trainingSentence("tweet CONTENT")
                .trainingSentence("post CONTENT")
                .trainingSentence("post tweet CONTENT")
                .trainingSentence("post this tweet: CONTENT")
                .trainingSentence("please tweet CONTENT")
				.trainingSentence("please post CONTENT")
				.trainingSentence("just tweet CONTENT")
    			.trainingSentence("just post CONTENT")
    			.parameter("content")
                .fromFragment("CONTENT")
                .entity(any());

		val searchTweets = intent("SearchTweets")
                .trainingSentence("can you show tweets about TWEETS")
                .trainingSentence("can you search for TWEETS")
                .trainingSentence("can you search tweets with TWEETS")
                .trainingSentence("can you look for TWEETS")
                .trainingSentence("can you find TWEETS")
                .trainingSentence("show tweets of TWEETS")
                .trainingSentence("show tweets about TWEETS")
                .trainingSentence("search for TWEETS")
                .trainingSentence("search for tweets TWEETS")
                .trainingSentence("search for tweets of TWEETS")
                .trainingSentence("search tweets with TWEETS")
                .trainingSentence("search tweets about TWEETS")
                .trainingSentence("look for TWEETS")
                .trainingSentence("look for tweets TWEETS")
                .trainingSentence("find TWEETS")
                .trainingSentence("find tweets TWEETS")
    			.parameter("query")
                .fromFragment("TWEETS")
                .entity(any());

		val sendDM = intent("SendDM") //followup
                .trainingSentence("can you send dm to USER")
                .trainingSentence("can you send message to USER")
                .trainingSentence("can you send text to USER")
                .trainingSentence("dm to USER")
                .trainingSentence("message to USER")
                .trainingSentence("text to USER")
                .trainingSentence("send dm to USER")
                .trainingSentence("send message to USER")
                .trainingSentence("send text to USER")
                .trainingSentence("please send dm to USER")
                .trainingSentence("please send message to USER")
                .trainingSentence("please text to USER")
    			.parameter("user")
                .fromFragment("USER")
                .entity(any());
				
		val specifyDM = intent("SpecifyDM") //followup de send dm
                .trainingSentence("DM content: TEXT")
                .parameter("text")
                .fromFragment("TEXT")
                .entity(any());
				
		val receiveDM = intent("ReceiveDM")
                .trainingSentence("can you show me my messages")
                .trainingSentence("can you show me messages")
                .trainingSentence("can you show me all messages")
                .trainingSentence("show me my messages")
                .trainingSentence("show me messages")
                .trainingSentence("show me all messages")
                .trainingSentence("who wrote me")
                .trainingSentence("show messages")
                .trainingSentence("show dm")
                .trainingSentence("do I have messages")
                .trainingSentence("are there messages");

		val getTrends = intent("GetTrends")
                .trainingSentence("get trends")
                .trainingSentence("show trends")
                .trainingSentence("get trending topics")
                .trainingSentence("show trending topics")
                .trainingSentence("get trends from 123")
                .trainingSentence("show trends from 123")
                .trainingSentence("get trending topics from 123")
                .trainingSentence("show trending topics from 123")
                .trainingSentence("get trends from Uruguay")
                .trainingSentence("show trends from Uruguay")
                .trainingSentence("get trending topics from Uruguay")
                .trainingSentence("show trending topics from Uruguay");

        /*
         * Instantiate the platform we will use in the bot definition.
         */
        /*
         * Similarly, instantiate the intent/event providers we want to use.
         */
        TwitterPlatform twitterPlatform = new TwitterPlatform();
        ReactPlatform reactPlatform = new ReactPlatform();
        ReactEventProvider reactEventProvider = reactPlatform.getReactEventProvider();
        ReactIntentProvider reactIntentProvider = reactPlatform.getReactIntentProvider();

        /*
         * Create the states we want to use in our bot.
         */
        //Initial states
        val init 				= state("Init");
        val awaitingInput		= state("AwaitingInput");
        //Intro
    	val handleGreetings 	= state("HandleGreetings");
    	//Tweets
    	val handleSearchTweets 	= state("HandleSearchTweets");
    	val handlePostTweet 	= state("HandlePostTweet");
		//DMs
    	val handleSendDM 		= state("HandleSendDM"); //followup
		val handleSpecifyDM 	= state("HandleSpecifyDM"); 
		val handleReceiveDM 	= state("HandleReceiveDM");
		//Trends
		val handleGetTrends 	= state("HandleGetTrends");

        /*
         * Specify the content of the bot states (i.e. the behavior of the bot).
         */
        init
                .next()
                /*
                 * We check that the received event matches the ClientReady event defined in the
                 * ReactEventProvider. The list of events defined in a provider is available in the provider's
                 * wiki page.
                 */
                .when(eventIs(ReactEventProvider.ClientReady)).moveTo(awaitingInput);

        awaitingInput
                .next()
                /*
                 * The Xatkit DSL offers dedicated predicates (intentIs(IntentDefinition) and eventIs
                 * (EventDefinition) to check received intents/events.
                 * <p>
                 * You can also check a condition over the underlying bot state using the following syntax:
                 * <pre>
                 * {@code
                 * .when(context -> [condition manipulating the context]).moveTo(state);
                 * }
                 * </pre>
                 */
                
                .when(intentIs(greetings)).moveTo(handleGreetings)
                .when(intentIs(postTweet)).moveTo(handlePostTweet)
                .when(intentIs(searchTweets)).moveTo(handleSearchTweets)
                .when(intentIs(sendDM)).moveTo(handleSendDM)
                .when(intentIs(receiveDM)).moveTo(handleReceiveDM)
                .when(intentIs(getTrends)).moveTo(handleGetTrends);

		handleGreetings
                .body(context -> reactPlatform.reply(context, "Hi, I can help you interact with twitter!\nYou can start with something like `Can you post <whatever you want>?"))
                .next()
                /*
                 * A transition that is automatically navigated: in this case once we have answered the user we
                 * want to go back in a state where we wait for the next intent.
                 */
                .moveTo(awaitingInput);

		
		
		handleSearchTweets
		        .body(context -> {
                    String contentQuery = (String) context.getIntent().getValue("query");
	            	if(!contentQuery.equals("")){
	            		String result = (String) twitterPlatform.lookForTweets(context,contentQuery);	
	            		
	            		if(result.equals("1")){
	            			reactPlatform.reply(context,"I couldn't do that search");
	            		}else{
	            			if(result.equals("0")){
	            				reactPlatform.reply(context,"No results for: _" + contentQuery + "_");
	            			}else{
	            				reactPlatform.reply(context,result);
	            			}
	            		}		
	            	}else{
	            		reactPlatform.reply(context,"I can't search for that");
	            	}		        	
                })
		        .next()
		        .moveTo(awaitingInput);
		
		handlePostTweet
		        .body(context -> {
                    String content = (String) context.getIntent().getValue("content");
	            	if(!content.equals("")){
	            		String result = (String) twitterPlatform.postAtweet(context,content);
	            		
	            		if(result.equals("0")){
	            			reactPlatform.reply(context,"I just tweeted: _" + content + "_ for you.");
	            		}else{
	            			reactPlatform.reply(context,"I couldn't tweet that for you right now.");
	            		}		
	            	}else{
	            		reactPlatform.reply(context,"I'm not going to post an empty tweet, look somewhere else");
	            	}
	        	})
			    .next()
			    .moveTo(awaitingInput);

    	handleSendDM
		        .body(context -> {
                    String user = (String) context.getIntent().getValue("user");
		        	if(!user.equals("")){
		        		reactPlatform.reply(context,"Ok, so you want to send a DM to *@" + user + "*");
		        		reactPlatform.reply(context,"Write 'DM content:' and then add what you want to say to him/her in double quotes.");
		        	}else{
		        		reactPlatform.reply(context,"I don't know anyone by that name");
		        	}
		    	})
			    .next()
			    .moveTo(handleSpecifyDM);

    	handleSpecifyDM
		        .body(context -> {
                    String DMcontent = (String) context.getIntent().getValue("text");
                	if(!DMcontent.equals("")){
                		String result = (String)twitterPlatform.sendDM(context,(String) context.getIntent().getValue("user"), DMcontent);
                		if(result.equals(0)){
                			reactPlatform.reply(context,"Message Sent! :smile:");
                		}else{
                			reactPlatform.reply(context,"I couldn't send it :upside_down_face:");
                		}		
                	}else{
                		reactPlatform.reply(context,"I'm not going to send that");
                	}
		    	})
			    .next()
			    .moveTo(awaitingInput);
		
		handleReceiveDM
		        .body(context -> {
		        	String result = (String)twitterPlatform.receiveDM(context);
                	if (result.equals("1")){
                		reactPlatform.reply(context,"I couldn't retrieve any message, try again later. :upside_down_face:");
                	}else{
                		if(result.equals("0")){
                			reactPlatform.reply(context,"There are no messages...");
                		}else{
                			reactPlatform.reply(context,result);
                		}
                	}
		    	})
			    .next()
			    .moveTo(awaitingInput);

		handleGetTrends
		        .body(context -> {/*
		        	var Object result = null;
		        	var noResultsMessage = "";
		        	if(context.get("Twitter").get("woeid") != ""){
		        		result = twitterPlatform.GetTrends(Integer.parseInt(context.get("Twitter").get("woeid") as String))
		        		noResultsMessage = "No trending topics where found for the WOEID: " + context.get("Twitter").get("woeid")
		        	}
		        	else {
		        		if (context.get("Twitter").get("locationName") != ""){
		        			result = twitterPlatform.GetTrends(context.get("Twitter").get("locationName") as String)
		        			noResultsMessage = "No trending topics where found for the location with name: " + context.get("Twitter").get("locationName")
		        		}else {
		        			result = twitterPlatform.GetTrends()
		        			noResultsMessage = "No trending topics where found worldwide"
		        		}
		        	}
		        	if (result == "1"){
		        		reactPlatform.reply(context,"An error occurred while trying to retrieve the ternding topics, try again later. :upside_down_face:")
		        	}else{
		        		if(result == "0"){
		        			reactPlatform.reply(context,noResultsMessage)
		        		}else{
		        			reactPlatform.reply(context,result as java.util.List<com.github.seratch.jslack.api.model.Attachment>)	
		        		}
		        	}
		    	*/})
			    .next()
			    .moveTo(awaitingInput);

		val defaultFallback = fallbackState()
                .body(context -> reactPlatform.reply(context, "\"Sorry I'll have to check that on my dictionary and get back to you later :(\")"));
        
        /*
         * Creates the bot model that will be executed by the Xatkit engine.
         * <p>
         * A bot model contains:
         * - A list of platforms used by the bot. Xatkit will take care of starting and initializing the platforms
         * when starting the bot.
         * - A list of providers the bot should listen to for events/intents. As for the platforms Xatkit will take
         * care of initializing the provider when starting the bot.
         * - The entry point of the bot (a.k.a init state). Full list of intents and states are calculated based on this entry point
         * - The default fallback state: the state that is executed if the engine doesn't find any navigable
         * transition in a state and the state doesn't contain a fallback.
         */
        val botModel = model()
                .usePlatform(reactPlatform)
                .usePlatform(twitterPlatform)
                .listenTo(reactEventProvider)
                .listenTo(reactIntentProvider)
                .initState(init)
                .defaultFallbackState(defaultFallback);

        Configuration botConfiguration = new BaseConfiguration();
        /*
         * Add configuration properties (e.g. authentication tokens, platform tuning, intent provider to use).
         * Check the corresponding platform's wiki page for further information on optional/mandatory parameters and
         * their values.
         */

        /* DEPRECATED
	        # Execution file containing the logic of the bot
	        botConfiguration.addProperty("xatkit.execution.model","src/CanYouTweet.execution");
	
	        # Resolve alias imports in Execution model
	        botConfiguration.addProperty("xatkit.libraries.custom.CanYouTweetLib","src/CanYouTweet.intent");

	        # Slack Credentials (see https://github.com/xatkit-bot-platform/xatkit/wiki/Deploying-chatbots)
	        botConfiguration.addProperty("xatkit.slack.token","<Slack Bot Token>");
			
	        # DialogFlow configuration (remove if not planning to use DialogFlow)
	        botConfiguration.addProperty("xatkit.dialogflow.projectId","");
			botConfiguration.addProperty("xatkit.dialogflow.credentials.path","");
			botConfiguration.addProperty("xatkit.dialogflow.language","en-US");
			botConfiguration.addProperty("xatkit.dialogflow.clean_on_startup",true);

         */


        // Twitter configuration
        botConfiguration.addProperty("xatkit.twitter.consumerKey","");
		botConfiguration.addProperty("xatkit.twitter.consumerSecret","");
		botConfiguration.addProperty("xatkit.twitter.accessToken","");
		botConfiguration.addProperty("xatkit.twitter.accessSecretToken","");


        XatkitBot xatkitBot = new XatkitBot(botModel, botConfiguration);
        xatkitBot.run();
        /*
         * The bot is now started, you can check http://localhost:5000/admin to test it.
         * The logs of the bot are stored in the logs folder at the root of this project.
         */
    }
}
