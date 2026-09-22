package net.hollowcube.schem.reader;

import net.hollowcube.schem.BlockEntityData;
import net.hollowcube.schem.SpongeSchematic;
import net.kyori.adventure.nbt.BinaryTagIO;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LegacyBlockEntityIdTest {

    @Test
    void legacyIdsBecomeBlockKeys() throws IOException {
        var schematic = (SpongeSchematic) SchematicReader.legacyMcEdit().read(mcedit());
        var ids = schematic.blockEntities().stream().map(BlockEntityData::id).sorted().toList();
        assertEquals(java.util.List.of("minecraft:daylight_detector", "minecraft:dispenser"), ids);
    }

    @Test
    void payloadSurvivesTheRename() throws IOException {
        var schematic = (SpongeSchematic) SchematicReader.legacyMcEdit().read(mcedit());
        var dispenser = schematic.blockEntities().stream()
                .filter(entity -> entity.id().equals("minecraft:dispenser")).findFirst().orElseThrow();
        assertTrue(dispenser.data().getBoolean("marker"));
    }

    // 2x1x1: a 1.8 dispenser ("Trap") and daylight sensor ("DLDetector"), the two names that match no handler
    private static byte[] mcedit() throws IOException {
        var root = CompoundBinaryTag.builder()
                .putShort("Width", (short) 2)
                .putShort("Height", (short) 1)
                .putShort("Length", (short) 1)
                .putByteArray("Blocks", new byte[]{23, (byte) 151})
                .putByteArray("Data", new byte[]{0, 0})
                .put("TileEntities", ListBinaryTag.builder()
                        .add(tileEntity("Trap", 0).putBoolean("marker", true).build())
                        .add(tileEntity("DLDetector", 1).build())
                        .build())
                .put("Entities", ListBinaryTag.empty())
                .build();
        var out = new ByteArrayOutputStream();
        BinaryTagIO.writer().writeNamed(java.util.Map.entry("Schematic", root), out, BinaryTagIO.Compression.GZIP);
        return out.toByteArray();
    }

    private static CompoundBinaryTag.Builder tileEntity(String id, int x) {
        return CompoundBinaryTag.builder().putString("id", id).putInt("x", x).putInt("y", 0).putInt("z", 0);
    }
}
