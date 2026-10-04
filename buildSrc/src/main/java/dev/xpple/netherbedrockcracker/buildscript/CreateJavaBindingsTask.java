package dev.xpple.netherbedrockcracker.buildscript;

import org.apache.tools.ant.taskdefs.condition.Os;
import org.gradle.api.tasks.Exec;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class CreateJavaBindingsTask extends Exec {

    private static final String EXTENSION = Os.isFamily(Os.FAMILY_WINDOWS) ? ".bat" : "";

    {
        // always run task
        this.getOutputs().upToDateWhen(task -> false);

        this.setWorkingDir(this.getProject().getRootDir());
        this.setStandardOutput(System.out);
        this.commandLine("./jextract/build/jextract/bin/jextract" + EXTENSION, "--output", "src/main/java", "--use-system-load-library", "--target-package", "com.github.netherbedrockcracker", "--header-class-name", "NetherBedrockCracker", "@includes.txt", "src/main/rust/bedrock_cracker/netherbedrockcracker.h");

        this.doLast(task -> {
            Path sharedJava = getProject().getRootDir().toPath().resolve("src/main/java/com/github/netherbedrockcracker/NetherBedrockCracker$shared.java");
            Path mainJava = getProject().getRootDir().toPath().resolve("src/main/java/com/github/netherbedrockcracker/NetherBedrockCracker.java");
            try {
                if (Files.exists(sharedJava)) {
                    String content = Files.readString(sharedJava);
                    content = content.replace("(ValueLayout.OfBoolean) Linker.nativeLinker().canonicalLayouts().get(\"bool\")", "ValueLayout.JAVA_BOOLEAN");
                    content = content.replace("(ValueLayout.OfByte) Linker.nativeLinker().canonicalLayouts().get(\"char\")", "ValueLayout.JAVA_BYTE");
                    content = content.replace("(ValueLayout.OfByte)Linker.nativeLinker().canonicalLayouts().get(\"char\")", "ValueLayout.JAVA_BYTE");
                    content = content.replace("(ValueLayout.OfShort) Linker.nativeLinker().canonicalLayouts().get(\"short\")", "ValueLayout.JAVA_SHORT");
                    content = content.replace("(ValueLayout.OfInt) Linker.nativeLinker().canonicalLayouts().get(\"int\")", "ValueLayout.JAVA_INT");
                    content = content.replace("(ValueLayout.OfLong) Linker.nativeLinker().canonicalLayouts().get(\"long long\")", "ValueLayout.JAVA_LONG");
                    content = content.replace("(ValueLayout.OfFloat) Linker.nativeLinker().canonicalLayouts().get(\"float\")", "ValueLayout.JAVA_FLOAT");
                    content = content.replace("(ValueLayout.OfDouble) Linker.nativeLinker().canonicalLayouts().get(\"double\")", "ValueLayout.JAVA_DOUBLE");
                    content = content.replace("((AddressLayout) Linker.nativeLinker().canonicalLayouts().get(\"void*\"))", "ValueLayout.ADDRESS");
                    Files.writeString(sharedJava, content);
                }
                if (Files.exists(mainJava)) {
                    String content = Files.readString(mainJava);
                    content = content.replace(".findOrThrow(", ".find(");
                    content = content.replaceAll("SYMBOL_LOOKUP\\.find\\(([^)]+)\\)", "SYMBOL_LOOKUP.find($1).orElseThrow()");
                    Files.writeString(mainJava, content);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
