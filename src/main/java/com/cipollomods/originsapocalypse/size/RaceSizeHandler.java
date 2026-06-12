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
 * Aplica el tamaño del jugador según su raza y su clase mediante Pehkui.
 *
 * La raza define el ancho y el alto base. Si además la clase es Titán, la
 * altura se multiplica por un factor para convertirlo en un gigante,
 * manteniendo el ancho de su raza.
 */
@Mod.EventBusSubscriber(modid = OriginsApocalypse.MOD_ID)
public final class RaceSizeHandler {

    private static final ResourceKey<OriginLayer> RAZA_LAYER =
            ResourceKey.create(
                    OriginsDynamicRegistries.LAYERS_REGISTRY,
                    new ResourceLocation(OriginsApocalypse.MOD_ID, "raza"));

    private static final ResourceKey<OriginLayer> CLASE_LAYER =
            ResourceKey.create(
                    OriginsDynamicRegistries.LAYERS_REGISTRY,
                    new ResourceLocation(OriginsApocalypse.MOD_ID, "clase"));


    private static final String TITAN = "titan";


    /**
     * Devuelve el último segmento del path de un identificador.
     *
     * @param id identificador del origen
     * @return nombre final sin la ruta de carpetas
     */
    private static String leaf(ResourceLocation id) {
        String path = id.getPath();
        int slash = path.lastIndexOf('/');
        return slash >= 0 ? path.substring(slash + 1) : path;
    }

    /**
     * Factor de altura que aplica la clase Titán, multiplica
     * el alto de las razas.
     */
    private static final float titanHeightFactor = 2.2F;

    /**
     * Factor en el que multiplica el ancho base de cada raza
     * en caso de que tenga de clase Titán. Así el elfo
     * no es Slenderman xD
     *
     * @param race nombre pelado de la raza
     * @return factor de anchura del Titán
     */
    private static float titanWidthFactor(String race) {
        return switch (race) {
            case "elfo" -> 2.0F;
            default -> 1.0F;
        };
    }

    /**
     * Revisa raza y clase en cada tick del servidor y ajusta la escala.
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

            float width = size[0];
            float height = size[1];

            String clase = leaf(container.getOrigin(CLASE_LAYER).location());
            if (TITAN.equals(clase)) {
                height *= titanHeightFactor;
                width *= titanWidthFactor(leaf(race));
            }

            applyScale(player, ScaleTypes.WIDTH, width);
            applyScale(player, ScaleTypes.HEIGHT, height);
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
        return switch (leaf(race)) {
            case "humano" -> new float[]{1.0F, 1.0F};
            case "alien" -> new float[]{0.5F, 0.5F};
            case "elfo" -> new float[]{0.75F, 1.25F};
            case "orco" -> new float[]{2.0F, 1.5F};
            case "enano" -> new float[]{1.5F, 0.75F};
            default -> null;
        };
    }

    /**
     * Fija una escala de Pehkui solo si se diferencia de la actual.
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