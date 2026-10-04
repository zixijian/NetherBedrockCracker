package dev.xpple.netherbedrockcracker;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.logging.LogUtils;
import dev.xpple.netherbedrockcracker.command.commands.CrackCommand;
import dev.xpple.netherbedrockcracker.command.commands.SourceCommand;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.commands.CommandBuildContext;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

public class NetherBedrockCrackerMod implements ClientModInitializer {

    public static final String MOD_ID = "netherbedrockcracker";
    public static final Logger LOGGER = LogUtils.getLogger();

    static {
        String libraryName = System.mapLibraryName("bedrockcracker");
        ModContainer modContainer = FabricLoader.getInstance().getModContainer(MOD_ID).orElseThrow();
        Path tempFile;
        try {
            tempFile = Files.createTempFile(libraryName, "");
            String osName = System.getProperty("os.name", "").toLowerCase();
            String osArch = System.getProperty("os.arch", "").toLowerCase();
            Optional<Path> libraryPath = Optional.empty();

            if (osArch.contains("aarch64") || osArch.contains("arm64")) {
                boolean isAndroid = osName.contains("android")
                        || System.getProperty("java.vendor", "").toLowerCase().contains("android")
                        || System.getProperty("java.vm.vendor", "").toLowerCase().contains("android")
                        || System.getProperty("java.runtime.name", "").toLowerCase().contains("android")
                        || System.getenv("ANDROID_ROOT") != null
                        || System.getenv("ANDROID_DATA") != null;

                if (isAndroid) {
                    libraryPath = modContainer.findPath("libbedrockcracker_android_arm64.so");
                } else {
                    libraryPath = modContainer.findPath("libbedrockcracker_linux_arm64.so");
                }
            }
            if (libraryPath.isEmpty()) {
                libraryPath = modContainer.findPath(libraryName);
            }
            if (libraryPath.isEmpty()) {
                libraryPath = modContainer.findPath("libbedrockcracker_android_arm64.so");
            }
            if (libraryPath.isEmpty()) {
                libraryPath = modContainer.findPath("libbedrockcracker_linux_arm64.so");
            }
            Files.copy(libraryPath.orElseThrow(() -> new RuntimeException("Could not find native library " + libraryName)), tempFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.load(tempFile.toAbsolutePath().toString());
    }

    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register(NetherBedrockCrackerMod::registerCommands);
    }

    private static void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext buildContext) {
        CrackCommand.register(dispatcher);
        SourceCommand.register(dispatcher);
    }
}
