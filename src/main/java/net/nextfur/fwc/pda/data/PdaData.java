package net.nextfur.fwc.pda.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public record PdaData(
        UUID pdaId,
        Optional<UUID> ownerUuid,
        String ownerName,
        PdaColor colorVariant,
        List<PdaContact> contacts,
        List<PdaNote> notes,
        List<PdaMessage> messages,
        long bankBalanceCents
) {
    public static final Codec<PdaData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    UUIDUtil.CODEC.fieldOf("pda_id").forGetter(PdaData::pdaId),
                    UUIDUtil.CODEC.optionalFieldOf("owner_uuid").forGetter(PdaData::ownerUuid),
                    Codec.STRING.optionalFieldOf("owner_name", "").forGetter(PdaData::ownerName),
                    PdaColor.CODEC.optionalFieldOf("color", PdaColor.BLUE).forGetter(PdaData::colorVariant),
                    PdaContact.CODEC.listOf().optionalFieldOf("contacts", List.of()).forGetter(PdaData::contacts),
                    PdaNote.CODEC.listOf().optionalFieldOf("notes", List.of()).forGetter(PdaData::notes),
                    PdaMessage.CODEC.listOf().optionalFieldOf("messages", List.of()).forGetter(PdaData::messages),
                    Codec.LONG.optionalFieldOf("bank_balance_cents", 0L).forGetter(PdaData::bankBalanceCents)
            ).apply(instance, PdaData::new)
    );

    public static final StreamCodec<ByteBuf, PdaData> STREAM_CODEC = StreamCodec.of(
            (buf, data) -> {
                UUIDUtil.STREAM_CODEC.encode(buf, data.pdaId);
                ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC).encode(buf, data.ownerUuid);
                ByteBufCodecs.STRING_UTF8.encode(buf, data.ownerName);
                PdaColor.STREAM_CODEC.encode(buf, data.colorVariant);
                PdaContact.STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buf, data.contacts);
                PdaNote.STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buf, data.notes);
                PdaMessage.STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buf, data.messages);
                ByteBufCodecs.VAR_LONG.encode(buf, data.bankBalanceCents);
            },
            buf -> new PdaData(
                    UUIDUtil.STREAM_CODEC.decode(buf),
                    ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC).decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    PdaColor.STREAM_CODEC.decode(buf),
                    PdaContact.STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buf),
                    PdaNote.STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buf),
                    PdaMessage.STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buf),
                    ByteBufCodecs.VAR_LONG.decode(buf)
            )
    );

    public static PdaData createNew(PdaColor color) {
        return new PdaData(
                UUID.randomUUID(),
                Optional.empty(),
                "",
                color,
                List.of(),
                List.of(),
                List.of(),
                0L
        );
    }

    public boolean hasOwner() {
        return ownerUuid.isPresent() && !ownerName.isBlank();
    }

    public PdaData withOwner(UUID newOwnerUuid, String newOwnerName) {
        return new PdaData(
                pdaId,
                Optional.ofNullable(newOwnerUuid),
                newOwnerName != null ? newOwnerName : "",
                colorVariant,
                contacts,
                notes,
                messages,
                bankBalanceCents
        );
    }

    public PdaData resetOwner() {
        return new PdaData(
                pdaId,
                Optional.empty(),
                "",
                colorVariant,
                contacts,
                notes,
                messages,
                bankBalanceCents
        );
    }

    public PdaData withBankBalance(long newBalance) {
        return new PdaData(
                pdaId,
                ownerUuid,
                ownerName,
                colorVariant,
                contacts,
                notes,
                messages,
                newBalance
        );
    }

    public boolean hasContact(UUID contactUuid) {
        return contacts.stream().anyMatch(c -> c.uuid().equals(contactUuid));
    }

    public PdaData withContact(PdaContact contact) {
        List<PdaContact> updated = new ArrayList<>(contacts.stream().filter(c -> !c.uuid().equals(contact.uuid())).toList());
        updated.add(contact);
        return new PdaData(pdaId, ownerUuid, ownerName, colorVariant, List.copyOf(updated), notes, messages, bankBalanceCents);
    }

    public PdaData withoutContact(UUID contactUuid) {
        List<PdaContact> updated = contacts.stream().filter(c -> !c.uuid().equals(contactUuid)).toList();
        return new PdaData(pdaId, ownerUuid, ownerName, colorVariant, updated, notes, messages, bankBalanceCents);
    }

    public PdaData withNote(PdaNote note) {
        List<PdaNote> updated = new ArrayList<>(notes.stream().filter(n -> !n.id().equals(note.id())).toList());
        updated.add(0, note); // newest note first
        return new PdaData(pdaId, ownerUuid, ownerName, colorVariant, contacts, List.copyOf(updated), messages, bankBalanceCents);
    }

    public PdaData withoutNote(UUID noteId) {
        List<PdaNote> updated = notes.stream().filter(n -> !n.id().equals(noteId)).toList();
        return new PdaData(pdaId, ownerUuid, ownerName, colorVariant, contacts, updated, messages, bankBalanceCents);
    }

    public PdaData withMessage(PdaMessage message) {
        List<PdaMessage> updated = new ArrayList<>(messages);
        updated.add(message);
        // Cap messages at 150 to keep item stack size reasonable
        if (updated.size() > 150) {
            updated = updated.subList(updated.size() - 150, updated.size());
        }
        return new PdaData(pdaId, ownerUuid, ownerName, colorVariant, contacts, notes, List.copyOf(updated), bankBalanceCents);
    }

    public List<PdaMessage> getConversationWith(UUID contactUuid) {
        return messages.stream().filter(m ->
                m.recipientUuid().equals(contactUuid) || m.senderUuid().equals(contactUuid)
        ).toList();
    }
}
