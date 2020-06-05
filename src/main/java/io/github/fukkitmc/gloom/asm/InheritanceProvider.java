package io.github.fukkitmc.gloom.asm;

/**
 * Provides inheritance information, as
 * <a href="https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-5.html#jvms-5.4.3.2">this</a> and
 * <a href="https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-5.html#jvms-5.4.3.3">this</a> can happen
 */
public interface InheritanceProvider {

    String resolveFieldOwner(String owner, String name, String descriptor);

    String resolveMethodOwner(String owner, String name, String descriptor);
}
