package net.idothehax.endcampfire;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.world.GameRules;
import net.minecraft.world.GameRules.BooleanRule;
import net.minecraft.world.GameRules.Key;

public class EndCampfireGameRules {
    public static final GameRules.Key<BooleanRule> TOGGLE_CUSTOM_BLOCK_COLLISION =
            GameRuleRegistry.register("shouldEndCampfireFreeze", GameRules.Category.PLAYER, GameRuleFactory.createBooleanRule(false));

    public static void init() {
        // Optional: Can be used for additional initialization if needed
    }
}
