package dev.denimred.simplemuseum.mixin;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.denimred.simplemuseum.puppet.PuppetCommands;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Consumer;

@Mixin(EntitySelectorParser.class)
public abstract class EntitySelectorParserMixin {
    @Inject(method = "fillSelectorSuggestions", at = @At("RETURN"))
    private static void injectCustomSelectorSuggestions(SuggestionsBuilder builder, CallbackInfo ci) {
        PuppetCommands.fillCustomSelectorSuggestions(builder);
    }

    @Inject(method = "parse", at = @At(value = "INVOKE", target = "Lnet/minecraft/commands/arguments/selector/EntitySelectorParser;parseSelector()V"), cancellable = true)
    private void injectCustomSelectors(CallbackInfoReturnable<EntitySelector> cir) throws CommandSyntaxException {
        if (!PuppetCommands.parseCustomSelectors((EntitySelectorParser) (Object) this)) return;

        setSuggestions(this::suggestOpenOptions);
        if (getReader().canRead() && getReader().peek() == '[') {
            getReader().skip();
            setSuggestions(this::suggestOptionsKeyOrClose);
            parseOptions();
        }
        finalizePredicates();
        cir.setReturnValue(getSelector());
    }

    @Shadow
    public abstract StringReader getReader();

    @Shadow
    public abstract void setSuggestions(BiFunction<SuggestionsBuilder, Consumer<SuggestionsBuilder>, CompletableFuture<Suggestions>> suggestionHandler);

    @Shadow
    public abstract EntitySelector getSelector();

    @Shadow
    protected abstract CompletableFuture<Suggestions> suggestOpenOptions(SuggestionsBuilder builder, Consumer<SuggestionsBuilder> consumer);

    @Shadow
    protected abstract CompletableFuture<Suggestions> suggestOptionsKeyOrClose(SuggestionsBuilder builder, Consumer<SuggestionsBuilder> consumer);

    @Shadow
    protected abstract void parseOptions() throws CommandSyntaxException;

    @Shadow
    protected abstract void finalizePredicates();
}
