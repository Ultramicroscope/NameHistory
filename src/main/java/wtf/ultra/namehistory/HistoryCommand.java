package wtf.ultra.namehistory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiControls;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.weavemc.loader.api.command.Command;
import org.jetbrains.annotations.NotNull;

public class HistoryCommand extends Command {
    private static final String MOJANG = "https://api.mojang.com/users/profiles/minecraft/%s",
            LABY = "https://laby.net/api/v2/user/%s/get-profile";
    private final HoverEvent CLICK_TO_COPY = new HoverEvent(
            HoverEvent.Action.SHOW_TEXT, new ChatComponentText(
            EnumChatFormatting.YELLOW + "click to copy"
    )
    );

    public HistoryCommand() {
        super("history", "his");
    }

    @Override
    public void handle(@NotNull String[] args) {
        if (args.length < 1) return;
        String input = args[0];
        if (input.equals("$SENDTOCLIPBOARD")) {
            GuiControls.setClipboardString(args[1]);
            return;
        }

        (new Thread(() -> {
            try {
                String uuid = null;
                for (EntityPlayer player : Minecraft.getMinecraft().theWorld.playerEntities) {
                    if (player.getName().equals(args[0])) {
                        uuid = player.getUniqueID().toString();
                        break;
                    }
                }

                if (uuid == null) {
                    uuid = new JsonParser().parse(
                            new URLRequest(MOJANG.replace("%s", input)).getResponse()
                    ).getAsJsonObject().get("id").getAsString();
                }

                uuid = unstripUuidAsString(uuid);
                IChatComponent uuidChatComponent = new ChatComponentText(
                        String.valueOf(EnumChatFormatting.DARK_GREEN) + EnumChatFormatting.BOLD + uuid
                );
                ChatStyle style = new ChatStyle().setChatClickEvent(
                        new ClickEvent(
                                ClickEvent.Action.RUN_COMMAND, "/history $SENDTOCLIPBOARD " + uuid
                        )
                );
                uuidChatComponent.setChatStyle(style);
                uuidChatComponent.getChatStyle().setChatHoverEvent(CLICK_TO_COPY);
                Minecraft.getMinecraft().thePlayer.addChatMessage(uuidChatComponent);

                JsonArray nameList = new JsonParser().parse(
                        new URLRequest(LABY.replace("%s", uuid)).getResponse()
                ).getAsJsonObject().get("username_history").getAsJsonArray();

                String date;
                StringBuilder result = new StringBuilder(EnumChatFormatting.RED.toString() + EnumChatFormatting.STRIKETHROUGH + "-----------------------------------------------------\n");
                for (JsonElement i: nameList) {
                    date = i.getAsJsonObject().get("changed_at").toString();
                    date = date.replace("\"", "");
                    result.append(EnumChatFormatting.DARK_AQUA).append(i.getAsJsonObject().get("name").getAsString()).append(" ").append(EnumChatFormatting.GRAY).append(date.length() >= 10 ? date.substring(0, 10) : date).append("\n");
                }
                result.append(EnumChatFormatting.RED).append(EnumChatFormatting.STRIKETHROUGH).append("-----------------------------------------------------");
                Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(result.toString()));
            } catch (Exception e) {
                e.printStackTrace();
                Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Couldn't find name history for " + args[0]));
            }
        })).start();
    }

    private String unstripUuidAsString(String uuid) {
        return uuid.length() != 32 ? uuid : uuid.substring(0, 8) + "-" + uuid.substring(8, 12) + "-" + uuid.substring(12, 16) + "-" + uuid.substring(16, 20) + "-" + uuid.substring(20, 32);
    }
}
