package net.nextfur.fwc.client.gui;

import net.minecraft.client.Minecraft;
import net.nextfur.fwc.persona.PersonaData;

import java.util.UUID;

public class PersonaClientHelper {
    public static void openScreen(UUID targetUuid, String targetName, PersonaData data, boolean adminEditMode) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            mc.setScreen(new PersonaSheetScreen(targetUuid, targetName, data, adminEditMode));
        }
    }
}
