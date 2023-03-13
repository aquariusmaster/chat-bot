package com.anderb.chatbot;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class BotApplication implements RequestHandler<Update, BotApiMethod<?>> {

    private static ChatBot bot;

//    public BotApplication() throws TelegramApiException {
//        var config = new BotConfig();
//        bot = new ChatBot(config.getWebhookPath(), config.getBotName(), config.getBotToken());
//        var setWebhook = SetWebhook.builder().url(config.getWebhookPath()).build();
//        bot.setWebhook(setWebhook);
//    }

    public static void main(String[] args) throws TelegramApiException {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        try {
            var config = new BotConfig();
            bot = new ChatBot(config.getWebhookPath(), config.getBotName(), config.getBotToken());
            var setWebhook = SetWebhook.builder().url(config.getWebhookPath()).build();
            telegramBotsApi.registerBot(bot, setWebhook);
        } catch (TelegramApiRequestException e) {
            System.out.println("Failed to register bot(check internet connection / bot token or make sure only one instance of bot is running).");
            throw e;
        }
        System.out.println("Telegram bot is ready to accept updates from user......");
    }

    public BotApiMethod<?> handleRequest(Update chatUpdate, Context context) {
        LambdaLogger logger = context.getLogger();
        logger.log("Chat update: " + chatUpdate);
        return bot.onWebhookUpdateReceived(chatUpdate);
    }
}
