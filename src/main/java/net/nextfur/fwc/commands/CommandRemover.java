package net.nextfur.fwc.commands;

import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.nextfur.fwc.FwMain;

import java.lang.reflect.Field;
import java.util.Map;

public class CommandRemover {
    private static final Field CHILDREN_FIELD;
    private static final Field LITERALS_FIELD;
    private static final Field ARGUMENTS_FIELD;

    static {
        Field children = null;
        Field literals = null;
        Field arguments = null;
        try {
            children = CommandNode.class.getDeclaredField("children");
            children.setAccessible(true);

            literals = CommandNode.class.getDeclaredField("literals");
            literals.setAccessible(true);

            arguments = CommandNode.class.getDeclaredField("arguments");
            arguments.setAccessible(true);
        } catch (ReflectiveOperationException e) {
            FwMain.LOGGER.error("Failed to initialize CommandNode reflection fields", e);
        }
        CHILDREN_FIELD = children;
        LITERALS_FIELD = literals;
        ARGUMENTS_FIELD = arguments;
    }

    @SuppressWarnings("unchecked")
    public static void removeCommand(CommandNode<?> rootNode, String commandName) {
        if (CHILDREN_FIELD == null || LITERALS_FIELD == null || ARGUMENTS_FIELD == null) {
            FwMain.LOGGER.warn("CommandRemover reflection fields are not initialized. Cannot remove command: {}", commandName);
            return;
        }

        CommandNode<?> targetChild = rootNode.getChild(commandName);
        if (targetChild == null) return;

        try {
            Map<String, CommandNode<?>> children = (Map<String, CommandNode<?>>) CHILDREN_FIELD.get(rootNode);
            if (children != null) {
                children.remove(commandName);
            }

            if (targetChild instanceof LiteralCommandNode) {
                Map<String, ?> literals = (Map<String, ?>) LITERALS_FIELD.get(rootNode);
                if (literals != null) {
                    literals.remove(commandName);
                }
            } else if (targetChild instanceof ArgumentCommandNode) {
                Map<String, ?> arguments = (Map<String, ?>) ARGUMENTS_FIELD.get(rootNode);
                if (arguments != null) {
                    arguments.remove(commandName);
                }
            }
            FwMain.LOGGER.info("Removed command node '{}' from Brigadier root", commandName);
        } catch (IllegalAccessException e) {
            FwMain.LOGGER.error("Failed to remove command {}", commandName, e);
        }
    }
}
