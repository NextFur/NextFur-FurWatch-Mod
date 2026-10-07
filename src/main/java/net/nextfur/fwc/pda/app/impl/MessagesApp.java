package net.nextfur.fwc.pda.app.impl;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;
import net.nextfur.fwc.FwMain;
import net.nextfur.fwc.pda.app.PdaApp;
import net.nextfur.fwc.pda.client.PdaScreen;
import net.nextfur.fwc.pda.client.PdaTheme;
import net.nextfur.fwc.pda.data.PdaContact;
import net.nextfur.fwc.pda.data.PdaData;
import net.nextfur.fwc.pda.data.PdaMessage;
import net.nextfur.fwc.pda.network.PdaActionC2SPacket;
import org.lwjgl.glfw.GLFW;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class MessagesApp extends PdaApp {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(FwMain.MODID, "messages");
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm");

    private PdaScreen screen;
    private EditBox messageInput;
    private Button sendBtn;
    private UUID selectedContactUuid = null;

    public MessagesApp() {
        super(
                ID,
                Component.literal("Mensagens"),
                Component.literal("Comunicação criptografada entre PDAs."),
                0xFF00B4D8,
                null
        );
    }

    @Override
    protected void renderProceduralIcon(GuiGraphics gui, int x, int y, int size, boolean hovered) {
        int color = hovered ? 0xFFFFFFFF : (0xFF000000 | getThemeColor());
        int fill = hovered ? 0x4400B4D8 : 0x2200B4D8;

        int cx = x + size / 2;
        int cy = y + size / 2;

        gui.fill(x + 4, y + 4, x + size - 4, y + size - 4, fill);
        gui.renderOutline(x + 4, y + 4, size - 8, size - 8, color);

        gui.drawCenteredString(gui.guiWidth() > 0 ? Minecraft.getInstance().font : null,
                "✉", cx, cy - 4, color);
    }

    @Override
    public void init(PdaScreen screen, int contentX, int contentY, int contentWidth, int contentHeight) {
        this.screen = screen;
        Font font = Minecraft.getInstance().font;

        int leftWidth = 84;
        int rightX = contentX + leftWidth + 4;
        int rightWidth = contentWidth - leftWidth - 6;

        int inputY = contentY + contentHeight - 19;
        int sendBtnWidth = 44;

        this.messageInput = new EditBox(font, rightX, inputY, rightWidth - sendBtnWidth - 3, 16, Component.literal("Mensagem"));
        this.messageInput.setMaxLength(160);
        this.messageInput.setHint(Component.literal("Digite..."));
        screen.addAppWidget(messageInput);

        this.sendBtn = Button.builder(Component.literal("Enviar"), b -> sendMessage())
                .pos(rightX + rightWidth - sendBtnWidth, inputY)
                .size(sendBtnWidth, 16)
                .build();
        screen.addAppWidget(sendBtn);

        // Auto select first contact if none selected
        PdaData data = screen.getPdaData();
        if (data != null && !data.contacts().isEmpty() && selectedContactUuid == null) {
            selectedContactUuid = data.contacts().get(0).uuid();
        }
    }

    private void sendMessage() {
        if (selectedContactUuid == null || messageInput == null) return;
        String text = messageInput.getValue().trim();
        if (text.isEmpty()) return;

        PacketDistributor.sendToServer(new PdaActionC2SPacket(
                PdaActionC2SPacket.ACTION_SEND_MESSAGE,
                screen.isMainHand(),
                Optional.of(selectedContactUuid),
                text,
                "",
                Optional.empty()
        ));

        messageInput.setValue("");
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick, int contentX, int contentY, int contentWidth, int contentHeight) {
        Font font = Minecraft.getInstance().font;
        PdaData data = screen.getPdaData();

        int leftWidth = 84;
        int rightX = contentX + leftWidth + 4;
        int rightWidth = contentWidth - leftWidth - 6;

        // Draw Left Contacts Panel
        gui.fill(contentX + 1, contentY + 1, contentX + leftWidth, contentY + contentHeight - 1, PdaTheme.BG_CARD);
        gui.renderOutline(contentX + 1, contentY + 1, leftWidth - 1, contentHeight - 2, 0x4400E5FF);

        gui.drawString(font, ChatFormatting.BOLD + "CONTATOS", contentX + 4, contentY + 4, 0xFF00E5FF, false);

        List<PdaContact> contacts = (data != null) ? data.contacts() : List.of();

        if (contacts.isEmpty()) {
            gui.drawWordWrap(font, Component.literal("Nenhum contato.\n\nClique com o PDA em outro jogador para salvar."),
                    contentX + 4, contentY + 18, leftWidth - 6, 0xFF7A8DAB);
        } else {
            int contactItemHeight = 18;
            int listY = contentY + 16;

            for (int i = 0; i < contacts.size(); i++) {
                PdaContact contact = contacts.get(i);
                int itemY = listY + (i * contactItemHeight);
                if (itemY + contactItemHeight > contentY + contentHeight - 2) break;

                boolean isSelected = contact.uuid().equals(selectedContactUuid);
                boolean isHovered = mouseX >= contentX + 2 && mouseX <= contentX + leftWidth - 1 && mouseY >= itemY && mouseY < itemY + contactItemHeight;

                boolean isOnline = Minecraft.getInstance().getConnection() != null &&
                        Minecraft.getInstance().getConnection().getPlayerInfo(contact.uuid()) != null;

                int bg = isSelected ? 0xAA005F73 : (isHovered ? 0x6600E5FF : 0x2200E5FF);
                gui.fill(contentX + 2, itemY, contentX + leftWidth - 1, itemY + contactItemHeight - 1, bg);
                if (isSelected) {
                    gui.renderOutline(contentX + 2, itemY, leftWidth - 3, contactItemHeight - 1, 0xFF00E5FF);
                }

                int dotColor = isOnline ? 0xFF00FF7F : 0xFF666666;
                gui.fill(contentX + 5, itemY + 6, contentX + 9, itemY + 10, dotColor);

                String name = font.plainSubstrByWidth(contact.name(), leftWidth - 16);
                gui.drawString(font, name, contentX + 12, itemY + 3, isSelected ? 0xFFFFFFFF : 0xFFCCE5FF, false);
            }
        }

        // Draw Right Chat Area
        int chatAreaHeight = contentHeight - 24;
        gui.fill(rightX, contentY + 1, rightX + rightWidth, contentY + 1 + chatAreaHeight, PdaTheme.BG_CARD);
        gui.renderOutline(rightX, contentY + 1, rightWidth, chatAreaHeight, 0x4400E5FF);

        PdaContact currentContact = null;
        if (selectedContactUuid != null && data != null) {
            currentContact = data.contacts().stream()
                    .filter(c -> c.uuid().equals(selectedContactUuid))
                    .findFirst()
                    .orElse(null);
        }

        if (currentContact == null) {
            gui.drawCenteredString(font, "Selecione um contato.", rightX + rightWidth / 2, contentY + chatAreaHeight / 2 - 4, 0xFF8FA3BF);
        } else {
            // Header bar of chat
            gui.fill(rightX + 1, contentY + 2, rightX + rightWidth - 1, contentY + 15, 0xDD0A1526);
            gui.renderOutline(rightX + 1, contentY + 2, rightWidth - 2, 13, 0x3300E5FF);

            boolean isOnline = Minecraft.getInstance().getConnection() != null &&
                    Minecraft.getInstance().getConnection().getPlayerInfo(currentContact.uuid()) != null;

            String statusStr = isOnline ? "§a● Online" : "§7○ Offline";
            gui.drawString(font, "Chat: §f" + currentContact.name() + " " + statusStr, rightX + 4, contentY + 4, 0xFF00E5FF, false);

            // Messages history
            List<PdaMessage> convo = data.getConversationWith(currentContact.uuid());
            int msgAreaTop = contentY + 18;
            int msgAreaBottom = contentY + chatAreaHeight - 2;

            int currentY = msgAreaBottom;
            for (int i = convo.size() - 1; i >= 0; i--) {
                PdaMessage msg = convo.get(i);
                boolean isFromMe = (data.ownerUuid().isPresent() && msg.senderUuid().equals(data.ownerUuid().get())) ||
                        msg.recipientUuid().equals(currentContact.uuid());

                int bubbleWidth = Math.min(rightWidth - 16, font.width(msg.content()) + 12);
                int bubbleHeight = 15;
                currentY -= (bubbleHeight + 2);
                if (currentY < msgAreaTop) break;

                int bubbleX = isFromMe ? (rightX + rightWidth - bubbleWidth - 4) : (rightX + 4);
                int bubbleBg = isFromMe ? 0x88007799 : 0x881D2D44;
                int bubbleBorder = isFromMe ? 0xFF00E5FF : 0xFF4A6572;

                gui.fill(bubbleX, currentY, bubbleX + bubbleWidth, currentY + bubbleHeight, bubbleBg);
                gui.renderOutline(bubbleX, currentY, bubbleWidth, bubbleHeight, bubbleBorder);

                String timeStr = TIME_FORMAT.format(new Date(msg.timestamp()));
                gui.drawString(font, msg.content(), bubbleX + 3, currentY + 2, 0xFFFFFFFF, false);
                gui.drawString(font, timeStr, bubbleX + bubbleWidth - font.width(timeStr) - 2, currentY + 7, 0x88FFFFFF, false);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && screen != null) {
            PdaData data = screen.getPdaData();
            if (data == null) return false;

            int contentX = screen.getContentX();
            int contentY = screen.getContentY();
            int leftWidth = 84;
            int listY = contentY + 16;
            int contactItemHeight = 18;

            for (int i = 0; i < data.contacts().size(); i++) {
                int itemY = listY + (i * contactItemHeight);
                if (mouseX >= contentX + 2 && mouseX <= contentX + leftWidth - 1 && mouseY >= itemY && mouseY < itemY + contactItemHeight) {
                    this.selectedContactUuid = data.contacts().get(i).uuid();
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            if (messageInput != null && messageInput.isFocused()) {
                sendMessage();
                return true;
            }
        }
        return false;
    }
}
