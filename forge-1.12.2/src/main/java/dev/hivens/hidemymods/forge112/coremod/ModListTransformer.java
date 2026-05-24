package dev.hivens.hidemymods.forge112.coremod;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

/**
 * Replaces the body of FMLHandshakeMessage$ModList.toBytes(ByteBuf)
 * with a four-instruction sequence that writes a single varint zero
 * and returns. The resulting on-wire payload claims the client has
 * zero installed mods, while the in-memory mod registry is left
 * intact so every loaded mod continues to function normally.
 *
 * The class name FMLHandshakeMessage$ModList is stable across all
 * 1.12.2 Forge builds the FML handshake has shipped under. FML and
 * Forge classes are NOT SRG-remapped at runtime (only Minecraft
 * classes are), so the deobfuscated name matches the runtime name
 * verbatim and transformedName is the correct match key.
 */
public final class ModListTransformer implements IClassTransformer {

    private static final String TARGET_CLASS =
        "net.minecraftforge.fml.common.network.handshake.FMLHandshakeMessage$ModList";

    private static final String TO_BYTES_NAME =
        "toBytes";
    private static final String TO_BYTES_DESC =
        "(Lio/netty/buffer/ByteBuf;)V";

    private static final String BYTE_BUF_UTILS_INTERNAL =
        "net/minecraftforge/fml/common/network/ByteBufUtils";
    private static final String WRITE_VAR_INT_NAME =
        "writeVarInt";
    private static final String WRITE_VAR_INT_DESC =
        "(Lio/netty/buffer/ByteBuf;II)V";

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null || !TARGET_CLASS.equals(transformedName)) {
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

            m.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
            m.instructions.add(new InsnNode(Opcodes.ICONST_0));
            m.instructions.add(new InsnNode(Opcodes.ICONST_3));
            m.instructions.add(new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                BYTE_BUF_UTILS_INTERNAL,
                WRITE_VAR_INT_NAME,
                WRITE_VAR_INT_DESC,
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
