package wtf.ultra.namehistory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiControls;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

import javax.annotation.Nonnull;

public class NameHistory implements ICommand {
    private final HoverEvent CLICK_TO_COPY = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(EnumChatFormatting.YELLOW + "click to copy"));
    private final String MOJANG = "https://api.mojang.com/users/profiles/minecraft/%s";
    private final String LABY = "https://laby.net/api/v2/user/%s/get-profile";


    public int compareTo(@Nonnull ICommand a) {
        return 0;
    }

    public String getCommandName() {
        return "history";
    }

    public String getCommandUsage(ICommandSender sender) {
        return "history <name>";
    }

    public List<String> getCommandAliases() {
        List<String> commandAliases = new ArrayList<>();
        commandAliases.add("his");
        return commandAliases;
    }

    public void processCommand(ICommandSender icommandsender, String[] args) { // throws CommandException
        if (args.length < 1) return;
        String input = args[0];
        if (input.equals("$SENDTOCLIPBOARD") && args.length > 1) {
            GuiControls.setClipboardString(args[1]);
            return;
        }

        (new Thread(() -> {
            try {
                String uuid = null;
                for (EntityPlayer player : Minecraft.getMinecraft().theWorld.playerEntities) {
                    if (player.getName().equals(input)) {
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
                IChatComponent uuidChatComponent = new ChatComponentText(String.valueOf(EnumChatFormatting.DARK_GREEN) + EnumChatFormatting.BOLD + uuid);
                ChatStyle style = new ChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/history $SENDTOCLIPBOARD " + uuid));
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
                Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Couldn't find name history for " + input));
            }
        })).start();
    }

    public boolean canCommandSenderUseCommand(ICommandSender icommandsender){
        return true;
    }

    public List<String> addTabCompletionOptions(ICommandSender icommandsender, String[] strings, BlockPos pos){
        Minecraft mc = Minecraft.getMinecraft();
        List<IChatComponent> igns = new ArrayList<>();
        for (EntityPlayer player : mc.theWorld.playerEntities) {
            String name = player.getName();
            IChatComponent ign = new ChatComponentText(
                    EnumChatFormatting.GREEN + name
            );
            ChatStyle queryIGN = new ChatStyle().setChatClickEvent(
                    new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                    "/history " + EntityPlayer.getUUID(player.getGameProfile()).toString())
            );
            HoverEvent showIGN = new HoverEvent(
                    HoverEvent.Action.SHOW_TEXT, new ChatComponentText(
                    EnumChatFormatting.YELLOW + name)
            );
            ign.setChatStyle(queryIGN);
            ign.getChatStyle().setChatHoverEvent(showIGN);
            igns.add(ign);
        }

        int n = igns.size();
        IChatComponent[] ignsArr = new IChatComponent[n];
        for (int i = 0; i < n; i++) {
            ignsArr[i] = igns.get(i);
        }

        mergeSort(ignsArr);

        IChatComponent ignList = new ChatComponentText("select a player: ");
        for (int i = 0; i < n - 1; i++) {
            ignList.appendSibling(ignsArr[i]);
            ignList.appendText(", ");
        }
        ignList.appendSibling(ignsArr[n - 1]);
        mc.thePlayer.addChatMessage(ignList);

        return new ArrayList<>();
    }

    public boolean isUsernameIndex(String[] strings, int i){
        return false;
    }

    private static String unstripUuidAsString(String uuid) {
        return uuid.length() != 32 ? uuid : uuid.substring(0, 8) + "-" + uuid.substring(8, 12) + "-" + uuid.substring(12, 16) + "-" + uuid.substring(16, 20) + "-" + uuid.substring(20, 32);
    }

    private static void mergeSort(IChatComponent[] a) {
        if (a.length >= 2) {
            IChatComponent[] left = new IChatComponent[a.length / 2];
            IChatComponent[] right = new IChatComponent[a.length - a.length / 2];

            System.arraycopy(a, 0, left, 0, left.length);
            System.arraycopy(a, a.length / 2, right, 0, right.length);

            mergeSort(left);
            mergeSort(right);
            merge(a, left, right);
        }
    }

    private static void merge(IChatComponent[] result, IChatComponent[] left, IChatComponent[] right) {
        int i1 = 0;
        int i2 = 0;
        for (int i = 0; i < result.length; i++) {
            if (i2 >= right.length || (i1 < left.length &&
                    str(left[i1]).compareToIgnoreCase(str(right[i2])) < 0)) {
                result[i] = left[i1];
                i1++;
            } else {
                result[i] = right[i2];
                i2++;
            }
        }
    }

    private static String str(IChatComponent component) {
        return component.getUnformattedText();
    }
}