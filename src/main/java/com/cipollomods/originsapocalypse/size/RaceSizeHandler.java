package com.cipollomods.originsapocalypse.size;

import com.cipollomods.originsapocalypse.OriginsApocalypse;
import io.github.edwinmindcraft.origins.api.capabilities.IOriginContainer;
import io.github.edwinmindcraft.origins.api.origin.OriginLayer;
import io.github.edwinmindcraft.origins.api.registry.OriginsDynamicRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleType;
import virtuoel.pehkui.api.ScaleTypes;

/**
 * Aplica el tamaño de cada raza mediante Pehkui.
 *
 * Se comprueba en cada tick del
 * jugador en el servidor y solo se reescribe la escala cuando cambia, de modo
 * que el tamaño se restablece solo tras revivir, cambiar de dimensión,
 * reconectar o usar el comando {@code /origin}.</p>
 */
@Mod.EventBusSubscriber(modid = OriginsApocalypse.MOD_ID)
public final class RaceSizeHandler {

    private static final ResourceKey<OriginLayer> RAZA_LAYER =
            ResourceKey.create(
                    OriginsDynamicRegistries.LAYERS_REGISTRY,
                    new ResourceLocation(OriginsApocalypse.MOD_ID, "raza"));

    private RaceSizeHandler() {
    }

    /**
     * Revisa la raza del jugador en cada tick del servidor y ajusta su escala.
     *
     * @param event evento de tick del jugador
     */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        if (!(event.player instanceof ServerPlayer player)) {
            return;
        }

        IOriginContainer.get(player).ifPresent(container -> {
            ResourceLocation race = container.getOrigin(RAZA_LAYER).location();
            float[] size = sizeFor(race);
            if (size == null) {
                return;
            }
            applyScale(player, ScaleTypes.WIDTH, size[0]);
            applyScale(player, ScaleTypes.HEIGHT, size[1]);
        });
    }

    /**
     * Devuelve la escala {ancho, alto} de una raza, o {@code null} si el
     * identificador no corresponde a ninguna raza del mod.
     *
     * @param race identificador del origen elegido en la capa de raza
     * @return par {ancho, alto} o {@code null}
     */
    private static float[] sizeFor(ResourceLocation race) {
        if (!OriginsApocalypse.MOD_ID.equals(race.getNamespace())) {
            return null;
        }
        return switch (race.getPath()) {
            case "humano" -> new float[]{1.0F, 1.0F};
            case "alien" -> new float[]{0.25F, 0.25F};
            case "elfo" -> new float[]{0.75F, 1.25F};
            case "orco" -> new float[]{2.0F, 1.5F};
            case "enano" -> new float[]{1.0F, 0.5F};
            default -> null;
        };
    }

    /**
     * Fija una escala de Pehkui solo si difiere de la actual.
     *
     * @param player jugador objetivo
     * @param type   tipo de escala (ancho o alto)
     * @param target valor de escala deseado
     */
    private static void applyScale(ServerPlayer player, ScaleType type, float target) {
        ScaleData data = type.getScaleData(player);
        if (Math.abs(data.getTargetScale() - target) > 1.0E-4F) {
            data.setScale(target);
        }
    }
}