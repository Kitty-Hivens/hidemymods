package dev.hivens.hidemymods.forge112.coremod;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

/**
 * Replaces the body of FMLHandshakeMessage$ModList.toBytes(ByteBuf)
 * with a single delegation to ModListSpoof.writeModList. The runtime
 * helper decides per-call whether to write the real loaded mod-list
 * or a spoofed one read from disk.
 *
 * Keeping the wire-format logic in a runtime class instead of the
 * ASM transformer means the bytecode patch stays trivial (five
 * instructions, no branches, no try/catch) and all config / IO /
 * fallback decisions live in normal Java where they can be edited
 * without re-running ASM.
 *
 * The class name FMLHandshakeMessage$ModList is stable across all
 * 1.12.2 Forge builds the FML handshake has shipped under. FML and
 * Forge classes are NOT SRG-remapped at runtime (only Minecraft
 * classes are), so the deobfuscated name matches the runtime name
 * verbatim and transformedName is the correct match key.
 */
public final class ModListTransformer implements IClassTransformer {

    private static final String TARGET_CLASS_DOT =
        "net.minecraftforge.fml.common.network.handshake.FMLHandshakeMessage$ModList";

    private static final String TARGET_CLASS_INTERNAL =
        "net/minecraftforge/fml/common/network/handshake/FMLHandshakeMessage$ModList";

    private static final String MOD_TAGS_FIELD = "modTags";
    private static final String MOD_TAGS_DESC  = "Ljava/util/Map;";

    private static final String TO_BYTES_NAME = "toBytes";
    private static final String TO_BYTES_DESC = "(Lio/netty/buffer/ByteBuf;)V";

    private static final String SPOOF_INTERNAL =
        "dev/hivens/hidemymods/forge112/coremod/ModListSpoof";
    private static final String SPOOF_METHOD_NAME = "writeModList";
    private static final String SPOOF_METHOD_DESC =
        "(Ljava/util/Map;Lio/netty/buffer/ByteBuf;)V";

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null || !TARGET_CLASS_DOT.equals(transformedName)) {
            return basicClass;
        }

        ClassReader reader = new ClassReader(basicClass);
        ClassNode   node   = new ClassNode();
        reader.accept(node, 0);

        boolean patched = false;
        for (MethodNode m : node.methods) {
            if (!TO_BYTES_NAME.equals(m.name) || !TO_BYTES_DESC.equals(m.desc)) {
                continue;
            }

            m.instructions.clear();
            m.localVariables = null;
            m.tryCatchBlocks = null;

            m.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
            m.instructions.add(new FieldInsnNode(
                Opcodes.GETFIELD,
                TARGET_CLASS_INTERNAL,
                MOD_TAGS_FIELD,
                MOD_TAGS_DESC
            ));
            m.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
            m.instructions.add(new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                SPOOF_INTERNAL,
                SPOOF_METHOD_NAME,
                SPOOF_METHOD_DESC,
                false
            ));
            m.instructions.add(new InsnNode(Opcodes.RETURN));

            patched = true;
            break;
        }

        if (!patched) {
            return basicClass;
        }

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        return writer.toByteArray();
    }
}
