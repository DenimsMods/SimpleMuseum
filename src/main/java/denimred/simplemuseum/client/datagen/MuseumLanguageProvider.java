package denimred.simplemuseum.client.datagen;

import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.LanguageProvider;

import cryptcraft.cryptmaster.plugin.client.UtilityTool;
import denimred.simplemuseum.common.entity.puppet.manager.PuppetAnimationManager;
import denimred.simplemuseum.common.entity.puppet.manager.PuppetAudioManager;
import denimred.simplemuseum.common.entity.puppet.manager.PuppetBehaviorManager;
import denimred.simplemuseum.common.entity.puppet.manager.PuppetRenderManager;
import denimred.simplemuseum.common.entity.puppet.manager.PuppetSourceManager;
import denimred.simplemuseum.common.entity.puppet.manager.value.PuppetValueProvider;
import denimred.simplemuseum.common.i18n.I18nUtil;
import denimred.simplemuseum.common.i18n.lang.GuiLang;
import denimred.simplemuseum.common.i18n.lang.MiscLang;
import denimred.simplemuseum.common.init.MuseumEntities;
import denimred.simplemuseum.common.init.MuseumItems;
import denimred.simplemuseum.modcompat.cryptmaster.MuseumTool;

public class MuseumLanguageProvider extends LanguageProvider {
    public MuseumLanguageProvider(DataGenerator gen, String modId, String locale) {
        super(gen, modId, locale);
    }

    @Override
    protected void addTranslations() {
        // Lang for registry objects
        this.addEntityType(MuseumEntities.MUSEUM_PUPPET, "Museum Puppet");
        this.addItem(MuseumItems.CURATORS_CANE, "Curator's Cane");
        // Gui lang
        for (GuiLang lang : GuiLang.values()) {
            lang.provide(this);
        }
        // Misc lang
        for (MiscLang lang : MiscLang.values()) {
            lang.provide(this);
        }
        // Render Manager
        this.addDescriptive(
                PuppetRenderManager.TRANSLATION_KEY,
                "Render Manager",
                "Manages puppet properties relating to general rendering. Most properties are very specific and niche, but can be important for certain effects.");
        //        this.addValueProvider(
        //                PuppetRenderManager.TINT_COLOR,
        //                "Tint Color",
        //                "When set, the puppet will be tinted with the provided color. The exact
        // method of tinting is multiplicative; pure white will appear as being untinted.");
        this.addValueProvider(
                PuppetRenderManager.FLAMING,
                "Always On Fire",
                "When true, the puppet will always appear to be on fire.");
        //        this.addValueProvider(
        //                PuppetRenderManager.DEFAULT_RENDER_LAYER,
        //                "Default Render Layer",
        //                "Defines the layer that the puppet will render on by default. Can cause
        // notable rendering issues, use with caution. Consider using layer-named bones instead,
        // depending on the desired effect.");
        this.addValueProvider(
                PuppetRenderManager.NAMEPLATE_BEHAVIOR,
                "Nameplate Behavior",
                "Determines how the nameplate should render. ON_HOVER is the regular behavior, ALWAYS is player-like and always renders, NEVER will make it never render.");
        this.addValueProvider(
                PuppetRenderManager.IGNORE_LIGHTING,
                "Ignore Lighting",
                "When true, the puppet will appear to be fully lit even in dark areas.");
        this.addValueProvider(
                PuppetRenderManager.SCALE,
                "Render Scale",
                "Determines the rendering scale of the puppet. Will not affect the puppet's physical size or collision bounding box.");
        // Animation Manager
        this.addDescriptive(
                PuppetAnimationManager.TRANSLATION_KEY,
                "Animation Manager",
                "Manages puppet properties relating to animations. Be sure to define the animations file in the Source Manager first.");
        this.addValueProvider(
                PuppetAnimationManager.IDLE,
                "Idle Animation",
                "This animation will play when no other animation is playing.");
        this.addValueProvider(
                PuppetAnimationManager.IDLE_SNEAK,
                "Idle Animation (Sneaking)",
                "This animation will play when the puppet is sneaking and no other animation is playing. §6Note: Currently, puppets can only sneak when possessed with CryptMaster!");
        this.addValueProvider(
                PuppetAnimationManager.MOVING,
                "Moving Animation",
                "This animation will play when the puppet is moving.");
        this.addValueProvider(
                PuppetAnimationManager.MOVING_SNEAK,
                "Walking Animation (Sneaking)",
                "This animation will play when the puppet is sneaking and moving. §6Note: Currently, puppets can only sneak when possessed with CryptMaster!");
        this.addValueProvider(
                PuppetAnimationManager.SPRINTING,
                "Sprinting Animation",
                "This animation will play when the puppet is sprinting. §6Note: Currently, puppets can only sprint when possessed with CryptMaster!");
        this.addValueProvider(
                PuppetAnimationManager.SITTING,
                "Sitting Animation",
                "This animation will play when the puppet is sitting.");
        this.addValueProvider(
                PuppetAnimationManager.DEATH,
                "Death Animation",
                "This animation will play when the puppet is killed. §6Note: Currently, puppets can only \"die\" by forcibly setting their health to 0 with commands!");
        this.addValueProvider(
                PuppetAnimationManager.DEATH_LENGTH,
                "Death Animation Length",
                "Determines how long to wait before the puppet turns invisible, in ticks. 20 ticks equals 1 second.");
        // Source Manager
        this.addDescriptive(
                PuppetSourceManager.TRANSLATION_KEY,
                "Source Manager",
                "Manages puppet properties relating to the files that the puppet will use for its model, texture, animations, etc.");
        this.addValueProvider(
                PuppetSourceManager.MODEL,
                "Model File",
                "The location of the model file for the puppet to use.");
        this.addValueProvider(
                PuppetSourceManager.TEXTURE,
                "Texture File",
                "The location of the texture file that will be applied over the puppet's model.");
        this.addValueProvider(
                PuppetSourceManager.ANIMATIONS,
                "Animations File",
                "The location of the animations file that contains all of the animations for the puppet to perform.");
        // Audio Manager
        this.addDescriptive(
                PuppetAudioManager.TRANSLATION_KEY,
                "Audio Manager",
                "Manages puppet properties relating to sound effects and other audible features.");
        this.addValueProvider(
                PuppetAudioManager.AMBIENT,
                "Ambient Sound Effect",
                "This sound effect will play at random intervals. Just like how vanilla mobs passively make noises.");
        this.addValueProvider(
                PuppetAudioManager.CATEGORY,
                "Sound Category",
                "Determines the category for all of the puppet's sounds to play under. Used in the vanilla sound settings to control the volume of different categories.");
        // Behavior Manager
        this.addDescriptive(
                PuppetBehaviorManager.TRANSLATION_KEY,
                "Behavior Manager",
                "Manages puppet properties relating to behavior. This includes things like AI, inventory, entity attributes, etc.");
        this.addValueProvider(
                PuppetBehaviorManager.CUSTOM_NAME,
                "Custom Name",
                "Determines the name of the puppet, which will render on its nameplate. §6Note: Currently, you have to use command to alter text effects (color, italics, etc)!");
        this.addValueProvider(
                PuppetBehaviorManager.PHYSICAL_SIZE,
                "Physical Size",
                "Determines the physical width and height of the puppet. Used for the collision box and other gameplay logic; doesn't affect its appearance.");
        this.addValueProvider(
                PuppetBehaviorManager.GOAL_TREE,
                "AI Goal Tree",
                "TODO Add This :)");
        // Mod compat lang (datagen is done in dev, no need to worry about classloading)
        this.addCryptMasterTool(MuseumTool.INSTANCE, "Create/Edit Museum Puppet");
    }

    private void addValueProvider(PuppetValueProvider<?, ?> provider, String title, String desc) {
        this.addDescriptive(provider.translationKey, title, desc);
    }

    private void addDescriptive(String key, String title, String desc) {
        this.add(key, title);
        this.add(I18nUtil.desc(key), desc);
    }

    @SuppressWarnings("SameParameterValue")
    private void addCryptMasterTool(UtilityTool tool, String name) {
        this.add(tool.getTooltip().getKey(), name);
    }
}
