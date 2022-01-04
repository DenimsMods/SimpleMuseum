package denimred.simplemuseum.common.entity.puppet.manager;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.EntityDimensions;

import denimred.simplemuseum.common.entity.puppet.PuppetEntity;
import denimred.simplemuseum.common.entity.puppet.manager.value.PuppetKey;
import denimred.simplemuseum.common.entity.puppet.manager.value.standard.EntitySizeProvider;
import denimred.simplemuseum.common.entity.puppet.manager.value.standard.EntitySizeValue;
import denimred.simplemuseum.common.entity.puppet.manager.value.vanilla.CustomNameProvider;
import denimred.simplemuseum.common.entity.puppet.manager.value.vanilla.CustomNameValue;
import denimred.simplemuseum.common.i18n.I18nUtil;

public final class PuppetBehaviorManager extends PuppetValueManager {
    public static final String NBT_KEY = "BehaviorManager";
    public static final String TRANSLATION_KEY = I18nUtil.valueManager(NBT_KEY);

    public static final CustomNameProvider CUSTOM_NAME = new CustomNameProvider(key("CustomName"));
    public static final EntitySizeProvider PHYSICAL_SIZE =
            new EntitySizeProvider(
                    key("PhysicalSize"),
                    EntityDimensions.scalable(0.5625F, 2.03125F),
                    (puppet, size) -> puppet.refreshDimensions(),
                    EntityDimensions.scalable(0.25F, 0.25F),
                    EntityDimensions.scalable(10.0F, 10.0F));
    //    public static final VanillaProvider<Boolean> INVULNERABLE =
    //            new VanillaProvider<>(
    //                    key("Invulnerable"),
    //                    true,
    //                    PuppetEntity::isInvulnerable,
    //                    PuppetEntity::setInvulnerable,
    //                    true);
    //    public static final CheckedProvider<String> COMMAND_INTERACT =
    //            new CheckedProvider<>(
    //                    key("CommandInteract"), "", PuppetBehaviorManager::validateCommand);

    public final CustomNameValue customName = this.value(CUSTOM_NAME);
    public final EntitySizeValue physicalSize = this.value(PHYSICAL_SIZE);
    //    public final VanillaValue<Boolean> invulnerable = this.value(INVULNERABLE);
    //    public final CheckedValue<String> commandInteract = this.value(COMMAND_INTERACT);

    public PuppetBehaviorManager(PuppetEntity puppet) {
        super(puppet, NBT_KEY, TRANSLATION_KEY);
    }

    private static PuppetKey key(String provider) {
        return new PuppetKey(NBT_KEY, provider);
    }

    private static boolean validateCommand(PuppetEntity puppet, String command) {
        if (puppet.level.isClientSide || command.isEmpty()) {
            return true;
        }
        final MinecraftServer server = puppet.level.getServer();
        if (server != null && server.isCommandBlockEnabled()) {
            puppet.createCommandSourceStack().withPermission(2);
        }
        return false;
    }
}
