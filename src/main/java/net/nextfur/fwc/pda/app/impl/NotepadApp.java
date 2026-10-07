package net.nextfur.fwc.pda.app.impl;

import net.minecraft.ChatFormatting;
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
import net.nextfur.fwc.pda.data.PdaData;
import net.nextfur.fwc.pda.data.PdaNote;
import net.nextfur.fwc.pda.network.PdaActionC2SPacket;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class NotepadApp extends PdaApp {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(FwMain.MODID, "notepad");
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM HH:mm");

    private PdaScreen screen;
    private EditBox titleBox;
    private EditBox contentBox;
    private Button saveBtn;
    private Button deleteBtn;
    private Button newNoteBtn;

    private UUID selectedNoteId = null;
    private int notePage = 0;
    private static final int NOTES_PER_PAGE = 5;

    public NotepadApp() {
        super(
                ID,
                Component.literal("Anotações"),
                Component.literal("Crie e armazene anotações importantes."),
                0xFF00E5FF,
                null
        );
    }

    @Override
    protected void renderProceduralIcon(GuiGraphics gui, int x, int y, int size, boolean hovered) {
        int color = hovered ? 0xFFFFFFFF : (0xFF000000 | getThemeColor());
        int fill = hovered ? 0x4400E5FF : 0x2200E5FF;

        int cx = x + size / 2;
        int cy = y + size / 2;

        gui.fill(x + 4, y + 4, x + size - 4, y + size - 4, fill);
        gui.renderOutline(x + 4, y + 4, size - 8, size - 8, color);

        gui.drawCenteredString(gui.guiWidth() > 0 ? net.minecraft.client.Minecraft.getInstance().font : null,
                "✎", cx, cy - 4, color);
    }

    @Override
    public void init(PdaScreen screen, int contentX, int contentY, int contentWidth, int contentHeight) {
        this.screen = screen;
        Font font = net.minecraft.client.Minecraft.getInstance().font;

        int leftColWidth = 78;
        int rightColX = contentX + leftColWidth + 4;
        int rightColWidth = contentWidth - leftColWidth - 6;

        this.newNoteBtn = Button.builder(Component.literal("＋ Nova"), b -> {
            this.selectedNoteId = null;
            if (titleBox != null) titleBox.setValue("");
            if (contentBox != null) contentBox.setValue("");
        }).pos(contentX + 2, contentY + 2).size(leftColWidth - 2, 14).build();
        screen.addAppWidget(newNoteBtn);

        this.titleBox = new EditBox(font, rightColX, contentY + 2, rightColWidth, 14, Component.literal("Título"));
        this.titleBox.setMaxLength(40);
        this.titleBox.setHint(Component.literal("Título..."));
        screen.addAppWidget(titleBox);

        int contentBoxH = contentHeight - 38;
        this.contentBox = new EditBox(font, rightColX, contentY + 18, rightColWidth, contentBoxH, Component.literal("Conteúdo"));
        this.contentBox.setMaxLength(500);
        this.contentBox.setHint(Component.literal("Conteúdo da nota..."));
        screen.addAppWidget(contentBox);

        int btnY = contentY + contentHeight - 17;
        this.saveBtn = Button.builder(Component.literal("Salvar"), b -> {
            String title = titleBox.getValue().trim();
            String content = contentBox.getValue();
            if (title.isEmpty() && content.isEmpty()) return;

            PacketDistributor.sendToServer(new PdaActionC2SPacket(
                    PdaActionC2SPacket.ACTION_SAVE_NOTE,
                    screen.isMainHand(),
                    Optional.empty(),
                    title.isEmpty() ? "Sem título" : title,
                    content,
                    Optional.ofNullable(selectedNoteId)
            ));
        }).pos(rightColX, btnY).size(55, 15).build();
        screen.addAppWidget(saveBtn);

        this.deleteBtn = Button.builder(Component.literal("Excluir"), b -> {
            if (selectedNoteId != null) {
                PacketDistributor.sendToServer(new PdaActionC2SPacket(
                        PdaActionC2SPacket.ACTION_DELETE_NOTE,
                        screen.isMainHand(),
                        Optional.empty(),
                        "",
                        "",
                        Optional.of(selectedNoteId)
                ));
                selectedNoteId = null;
                titleBox.setValue("");
                contentBox.setValue("");
            }
        }).pos(rightColX + 58, btnY).size(48, 15).build();
        screen.addAppWidget(deleteBtn);

        loadSelectedNote();
    }

    private void loadSelectedNote() {
        PdaData data = screen.getPdaData();
        if (data == null || selectedNoteId == null) return;

        data.notes().stream()
                .filter(n -> n.id().equals(selectedNoteId))
                .findFirst()
                .ifPresent(note -> {
                    if (titleBox != null) titleBox.setValue(note.title());
                    if (contentBox != null) contentBox.setValue(note.content());
                });
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick, int contentX, int contentY, int contentWidth, int contentHeight) {
        Font font = net.minecraft.client.Minecraft.getInstance().font;
        PdaData data = screen.getPdaData();

        int leftColWidth = 78;
        int listY = contentY + 18;
        int listHeight = contentHeight - 20;

        gui.fill(contentX + 1, listY, contentX + leftColWidth, listY + listHeight, PdaTheme.BG_CARD);
        gui.renderOutline(contentX + 1, listY, leftColWidth - 1, listHeight, 0x4400E5FF);

        List<PdaNote> notes = (data != null) ? data.notes() : List.of();

        if (notes.isEmpty()) {
            gui.drawWordWrap(font, Component.literal("Sem notas."), contentX + 4, listY + 6, leftColWidth - 6, 0xFF8FA3BF);
        } else {
            int totalPages = Math.max(1, (notes.size() + NOTES_PER_PAGE - 1) / NOTES_PER_PAGE);
            if (notePage >= totalPages) notePage = totalPages - 1;

            int startIdx = notePage * NOTES_PER_PAGE;
            int endIdx = Math.min(startIdx + NOTES_PER_PAGE, notes.size());

            for (int i = startIdx; i < endIdx; i++) {
                PdaNote note = notes.get(i);
                int itemY = listY + 2 + ((i - startIdx) * 21);
                boolean isSelected = note.id().equals(selectedNoteId);
                boolean isHovered = mouseX >= contentX + 2 && mouseX <= contentX + leftColWidth - 1 && mouseY >= itemY && mouseY < itemY + 20;

                int itemBg = isSelected ? 0xAA005F73 : (isHovered ? 0x6600E5FF : 0x2200E5FF);
                gui.fill(contentX + 2, itemY, contentX + leftColWidth - 1, itemY + 20, itemBg);
                if (isSelected) {
                    gui.renderOutline(contentX + 2, itemY, leftColWidth - 3, 20, 0xFF00E5FF);
                }

                String displayTitle = font.plainSubstrByWidth(note.title(), leftColWidth - 8);
                gui.drawString(font, displayTitle, contentX + 4, itemY + 2, isSelected ? 0xFFFFFFFF : 0xFFCCE5FF, false);
                gui.drawString(font, DATE_FORMAT.format(new Date(note.timestamp())), contentX + 4, itemY + 11, PdaTheme.TEXT_MUTED, false);
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
            int leftColWidth = 78;
            int listY = contentY + 18;

            List<PdaNote> notes = data.notes();
            int startIdx = notePage * NOTES_PER_PAGE;
            int endIdx = Math.min(startIdx + NOTES_PER_PAGE, notes.size());

            for (int i = startIdx; i < endIdx; i++) {
                int itemY = listY + 2 + ((i - startIdx) * 21);
                if (mouseX >= contentX + 2 && mouseX <= contentX + leftColWidth - 1 && mouseY >= itemY && mouseY < itemY + 20) {
                    PdaNote clickedNote = notes.get(i);
                    this.selectedNoteId = clickedNote.id();
                    if (titleBox != null) titleBox.setValue(clickedNote.title());
                    if (contentBox != null) contentBox.setValue(clickedNote.content());
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void onDataUpdated(PdaData data) {
        loadSelectedNote();
    }
}
