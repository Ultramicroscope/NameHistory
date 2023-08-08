package wtf.ultra;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class NameHistoryMod implements ModInitializer {
    private static final String commandName = "his",
            illegal = "@'\"+-.",
            MOJANG = "https://api.mojang.com/users/profiles/minecraft/%s",
            LABY = "https://laby.net/api/v2/user/%s/get-profile";

    @Override
    public void onInitialize() {
        ClientCommandRegistrationCallback.EVENT.register(this::registerCommand);
    }

    public void registerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher,
                                CommandRegistryAccess commandRegistryAccess) {
        dispatcher.register(ClientCommandManager.literal(commandName).then(ClientCommandManager.argument("player", EntityArgumentType.player()).executes(context -> {
            String input = context.getInput().substring(commandName.length() + 1);
            for (int i = 0; i < input.length(); i++) {
                if (illegal.contains(String.valueOf(input.charAt(i)))) {
                    throw new IllegalArgumentException("Non-username input: " + input);
                }
            }

            (new Thread(() -> {
                try {
                    String uuid;
                    try {
                        uuid = context
                                .getSource()
                                .getClient()
                                .getNetworkHandler()
                                .getPlayerListEntry(input)
                                .getProfile()
                                .getId()
                                .toString();
                    } catch (NullPointerException e) {
                        uuid = JsonParser.parseString(
                                new URLRequest(MOJANG.replace("%s", input)).getResponse()
                        ).getAsJsonObject().get("id").getAsString();
                    }
                    uuid = unstripUuidAsString(uuid);

                    ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, uuid);
                    context.getSource().sendFeedback(Text.literal(uuid)
                            .styled(style -> style
                                    .withBold(true)
                                    .withColor(Formatting.DARK_GREEN)
                                    .withClickEvent(clickEvent)
                                    .withHoverEvent(
                                            new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                                    Text.literal("click to copy")
                                                            .styled(hoverEventStyle -> hoverEventStyle
                                                                    .withColor(Formatting.YELLOW))))));

                    JsonArray nameList = JsonParser.parseString(
                            new URLRequest(LABY.replace("%s", uuid)).getResponse()
                    ).getAsJsonObject().get("username_history").getAsJsonArray();

                    Text message = Text.literal("-----------------------------------------------------\n")
                            .styled(style -> style.withStrikethrough(true).withColor(Formatting.RED));
                    for (JsonElement entry : nameList) {
                        String date = entry.getAsJsonObject().get("changed_at").toString();
                        date = date.replace("\"", "");
                        message.getSiblings().add(Text.literal(entry.getAsJsonObject().get("name").getAsString())
                                .styled(style -> style.withStrikethrough(false).withColor(Formatting.DARK_AQUA)));
                        message.getSiblings()
                                .add(Text.literal(" " + (date.length() >= 10 ? date.substring(0, 10) : date))
                                        .styled(style -> style.withStrikethrough(false).withColor(Formatting.GRAY)));
                        message.getSiblings().add(Text.literal("\n"));
                    }
                    message.getSiblings()
                            .add(Text.literal("-----------------------------------------------------")
                                    .styled(style -> style
                                            .withStrikethrough(true)
                                            .withColor(Formatting.RED)));
                    context.getSource().sendFeedback(message);
                } catch (Exception outer) {
                    context.getSource().sendFeedback(Text.literal(outer.toString()).styled(style -> style.withColor(Formatting.RED)));
                }
            })).start();

            return Command.SINGLE_SUCCESS;
        })));
    }

    private static String unstripUuidAsString(String uuid) {
        return uuid.length() != 32 ? uuid : uuid.substring(0, 8) + "-" + uuid.substring(8, 12) + "-" + uuid.substring(12, 16) + "-" + uuid.substring(16, 20) + "-" + uuid.substring(20, 32);
    }
}
